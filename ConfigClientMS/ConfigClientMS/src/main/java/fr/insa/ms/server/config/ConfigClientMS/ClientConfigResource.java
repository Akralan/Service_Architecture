package fr.insa.ms.server.config.ConfigClientMS;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClientConfigResource {

	
	@Value("${server.port}")
	private String serverPort;
	
	/*@Value("${db.port}")
	private String dbPort;
	
	@Value("${db.host}")
	private String dbHost;
	
	@Value("${db.user}")
	private String dbUser;
	
	@Value("${db.password}")
	private String dbPassword;*/
	
	@GetMapping("/Serverport")
	public String getServerPort() {
	    return serverPort;
	}

	public void setServerPort(String serverPort) {
	    this.serverPort = serverPort;
	}

	/*public String getDbPort() {
	    return dbPort;
	}

	public void setDbPort(String dbPort) {
	    this.dbPort = dbPort;
	}

	public String getDbHost() {
	    return dbHost;
	}

	public void setDbHost(String dbHost) {
	    this.dbHost = dbHost;
	}

	public String getDbUser() {
	    return dbUser;
	}

	public void setDbUser(String dbUser) {
	    this.dbUser = dbUser;
	}

	public String getDbPassword() {
	    return dbPassword;
	}

	public void setDbPassword(String dbPassword) {
	    this.dbPassword = dbPassword;
	}*/


}


