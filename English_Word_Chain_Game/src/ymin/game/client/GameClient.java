package ymin.game.client;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class GameClient {
	SocketChannel socketChannel;
	
	public void startClient() {
		try {
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(true);
			socketChannel.connect(new InetSocketAddress("localhost",12000));
		} catch (Exception e) {
			// TODO: handle exception
			if(socketChannel.isOpen())
				stopClient();
			return;
		}
	}
	
	public void stopClient() {
		try {
			if(socketChannel != null && socketChannel.isOpen())
				socketChannel.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
