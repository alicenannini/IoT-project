package iot;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class ObservingClient extends Thread {
	private NodeResource r;
	private CoapClient client;
	CoapObserveRelation relation;
	
	public ObservingClient(NodeResource r) {
		super();
		this.r = r;
	}
	
	public void run() {
    	//System.out.println(r.toString());
    	this.client = new CoapClient(r.getCoapURI());
		relation = client.observe(new ObservingHandler(this.r), MediaTypeRegistry.APPLICATION_JSON);
		System.out.println("Start observing "+r.toString());
	}
	
	public void stopObserving() {
		this.relation.proactiveCancel();
		System.out.println("Stop observing "+r.toString());
	}
	
	public NodeResource getResource() { return this.r; };
	
	@Override
	public boolean equals(Object o) {
		ObservingClient obsCl = (ObservingClient)o;
		return (obsCl.r.equals(this.r));
	}
}
