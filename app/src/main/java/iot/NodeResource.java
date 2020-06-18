package iot;

import java.sql.Timestamp;
import java.util.Map;
import java.util.TreeMap;

public class NodeResource {
	private String path;
	private String name;
	private String nodeAddress;
	private Map<Timestamp,String> values = new TreeMap<Timestamp,String>();
	
	public NodeResource(String p, String n, String a) {
		this.path = p;
		this.name = n;
		this.nodeAddress = a;
	}
	
	public void setPath(String p) { this.path = p; }
	
	public void setName(String n) { this.name = n; }
	
	public void setNodeAddress(String a) { this.nodeAddress = a; }
	
	public void setValues(Map<Timestamp,String> v) { 
		// first remove from the list all values before the last hour
		long lastHour = System.currentTimeMillis() - 36000000;
		for(Timestamp key : v.keySet()) {
			   if( key.getTime() < lastHour) {
			       v.remove(key);
			   }
		}
		// then update the list of the class
		this.values = v; 
	}
	
	public String getPath() { return this.path; }
	
	public String getName() { return this.name; }
	
	public String getNodeAddress() { return this.nodeAddress; }
	
	public Map<Timestamp,String> getValues(){ return this.values; }
	
	public String getCoapURI() { return "coap://["+this.nodeAddress+"]:5683/"+this.path; }
	
	@Override
	public String toString() {
		return "Node "+ this.nodeAddress.substring(this.nodeAddress.length()-1) +" "+ this.path;
		//return "Node: "+this.nodeAddress+", Path: "+this.path;
		//return "Node: "+this.nodeAddress+", Path: "+this.path+", Name:"+this.name;
	}
	
	public boolean equals(String path, String nodeAddress) {
		return (this.path.equals(path) && this.nodeAddress.equals(nodeAddress));
	}
	
}
