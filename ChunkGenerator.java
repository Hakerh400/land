package land;

public class ChunkGenerator extends Thread {
  public World world;
  public Chunk chunk;
  
  public ChunkGenerator(Chunk chunk) {
    this.world = chunk.world;
    this.chunk = chunk;
  }
  
  public void run() {
    short[] blocks = chunk.blocks;
    
    for(int y = 0, i = 0; y < World.chunkHeight; y++) {
      for(int z = 0; z < World.chunkWidth; z++) {
        for(int x = 0; x < World.chunkWidth; x++, i++) {
          if(y >= 100) {
            blocks[i] = 0;
            continue;
          }
          
          blocks[i] = (short)(Math.random() > (y / 100f) ? 1 : 0);
        }
      }
    }
    
    chunk.updateAllFaces();
    
    chunk.ready = true;
  }
}