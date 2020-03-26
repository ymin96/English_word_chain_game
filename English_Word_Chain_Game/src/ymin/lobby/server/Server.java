package ymin.lobby.server;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class Server {
	ServerSocketChannel serverSocketChannel;
	public static Selector selector;
	List<User> connections = new Vector<User>();
	
	public void startServer() {
		try {
			selector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.bind(new InetSocketAddress(6001));
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (Exception e) {
			if(serverSocketChannel.isOpen()) {
				stopServer();
			}
			return;
		}
		
		Thread thread = new Thread(()->{
			while(true) {
				try {
					int keyCount = selector.select();
					
					if(keyCount == 0) { continue;}
					
					Set<SelectionKey> selectedKeys = selector.selectedKeys();
					Iterator<SelectionKey> iterator = selectedKeys.iterator();
					while(iterator.hasNext()) {
						SelectionKey key = iterator.next();
						if(key.isAcceptable()) {
							accept(key);
						}
						else if(key.isReadable()) {
							User User = (User)key.attachment();
							
						}
						else if(key.isWritable()) {
							User User = (User)key.attachment();
						}
						iterator.remove();
					}
				}catch(Exception e){
					if(serverSocketChannel.isOpen()) {
						stopServer();
						break;
					}
				}
			}
		});
		
		thread.start();
	}
	
	public void stopServer() {
		try {
			Iterator<User> iterator = connections.iterator();
			while(iterator.hasNext()) {
				User user = iterator.next();
				user.socketChannel.close();
				iterator.remove();
			}
			if(serverSocketChannel != null && serverSocketChannel.isOpen())
				serverSocketChannel.close();
			if(selector != null && selector.isOpen())
				selector.close();
		} catch(Exception e) {}
	}
	
	private void accept(SelectionKey key) {
		try {
			ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
			SocketChannel socketChannel = serverSocketChannel.accept();
			
			System.out.println("클라이언트 연결 수락");
			
			User user = new User(socketChannel);
			connections.add(user);
			
		} catch(Exception e) {
			
		}
	}
}
