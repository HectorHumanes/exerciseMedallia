package es.hector.application;

public class Image {
	
	private String url;
	private String domain;
	
	public Image(String domain, String url) {
		this.setDomain(domain);
		this.setUrl(url);
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
}
