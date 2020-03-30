package ymin.lobby.server;

public class Host {
	String ipAddress;
	String title;
	
	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	Host(String ipAddress, String title){
		this.ipAddress = ipAddress;
		this.title = title;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return ipAddress + ":" + title;
	}
}
