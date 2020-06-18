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
	public static final String[] commands = {"resources","ON","OFF","sensors","observe","actuators","stop observing"};
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
		        }else if(newCommand.contains(commands[4])) {
		        	// starting to observe sensors or actuators
		        	String nodeType = newCommand.split(" ")[1];
		        	if(nodeType.contentEquals("s"))
		        		startObservingResources("sensors");
		        	else if(nodeType.contentEquals("a"))
		        		startObservingResources("actuators");
		        }else if(newCommand.contains(commands[6])) {
		        	// stopping to observe sensors or actuators
		        	String nodeType = newCommand.split(" ")[2];
		        	if(nodeType.contentEquals("s"))
		        		stopObservingResources("sensors");
		        	else if(nodeType.contentEquals("a"))
		        		stopObservingResources("actuators");
		        }else if(newCommand.contentEquals(commands[3])) {
		        	// showing the status of observated sensors
		        	showResourcesStatus(commands[3]);
		        }else if(newCommand.contentEquals(commands[5])) {
		        	// shoing the status of observed actuators
		        	showResourcesStatus(commands[5]);
		        }else if(newCommand.contains(commands[1])) {
		        	// switching ON the status of chosen bulb actuator
		        	int index = Integer.parseInt(newCommand.split(" ")[1]) - 1;
		        	NodeResource r = nodeResources.get(index);
		        	switchBulbMode(commands[1],r);
		        }else if(newCommand.contains(commands[2])) {
		        	// switching OFF the status of chosen bulb actuator
		        	int index = Integer.parseInt(newCommand.split(" ")[1]) - 1;
		        	NodeResource r = nodeResources.get(index);
		        	switchBulbMode(commands[2],r);
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
        	System.out.println("("+(i+1)+")\t"+r.toString());
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
				long millis = System.currentTimeMillis();
				Timestamp now = new Timestamp(millis - (millis%1000));
				Map<Timestamp,String> v = res.getValues();
				v.put(now, "1");
				res.setValues(v);
			}else {
				System.err.println("ERROR: response code "+code);
			}
    		
		}else {
			System.err.println("Can't switch bulb: This is not an actuator");
		}
	}
	
	// TOGLIERE ????
	public static void startObservingResources(String nodeType) {
		int i = 0;
		for(NodeResource r : nodeResources) {
			if(r.getPath().contains(nodeType)) {
				ObservingClient newObs = new ObservingClient(r);
				if(!obsClients.contains(newObs)) {
		        	//System.out.println(r.toString());
		        	obsClients.add(i,newObs);
		        	obsClients.get(i).start();
				}
	        	i++;
			}
		}
	}
	
	// TOGLIERE ????
	public static void stopObservingResources(String nodeType) throws InterruptedException {
		synchronized(obsClients) {
			for(ObservingClient c : obsClients) {
				if(c.getResource().getPath().contains(nodeType)) {
					c.stopObserving();
					obsClients.remove(c);
				}
			}
		}
	}
	
	public static void showResourcesStatus(String nodeType) {
		for(NodeResource r : nodeResources) {
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
	}
	
	
	public static void showCommandList() {
        System.out.println("--------------------------------------------------------------------------------------------\n");
		System.out.println("TYPE ONE OF THE FOLLOWING COMMANDS AND PRESS ENTER:");
		System.out.println("\""+commands[0]+"\"\t\t-> to get the available resources and their addresses");
		System.out.println("\""+commands[4]+" s/a\"\t\t-> to to start observing the available sensors (s) or actuators (a)");
		System.out.println("\""+commands[6]+" s/a\"\t-> to to stop observing the available sensors (s) or actuators (a)");
		System.out.println("\""+commands[1]+" $index\"\t\t-> to switch on the bulb at the corresponding index");
		System.out.println("\""+commands[2]+" $index\"\t\t-> to switch off the bulb at the corresponding index");
		System.out.println("\""+commands[3]+"\"\t\t-> to print the status of all available sensors");
		System.out.println("\""+commands[5]+"\"\t\t-> to print the status of all available actuators");
		System.out.println("");
	}

}
