package land;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

public class Chunk {
  public World world;
  public Camera camera;
  
  public int x, z;
  public int x1, x2, z1, z2;
  public boolean ready;
  public boolean hasBuffs;
  public boolean hasFaces;
  public boolean missingX = true;
  public boolean missingZ = true;
  
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
    
    x1 = x * World.chunkWidth;
    x2 = x1 + World.chunkWidth;
    
    z1 = z * World.chunkWidth;
    z2 = z1 + World.chunkWidth;
  }
  
  public void generate() {
    ready = false;
    missingX = true;
    missingZ = true;
    
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
    
    if((missingX && world.getBlock(x1 - 1, 0, z1) != -1) || (missingZ && world.getBlock(x1, 0, z1 - 1) != -1)) {
      updateAllFaces();
    }
    
    updateBuffs();
    
    glDrawArrays(GL_QUADS, 0, buffLen);
  }
  
  public void updateAllFaces() {
    buffLen = 0;
    
    missingX = world.getBlock(x1 - 1, 0, z1) == -1;
    missingZ = world.getBlock(x1, 0, z1 - 1) == -1;
    
    for(int y = 0, i = 0; y < World.chunkHeight; y++) {
      for(int z = z1; z < z2; z++) {
        for(int x = x1; x < x2; x++, i++) {
          short block = blocks[i];
          
          short adjY = y != 0 ? blocks[i - World.chunkWidthSquared] : -1;
          short adjZ = z != z1 ? blocks[i - World.chunkWidth] : world.getBlock(x, y, z - 1);
          short adjX = x != x1 ? blocks[i - 1] : world.getBlock(x - 1, y, z);
          
          float red, green, blue;
          
          if(adjY != -1 && ((block != 0 && adjY == 0) || (block == 0 && adjY != 0))) {
            vFaces[buffLen] = x; vFaces[buffLen + 1] = y; vFaces[buffLen + 2] = z;
            vFaces[buffLen + 3] = x + 1; vFaces[buffLen + 4] = y; vFaces[buffLen + 5] = z;
            vFaces[buffLen + 6] = x + 1; vFaces[buffLen + 7] = y; vFaces[buffLen + 8] = z + 1;
            vFaces[buffLen + 9] = x; vFaces[buffLen + 10] = y; vFaces[buffLen + 11] = z + 1;
            
            red = (float)(Math.random() * .1);
            green = (float)(Math.random() * .5 + .5);
            blue = (float)(Math.random() * .1);
            
            cFaces[buffLen] = red; cFaces[buffLen + 1] = green; cFaces[buffLen + 2] = blue;
            cFaces[buffLen + 3] = red; cFaces[buffLen + 4] = green; cFaces[buffLen + 5] = blue;
            cFaces[buffLen + 6] = red; cFaces[buffLen + 7] = green; cFaces[buffLen + 8] = blue;
            cFaces[buffLen + 9] = red; cFaces[buffLen + 10] = green; cFaces[buffLen + 11] = blue;
            
            buffLen += 12;
          }
          
          if(adjZ != -1 && ((block != 0 && adjZ == 0) || (block == 0 && adjZ != 0))) {
              vFaces[buffLen] = x; vFaces[buffLen + 1] = y; vFaces[buffLen + 2] = z;
              vFaces[buffLen + 3] = x + 1; vFaces[buffLen + 4] = y; vFaces[buffLen + 5] = z;
              vFaces[buffLen + 6] = x + 1; vFaces[buffLen + 7] = y + 1; vFaces[buffLen + 8] = z;
              vFaces[buffLen + 9] = x; vFaces[buffLen + 10] = y + 1; vFaces[buffLen + 11] = z;
              
              red = (float)(Math.random() * .1 + .2);
              green = (float)(Math.random() * .02);
              blue = (float)(Math.random() * .02);
              
              cFaces[buffLen] = red; cFaces[buffLen + 1] = green; cFaces[buffLen + 2] = blue;
              cFaces[buffLen + 3] = red; cFaces[buffLen + 4] = green; cFaces[buffLen + 5] = blue;
              cFaces[buffLen + 6] = red; cFaces[buffLen + 7] = green; cFaces[buffLen + 8] = blue;
              cFaces[buffLen + 9] = red; cFaces[buffLen + 10] = green; cFaces[buffLen + 11] = blue;
              
              buffLen += 12;
          }
          
          if(adjX != -1 && ((block != 0 && adjX == 0) || (block == 0 && adjX != 0))) {
              vFaces[buffLen] = x; vFaces[buffLen + 1] = y; vFaces[buffLen + 2] = z;
              vFaces[buffLen + 3] = x; vFaces[buffLen + 4] = y + 1; vFaces[buffLen + 5] = z;
              vFaces[buffLen + 6] = x; vFaces[buffLen + 7] = y + 1; vFaces[buffLen + 8] = z + 1;
              vFaces[buffLen + 9] = x; vFaces[buffLen + 10] = y; vFaces[buffLen + 11] = z + 1;
              
              red = (float)(Math.random() * .1 + .2);
              green = (float)(Math.random() * .02);
              blue = (float)(Math.random() * .02);
              
              cFaces[buffLen] = red; cFaces[buffLen + 1] = green; cFaces[buffLen + 2] = blue;
              cFaces[buffLen + 3] = red; cFaces[buffLen + 4] = green; cFaces[buffLen + 5] = blue;
              cFaces[buffLen + 6] = red; cFaces[buffLen + 7] = green; cFaces[buffLen + 8] = blue;
              cFaces[buffLen + 9] = red; cFaces[buffLen + 10] = green; cFaces[buffLen + 11] = blue;
              
              buffLen += 12;
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