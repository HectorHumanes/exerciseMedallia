package es.hector.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.arangodb.ArangoDBException;
import es.hector.application.models.ApiResponseMessage;
import es.hector.application.models.Database;
import es.hector.application.models.DatabaseLocationResponse;
import es.hector.application.models.Image;
import es.hector.application.models.ImageSet;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;

@RestController
public class ProcessingService {

	private Logger logger = LoggerFactory.getLogger(ProcessingService.class);

	@Autowired
	private RestTemplate restTemplate;
	private Map<String, Database> dbs = new HashMap<String, Database>();

	@Value("${testcontainername}")
    private String testLocation;

	@PostConstruct
	@Scheduled(fixedDelay = 30000, initialDelay = 30000)
	public boolean populateConfig() {
		try {
			ResponseEntity<Database[]> dbsResponse = restTemplate.getForEntity("http://"+testLocation+":8080/getDatabases", Database[].class);
			List<Database> dbList = Arrays.asList(dbsResponse.getBody());
			for (Database x : dbList) {
				Database databaseToInclude = x;
				if(!testLocation.contains("localhost")) databaseToInclude.setHost("arangodb");
				this.dbs.put(String.valueOf(x.getId()), databaseToInclude);
			}
			logger.info("Processing service DBs list populated");
			return true;
		} catch (RestClientException e) {			
			logger.error("The component couldn't get the database configuration: "+e);
			return false;
		}
	}

	@PostMapping(path = "/process", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Retryable(value = { NullPointerException.class }, maxAttempts = 3, backoff = @Backoff(delayExpression = "${retry.maxDelay}"))
	public ResponseEntity<ApiResponseMessage> process(@RequestBody List<ImageSet> imageSet) {
		if (dbs.size() == 0) {
			logger.info("Trying to populate the configuration again...");
			boolean sucess = populateConfig();
			if(sucess) logger.info("Configuration populated!");
		}		
		int totalImages = 0;
		
		for (ImageSet set : imageSet) {
			try {
				totalImages = totalImages + set.getImages().size();
				getDBAndSaveImages(set.getDomain(), set.getImages());
			} catch (Exception e) {
				logger.error("Database unavailable. Retrying in 5 minutes...");
				throw new NullPointerException();
			}
		}
		return new ResponseEntity<>(new ApiResponseMessage("Processed "+totalImages+" images", 200), HttpStatus.OK);
	}

	@GetMapping("/images")
	public ResponseEntity<List<Image>> getImages(@RequestParam(name = "domain", required = false) String domain, @RequestParam(name = "page", required = false) String page) {
		try {
			int pageNumber = -1;
			if(page != null) pageNumber = Integer.valueOf(page);			
			
			if (dbs.size() == 0) {
				logger.info("Trying to populate the configuration again...");
				boolean sucess = populateConfig();
				if(sucess) logger.info("Configuration populated!");
			}
			
			logger.debug("Database pool size: "+dbs.size());
			List<Image> images = new ArrayList<Image>();
			this.dbs.forEach((k, v) -> {
				logger.debug("Getting images from: "+v.getSchema());
				ArangoDatabase db = new ArangoDatabase(v.getUser(), v.getPassword(), v.getHost(), 8529);
				images.addAll(db.getImages(v.getSchema(), domain, -1));
			});
			
			images.sort(Comparator.comparing(Image::getUrl));			
			if(images.size() > 0) {
				if(pageNumber >= 0) return new ResponseEntity<List<Image>>(images.subList(pageNumber*50, (pageNumber*50)+50), HttpStatus.OK);
				return new ResponseEntity<List<Image>>(images, HttpStatus.OK);
			}
			else return new ResponseEntity<List<Image>>(images, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (ArangoDBException e) {
			logger.error("Failed to execute query. " + e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (NullPointerException e) {
			e.printStackTrace();
			logger.error("Database is currently unavailable. Try again later");
			return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	private boolean getDBAndSaveImages(String domain, List<String> images) throws NullPointerException {
		logger.debug("Processing domain: " + domain + " images");
		Database dbData = getDatabaseConfigForDomain(domain);		
		ArangoDatabase db = new ArangoDatabase(dbData.getUser(), dbData.getPassword(), dbData.getHost(), 8529);
		
		boolean sucess = true;
		
		for (String img : images) {
			CompletableFuture<String> result = db.save(dbData.getSchema(), "images", new Image(domain, img));
			if(result.isCompletedExceptionally() && sucess) sucess = false;
		}
		return sucess;	
	}

	private Database getDatabaseConfigForDomain(String domain) {
		logger.debug("Getting db for domain: " + domain);
		
		ResponseEntity<DatabaseLocationResponse> locationResponse = restTemplate
				.getForEntity("http://"+testLocation+":8080/getDBForDomain/" + domain, DatabaseLocationResponse.class);
		
		//logger.info(locationResponse.getBody().toString());
		
		Database dbData = dbs.get(locationResponse.getBody().getDbId());
		return dbData;
	}

	@Recover
	public ResponseEntity<ApiResponseMessage> recover(List<ImageSet> imageSet, NullPointerException cause) {
		return new ResponseEntity<ApiResponseMessage>(
				new ApiResponseMessage("Database unavailable after 3 attempts. Try again later.", 500),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	/// Getters and setters ///

	public Map<String, Database> getDbs() {
		return dbs;
	}

	public void setDbs(Map<String, Database> dbs) {
		this.dbs = dbs;
	}
	
	public void clearDbs() {
		this.dbs.clear();
	}
	
	

}
