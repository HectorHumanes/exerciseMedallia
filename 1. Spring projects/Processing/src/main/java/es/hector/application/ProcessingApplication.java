package es.hector.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import com.arangodb.ArangoDBException;

@SpringBootApplication
@EnableAsync
@EnableRetry
@EnableScheduling
public class ProcessingApplication implements CommandLineRunner {
	private Logger logger = LoggerFactory.getLogger(ProcessingApplication.class);
	
	@Value("${arangocontainername}")
    private String arangoLocation;
	
    @Bean
    public RestTemplate getresttemplate() {
    	return new RestTemplateBuilder()
        .errorHandler(new RestTemplateResponseErrorHandler())
        .build();
    }

	public static void main(String[] args) {
		SpringApplication.run(ProcessingApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		try {
			logger.info("Trying to prepare database for exercise on: "+arangoLocation+":8529");
			ArangoDatabase db = new ArangoDatabase("root", "", arangoLocation, 8529);
			db.spawnDatabaseForExercise();	
		} catch (ArangoDBException e) {
			logger.warn("It seems that you currently have the exercise BD configured. Reason: "+e);
		}			
	}
}
