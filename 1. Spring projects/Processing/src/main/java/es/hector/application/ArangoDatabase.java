package es.hector.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.Permissions;
import com.arangodb.mapping.ArangoJack;

import es.hector.application.models.Image;

public class ArangoDatabase {

	private Logger logger = LoggerFactory.getLogger(ArangoDatabase.class);

	private String host;
	private int port;
	private String user;
	private String password;
	private ArangoDB db;
	boolean connected = false;	

	public ArangoDatabase() {
		this.db = new ArangoDB.Builder().serializer(new ArangoJack()).host(host, port).user(user).password(password)
				.build();
		if (!db.getVersion().getVersion().isEmpty())
			this.connected = true;
	}

	public ArangoDatabase(String user, String password, String host, int port) {
		this.db = new ArangoDB.Builder().serializer(new ArangoJack()).host(host, port).user(user).password(password)
				.build();
		if (!db.getVersion().getVersion().isEmpty())
			this.connected = true;
	}

	@Async
	public CompletableFuture<String> save(String dbName, String collectionName, Object document) {
		try {
			CompletableFuture<String> imgKey = CompletableFuture
					.completedFuture(db.db(dbName).collection(collectionName).insertDocument(document).getKey());
			return imgKey;
		} catch (ArangoDBException e) {
			logger.error("Failed to create document: " + e.getMessage());
			return null;
		}
	}

	public List<Image> getImages(String dbName, String domain, int page) {
		try {
			Map<String, Object> bindVars = new HashMap<String, Object>();
			String query;
			String pageSelector = "";
			if(page >=0 ) {
				int offset = page*50;
				pageSelector = "LIMIT "+offset+", 50";
			}	
			
			if(domain != null) {
				query = "FOR t IN images FILTER t.domain == @domain "+pageSelector+" RETURN t";
				bindVars = Collections.singletonMap("domain", domain);
			} else query = "FOR t IN images "+pageSelector+" RETURN t";
			
			ArangoCursor<Image> resultset = db.db(dbName).query(query, bindVars, null, Image.class);
			return resultset.asListRemaining();
		} catch (ArangoDBException e) {
			logger.error("Failed to execute query. " + e.getMessage());			
		}
		return new ArrayList<Image>();
	}
	
	public void spawnDatabaseForExercise() {
		logger.info("Creating the DB configuration if needed");		
		this.db.createUser("user1", "password");
		this.db.createUser("user2", "password");
		this.db.createUser("user3", "password");
		this.db.createUser("user4", "password");
		this.db.grantDefaultDatabaseAccess("user1", Permissions.RW);
		this.db.grantDefaultDatabaseAccess("user2", Permissions.RW);
		this.db.grantDefaultDatabaseAccess("user3", Permissions.RW);
		this.db.grantDefaultDatabaseAccess("user4", Permissions.RW);
		this.db.createDatabase("schema1");
		this.db.createDatabase("schema2");
		this.db.createDatabase("schema3");
		this.db.createDatabase("schema4");
		this.db.db("schema1").createCollection("images");
		this.db.db("schema2").createCollection("images");
		this.db.db("schema3").createCollection("images");
		this.db.db("schema4").createCollection("images");
	}
}
