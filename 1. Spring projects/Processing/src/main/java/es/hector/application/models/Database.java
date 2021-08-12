package es.hector.application.models;

public class Database implements Comparable<Database>{
	
	private int id;
	private String host;
	private String schema;
	private String user;
	private String password;	
	
	public Database() {		
	}

	public Database(int id, String host, String schema, String user, String password) {
		super();
		this.id = id;
		this.host = host;
		this.schema = schema;
		this.user = user;
		this.password = password;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	@Override
	public String toString() {
		return "Database [id=" + id + ", host=" + host + ", schema=" + schema + ", user=" + user + ", password="
				+ password + "]";
	}

	@Override
	public int compareTo(Database arg0) {
		if(this.host.equals(arg0.getHost())&&this.id==arg0.getId()&&this.schema.equals(arg0.getSchema())&&this.user.equals(arg0.getUser())&&this.password.equals(arg0.getPassword())) return 1;		
		else return 0;
	}	
}
