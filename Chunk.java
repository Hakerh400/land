package land;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

public class Chunk {
  public World world;
  public Camera camera;
  
  public int x, z;
  public boolean ready;
  public boolean hasBuffs;
  public boolean hasFaces;
  
  public short[] blocks = new short[World.chunkSize];
  
  public float[] vFaces = new float[World.chunkBuffSize];
  public float[] cFaces = new float[World.chunkBuffSize];
  
  public int facesLen;
  public int buffLen;
  public ArrayList<Integer> buffUpdates = new ArrayList<Integer>();
  
  private int vBuff;
  private int cBuff;
  
  public Chunk(World world, int x, int z) {
    this.world = world;
    this.camera = world.camera;
    
    this.x = x;
    this.z = z;
  }
  
  public void generate() {
    this.ready = false;
    
    ChunkGenerator generator = new ChunkGenerator(this);
    generator.start();
  }
  
  public void initBuffs() {
    vBuff = glGenBuffers();
    glBindBuffer(GL_ARRAY_BUFFER, vBuff);
    glBufferData(GL_ARRAY_BUFFER, World.chunkBuffSizeBytes, GL_DYNAMIC_DRAW);
    
    cBuff = glGenBuffers();
    glBindBuffer(GL_ARRAY_BUFFER, cBuff);
    glBufferData(GL_ARRAY_BUFFER, World.chunkBuffSizeBytes, GL_DYNAMIC_DRAW);
    
    hasBuffs = true;
  }
  
  public void draw() {
    if(!ready) return;
    
    updateBuffs();
    
    glDrawArrays(GL_TRIANGLES, 0, buffLen);
  }
  
