package es.hector.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CollectorTest {
	
	@Autowired
	private Collector collector;
	
	@Test
	void givenAValidUrl_WhenIsAccessed_thenImagesObjectsAreReturned() {
		//Given
		String url = "http://google.es";
		
		//When
		List<Image> images = collector.collectImagesFromWebsite(url);
		
		//Then		
		assertThat(images.size() == 1);
	}
	
	@Test
	public void givenAnInvalidUrl_WhenIsAccessed_ExceptionIsThrown() {
		 Exception exception = assertThrows(IllegalArgumentException.class, () -> collector.collectImagesFromWebsite("thisisnotanurl"));
		 assertTrue(exception.getMessage().contains("Malformed URL: "));
	}
	
	
}
