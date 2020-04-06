package ymin.game.client;

public class GameMasterClient extends GameClient {
	
	@Override
	protected void readyPrint() {
		System.out.println("---------------------");
		System.out.println("1.참가자 조회");
		System.out.println("2.나가기");
		System.out.println("3.게임 시작");
		System.out.println("---------------------");
		int check = sc.nextInt();
		sc.nextLine();
		switch (check) {
		case 1:
			send("1:master");
			break;
		case 2:
			send("2:master");
			return;
		case 3:
			send("3:master");
			break;
		}
	}

}
