package ymin.lobby.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

import ymin.game.client.GameClient;
import ymin.game.client.GameMasterClient;
import ymin.game.server.GameServer;

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
	}

	public void stopClient() {
		try {
			if (socketChannel != null && socketChannel.isOpen())
				socketChannel.close();
		} catch (IOException e) {
			// TODO: handle exception
		}
	}

	String receive() {
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

			return data;
		} catch (Exception e) {
			stopClient();
			return "error";
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

	public void run() {
		while (true) {
			System.out.println("---------------------");
			System.out.println("1.리스트 갱신");
			System.out.println("2.방 생성");
			System.out.println("3.접속 요청");
			System.out.println("---------------------");
			System.out.print("번호 입력:");
			Scanner sc = new Scanner(System.in);
			int check = sc.nextInt();
			sc.nextLine();
			String resultReceive;
			switch (check) {
			case 1:
				send(Integer.toString(check));
				resultReceive = receive();
				System.out.println("<---------------------->");
				System.out.println(resultReceive);
				System.out.println("<---------------------->");
				break;
			case 2:
				try {
					System.out.println("방 제목 입력:");
					String title = sc.nextLine();
					InetAddress ia = InetAddress.getLocalHost();
					send(check + ":" + ia.getHostName() + ":" + title);

					Thread gServerThread = new Thread(() -> {
						GameServer gameServer = new GameServer();
						gameServer.startServer();
					});
					gServerThread.start();
					try {
						Thread.sleep(100);
						GameMasterClient masterClient = new GameMasterClient();
						masterClient.startClient("localhost");
						masterClient.run();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case 3:
				try {
					System.out.println("들어갈 방의 번호 입력:");
					int roomNum = sc.nextInt();
					send(check + ":" + roomNum);
					resultReceive = receive();
					String result = resultReceive.split(":")[0];
					String content = resultReceive.split(":")[1];
					if (result.equals("true")) {
						GameClient gameClient = new GameClient();
						gameClient.startClient(content);
						gameClient.run();
					} else if (result.equals("false")) {
						System.out.println(content);
					}
				} catch (Exception e) {

				}
				break;
			}
		}
	}
}
