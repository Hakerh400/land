package land;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

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
  
  public boolean needsBuffering;
  public int buffLen;
  private FloatBuffer vFaces = BufferUtils.createFloatBuffer(World.chunkBuffSize);
  private FloatBuffer cFaces = BufferUtils.createFloatBuffer(World.chunkBuffSize);
  
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
    vFaces.clear();
    cFaces.clear();
    
    missingX = world.getBlock(x1 - 1, 0, z1) == -1;
    missingZ = world.getBlock(x1, 0, z1 - 1) == -1;
    
    for(int y = 0, i = 0; y < World.chunkHeight; y++) {
      for(int z = z1; z < z2; z++) {
        for(int x = x1; x < x2; x++, i++) {
          updateBlockFaces(x, y, z, i);
        }
      }
    }
    
    vFaces.position(0);
    vFaces.limit(buffLen);
    
    cFaces.position(0);
    cFaces.limit(buffLen);
    
    buffLen /= 3;
    
    needsBuffering = true;
  }
  
  public void updateBlock(int x, int y, int z, int i) {
    updateAllFaces();
  }
  
  public void updateBlockFaces(int x, int y, int z, int i) {
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
      
      vFaces.put(x).put(y).put(z)
            .put(x + 1).put(y).put(z)
            .put(x + 1).put(y).put(z + 1)
            .put(x).put(y).put(z + 1);
      
      cFaces.put(tx1).put(ty1).put(0)
            .put(tx2).put(ty1).put(0)
            .put(tx2).put(ty2).put(0)
            .put(tx1).put(ty2).put(0);
      
      buffLen += 12;
    }
    
    if(adjZ != -1 && ((block != 0 && adjZ == 0) || (block == 0 && adjZ != 0))) {
      textureIndex = (block | adjZ) << 2;
      
      tx1 = TexturesData.blocks[textureIndex]; ty1 = TexturesData.blocks[textureIndex + 1];
      tx2 = tx1 + TexturesData.increment; ty2 = ty1 + TexturesData.increment;
      
      vFaces.put(x + 1).put(y + 1).put(z)
            .put(x).put(y + 1).put(z)
            .put(x).put(y).put(z)
            .put(x + 1).put(y).put(z);
      
      cFaces.put(tx1).put(ty1).put(0)
            .put(tx2).put(ty1).put(0)
            .put(tx2).put(ty2).put(0)
            .put(tx1).put(ty2).put(0);
      
      buffLen += 12;
    }
    
    if(adjX != -1 && ((block != 0 && adjX == 0) || (block == 0 && adjX != 0))) {
      textureIndex = (block | adjX) << 2;
      
      tx1 = TexturesData.blocks[textureIndex]; ty1 = TexturesData.blocks[textureIndex + 1];
      tx2 = tx1 + TexturesData.increment; ty2 = ty1 + TexturesData.increment;
      
      vFaces.put(x).put(y + 1).put(z + 1)
            .put(x).put(y + 1).put(z)
            .put(x).put(y).put(z)
            .put(x).put(y).put(z + 1);
      
      cFaces.put(tx1).put(ty1).put(0)
            .put(tx2).put(ty1).put(0)
            .put(tx2).put(ty2).put(0)
            .put(tx1).put(ty2).put(0);
      
      buffLen += 12;
    }
  }
  
  private void updateBuffs() {
    glBindBuffer(GL_ARRAY_BUFFER, vBuff);
    if(needsBuffering)
      glBufferSubData(GL_ARRAY_BUFFER, 0, vFaces);
    glVertexAttribPointer(camera.attribPos, 3, GL_FLOAT, false, 0, 0);
  
    glBindBuffer(GL_ARRAY_BUFFER, cBuff);
    if(needsBuffering) {
      glBufferSubData(GL_ARRAY_BUFFER, 0, cFaces);
      needsBuffering = false;
    }
    glVertexAttribPointer(camera.attribText, 3, GL_FLOAT, false, 0, 0);
  }
  
  public void dispose() {
    glDeleteBuffers(vBuff);
    glDeleteBuffers(cBuff);
  }
}