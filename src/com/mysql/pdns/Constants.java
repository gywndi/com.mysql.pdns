
package com.mysql.pdns;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Constants {
	public static Configure configure = null;
	public static String replUser = null;
	public static String replPass = null;
	public static String replHost = null;
	public static String pdnsManager = null;
	public static int replPort = 3306;
	public static int appSId = 0;
	public static Random RAND = new Random();

	
	public static void load(String path) throws FileNotFoundException, IOException{
		configure = new Configure(path);
		replUser = configure.getString("db.repl.user");
		replPass = configure.getString("db.repl.pass");
		replHost = configure.getString("db.repl.host");
		pdnsManager = configure.getString("pdns.manager");
		
		// Port
		try{
			replPort = Integer.parseInt(configure.getString("db.repl.port"));
		}catch(Exception e){
			System.out.println("[Warn] Get Port Number Fail - Use Default Port 3306");
		}
		
		// Server ID
		try{
			appSId = Integer.parseInt(configure.getString("app.sid"));
		}catch(Exception e){
			appSId = RAND.nextInt(10000)+10000;
			System.out.println("[Warn] Get Server Id Fail - Use Random ID ["+appSId+"]");
		}
	}

	public static void main(String[] argv){
		System.out.println(RAND.nextInt(10000)+10000);
	}
}