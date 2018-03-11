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
    
    if(chunk.x != 0 || chunk.z != 0)
    for(int y = 0, i = 0; y < World.chunkHeight; y++) {
      for(int z = z1; z < z2; z++) {
        for(int x = x1; x < x2; x++, i++) {
          short id = 0;
          
          double xx = Math.sin((z + 7) / 20.) * 5. +
              Math.cos((z + x) / 24.7) * 5. * 14.7 / 20. +
              Math.sin(x / 7.3) * 5. * 7.3 / 20.;
          
          double zz = Math.cos((x - z * 1.2 + 3) / 43.) * 5. +
              Math.cos(-x / 24.2) * 5. * 14.7 / 20. +
              Math.cos(z / 8.1) * 5. * 7.4 / 20.;
          
          int k = (int)Math.round(100. + xx + zz);
          
          if(y > k) id = 0;
          else if(y == k) id = 1;
          else if(y > k - 10) id = 2;
          else if(y > 5) id = 3;
          else id = (short)(Math.random() > 1. / (y + 1) ? 3 : 4);
          
          blocks[i] = id;
        }
      }
    }
    
    chunk.updateAllFaces();
    
    chunk.ready = true;
    chunk.world.ready = true;
  }
}