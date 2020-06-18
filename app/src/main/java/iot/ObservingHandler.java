package iot;

import java.sql.Timestamp;
import java.util.Map;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ObservingHandler implements CoapHandler {
	private NodeResource resource;
	
	public ObservingHandler(NodeResource r) {
		super();
		this.resource = r;
	}
	
	
	public void onLoad(CoapResponse response) {
		try {
			if(resource.getPath().contains("sensor")) {
				Object obj = new JSONParser().parse(response.getResponseText());
				// typecasting obj to JSONObject 
		        JSONObject jo = (JSONObject) obj; 
		        // getting firstName and lastName 
		        Double light_value = (Double)jo.get("light"); 
		        Timestamp timestamp = new Timestamp(((Long) jo.get("timestamp"))*1000); 
		        
				//System.out.println(response.advanced().getSource().toString()+": "+timestamp+", light "+light_value);
				
				Map<Timestamp,Double> resourceValues = resource.getValues();
				resourceValues.put(timestamp, light_value);
				Main.nodeResources.get(Main.nodeResources.indexOf(resource)).setValues(resourceValues);
		        
			}
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
          
		
	}

	public void onError() {
		System.err.println("-Observing Failed--------");
		
	}

}
