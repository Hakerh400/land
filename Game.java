package land;

public class Game {
  public static int sleepTime = (int)1e3;
  
  public World world;
  public Player player;
  public Camera camera;
  
  public Game() {
    world = new World(this);
    player = world.player;
    camera = world.camera;
  }
  
  public void start() {
    camera.start();
    
    try {
      while(!camera.ready) Thread.sleep(Game.sleepTime);
    }catch(InterruptedException e) {
      System.out.println(e.getMessage());
    }
    
    world.start();
  }
}