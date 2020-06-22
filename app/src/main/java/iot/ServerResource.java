package iot;
import java.net.InetAddress;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class ServerResource extends CoapResource {
	
	public ServerResource(String name) {
		super(name);
 	}
	
 	public void handleGET(CoapExchange exchange) {
 		// accept request sending ACK
 		exchange.accept();
 		
 		// get the source-node address
 		InetAddress node_addr = exchange.getSourceAddress();
 		System.out.println("Registering node "+node_addr);
 		
 		// create a new request to get the node resources
 		CoapClient client = new CoapClient("coap://["+node_addr.getHostAddress()+"]:5683/.well-known/core");
 		CoapResponse response = client.get();
 		
 		// register the resources of the node
 		String code = response.getCode().toString();
 		if(code.startsWith("2")) {
	 		for (String res : response.getResponseText().split(",")) {
	 			if(res.contains("well-known"))
	 				continue;
	 			
	 			String path = res.split(";")[0].substring(1).split(">")[0].substring(1);
	 			String name = res.split(">")[1].substring(1);
	 			NodeResource newRes = new NodeResource(path,name,node_addr.getHostAddress());
	 			if(!Main.nodeResources.contains(newRes)) {
	 				Main.nodeResources.add(newRes);
	 				startObservingResource(newRes);
	 			}
	 		}
 		}
 	}
 	
 	private static void startObservingResource(NodeResource r) {
		Main.obsClients.add(0,new ObservingClient(r));
    	Main.obsClients.get(0).start();
	}
	
}