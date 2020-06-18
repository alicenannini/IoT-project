package iot;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class NodeResource {
	private String path;
	private String name;
	private String nodeAddress;
	private Map<Timestamp,Double> values = new HashMap<Timestamp,Double>();
	
	public NodeResource(String p, String n, String a) {
		this.path = p;
		this.name = n;
		this.nodeAddress = a;
	}
	
	public void setPath(String p) { this.path = p; }
	
	public void setName(String n) { this.name = n; }
	
	public void setNodeAddress(String a) { this.nodeAddress = a; }
	
	public void setValues(Map<Timestamp,Double> v) { this.values = v; }
	
	public String getPath() { return this.path; }
	
	public String getName() { return this.name; }
	
	public String getNodeAddress() { return this.nodeAddress; }
	
	public Map<Timestamp,Double> getValues(){ return this.values; }
	
	public String getCoapURI() { return "coap://["+this.nodeAddress+"]:5683/"+this.path; }
	
	@Override
	public String toString() {
		return "Node: "+this.nodeAddress+", Path: "+this.path;
		//return "Path:\""+this.path+"\", Name:"+this.name+", Node:"+this.nodeAddress;
	}
	
	public boolean equals(String path, String nodeAddress) {
		return (this.path.equals(path) && this.nodeAddress.equals(nodeAddress));
	}
	
}
