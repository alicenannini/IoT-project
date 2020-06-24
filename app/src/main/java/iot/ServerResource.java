package iot;
import java.net.InetAddress;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class ServerResource extends CoapResource {
	
	public ServerResource(String name) {
		super(name);
		this.setObservable(true);
		this.setObserveType(CoAP.Type.CON); 
 	}
	
 	public void handleGET(CoapExchange exchange) {
 		// accept request sending ACK
 		exchange.accept();
 		
 		// get the source-node address
 		InetAddress node_addr = exchange.getSourceAddress();
 		//System.out.println("Registering node "+node_addr);
 		
 		// create a new request to get the node resources
 		CoapClient client = new CoapClient("coap://["+node_addr.getHostAddress()+"]:5683/.well-known/core");
 		CoapResponse response = client.get();
 		
 		// register the resources of the node
 		String code = response.getCode().toString();
 		if(code.startsWith("2")) {
 			NodeResource newRes;
	 		for (String res : response.getResponseText().split(",")) {
	 			if(res.contains("well-known"))
	 				continue;
	 			
	 			String path = res.split(";")[0].substring(1).split(">")[0].substring(1);
	 			String info = res.split(">")[1].substring(1);
	 			newRes = new NodeResource(path,node_addr.getHostAddress(),info);
	 			if(!Main.nodeResources.contains(newRes)) {
	 				Main.nodeResources.add(newRes);
	 				if(newRes.getInfo().contains("obs"))
	 					startObservingResource(newRes);
	 			}
	 			
	 		}
 		}else System.err.println("[SERVER RESOURCE] Response code "+code);
 	}
 	
 	
 	private static void startObservingResource(NodeResource r) {
 		Main.obsClients.add(0,new ObservingClient(r));
	    Main.obsClients.get(0).start();
	}
	
}