  public void updateAllFaces() {
    buffLen = 0;
    
    int z1 = this.z * World.chunkWidth;
    int z2 = z1 + World.chunkWidth;
    
    int x1 = this.x * World.chunkWidth;
    int x2 = x1 + World.chunkWidth;
    
    for(int y = 0, i = 0; y < World.chunkHeight; y++) {
      for(int z = z1; z < z2; z++) {
        for(int x = x1; x < x2; x++, i++) {
          int iy = i - World.chunkWidthSquared;
          int iz = i - World.chunkWidth;
          int ix = i - 1;
          
          if((y == 0 && blocks[i] != 0) ||
             (y != 0 && blocks[i] == 0 && blocks[iy] != 0) ||
             (y != 0 && blocks[i] != 0 && blocks[iy] == 0)) {
            vFaces[buffLen] = x; vFaces[buffLen + 1] = y; vFaces[buffLen + 2] = z;
            vFaces[buffLen + 3] = x + 1; vFaces[buffLen + 4] = y; vFaces[buffLen + 5] = z;
            vFaces[buffLen + 6] = x; vFaces[buffLen + 7] = y; vFaces[buffLen + 8] = z + 1;
            
            vFaces[buffLen + 9] = x + 1; vFaces[buffLen + 10] = y; vFaces[buffLen + 11] = z + 1;
            vFaces[buffLen + 12] = x + 1; vFaces[buffLen + 13] = y; vFaces[buffLen + 14] = z;
            vFaces[buffLen + 15] = x; vFaces[buffLen + 16] = y; vFaces[buffLen + 17] = z + 1;
            
            cFaces[buffLen] = 1; cFaces[buffLen + 1] = 1; cFaces[buffLen + 2] = 0;
            cFaces[buffLen + 3] = 1; cFaces[buffLen + 4] = 1; cFaces[buffLen + 5] = 0;
            cFaces[buffLen + 6] = 1; cFaces[buffLen + 7] = 1; cFaces[buffLen + 8] = 0;

            cFaces[buffLen + 9] = 1; cFaces[buffLen + 10] = 1; cFaces[buffLen + 11] = 0;
            cFaces[buffLen + 12] = 1; cFaces[buffLen + 13] = 1; cFaces[buffLen + 14] = 0;
            cFaces[buffLen + 15] = 1; cFaces[buffLen + 16] = 1; cFaces[buffLen + 17] = 0;
            
            buffLen += 18;
          }
          
          if((z == z1 && blocks[i] != 0) ||
             (z != z1 && blocks[i] == 0 && blocks[iz] != 0) ||
             (z != z1 && blocks[i] != 0 && blocks[iz] == 0)) {
              vFaces[buffLen] = x; vFaces[buffLen + 1] = y; vFaces[buffLen + 2] = z;
              vFaces[buffLen + 3] = x + 1; vFaces[buffLen + 4] = y; vFaces[buffLen + 5] = z;
              vFaces[buffLen + 6] = x; vFaces[buffLen + 7] = y + 1; vFaces[buffLen + 8] = z;
              
              vFaces[buffLen + 9] = x + 1; vFaces[buffLen + 10] = y + 1; vFaces[buffLen + 11] = z;
              vFaces[buffLen + 12] = x + 1; vFaces[buffLen + 13] = y; vFaces[buffLen + 14] = z;
              vFaces[buffLen + 15] = x; vFaces[buffLen + 16] = y + 1; vFaces[buffLen + 17] = z;
              
              cFaces[buffLen] = 0; cFaces[buffLen + 1] = 1; cFaces[buffLen + 2] = 0;
              cFaces[buffLen + 3] = 0; cFaces[buffLen + 4] = 1; cFaces[buffLen + 5] = 0;
              cFaces[buffLen + 6] = 0; cFaces[buffLen + 7] = 1; cFaces[buffLen + 8] = 0;

              cFaces[buffLen + 9] = 0; cFaces[buffLen + 10] = 1; cFaces[buffLen + 11] = 0;
              cFaces[buffLen + 12] = 0; cFaces[buffLen + 13] = 1; cFaces[buffLen + 14] = 0;
              cFaces[buffLen + 15] = 0; cFaces[buffLen + 16] = 1; cFaces[buffLen + 17] = 0;
              
              buffLen += 18;
          }
          
          if((x == x1 && blocks[i] != 0) ||
             (x != x1 && blocks[i] == 0 && blocks[ix] != 0) ||
             (x != x1 && blocks[i] != 0 && blocks[ix] == 0)) {
              vFaces[buffLen] = x; vFaces[buffLen + 1] = y; vFaces[buffLen + 2] = z;
              vFaces[buffLen + 3] = x; vFaces[buffLen + 4] = y + 1; vFaces[buffLen + 5] = z;
              vFaces[buffLen + 6] = x; vFaces[buffLen + 7] = y; vFaces[buffLen + 8] = z + 1;
              
              vFaces[buffLen + 9] = x; vFaces[buffLen + 10] = y + 1; vFaces[buffLen + 11] = z + 1;
              vFaces[buffLen + 12] = x; vFaces[buffLen + 13] = y + 1; vFaces[buffLen + 14] = z;
              vFaces[buffLen + 15] = x; vFaces[buffLen + 16] = y; vFaces[buffLen + 17] = z + 1;
              
              cFaces[buffLen] = 0; cFaces[buffLen + 1] = 1; cFaces[buffLen + 2] = 1;
              cFaces[buffLen + 3] = 0; cFaces[buffLen + 4] = 1; cFaces[buffLen + 5] = 1;
              cFaces[buffLen + 6] = 0; cFaces[buffLen + 7] = 1; cFaces[buffLen + 8] = 1;

              cFaces[buffLen + 9] = 0; cFaces[buffLen + 10] = 1; cFaces[buffLen + 11] = 1;
              cFaces[buffLen + 12] = 0; cFaces[buffLen + 13] = 1; cFaces[buffLen + 14] = 1;
              cFaces[buffLen + 15] = 0; cFaces[buffLen + 16] = 1; cFaces[buffLen + 17] = 1;
              
              buffLen += 18;
          }
        }
      }
    }
    
    facesLen = buffLen / 3;
    
    buffUpdates.clear();
    buffUpdates.add(0);
    buffUpdates.add(buffLen);
  }
  
  private void updateBuffs() {
    glBindBuffer(GL_ARRAY_BUFFER, vBuff);
    if(buffUpdates.size() != 0) glBufferSubData(GL_ARRAY_BUFFER, 0, vFaces);
    glVertexAttribPointer(camera.attribPos, 3, GL_FLOAT, false, 0, 0);
  
    glBindBuffer(GL_ARRAY_BUFFER, cBuff);
    if(buffUpdates.size() != 0) glBufferSubData(GL_ARRAY_BUFFER, 0, cFaces);
    glVertexAttribPointer(camera.attribCol, 3, GL_FLOAT, false, 0, 0);
    
    buffUpdates.clear();
  }
  
  public void dispose() {
    glDeleteBuffers(vBuff);
    glDeleteBuffers(cBuff);
  }
}