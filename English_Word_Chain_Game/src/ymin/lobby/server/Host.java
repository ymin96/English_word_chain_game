package ymin.lobby.server;

public class Host {
	String ipAddress;
	int portNumber;
	
	Host(String ipAddress, int portNumber){
		this.ipAddress = ipAddress;
		this.portNumber = portNumber;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return ipAddress + ":" + Integer.toString(portNumber);
	}
}
