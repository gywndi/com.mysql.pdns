package com.mysql.pdns;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Configure {
	private Properties props;

	public Configure(String path) throws FileNotFoundException, IOException{
        props = new Properties();
        props.load(new java.io.BufferedInputStream(new FileInputStream(path)));
	}
	
    public String getString(String k){
    	String v = props.getProperty(k);
    	return v != null ? v.trim() : "";
    }
    
    public String[] getStringArray(String k, String s){
    	String v = props.getProperty(k);
    	if(v == null || v.trim().length() == 0)
    		return new String[0];
    		
    	String[] r = v.trim().split(s);
    	for(int i = 0; i < r.length; i++){
    		r[i] = r[i].trim();
    	}
    	return r;
    }
    
    public long getLong(String k){
    	return getLong(k, -1);
    }
    
    public long getLong(String k, long d){
    	try{
    		return Long.parseLong(props.getProperty(k));
    	}catch(Exception e){}
    	return d;
    }
}
