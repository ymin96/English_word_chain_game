package ymin.lobby.server;

public class Host {
	String ipAddress;
	int portNumber;
	
	Host(String ipAddress, int portNumber){
		this.ipAddress = ipAddress;
		this.portNumber = portNumber;
	}
	
	Host(String ipAddress){
		this.ipAddress = ipAddress;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return ipAddress + ":" + Integer.toString(portNumber);
	}
}
