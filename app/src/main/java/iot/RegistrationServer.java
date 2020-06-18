package iot;

import org.eclipse.californium.core.CaliforniumLogger;
import org.eclipse.californium.core.CoapServer;


public class RegistrationServer extends CoapServer {
	
	static {
		CaliforniumLogger.disableLogging();
	}
	
	public RegistrationServer() {
		super();
	}
	
	public RegistrationServer(int port) {
		super(port);
	}
	
	public void startServer() {
		this.add(new ServerResource("registration"));
		this.start();
	}
	
}
