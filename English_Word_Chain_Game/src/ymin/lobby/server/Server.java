package ymin.lobby.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class Server {
	ServerSocketChannel serverSocketChannel;
	public Selector selector;
	List<User> connections = new Vector<User>();
	Map<Integer, Host> hostMap = new HashMap<Integer, Host>();
	int hostNumber = 1;
	
	public void startServer() {
		try {
			selector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.bind(new InetSocketAddress(6001));
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("로비 서버 생성 성공");
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
							User user = (User)key.attachment();
							user.receive(key);
						}
						else if(key.isWritable()) {
							User user = (User)key.attachment();
							user.send(key);
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
	
	//클라이언트 연결 수락
	private void accept(SelectionKey key) {
		try {
			ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
			SocketChannel socketChannel = serverSocketChannel.accept();
			
			InetSocketAddress ia = (InetSocketAddress) socketChannel.getRemoteAddress();
			System.out.println(ia.getHostName()+" 클라이언트 연결 수락");
			
			User user = new User(socketChannel);
			connections.add(user);
			
		} catch(Exception e) {
			
		}
	}

	class User {
		protected SocketChannel socketChannel;
		public String sendData;
		
		User(SocketChannel socketChannel) throws IOException{
			this.socketChannel = socketChannel;
			socketChannel.configureBlocking(false);
			SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
			selectionKey.attach(this);
		}
		
		//연결된 클라이언트에게서 데이터 수신
		void receive(SelectionKey selectionKey) {
 			try {
				ByteBuffer byteBuffer = ByteBuffer.allocate(100);
				
				//데이터 받기
				int byteCount = socketChannel.read(byteBuffer);
				
				//상대방이 SokcetChannel의 close() 메소드를 호출할 경우
				if(byteCount == -1)
					throw new IOException();
				
				//받은 데이터 디코드 
				byteBuffer.flip();
				Charset charset = Charset.forName("UTF-8");
				String data = charset.decode(byteBuffer).toString();
				System.out.println("받은 데이터: "+ data);
				String tempData ="";
				switch (Integer.parseInt(data.split(":")[0])) {
				case 1:		//생성된 방 조회
					//생성된 방의 문자열 리스트를 만들어 준다.
					Set<Integer> hostKeySet = hostMap.keySet();
					Iterator<Integer> keyIterator = hostKeySet.iterator();
					while(keyIterator.hasNext()) {
						Integer hostKey = keyIterator.next();
						Host host = hostMap.get(hostKey);
						tempData += Integer.toString(hostKey)+" "+host.getTitle()+ "\n";
					}
					
					System.out.println("보낼 데이터: "+ tempData);
					this.sendData = tempData;
					selectionKey.interestOps(SelectionKey.OP_WRITE);
					selector.wakeup();
					break;
				case 2:		//방 생성 요청
					try {
						String ipAddress = data.split(":")[1];
						String title = data.split(":")[2];
						hostMap.put(hostNumber, new Host(ipAddress, title));
						System.out.println(hostNumber+"번 게임 서버 생성 성공");
						hostNumber++;
					} catch (Exception e) {
						// TODO: handle exception
						System.out.println("게임 서버 생성 실패");
					}
					break;
				case 3:	//방 참가 요청
					try {
						int roomNum = Integer.parseInt(data.split(":")[1]);
						//Map에 전달받은 키값이 있다면 해당 호스트의 ip를 넘겨준다.
						if(hostMap.containsKey(roomNum)) 
							tempData += "true:" + hostMap.get(roomNum).getIpAddress();
						//Map에 전달받은 키값이 없다면 false를 전달하고 오류 메시지를 보내준다.
						else
							tempData += "false:찾는 방이 존재하지 않습니다.\n리스트를 갱신해주세요.";
						this.sendData = tempData;
						selectionKey.interestOps(SelectionKey.OP_WRITE);
						selector.wakeup();
					}catch(Exception e) {
						e.printStackTrace();
					}
					break;
				case 4:		//방 삭제 요청
					try {
						int removeNum = Integer.parseInt(data.split(":")[1]);
						hostMap.remove(removeNum);
						System.out.println(removeNum + "번 게임 서버 삭제 성공");
					} catch (Exception e) {
						// TODO: handle exception
						System.out.println("게임 서버 삭제 실패");
					}
					break;
				}
			} catch (Exception e) {
				// TODO: handle exception
				try {
					connections.remove(this);
					socketChannel.close();
				} catch (IOException e2) {
					// TODO: handle exception
				}
			}
		}
		
		//연결된 클라이언트에게 데이터 송신
		void send(SelectionKey selectionKey) {
			try {
				Charset charset = Charset.forName("UTF-8");
				ByteBuffer byteBuffer = charset.encode(sendData);
				socketChannel.write(byteBuffer);	//데이터 전송
				selectionKey.interestOps(SelectionKey.OP_READ);		//작업 유형 변경
				selector.wakeup();		//변경된 작업 유형을 감지
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
		
		
	}
}
