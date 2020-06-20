package iot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class Main {
	
	public static List<NodeResource> nodeResources = new ArrayList<NodeResource>();
	public static final String[] commands = {"resources","ON","OFF","sensor","AUTO","actuator","MANUAL","exit"};
	public static List<ObservingClient> obsClients = new ArrayList<ObservingClient>();
	
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
		        	// showing available resources
		        	showAvailableResources();
		        }else if(newCommand.contains(commands[3])) {
		        	// showing the status of observed sensors
		        	int index = Integer.parseInt(newCommand.split(" ")[1]);
		        	if(index >= 0) {
			        	NodeResource r = nodeResources.get(index);
			        	showResourcesStatus(commands[3], r);
		        	}else {
		        		for(NodeResource r : nodeResources)
		        			showResourcesStatus(commands[3], r);
		        	}
		        }else if(newCommand.contains(commands[5])) {
		        	// showing the status of observed actuators
		        	int index = Integer.parseInt(newCommand.split(" ")[1]);
		        	if(index >= 0) {
			        	NodeResource r = nodeResources.get(index);
			        	showResourcesStatus(commands[5], r);
		        	}else {
		        		for(NodeResource r : nodeResources)
		        			showResourcesStatus(commands[5], r);
		        	}
		        }else if(newCommand.contains(commands[1])) {
		        	// switching ON the status of chosen bulb actuator
		        	int index = Integer.parseInt(newCommand.split(" ")[1]);
		        	if(index >= 0) {
			        	NodeResource r = nodeResources.get(index);
			        	switchBulbMode(commands[1],"mode",r);
		        	}else {
		        		for(NodeResource r : nodeResources)
		        			if(r.getPath().contains("actuator"))
		        				switchBulbMode(commands[1],"mode",r);
		        			else System.err.println("This is not an actuator");
		        	}
		        }else if(newCommand.contains(commands[2])) {
		        	// switching OFF the status of chosen bulb actuator
		        	int index = Integer.parseInt(newCommand.split(" ")[1]);
		        	if(index >= 0) {
			        	NodeResource r = nodeResources.get(index);
			        	switchBulbMode(commands[2],"mode",r);
		        	}else {
		        		for(NodeResource r : nodeResources)
		        			if(r.getPath().contains("actuator"))
		        				switchBulbMode(commands[2],"mode",r);
		        			else System.err.println("This is not an actuator");
		        	}
		        }else if(newCommand.contains(commands[4])) {
		        	// switching bulb actuator in automatic mode
		        	int index = Integer.parseInt(newCommand.split(" ")[1]);
		        	if(index >= 0) {
			        	NodeResource r = nodeResources.get(index);
			        	switchBulbMode(commands[1],"automatic",r);
		        	}else {
		        		for(NodeResource r : nodeResources)
		        			if(r.getPath().contains("actuator"))
		        				switchBulbMode(commands[1],"automatic",r);
		        			else System.err.println("This is not an actuator");
		        	}
		        }else if(newCommand.contains(commands[6])) {
		        	// switching bulb actuator in manual mode
		        	int index = Integer.parseInt(newCommand.split(" ")[1]);
		        	if(index >= 0) {
			        	NodeResource r = nodeResources.get(index);
			        	switchBulbMode(commands[2],"automatic",r);
		        	}else {
		        		for(NodeResource r : nodeResources)
		        			if(r.getPath().contains("actuator"))
		        				switchBulbMode(commands[2],"automatic",r);
		        			else System.err.println("This is not an actuator");
		        	}
		        }else if(newCommand.contentEquals(commands[7])) {
		        	System.exit(0);
		        }else {
		        	showCommandList();
		        }
		        
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
        	System.out.println("("+i+")\t"+r.toString());
		}
	}
	
	
	public static void switchBulbMode(String mode, String attribute, NodeResource res) {
		CoapClient client = new CoapClient(res.getCoapURI());
		if(res.getPath().contains("actuator")){
			CoapResponse response = client.post(attribute+"="+mode, MediaTypeRegistry.TEXT_PLAIN);
			String code = response.getCode().toString();
			if(code.startsWith("2")) {
				System.out.println("SUCCESS: the bulb "+attribute+" attribute is now "+mode);
				if(attribute.contentEquals("mode")) {
					long millis = System.currentTimeMillis();
					Timestamp now = new Timestamp(millis - (millis%1000));
					Map<Timestamp,String> v = res.getValues();
					String newValue = (mode == "ON")? "1" : "0";
					v.put(now, newValue);
					res.setValues(v);
				}
			}else {
				System.err.println("ERROR: response code "+code);
			}
    		
		}else {
			System.err.println("Can't switch bulb: This is not an actuator");
		}
	}
	
	
	
	public static void showResourcesStatus(String nodeType, NodeResource r) {
		if(r.getPath().contains(nodeType)) {
			int index = nodeResources.indexOf(r);
			System.out.print("("+index+") "+r.toString());
			System.out.println("\tTIMESTAMP\t\tVALUE");
			Map<Timestamp,String> v = r.getValues();
			for(Timestamp key : v.keySet()) {
				System.out.println("\t\t\t\t"+key + "   " + v.get(key));
			}
			System.out.println("");
		}
	}
	
	
	
	public static void showCommandList() {
        System.out.println("----------------------------------------------------------------------------------------------------------------\n");
		System.out.println("TYPE ONE OF THE FOLLOWING COMMANDS AND PRESS ENTER:");
		System.out.println("\""+commands[0]+"\"\t\t-> to get the available resources and their addresses");
		System.out.println("\""+commands[1]+" $index\"\t\t-> to switch on the bulb at the corresponding index (-1 for all bulbs)");
		System.out.println("\""+commands[2]+" $index\"\t\t-> to switch off the bulb at the corresponding index (-1 for all bulbs)");
		System.out.println("\""+commands[4]+" $index\"\t\t-> to automate the bulb at the corresponding index (-1 for all bulbs)");
		System.out.println("\""+commands[6]+" $index\"\t\t-> to disable automatic mode for the bulb at the corresponding index (-1 for all bulbs)");
		System.out.println("\""+commands[3]+" $index\"\t\t-> to print the status of the $index sensor (-1 for all sensors)");
		System.out.println("\""+commands[5]+" $index\"\t-> to print the status of the $index actuator (-1 for all actuators)");
		System.out.println("\""+commands[7]+"\"\t\t\t-> to close the application");
		System.out.println("");
	}

}
