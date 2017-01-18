package com.mysql.pdns;

import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.List;

import com.google.code.or.OpenReplicator;
import com.google.code.or.binlog.BinlogEventListener;
import com.google.code.or.binlog.BinlogEventV4;
import com.google.code.or.binlog.BinlogEventV4Header;
import com.google.code.or.binlog.impl.event.DeleteRowsEvent;
import com.google.code.or.binlog.impl.event.DeleteRowsEventV2;
import com.google.code.or.binlog.impl.event.RotateEvent;
import com.google.code.or.binlog.impl.event.TableMapEvent;
import com.google.code.or.binlog.impl.event.UpdateRowsEvent;
import com.google.code.or.binlog.impl.event.UpdateRowsEventV2;
import com.google.code.or.binlog.impl.event.WriteRowsEvent;
import com.google.code.or.binlog.impl.event.WriteRowsEventV2;
import com.google.code.or.common.glossary.Column;
import com.google.code.or.common.glossary.Pair;
import com.google.code.or.common.glossary.Row;
import com.google.code.or.common.util.MySQLConstants;

public class BinlogUtil {
	public static Object[] getMasterStatus(){
		Object[] objs = null;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://"+Constants.replHost+":"+Constants.replPort+"/pdns_util", Constants.replUser, Constants.replPass);
			stmt = conn.createStatement();
			rs = stmt.executeQuery("call show_master_status");
			if(rs.next()){
				objs = new Object[2];
				String binlogFile = rs.getString("File");
				Integer binlogPos = rs.getInt("Position");
				objs[0] = binlogFile;
				objs[1] = binlogPos;
			}else{
				System.out.println("Can not gather master status info");
				System.out.println(">> log_bin, log_slave-update option must be!");
			}
			rs.close();
		}catch(Exception e){
		}finally{
			try{rs.close();}catch(Exception e){}
			try{stmt.close();}catch(Exception e){}
			try{conn.close();}catch(Exception e){}
		}
		return objs;
	}
	
	public static String getRecordName(TableMapEvent tableMapEvent, List<Column> list){
		try{
			return list.get(2).toString().trim();
		}catch(Exception e){
			e.printStackTrace();
		}
		return "";
	}
	
	public static boolean execDnsPurge(String target){
		try {
			String command = Constants.pdnsManager + " purge " + target;
			System.out.println(command);
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrac e();
			return false;
		}
		return true;
	}
	
	public static OpenReplicator startReplicator(){
		System.out.println("Start DNS Replicator..");
		
		// Get Master Status
		int binlogPosition = -1;
		String binlogFileName = "";
		Object[] masterStatus = BinlogUtil.getMasterStatus();
		if(masterStatus != null){
			binlogFileName = (String) masterStatus[0];
			binlogPosition = ((Integer) masterStatus[1]).intValue();
		}

		System.out.println(">> Prepare.. master status : "+binlogFileName+":"+binlogPosition);
		OpenReplicator openReplicator = new OpenReplicator();
		openReplicator.setUser(Constants.replUser);
		openReplicator.setPassword(Constants.replPass);
		openReplicator.setHost(Constants.replHost);
		openReplicator.setPort(Constants.replPort);
		openReplicator.setServerId(Constants.appSId);
		openReplicator.setBinlogFileName(binlogFileName);
		openReplicator.setBinlogPosition(binlogPosition);
		openReplicator.setBinlogEventListener(new BinlogEventListener() {
			
			TableMapEvent lastTableMapEvent = null;
			public void onEvents(BinlogEventV4 event) {
				String targetStr = "";
				BinlogEventV4Header binlogEventV4header = event.getHeader();
				switch (binlogEventV4header.getEventType()){
					case MySQLConstants.TABLE_MAP_EVENT:
						lastTableMapEvent = (TableMapEvent) event;
						break;
						
					case MySQLConstants.WRITE_ROWS_EVENT:
						targetStr = "";

						// Check table name - only records
						if(!lastTableMapEvent.getTableName().toString().equals("records")){break;}
						
						for(Row row : ((WriteRowsEvent) event).getRows()){
							targetStr += BinlogUtil.getRecordName(lastTableMapEvent, row.getColumns()) + " ";
						}
						BinlogUtil.execDnsPurge(targetStr);
						break;
					case MySQLConstants.WRITE_ROWS_EVENT_V2:
						targetStr = "";

						// Check table name - only records
						if(!lastTableMapEvent.getTableName().toString().equals("records")){break;}
						
						for(Row row : ((WriteRowsEventV2) event).getRows()){
							targetStr += BinlogUtil.getRecordName(lastTableMapEvent, row.getColumns()) + " ";
						}
						BinlogUtil.execDnsPurge(targetStr);
						break;
						
					case MySQLConstants.UPDATE_ROWS_EVENT:
						targetStr = "";
						
						// Check table name
						if(!lastTableMapEvent.getTableName().toString().equals("records")){break;}

						for(Pair<Row> pair : ((UpdateRowsEvent) event).getRows()){
							targetStr += BinlogUtil.getRecordName(lastTableMapEvent, pair.getAfter().getColumns()) + " ";
						}
						BinlogUtil.execDnsPurge(targetStr);
						//BinlogUtil.execDnsPurge(sb.toString());
						break;
					case MySQLConstants.UPDATE_ROWS_EVENT_V2:
						targetStr = "";
						
						// Check table name
						if(!lastTableMapEvent.getTableName().toString().equals("records")){break;}

						for(Pair<Row> pair : ((UpdateRowsEventV2) event).getRows()){
							targetStr += BinlogUtil.getRecordName(lastTableMapEvent, pair.getAfter().getColumns()) + " ";
						}
						BinlogUtil.execDnsPurge(targetStr);
						break;
						
					case MySQLConstants.DELETE_ROWS_EVENT:
						targetStr = "";
						
						// Check table name
						if(!lastTableMapEvent.getTableName().toString().equals("records")){break;}

						for(Row row : ((DeleteRowsEvent) event).getRows()){
							targetStr += BinlogUtil.getRecordName(lastTableMapEvent, row.getColumns()) + " ";
						}
						BinlogUtil.execDnsPurge(targetStr);
						break;
					case MySQLConstants.DELETE_ROWS_EVENT_V2:
						targetStr = "";
						
						// Check table name
						if(!lastTableMapEvent.getTableName().toString().equals("records")){break;}

						for(Row row : ((DeleteRowsEventV2) event).getRows()){
							targetStr += BinlogUtil.getRecordName(lastTableMapEvent, row.getColumns()) + " ";
						}
						BinlogUtil.execDnsPurge(targetStr);
						break;
						
					case MySQLConstants.ROTATE_EVENT:
						RotateEvent rotateEvent = (RotateEvent)event;
						System.out.println("Binary Lof File Rotated!");
						System.out.println(">> File : "+rotateEvent.getBinlogFileName());
						System.out.println(">> Position : "+rotateEvent.getBinlogPosition());
						break;
					default:
				}
			}
		});
		try {
			openReplicator.start();
			System.out.println(">> DNS replicator started");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		// Reset all DNS packet cache
		BinlogUtil.execDnsPurge("");
		System.out.println(">> DNS all packet cache expired!");
		
		return openReplicator;
	}
}
