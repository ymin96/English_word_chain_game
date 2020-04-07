package ymin.game.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

import ymin.game.server.GameServer;

public class GameClient {
	SocketChannel socketChannel;
	int state = GameServer.LEADY;
	boolean exit = true;
	Scanner sc = new Scanner(System.in);
	
	public void startClient(String host) {
		try {
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(true);
			socketChannel.connect(new InetSocketAddress(host, 12000));
			receive();
		} catch (Exception e) {
			// TODO: handle exception
			if (socketChannel.isOpen())
				stopClient();
			return;
		}
	}

	public void stopClient() {
		try {
			if (socketChannel != null && socketChannel.isOpen())
				socketChannel.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	void receive() {
		Thread thread = new Thread(() -> {
			while (true) {
				try {
					ByteBuffer byteBuffer = ByteBuffer.allocate(100);

					// 서버가 비정상적으로 종료했을 경우 IOException 발생
					int readByteCount = socketChannel.read(byteBuffer);

					// 서버가 정상적으로 Socket의 close()를 호출했을 경우
					if (readByteCount == -1) {
						System.out.println("방장이 나가 서버가 닫혔습니다.\n숫자키를 입력하면 로비로 이동합니다.\n");
						exit = false;
						break;
					}

					byteBuffer.flip();
					Charset charset = Charset.forName("UTF-8");
					String data = charset.decode(byteBuffer).toString();
					
					System.out.println(data);
				} catch (Exception e) {
					stopClient();
					break;
				}
			}
		});
		thread.start();

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

	public void run() {
		System.out.println("**게임 서버 입장**");
		while (exit) {
			if(state == GameServer.LEADY)
				readyPrint();
			else if(state == GameServer.RUNNING)
				runningPrint();
		}
	}
	
	protected void readyPrint() {
		System.out.println("---------------------");
		System.out.println("1.참가자 조회");
		System.out.println("2.나가기");
		System.out.println("---------------------");
		int check = sc.nextInt();
		sc.nextLine();
		switch (check) {
		case 1:
			send("1:user");
			break;
		case 2:
			send("2:user");
			stopClient();
			exit = false;
			return;
		}
	}
	
	protected void runningPrint() {
		String sendData = sc.nextLine();
		send(sendData);
	}
}
