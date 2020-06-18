package iot;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapObserveRelation;

public class ObservingClient extends Thread {
	private NodeResource r;
	private CoapClient client;
	
	public ObservingClient(NodeResource r) {
		super();
		this.r = r;
	}
	
	public void run() {
		
    	//System.out.println(r.toString());
    	this.client = new CoapClient(r.getCoapURI());
		CoapObserveRelation relation = client.observe(new ObservingHandler(this.r));
		
		//try { Thread.sleep(6*1000); } catch (InterruptedException e) { }
		//relation.proactiveCancel(); // to cancel observing
		
	}
	
}
