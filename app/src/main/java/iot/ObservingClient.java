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
    	relation = this.observe(new ObservingHandler(this), MediaTypeRegistry.APPLICATION_JSON);
		System.out.println("[Start observing "+r.toString()+"\t]");
		new ObserveThread(this).start();
	}
	
	public void stopObserving() {
		this.relation.proactiveCancel();
		System.out.println("[Stop observing "+r.toString()+"\t]");
	}
	
	public NodeResource getResource() { return this.r; };
	
	@Override
	public boolean equals(Object o) {
		ObservingClient obsCl = (ObservingClient)o;
		return (obsCl.r.equals(this.r));
	}
	
	private class ObserveThread extends Thread {
		ObservingClient c;
		public ObserveThread(ObservingClient c) { super(); this.c = c; }
		
		public void run() {
			while(true) {
				try {
					/* sleep and then refresh the observe relation */
					sleep(60*1000);
					relation.reregister();
					//System.out.println("Reregister "+r.toString());
				} 
				catch (IllegalStateException e) { relation = c.observe(new ObservingHandler(c), MediaTypeRegistry.APPLICATION_JSON); }
				catch (InterruptedException e) { e.printStackTrace(); }
			}
		}
	}
	
}
