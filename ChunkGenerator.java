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
    
    int z1 = chunk.z * World.chunkWidth;
    int z2 = z1 + World.chunkWidth;
    
    int x1 = chunk.x * World.chunkWidth;
    int x2 = x1 + World.chunkWidth;
    
    for(int y = 0, i = 0; y < World.chunkHeight; y++) {
      for(int z = z1; z < z2; z++) {
        for(int x = x1; x < x2; x++, i++) {
          double xx = Math.sin(x / 17.) * 5 + Math.cos(x / 25.) * 4;
          double zz = Math.cos(z / 19.) * 8 + Math.sin(z / 23.) * 2.7;
          int k = (int)Math.round(100. + xx + zz);
          
          blocks[i] = (short)(y < k ? 1 : 0);
        }
      }
    }
    
    chunk.updateAllFaces();
    
    chunk.ready = true;
  }
}