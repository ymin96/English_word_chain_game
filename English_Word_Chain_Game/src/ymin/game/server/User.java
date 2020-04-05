package ymin.game.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.List;

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
	public void send(SelectionKey selectionKey, Selector selector, List<User> connections) {
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
	
	public void receive(SelectionKey selectionKey, Selector selector, List<User> connections, int mode) {
		try {
			ByteBuffer byteBuffer = ByteBuffer.allocate(100);
			
			int byteCount = socketChannel.read(byteBuffer);
			
			if(byteCount == -1)
				throw new IOException();
			
			byteBuffer.flip();
			Charset charset = Charset.forName("UTF-8");
			String data = charset.decode(byteBuffer).toString();
			int num = Integer.parseInt(data.split(":")[0]);
			String auth = data.split(":")[1];
			if(mode == GameServer.LEADY) {
				
			}
			else if(mode == GameServer.RUNNING) {
				
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
