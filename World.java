package land;

public class World {
	public static int chunkWidth = 16;
	public static int chunkHeight = 256;
	public static int chunkWidthSquared = chunkWidth * chunkWidth;
	public static int chunkSize = chunkWidthSquared * chunkHeight;
	public static int chunkBuffSize = chunkSize * 18;
	public static int chunkBuffSizeBytes = chunkBuffSize * 3 * Float.BYTES;
	
	Game game;
	
	public Player player;
	public Camera camera;
	
	public Chunk[] chunks;
	
	public World(Game game) {
		this.game = game;
		
		player = new Player(this, chunkWidth / 2f, 128f, chunkWidth / 2f);
		camera = new Camera(player);
	}
	
	public void start() {
		chunks = new Chunk[2];
		
		for(int i = 0; i < chunks.length; i++) {
			chunks[i] = new Chunk(this, i, 0);
			chunks[i].generate();
		}
	}
	
	public void dispose() {
		for(int i = 0; i < chunks.length; i++) {
			chunks[i].dispose();
		}
	}
}