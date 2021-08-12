package es.hector.application.models;

public class ApiResponseMessage {
	
	private String message;
	private int responseCode;
	
	
	public ApiResponseMessage(String message, int responseCode) {
		super();
		this.message = message;
		this.responseCode = responseCode;
	}

	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public int getResponseCode() {
		return responseCode;
	}


	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	
	
	
	

}
