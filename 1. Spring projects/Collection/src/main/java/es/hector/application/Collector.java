package es.hector.application;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.google.common.net.InternetDomainName;

@Component
public class Collector {
	
	private Logger logger = LoggerFactory.getLogger(Collector.class);

	private String userAgent = "Mozilla/5.0";
	private int timeout = 10000;
	
	// Since PhamtomJS or similar is needed to parse a JS website, for the sake of simplicity only pure html images are retrieved
	public List<Image> collectImagesFromWebsite(String websiteUrl) {		
		List<Image> images = new ArrayList<Image>();
		
		try {
			// Get the webpage
			Document document = Jsoup.connect(websiteUrl).userAgent(userAgent).timeout(timeout).get();
			// Get the img elements
			Elements imageElements = document.select("img");
			// Add every image to list
			for (Element imageElement : imageElements) {			
				String strImageURL = imageElement.attr("abs:src");
				String domain = InternetDomainName.from(new URL(strImageURL).getHost()).topPrivateDomain().toString();
				images.add(new Image(domain, strImageURL));
			}
		} catch (IOException e) {
			logger.error("Error during collection process: "+e);
		} /*catch (IllegalArgumentException e) {
			logger.error("Malformed url provided: "+e);
		}
		*/
		
		return images;
	}

	////// GETTERS AND SETTERS

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
}
