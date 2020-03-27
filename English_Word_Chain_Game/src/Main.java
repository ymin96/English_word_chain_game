import java.util.Scanner;

import ymin.lobby.client.Client;
import ymin.lobby.server.Server;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("1.서버 생성");
		System.out.println("2.클라이언트로 접속");
		Scanner sc = new Scanner(System.in);
		int check= sc.nextInt();
		
		switch (check) {
		case 1:
			Server lobbyServer = new Server();
			lobbyServer.startServer();
			break;
		case 2:
			Client lobbyClient = new Client();
			lobbyClient.startClient();
			break;
		}
	}

}
