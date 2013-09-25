package tpp;

import java.sql.*;
import java.util.Properties;

import com.mysql.jdbc.Connection;

public class DBConnection {
	
	private com.mysql.jdbc.Connection conn = null;
	private String userName;
	private String password;
	private String dbms;
	private String serverName;
	private String portNumber;
	private String dbName;
	private String table;
	
	public DBConnection(String userName, String password, String database, String table) {
		this.userName = userName;
		this.password = password;
		this.dbName = database;
		this.dbms = "mysql";
		this.serverName = "localhost";
		this.portNumber = "3306";
		this.table = table;
		
		
		try {
			setConnection();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		
	}

	
	private void setConnection() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		
	    Properties connectionProps = new Properties();
	    connectionProps.put("user", this.userName);
	    connectionProps.put("password", this.password);
	    Class.forName("com.mysql.jdbc.Driver").newInstance();

	    if (this.dbms.equals("mysql")) {
	        conn = (Connection) DriverManager.getConnection(
	                   "jdbc:" + dbms + "://" +
	                   serverName +
	                   ":" + portNumber + "/" +
	                   dbName,
	                   connectionProps);
	    } 
	    //return conn;
	}
	
	public com.mysql.jdbc.Connection getConnection() {
		return conn;
	}
	
	public String getTable() {
		return table;
	}
}
