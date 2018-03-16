package land;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

public class Matrix {
  public static FloatBuffer identity() {
    FloatBuffer buff = BufferUtils.createFloatBuffer(16);
    
    buff.put(1).put(0).put(0).put(0)
        .put(0).put(1).put(0).put(0)
        .put(0).put(0).put(1).put(0)
        .put(0).put(0).put(0).put(1);
    
    buff.rewind();
    
    return buff;
  }
}