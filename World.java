package land;

import java.util.ArrayList;

public class World {
  public static final int chunkWidth = 16;
  public static final int chunkHeight = 256;
  public static final int chunkWidthSquared = chunkWidth * chunkWidth;
  public static final int chunkSize = chunkWidthSquared * chunkHeight;
  public static final int chunkBuffSize = chunkSize;
  public static final int chunkBuffSizeBytes = chunkBuffSize * 4 * Float.BYTES;
  
  Game game;
  
  public boolean ready = true;
  
  public Player player;
  public Camera camera;
  
  ArrayList<Chunk> chunks = new ArrayList<Chunk>();
  
  public World(Game game) {
    this.game = game;
    
    player = new Player(this, chunkWidth / 2f, 128f, chunkWidth / 2f);
    camera = new Camera(player);
  }
  
  public void start() {
    int chunksNum = 1024;
    
    for(int i = 0; i < chunksNum; i++) {
      Chunk chunk = new Chunk(this, i % 32 - 16, i / 32 - 16);
      chunks.add(chunk);
    }
  }
  
  public Chunk getChunk(int x, int z) {
    x = (int)Math.floor((double)x / chunkWidth);
    z = (int)Math.floor((double)z / chunkWidth);
    
    for(int i = 0; i < chunks.size(); i++) {
      Chunk chunk = chunks.get(i);
      if(chunk.x == x && chunk.z == z) return chunk;
    }
    
    return null;
  }
  
  public short getBlock(int x, int y, int z) {
    Chunk chunk = getChunk(x, z);
    if(chunk == null || !chunk.ready) return -1;
    
    x %= chunkWidth;
    z %= chunkWidth;
    
    if(x < 0) x += chunkWidth;
    if(z < 0) z += chunkWidth;
    
    int i = y * chunkWidthSquared + z * chunkWidth + x;
    
    return chunk.blocks[i];
  }
  
  public void dispose() {
    for(int i = 0; i < chunks.size(); i++) {
      Chunk chunk = chunks.get(i);
      chunk.dispose();
    }
  }
}