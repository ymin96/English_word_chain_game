package ymin.game.server;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class GameServer {
	ServerSocketChannel serverSocketChannel;
	Selector selector;
	List<User> connections = new Vector<User>();
	
	public void startServer() {
		try {
			selector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.bind(new InetSocketAddress(12000));
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (Exception e) {
			// TODO: handle exception
			if(serverSocketChannel.isOpen())
				stopServer();
		}
		
		Thread thread = new Thread(()->{
			while(true) {
				try {
					int keyCount = selector.select();
					
					if(keyCount == 0)
						continue;
					
					Set<SelectionKey> selectedKeys = selector.selectedKeys();
					
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		});
	}
	
	public void stopServer() {
		try {
			Iterator<User> iterator = connections.iterator();
			while(iterator.hasNext()) {
				User user = iterator.next();
				user.socketChannel.close();
				iterator.remove();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	
}
