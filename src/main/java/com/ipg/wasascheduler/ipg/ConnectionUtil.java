package com.ipg.wasascheduler.ipg;

import org.springframework.context.annotation.ComponentScan;

import java.sql.Connection;
import java.sql.DriverManager;

@ComponentScan
public class ConnectionUtil {
	
	Connection connection;
	
	public void init()
	{
		try {
		String myDriver = "org.gjt.mm.mysql.Driver";
		String myUrl = "jdbc:mysql://localhost/billpayment";
		Class.forName(myDriver);
		connection = DriverManager.getConnection(myUrl, "root", "abcd@12345");
		} catch (Exception e) {
			System.err.println("Got an exception! ");
			System.err.println(e.getMessage());
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection conn) {
		this.connection = conn;
	}

}
