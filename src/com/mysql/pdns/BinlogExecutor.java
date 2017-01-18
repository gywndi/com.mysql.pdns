package com.mysql.pdns;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import com.google.code.or.*;

import com.google.code.or.binlog.*;
import com.google.code.or.common.util.*;
import com.google.code.or.binlog.impl.event.*;
import com.google.code.or.common.glossary.*;

public class BinlogExecutor {
	static OpenReplicator openReplicator = null;
	public static void main(String[] argv) throws FileNotFoundException, IOException, ClassNotFoundException, SQLException{
		Constants.load(argv[0]);
		while(true){
			if(openReplicator == null || !openReplicator.isRunning()){
				openReplicator = BinlogUtil.startReplicator();
				System.out.println("DNS replicator started!");
				continue;
			}
			try{
				Thread.sleep(1000);
			}catch(Exception e){}
		}
	}
}
