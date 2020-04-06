package ymin.game.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class GameServer {
	ServerSocketChannel serverSocketChannel;
	Selector selector;
	List<User> connections = new Vector<User>();
	public static final int LEADY = 100;
	public static final int RUNNING = 200;
	int state;
	boolean closeSignal = false;
	
	public void startServer() {
		try {
			selector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.bind(new InetSocketAddress(12000));
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			this.state = LEADY;
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
					Iterator<SelectionKey> iterator = selectedKeys.iterator();
					while(iterator.hasNext()) {
						SelectionKey key = iterator.next();
						if(key.isAcceptable()) {
							accept(key);
						}
						else if(key.isReadable()) {
							User user = (User)key.attachment();
							user.receive(key);
						}
						else if(key.isWritable()) {
							User user = (User) key.attachment();
							user.send(key);
						}
						iterator.remove();
					}
					
					if(closeSignal == true) {	//방장이 나갈 시 서버 닫기
						stopServer();
						return;
					}
				} catch (Exception e) {
					// TODO: handle exception
					stopServer();
					return;
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
			if(serverSocketChannel != null && serverSocketChannel.isOpen())
				serverSocketChannel.close();
			if(selector != null && selector.isOpen())
				selector.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	//클라이언트 연결 수락
	private void accept(SelectionKey key) {
		try {
			ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
			SocketChannel socketChannel = serverSocketChannel.accept();
			
			for(User user : connections) {
				InetSocketAddress ia = (InetSocketAddress) socketChannel.getRemoteAddress();
				String id = ia.getHostName();
				user.sendData = id + "님이 입장했습니다.";
				SelectionKey userKey = user.socketChannel.keyFor(selector);
				userKey.interestOps(SelectionKey.OP_WRITE);
				selector.wakeup();
			}
			
			User user = new User(socketChannel, selector);
			connections.add(user);
		}catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	
	public class User {
		SocketChannel socketChannel;
		public String sendData;

		User(SocketChannel socketChannel, Selector selector) throws IOException {
			this.socketChannel = socketChannel;
			socketChannel.configureBlocking(false);
			SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
			selectionKey.attach(this);
		}

		// 연결된 클라이언트에게 데이터 송신
		public void send(SelectionKey selectionKey) {
			try {
				Charset charset = Charset.forName("UTF-8");
				ByteBuffer byteBuffer = charset.encode(sendData);
				socketChannel.write(byteBuffer); // 데이터 전송
				selectionKey.interestOps(SelectionKey.OP_READ); // 작업 유형 변경
				selector.wakeup(); // 변경된 작업 유형을 감지
			} catch (Exception e) {
				// TODO: handle exception
				try {
					connections.remove(this);
					socketChannel.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		
		public void receive(SelectionKey selectionKey) {
			try {
				ByteBuffer byteBuffer = ByteBuffer.allocate(100);
				
				int byteCount = socketChannel.read(byteBuffer);
				
				if(byteCount == -1)
					throw new IOException();
				
				byteBuffer.flip();
				Charset charset = Charset.forName("UTF-8");
				String data = charset.decode(byteBuffer).toString();
				
				if(state == GameServer.LEADY) {
					int num = Integer.parseInt(data.split(":")[0]);
					String auth = data.split(":")[1];
					switch (num) {
					case 1:{		//현재 참가자 리스트
						String tempData = "--------참여 리스트---------";
						for(User user : connections) {
							InetSocketAddress ia = (InetSocketAddress)user.socketChannel.getRemoteAddress();
							tempData += ia.getHostName()+"\n";
						}
						tempData += "--------------------------";
						this.sendData = tempData;
						selectionKey.interestOps(SelectionKey.OP_WRITE);
						selector.wakeup();
						break;
					}
					case 2:{		//나가기
						if(auth.equals("user")) {
							InetSocketAddress ia = (InetSocketAddress) this.socketChannel.getRemoteAddress();
							String id = ia.getHostName();
							this.socketChannel.close();
							connections.remove(this);
							for(User user: connections) {
								user.sendData = id+"님이 방을 나갔습니다.\n";
								SelectionKey userKey = user.socketChannel.keyFor(selector);
								userKey.interestOps(SelectionKey.OP_WRITE);
								selector.wakeup();
							}
						}
						else if(auth.equals("master")) {
							for(User user : connections) {
								user.sendData = "방장이 방을 나가 서버가 닫혔습니다.\n";
								SelectionKey userKey = user.socketChannel.keyFor(selector);
								userKey.interestOps(SelectionKey.OP_WRITE);
								selector.wakeup();
							}
							closeSignal = true;
						}
						break;
					}
					case 3:
						
						break;
					}
					
				}
				else if(state == GameServer.RUNNING) {
					
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

}
