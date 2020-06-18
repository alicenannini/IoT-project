package iot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Main {
	
	public static List<NodeResource> nodeResources = new ArrayList<NodeResource>();
	public static final String[] commands = {"resources","ON","OFF","sensors"};
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		/* Starting the registration server in a thread */
		new Thread() {
			public void run() {
				RegistrationServer server = new RegistrationServer(4456);
				server.startServer();
			}
		}.start();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		showCommandList();
		
		while(true) {
			try{
				String newCommand = br.readLine();
		        
		        if(newCommand.contentEquals(commands[0])) {
		        	showAvailableResources();
		        }else if(newCommand.contentEquals(commands[3])) {
		        	showResourcesStatus("sensors");
		        }else if(newCommand.contains(commands[1])) {
		        	int index = Integer.parseInt(newCommand.split(" ")[1]) - 1;
		        	NodeResource r = nodeResources.get(index);
		        	switchBulbMode(commands[1],r);
		        }else if(newCommand.contains(commands[2])) {
		        	int index = Integer.parseInt(newCommand.split(" ")[1]) - 1;
		        	NodeResource r = nodeResources.get(index);
		        	switchBulbMode(commands[2],r);
		        }else {
		        	showCommandList();
		        }
		        //Thread.sleep(2000);
			}catch(Exception e) {
				System.err.println("PRESS ENTER AND TRY AGAIN");
				e.printStackTrace();
			}
	        
		}
	}
	
	
	public static void showAvailableResources() {
		System.out.println("INDEX - RESOURCE INFO");
		for(int i = 0; i < nodeResources.size(); i++) {
			NodeResource r = nodeResources.get(i);
        	System.out.println((i+1)+"\t"+r.toString());
		}
	}
	
	
	public static void switchBulbMode(String mode, NodeResource res) {
		CoapClient client = new CoapClient(res.getCoapURI());
		if(res.getPath().contains("actuator")){
			CoapResponse response = client.post("mode="+mode, MediaTypeRegistry.TEXT_PLAIN);
    		//CoapResponse response = client.post("{\"mode\":\"on\"}", MediaTypeRegistry.APPLICATION_JSON);
			String code = response.getCode().toString();
			if(code.startsWith("2")) {
				System.out.println("SUCCESS: the bulb is now "+mode);
			}else {
				System.err.println("ERROR: response code "+code);
			}
    		
		}else {
			System.err.println("Can't switch bulb: This is not an actuator");
		}
	}
	
	
	public static void showResourcesStatus(String nodeType) {
		for(NodeResource r : nodeResources) {
			if(nodeType.equals("sensors")) {
	        	//System.out.println(r.toString());
	        	new ObservingClient(r).start();
			}
		}
	}
	
	
	public static void showCommandList() {
        System.out.println("----------------------------------------------------------------------------\n");
		System.out.println("TYPE ONE OF THE FOLLOWING COMMANDS AND PRESS ENTER:");
		System.out.println("\""+commands[0]+"\"  -> to get the available resources and their addresses");
		System.out.println("\""+commands[1]+" $index\"  -> to switch on the bulb at the corresponding index");
		System.out.println("\""+commands[2]+" $index\" -> to switch off the bulb at the corresponding index");
		System.out.println("\""+commands[3]+"\"    -> to print the status of all available sensors");
		System.out.println("");
	}

}
