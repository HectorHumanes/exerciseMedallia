package es.hector.application;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
public class CollectionService {
	private Logger logger = LoggerFactory.getLogger(CollectionService.class);
	
	@Autowired
	private Collector collector;
	@Autowired
    private RestTemplate restTemplate;	
	@Value("${processingcontainername}")
    private String processingLocation;
	
	
	@GetMapping("/collect")
	public ResponseEntity<ApiResponseMessage> collect(@RequestParam(name="url", required=true) String url) {
		List<Image> imgs = collector.collectImagesFromWebsite(url);
		ImageSet imageSet = new ImageSet();
		if(imgs.size() > 0) {
			imageSet.setDomain(imgs.get(0).getDomain());
			List<String> urls = new ArrayList<String>();
			for(Image img : imgs) {
				urls.add(img.getUrl());
			}
			imageSet.setImages(urls);
			List<ImageSet> imagesets = new ArrayList<ImageSet>();
			imagesets.add(imageSet);
			try {
				logger.info("Processing component url -> " + "http://"+processingLocation+":8082/process");
				ApiResponseMessage response = restTemplate.postForObject("http://"+processingLocation+":8082/process", imagesets, ApiResponseMessage.class);
				return new ResponseEntity<ApiResponseMessage>(response ,HttpStatus.OK);
			} catch (HttpServerErrorException e) {
				return new ResponseEntity<ApiResponseMessage>(new ApiResponseMessage("Problem when calling the Processing service: Database unavailable. Retrying in 5 minutes", 500) ,HttpStatus.INTERNAL_SERVER_ERROR);
			}			
		}		
		return new ResponseEntity<ApiResponseMessage>(new ApiResponseMessage("Problem when collecting pictures", 500) ,HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
