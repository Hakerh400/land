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
    world.ready = false;
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
    if(!hasBuffs)
      initBuffs();
    
    if(!ready) {
      if(world.ready)
        generate();
      return;
    }
    
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
          updateBlock(x, y, z, i);
        }
      }
    }
    
    facesLen = buffLen / 3;
    
    buffUpdates.clear();
    buffUpdates.add(0);
    buffUpdates.add(buffLen);
  }
  
  public void updateBlock(int x, int y, int z, int i) {
    short block = blocks[i];
    
    short adjY = y != 0 ? blocks[i - World.chunkWidthSquared] : -1;
    short adjZ = z != z1 ? blocks[i - World.chunkWidth] : world.getBlock(x, y, z - 1);
    short adjX = x != x1 ? blocks[i - 1] : world.getBlock(x - 1, y, z);
    
    int textureIndex;
    float tx1, ty1, tx2, ty2;
    
    if(adjY != -1 && ((block != 0 && adjY == 0) || (block == 0 && adjY != 0))) {
      textureIndex = (block | adjY) << 2;
      
      tx1 = TexturesData.blocks[textureIndex + 2]; ty1 = TexturesData.blocks[textureIndex + 3];
      tx2 = tx1 + TexturesData.increment; ty2 = ty1 + TexturesData.increment;
      
      vFaces[buffLen] = x; vFaces[buffLen + 1] = y; vFaces[buffLen + 2] = z;
      vFaces[buffLen + 3] = x + 1; vFaces[buffLen + 4] = y; vFaces[buffLen + 5] = z;
      vFaces[buffLen + 6] = x + 1; vFaces[buffLen + 7] = y; vFaces[buffLen + 8] = z + 1;
      vFaces[buffLen + 9] = x; vFaces[buffLen + 10] = y; vFaces[buffLen + 11] = z + 1;
      
      cFaces[buffLen] = tx1; cFaces[buffLen + 1] = ty1; cFaces[buffLen + 2] = 0;
      cFaces[buffLen + 3] = tx2; cFaces[buffLen + 4] = ty1; cFaces[buffLen + 5] = 0;
      cFaces[buffLen + 6] = tx2; cFaces[buffLen + 7] = ty2; cFaces[buffLen + 8] = 0;
      cFaces[buffLen + 9] = tx1; cFaces[buffLen + 10] = ty2; cFaces[buffLen + 11] = 0;
      
      buffLen += 12;
    }
    
    if(adjZ != -1 && ((block != 0 && adjZ == 0) || (block == 0 && adjZ != 0))) {
      textureIndex = (block | adjZ) << 2;
      
      tx1 = TexturesData.blocks[textureIndex]; ty1 = TexturesData.blocks[textureIndex + 1];
      tx2 = tx1 + TexturesData.increment; ty2 = ty1 + TexturesData.increment;
      
      vFaces[buffLen] = x + 1; vFaces[buffLen + 1] = y + 1; vFaces[buffLen + 2] = z;
      vFaces[buffLen + 3] = x; vFaces[buffLen + 4] = y + 1; vFaces[buffLen + 5] = z;
      vFaces[buffLen + 6] = x; vFaces[buffLen + 7] = y; vFaces[buffLen + 8] = z;
      vFaces[buffLen + 9] = x + 1; vFaces[buffLen + 10] = y; vFaces[buffLen + 11] = z;
      
      cFaces[buffLen] = tx1; cFaces[buffLen + 1] = ty1; cFaces[buffLen + 2] = 0;
      cFaces[buffLen + 3] = tx2; cFaces[buffLen + 4] = ty1; cFaces[buffLen + 5] = 0;
      cFaces[buffLen + 6] = tx2; cFaces[buffLen + 7] = ty2; cFaces[buffLen + 8] = 0;
      cFaces[buffLen + 9] = tx1; cFaces[buffLen + 10] = ty2; cFaces[buffLen + 11] = 0;
      
      buffLen += 12;
    }
    
    if(adjX != -1 && ((block != 0 && adjX == 0) || (block == 0 && adjX != 0))) {
      textureIndex = (block | adjX) << 2;
      
      tx1 = TexturesData.blocks[textureIndex]; ty1 = TexturesData.blocks[textureIndex + 1];
      tx2 = tx1 + TexturesData.increment; ty2 = ty1 + TexturesData.increment;
      
      vFaces[buffLen] = x; vFaces[buffLen + 1] = y + 1; vFaces[buffLen + 2] = z + 1;
      vFaces[buffLen + 3] = x; vFaces[buffLen + 4] = y + 1; vFaces[buffLen + 5] = z;
      vFaces[buffLen + 6] = x; vFaces[buffLen + 7] = y; vFaces[buffLen + 8] = z;
      vFaces[buffLen + 9] = x; vFaces[buffLen + 10] = y; vFaces[buffLen + 11] = z + 1;
      
      cFaces[buffLen] = tx1; cFaces[buffLen + 1] = ty1; cFaces[buffLen + 2] = 0;
      cFaces[buffLen + 3] = tx2; cFaces[buffLen + 4] = ty1; cFaces[buffLen + 5] = 0;
      cFaces[buffLen + 6] = tx2; cFaces[buffLen + 7] = ty2; cFaces[buffLen + 8] = 0;
      cFaces[buffLen + 9] = tx1; cFaces[buffLen + 10] = ty2; cFaces[buffLen + 11] = 0;
      
      buffLen += 12;
    }
  }
  
  private void updateBuffs() {
    glBindBuffer(GL_ARRAY_BUFFER, vBuff);
    if(buffUpdates.size() != 0) glBufferSubData(GL_ARRAY_BUFFER, 0, vFaces);
    glVertexAttribPointer(camera.attribPos, 3, GL_FLOAT, false, 0, 0);
  
    glBindBuffer(GL_ARRAY_BUFFER, cBuff);
    if(buffUpdates.size() != 0) glBufferSubData(GL_ARRAY_BUFFER, 0, cFaces);
    glVertexAttribPointer(camera.attribText, 3, GL_FLOAT, false, 0, 0);
    
    buffUpdates.clear();
  }
  
  public void dispose() {
    glDeleteBuffers(vBuff);
    glDeleteBuffers(cBuff);
  }
}