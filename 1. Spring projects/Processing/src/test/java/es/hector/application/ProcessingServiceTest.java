package es.hector.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.hector.application.models.ApiResponseMessage;
import es.hector.application.models.Database;
import es.hector.application.models.DatabaseLocationResponse;
import es.hector.application.models.ImageSet;

@SpringBootTest
@ActiveProfiles("test")
public class ProcessingServiceTest {

	@Autowired
	private ProcessingService processingService;
	@Autowired
	private RestTemplate restTemplate;
	private MockRestServiceServer mockServer;
	private ObjectMapper mapper = new ObjectMapper();
	
	private Database[] dbs = { new Database(0, "localhost", "schema1", "user", "password"),
			new Database(1, "localhost", "schema2", "user", "password"),
			new Database(2, "localhost", "schema3", "user", "password") };
	
	private ImageSet imageSet = new ImageSet("test.com",new ArrayList<String>(Arrays.asList("img1.jpg","/path2/img2.jpg","/path2/path3/img3.jpg")));
	private DatabaseLocationResponse databaseLocationResponse = new DatabaseLocationResponse("test.com","1");
	

	@BeforeEach
	public void init() {
		mockServer = MockRestServiceServer.createServer(restTemplate);
		processingService.clearDbs();
	}

	@Test
	public void givenTheServiceGetsTheDBConfig_whenItsReceived_thenIsStored()
			throws JsonProcessingException, URISyntaxException {
		
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8080/getDatabases")))
				.andExpect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.OK)
						.contentType(MediaType.APPLICATION_JSON).body(mapper.writeValueAsString(dbs)));

		processingService.populateConfig();
		mockServer.verify();

		assertEquals(processingService.getDbs().get("0").compareTo(dbs[0]), 1);
		assertEquals(processingService.getDbs().get("1").compareTo(dbs[1]), 1);
		assertEquals(processingService.getDbs().get("2").compareTo(dbs[2]), 1);
	}

	@Test
	public void givenAnErrorInDatabaseProviderComponent_WhenIsAccessed_ThenFalseIsReturned() throws URISyntaxException, JsonProcessingException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8080/getDatabases")))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
				.contentType(MediaType.APPLICATION_JSON)
				.body("{\"timestamp\":\"2021-08-12T10:55:27.472+0000\",\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"Is time for an error\",\"path\":\"/getDatabases\"}")
				);		

		boolean success = processingService.populateConfig();
		mockServer.verify();
		assertFalse(success);
	}
	
	@Test
	public void givenTheDBConfigurationIsNotPresent_WhenProcessIsCalled_ThenConfigIsPopulated() throws JsonProcessingException, URISyntaxException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8080/getDatabases")))
				.andExpect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.OK)
						.contentType(MediaType.APPLICATION_JSON).body(mapper.writeValueAsString(dbs)));

		processingService.process(new ArrayList<ImageSet>(Arrays.asList(imageSet)));
		assertEquals(3, processingService.getDbs().size());
	}
	
	@Test
	public void givenAllTheParametersAreValid_WhenProcessIsCalled_ThenEndpointReturns200() throws JsonProcessingException, URISyntaxException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8080/getDatabases")))
		.andExpect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.OK)
				.contentType(MediaType.APPLICATION_JSON).body(mapper.writeValueAsString(dbs)));
		
		mockServer.expect(ExpectedCount.between(1, 3), requestTo(new URI("http://localhost:8080/getDBForDomain/test.com")))
		.andExpect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.OK)
				.contentType(MediaType.APPLICATION_JSON).body((mapper.writeValueAsString(databaseLocationResponse))));

		ResponseEntity<ApiResponseMessage> result = processingService.process(new ArrayList<ImageSet>(Arrays.asList(imageSet)));
		mockServer.verify();
		assertEquals(200, result.getStatusCodeValue());
	}
	
	@Test
	public void givenAllTheParametersAreValid_WhenTheDatabaseIsUnavailable_ThenTheMethodRetries() throws JsonProcessingException, URISyntaxException {
		mockServer.reset();
		mockServer.expect(ExpectedCount.manyTimes(), requestTo(new URI("http://localhost:8080/getDatabases")))
		.andExpect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.OK)
				.contentType(MediaType.APPLICATION_JSON).body(mapper.writeValueAsString(dbs)));
		
		mockServer.expect(ExpectedCount.min(3), requestTo(new URI("http://localhost:8080/getDBForDomain/test.com")))
		.andExpect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
				.contentType(MediaType.APPLICATION_JSON).body("{}"));

		processingService.process(new ArrayList<ImageSet>(Arrays.asList(imageSet)));
		mockServer.verify();
	}
	
	@Test
	public void givenOneDatabaseIsOffline_WhenIsAccessed_ThenCode500IsReturned() throws JsonProcessingException, URISyntaxException {
		mockServer.expect(ExpectedCount.manyTimes(), requestTo(new URI("http://localhost:8080/getDatabases")))
		.andExpect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
				.contentType(MediaType.APPLICATION_JSON).body("{}"));		
		
		 ResponseEntity<ApiResponseMessage> result = processingService.process(new ArrayList<ImageSet>(Arrays.asList(imageSet)));
		 assertEquals(500, result.getStatusCodeValue());
	}

	/*
	 * @Test void givenAValidUrl_WhenIsAccessed_thenImagesObjectsAreReturned() {
	 * //Given String url = "http://google.es";
	 * 
	 * //When List<Image> images = collector.collectImagesFromWebsite(url);
	 * 
	 * //Then assertThat(images.size() == 1); }
	 * 
	 */

}
