package iot;
import java.net.InetAddress;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class ServerResource extends CoapResource {
	
	public ServerResource(String name) {
		super(name);
 	}
	
 	public void handleGET(CoapExchange exchange) {
 		System.out.print("Received GET request ");
 		
 		// accept request sending ACK
 		exchange.accept();
 		
 		// get the source-node address
 		InetAddress node_addr = exchange.getSourceAddress();
 		System.out.println("from address: "+node_addr);
 		
 		// create a new request to get the node resources
 		CoapClient client = new CoapClient("coap://["+node_addr.getHostAddress()+"]:5683/.well-known/core");
 		CoapResponse response = client.get();
 		//System.out.println(response.getResponseText());
 		
 		// register the resources of the node
 		for (String res : response.getResponseText().split(",")) {
 			if(res.contains("well-known"))
 				continue;
 			
 			String path = res.split(";")[0].substring(1).split(">")[0].substring(1);
 			
 			NodeResource newRes = new NodeResource(path,res,node_addr.getHostAddress());
 				if(!Main.nodeResources.contains(newRes)) {
 					Main.nodeResources.add(newRes);
 				}
 		}
 	}
	
}