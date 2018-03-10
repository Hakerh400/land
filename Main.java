package land;

public class Main {
	public static void main(String[] args) {
		Game game = new Game();
		
		game.start();
		
		try {
			while(game.camera.enabled) Thread.sleep(Game.sleepTime);
		}catch(InterruptedException e) {
			System.out.println(e.getMessage());
		}
		
		System.exit(0);
	}
}