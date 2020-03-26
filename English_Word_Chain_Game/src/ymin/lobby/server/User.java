package ymin.lobby.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class User {
	protected SocketChannel socketChannel;
	public String sendData;
	
	User(SocketChannel socketChannel) throws IOException{
		this.socketChannel = socketChannel;
		socketChannel.configureBlocking(false);
		SelectionKey selectionKey = socketChannel.register(Server.selector, SelectionKey.OP_READ);
		selectionKey.attach(this);
	}
	
	void receive(SelectionKey selectionKey) {
		
	}
	
	void send(SelectionKey selectionKey) {
		
	}
}
