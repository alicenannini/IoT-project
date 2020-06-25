package iot;

import java.sql.Timestamp;
import java.util.Map;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

public class ObservingHandler implements CoapHandler {
	private ObservingClient client;
	
	public ObservingHandler(ObservingClient c) {
		super();
		this.client = c;
	}
	
	
	public void onLoad(CoapResponse response) {
		try {
			String code = response.getCode().toString();
			if(code.startsWith("2"))
				if(response.getOptions().getAccept() == MediaTypeRegistry.APPLICATION_JSON
						|| response.getOptions().getAccept() == MediaTypeRegistry.UNDEFINED) 
					addResourceFromJsonMsg(response.getResponseText());
				else if (response.getOptions().getAccept() == MediaTypeRegistry.TEXT_PLAIN)
					addResourceFromTextMsg(response.getResponseText());
				else System.err.println("[OBSERVING HANDLER] Accept option not supported");
			else {
				System.err.println("[OBSERVING HANDLER] Response Code "+code);
			}
			
		} catch (ParseException e) {
			e.printStackTrace();
		} 
          
		
	}

	
	public void onError() {
		this.client.stopObserving();
	}
	
	
	private void addResourceFromJsonMsg(String response) throws ParseException {
		String value; 
        Timestamp timestamp;
        JSONObject jo = (JSONObject) JSONValue.parseWithException(response);
		// getting light value and timestamp and value
		if( jo.containsKey("timestamp") )
        	timestamp = new Timestamp(((Long) jo.get("timestamp"))*1000); 
        else {
        	System.err.println("Can't find timestamp value");
        	return;
        }
		
		if(client.getResource().getPath().contains("sensor")) {
			if( jo.containsKey("light") )
				value = jo.get("light").toString(); 
			else {
				System.err.println("Can't find light value");
				return;
			}		        
		}else if(client.getResource().getPath().contains("actuators")) {
			if( jo.containsKey("mode") )
				value = jo.get("mode").toString(); 
			else {
				System.err.println("Can't find mode value");
				return;
			}	
		}else {
			System.err.println("Can't find node type");
			return;
		}
		
		//System.out.println(response.advanced().getSource().toString()+": "+ timestamp +", light "+value);
		
		Map<Timestamp,String> resourceValues = client.getResource().getValues();
		resourceValues.put(timestamp, value);
		int index = Main.nodeResources.indexOf(client.getResource());
		Main.nodeResources.get(index).setValues(resourceValues);
	}
	
	private void addResourceFromTextMsg(String response) {
		String value; 
        Timestamp timestamp;
        
        String[] maps = response.split(",");
        if(client.getResource().getPath().contains("sensor")) {
        	if(maps[0].contentEquals("light"))
        		value = maps[0].split("=")[1];
        	else {
				System.err.println("Can't find light value");
				return;
			}		
        }else if(client.getResource().getPath().contains("actuators")) {
        	if(maps[0].contentEquals("mode"))
        		value = maps[0].split("=")[1];
        	else {
				System.err.println("Can't find mode value");
				return;
        	}
        }else {
			System.err.println("Can't find node type");
			return;
        }
        
        if( maps[1].contentEquals("timestamp") )
        	timestamp = new Timestamp((Long.parseLong(maps[1].split("=")[1]))*1000); 
        else {
        	System.err.println("Can't find timestamp value");
        	return;
        }
        
        //System.out.println(response.advanced().getSource().toString()+": "+ timestamp +", light "+value);
		
  		Map<Timestamp,String> resourceValues = client.getResource().getValues();
  		resourceValues.put(timestamp, value);
  		Main.nodeResources.get(Main.nodeResources.indexOf(client.getResource())).setValues(resourceValues);
        
	}

}
