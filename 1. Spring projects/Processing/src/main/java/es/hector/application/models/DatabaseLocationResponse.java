package es.hector.application.models;

public class DatabaseLocationResponse {
	
	private String domain;
	private String dbId;	
	
	public DatabaseLocationResponse() {
	}
	
	public DatabaseLocationResponse(String domain, String dbId) {
		this.domain = domain;
		this.dbId = dbId;
	}
	
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getDbId() {
		return dbId;
	}
	public void setDbId(String dbId) {
		this.dbId = dbId;
	}
	@Override
	public String toString() {
		return "DatabaseLocationResponse [domain=" + domain + ", dbId=" + dbId + "]";
	}
	
}
