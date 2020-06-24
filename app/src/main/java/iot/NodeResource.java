package iot;

import java.sql.Timestamp;
import java.util.Map;
import java.util.TreeMap;

public class NodeResource {
	private final String path;
	private final String nodeAddress;
	private final String info;
	private Map<Timestamp,String> values = new TreeMap<Timestamp,String>();
	
	public NodeResource(String p, String a, String info) {
		this.path = p;
		this.nodeAddress = a;
		this.info = info;
	}
	
	synchronized public void setValues(Map<Timestamp,String> v) { 
		// first remove from the list all values before the last 10 minutes
		long lastHour = System.currentTimeMillis() - (60000 * 10);
		if(!v.isEmpty())
			for(Timestamp key : v.keySet()) {
				   if( key.getTime() < lastHour) {
				       v.remove(key);
				   }
			}
		// then update the list of the class
		this.values = v; 
	}
	
	public String getPath() { return this.path; }
	
	public String getNodeAddress() { return this.nodeAddress; }
	
	public String getInfo() { return this.info; }
	
	synchronized public Map<Timestamp,String> getValues(){ return this.values; }
	
	public String getCoapURI() { return "coap://["+this.nodeAddress+"]:5683/"+this.path; }
	
	@Override
	public String toString() {
		String[] addr = this.nodeAddress.split(":");
		return "Node "+ Integer.parseInt(addr[addr.length-1],16) +" "+ this.path;
	}
	
	public String toDetailedString() {
		return "Node: "+this.nodeAddress+", Path: "+this.path+", "+this.info;
	}
	
	@Override
	public boolean equals(Object o) {
		NodeResource n = (NodeResource) o;
		return (this.path.equals(n.path) && this.nodeAddress.equals(n.nodeAddress));
	}
	
}
