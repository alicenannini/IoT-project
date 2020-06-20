package iot;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class ObservingClient extends CoapClient {
	private NodeResource r;
	CoapObserveRelation relation;
	
	public ObservingClient(NodeResource r) {
		super(r.getCoapURI());
		this.r = r;
	}
	
	public void start() {
    	relation = this.observe(new ObservingHandler(this.r), MediaTypeRegistry.APPLICATION_JSON);
		System.out.println("Start observing "+r.toString());
		new Thread() {
			public void run() {
				while(true) {
					try {
						// sleep 1 hour and then refresh the observe relation
						sleep(3600*1000);
						relation.reregister();
						System.out.println("Reregister "+r.toString());
					} catch (InterruptedException e) { e.printStackTrace(); }
				}
			}
		};
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
