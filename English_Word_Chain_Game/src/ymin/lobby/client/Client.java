package ymin.lobby.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class Client {
	SocketChannel socketChannel;

	public void startClient() {
		try {
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(true);
			socketChannel.connect(new InetSocketAddress("localhost", 6001));
		} catch (Exception e) {
			// TODO: handle exception
			if (socketChannel.isOpen())
				stopClient();
			return;
		}
		print();
	}

	public void stopClient() {
		try {
			if (socketChannel != null && socketChannel.isOpen())
				socketChannel.close();
		} catch (IOException e) {
			// TODO: handle exception
		}
	}

	void receive() {
		try {
			ByteBuffer byteBuffer = ByteBuffer.allocate(100);

			// 서버가 비정상적으로 종료했을 경우 IOException 발생
			int readByteCount = socketChannel.read(byteBuffer);

			// 서버가 정상적으로 Socket의 close()를 호출했을 경우
			if (readByteCount == -1) {
				throw new IOException();
			}

			byteBuffer.flip();
			Charset charset = Charset.forName("UTF-8");
			String data = charset.decode(byteBuffer).toString();

			System.out.println(data);
		} catch (Exception e) {
			stopClient();
		}
	}

	void send(String data) {
		try {
			Charset charset = Charset.forName("UTF-8");
			ByteBuffer byteBuffer = charset.encode(data);
			socketChannel.write(byteBuffer);
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("서버 통신 안됨" + e);
			stopClient();
		}
	}

	void print() {
		while (true) {
			System.out.println("1.리스트 갱신");
			System.out.println("2.방 생성");
			System.out.println("3.접속 요청");
			Scanner sc = new Scanner(System.in);
			int num = sc.nextInt();
			switch (num) {
			case 1:
				send(Integer.toString(num));
				receive();
				break;
			case 2:
				try {
					InetAddress ia = InetAddress.getLocalHost();
					send(num + ":" + ia.getHostName());
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;
			case 3:
				break;
			}
		}
	}
}
