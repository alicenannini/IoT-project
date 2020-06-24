package iot;

import org.eclipse.californium.core.CaliforniumLogger;
import org.eclipse.californium.core.CoapServer;


public class RegistrationServer extends CoapServer {
	private static final  int port = 5683;
	static {
		CaliforniumLogger.disableLogging();
	}
	
	public RegistrationServer() {
		super(port);
	}
	
	public RegistrationServer(int port) {
		super(port);
	}
	
	public void startServer() {
		this.add(new ServerResource("registration"));
		this.start();
	}
	
}
