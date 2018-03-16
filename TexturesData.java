package land;

public class TexturesData {
  public static final int tileSize = 16;
  public static final int width = 22;
  public static final float increment = 1f / width;
  
  public static float[] blocks;
  
  static {
    blocks = new float[] {
      0, 0, 0, 0,   // 00 - Air
      3, 10, 6, 10, // 01 - Grass
      15, 1, 15, 1, // 02 - Dirt
      6, 18, 6, 18, // 03 - Stone
      5, 0, 5, 0,   // 04 - Bedrock
    };
    
    for(int i = 0; i < blocks.length; i++) {
      blocks[i] *= increment;
    }
  }
}