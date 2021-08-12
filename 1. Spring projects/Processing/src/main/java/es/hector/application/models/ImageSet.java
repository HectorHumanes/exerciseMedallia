package es.hector.application.models;

import java.util.List;

public class ImageSet {
	
	private String domain;
	private List<String> images;	
	
	public ImageSet() {
	}
	
	public ImageSet(String domain, List<String> images) {
		this.domain = domain;
		this.images = images;
	}
	
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public List<String> getImages() {
		return images;
	}
	public void setImages(List<String> images) {
		this.images = images;
	}

	@Override
	public String toString() {
		return "ImageSet [domain=" + domain + ", images=" + images + "]";
	}
	
	
}
