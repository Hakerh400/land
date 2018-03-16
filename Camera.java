package land;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.stb.STBImage.*;

public class Camera extends Thread {
  private static float pi = (float)Math.PI;
  private static float pi2 = pi * 2;
  private static float pih = pi / 2;
  private static float cursorSpeed = 3;
  
  public World world;
  public Entity ent;
  public boolean enabled;
  public boolean ready;
  
  private int w = 1920;
  private int h = 1080;
  
  private long window;
  private int vShader;
  private int fShader;
  private int program;
  
  private int wh = w >> 1;
  private int hh = h >> 1;

  private float aspectRatio = w / h;
  private float fov = pi / 3;
  private float fovt = (float)Math.tan(fov / 2);
  private float near = 1e-3f;
  private float far = 1e3f;

  private float speed = .5f;
  private int dir = 0;
  
  public int attribPos;
  public int attribText;
  
  private int uniformProjection;
  private int uniformTranslation;
  private int uniformRotationX;
  private int uniformRotationY;
  private int uniformViewDist;
  private int uniformFogCol;
  
  private FloatBuffer translationMat = Matrix.identity();
  private FloatBuffer rotationXMat = Matrix.identity();
  private FloatBuffer rotationYMat = Matrix.identity();
  
  private FloatBuffer vTargetBlockFrame = BufferUtils.createFloatBuffer(3 * 2 * 12);
  private FloatBuffer cTargetBlockFrame = BufferUtils.createFloatBuffer(3 * 2 * 12);
  private int vBuffTargetBlockFrame;
  private int cBuffTargetBlockFrame;
  
  private float viewDist = far;
  private FloatBuffer fogCol;
  
  public Camera(Entity ent) {
    this.world = ent.world;
    this.ent = ent;
  }
  
  private void initGL() {
     GLFWErrorCallback.createPrint(System.err).set();

     if (!glfwInit())
       throw new IllegalStateException("Unable to initialize GLFW");

     glfwDefaultWindowHints();
     glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
     glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

     window = glfwCreateWindow(w, h, "Land", glfwGetPrimaryMonitor(), NULL);
     if ( window == NULL )
       throw new RuntimeException("Failed to create the GLFW window");
     
     glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);

     try ( MemoryStack stack = stackPush() ) {
       IntBuffer pWidth = stack.mallocInt(1);
       IntBuffer pHeight = stack.mallocInt(1);

       glfwGetWindowSize(window, pWidth, pHeight);

       GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

       glfwSetWindowPos(
         window,
         (vidmode.width() - pWidth.get(0)) / 2,
         (vidmode.height() - pHeight.get(0)) / 2
       );
     }

     glfwMakeContextCurrent(window);
     glfwSwapInterval(1);

     glfwShowWindow(window);
     GL.createCapabilities();
  }
  
  private void init() {
    String vs = "";
    String fs = "";
    
    try {
      vs = new String(Files.readAllBytes(Paths.get("src/land/shaders/vs.glsl")));
      fs = new String(Files.readAllBytes(Paths.get("src/land/shaders/fs.glsl")));
    } catch (IOException e) {}
    
    glEnable(GL_DEPTH_TEST);
    glDepthFunc(GL_LEQUAL);
    
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    
    fogCol = BufferUtils.createFloatBuffer(3);
    fogCol.put(.4f).put(.7f).put(.7f);
    fogCol.rewind();

    glViewport(0, 0, w, h);
    glClearColor(fogCol.get(0), fogCol.get(1), fogCol.get(2), 1f);

    vShader = glCreateShader(GL_VERTEX_SHADER);
    glShaderSource(vShader, new StringBuilder(vs));
    glCompileShader(vShader);
    System.out.println(GL20.glGetShaderInfoLog(vShader, 500));

    fShader = glCreateShader(GL_FRAGMENT_SHADER);
    glShaderSource(fShader, new StringBuilder(fs));
    glCompileShader(fShader);
    System.out.println(GL20.glGetShaderInfoLog(fShader, 500));

    program = glCreateProgram();
    glAttachShader(program, vShader);
    glAttachShader(program, fShader);
    glLinkProgram(program);
    glUseProgram(program);

    uniformProjection = glGetUniformLocation(program, "projection");
    uniformTranslation = glGetUniformLocation(program, "translation");
    uniformRotationX = glGetUniformLocation(program, "rotationX");
    uniformRotationY = glGetUniformLocation(program, "rotationY");
    
    uniformViewDist = glGetUniformLocation(program, "viewDist");
    uniformFogCol = glGetUniformLocation(program, "fogCol");
    
    glUniformMatrix4fv(uniformProjection, false, new float[] {
      1 / aspectRatio, 0, 0, 0,
      0, 1 / fovt, 0, 0,
      0, 0, -(far + near) / (far - near), -1,
      0, 0, -2 * far * near / (far - near), 0
    });
    
    glUniformMatrix4fv(uniformTranslation, false, translationMat);
    
    glUniform1f(uniformViewDist, viewDist);
    glUniform3fv(uniformFogCol, fogCol);
    
    attribPos = glGetAttribLocation(program, "pos");
    glEnableVertexAttribArray(attribPos);

    attribText = glGetAttribLocation(program, "textureCoords");
    glEnableVertexAttribArray(attribText);
    
    vBuffTargetBlockFrame = glGenBuffers();
    cBuffTargetBlockFrame = glGenBuffers();
    
    glBindBuffer(GL_ARRAY_BUFFER, vBuffTargetBlockFrame);
    glBufferData(GL_ARRAY_BUFFER, vTargetBlockFrame.capacity(), GL_DYNAMIC_DRAW);
    glVertexAttribPointer(attribPos, 3, GL_FLOAT, false, 0, 0);
    
    glBindBuffer(GL_ARRAY_BUFFER, cBuffTargetBlockFrame);
    glBufferData(GL_ARRAY_BUFFER, cTargetBlockFrame.capacity(), GL_DYNAMIC_DRAW);
    glVertexAttribPointer(attribText, 3, GL_FLOAT, false, 0, 0);
    
    loadTextures();
  }
  
  private void loadTextures() {
    int[] w = new int[1];
    int[] h = new int[1];
    int[] comp = new int[1];
    
    ByteBuffer image = stbi_load("src/land/textures/blocks.png", w, h, comp, 0);
    if(image == null) {
      System.err.println(stbi_failure_reason());
      System.exit(1);
    }
    
    int textureID = glGenTextures();
    
    glBindTexture(GL_TEXTURE_2D, textureID);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, w[0], h[0], 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
  }
  
  private void processInput() {
    if(glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) dir |= 1;
    else dir &= ~1;
    
    if(glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) dir |= 2;
    else dir &= ~2;
    
    if(glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) dir |= 4;
    else dir &= ~4;
    
    if(glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) dir |= 8;
    else dir &= ~8;
    
    if(glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) dir |= 16;
    else dir &= ~16;
    
    if(glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) dir |= 32;
    else dir &= ~32;
    
    if(glfwGetKey(window, GLFW_KEY_G) == GLFW_PRESS) {
      
    }
    
    if(glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
      glfwSetWindowShouldClose(window, true);
    }
    
    double[] cx = new double[1];
    double[] cy = new double[1];
    glfwGetCursorPos(window, cx, cy);
    
    ent.rx = (float)Math.max(Math.min(ent.rx + (cy[0] - hh) * cursorSpeed / h, pih), -pih);
    ent.ry = (float)((ent.ry - (cx[0] - wh) * cursorSpeed / w) % pi2);
    
    glfwSetCursorPos(window, wh, hh);
  }

  public void run() {
    enabled = true;
    
    initGL();
    init();
    
    ready = true;
    
    while (!glfwWindowShouldClose(window)) {
      glfwPollEvents();
      
      processInput();
      
      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

      if(dir != 0) {
        float x, z;

        if((dir & 3) != 0) {
          x = (float)(speed * Math.sin(ent.ry));
          z = (float)(speed * Math.cos(ent.ry));

          if((dir & 1) != 0) {
            ent.x -= x;
            ent.z -= z;
          }
          
          if((dir & 2) != 0) {
            ent.x += x;
            ent.z += z;
          }
        }

        if((dir & 12) != 0) {
          x = (float)(speed * Math.sin(ent.ry));
          z = (float)(speed * Math.cos(ent.ry));

          if((dir & 4) != 0) {
            ent.x -= z;
            ent.z += x;
          }
          
          if((dir & 8) != 0) {
            ent.x += z;
            ent.z -= x;
          }
        }
        
        if((dir & 16) != 0) ent.y += speed;
        if((dir & 32) != 0) ent.y -= speed;
      }
        
      translationMat.put(12, -ent.x);
      translationMat.put(13, -ent.y);
      translationMat.put(14, -ent.z);

      glUniformMatrix4fv(uniformTranslation, false, translationMat);
      
      float s, c;
      
      s = (float)Math.sin(ent.rx);
      c = (float)Math.cos(ent.rx);
      rotationXMat.put(6, s).put(9, -s);
      rotationXMat.put(5, c).put(10, c);

      glUniformMatrix4fv(uniformRotationX, false, rotationXMat);
      
      s = (float)Math.sin(ent.ry);
      c = (float)Math.cos(ent.ry);
      rotationYMat.put(2, s).put(8, -s);
      rotationYMat.put(0, c).put(10, c);
      
      glUniformMatrix4fv(uniformRotationY, false, rotationYMat);
      
      drawChunks();
      drawTargetBlockFrame();
      
      glfwSwapBuffers(window);
    }
    
    glfwFreeCallbacks(window);
    glfwDestroyWindow(window);

    glfwTerminate();
    glfwSetErrorCallback(null).free();
    
    glDeleteShader(vShader);
    glDeleteShader(fShader);
    glDeleteProgram(program);
    
    world.dispose();
    
    enabled = false;
  }
  
  private void drawChunks() {
    for(int i = 0; i < world.chunks.size(); i++) {
      Chunk chunk = world.chunks.get(i);
      if(chunk == null) continue;
      
      chunk.draw();
    }
  }
  
  private void drawTargetBlockFrame() {
    float x = ent.x + 3;
    float y = ent.y - 3;
    float z = ent.z + 3;
    
    vTargetBlockFrame.clear();
    cTargetBlockFrame.clear();
    
    vTargetBlockFrame
      .put(x).put(y).put(z).put(x + 1f).put(y).put(z)
      .put(x).put(y).put(z).put(x).put(y + 1f).put(z)
      .put(x).put(y).put(z).put(x).put(y).put(z + 1f)
      .put(x + 1f).put(y).put(z).put(x + 1f).put(y + 1f).put(z)
      .put(x + 1f).put(y).put(z).put(x + 1f).put(y).put(z + 1f)
      .put(x).put(y + 1f).put(z).put(x + 1f).put(y + 1f).put(z)
      .put(x).put(y + 1f).put(z).put(x).put(y + 1f).put(z + 1f)
      .put(x).put(y).put(z + 1f).put(x + 1f).put(y).put(z + 1f)
      .put(x).put(y).put(z + 1f).put(x).put(y + 1f).put(z + 1f)
      .put(x + 1f).put(y + 1f).put(z).put(x + 1f).put(y + 1f).put(z + 1f)
      .put(x + 1f).put(y).put(z + 1f).put(x + 1f).put(y + 1f).put(z + 1f)
      .put(x).put(y + 1f).put(z + 1f).put(x + 1f).put(y + 1f).put(z + 1f);
    
    cTargetBlockFrame
      .put(0).put(0).put(0).put(1).put(1).put(0)
      .put(0).put(0).put(0).put(1).put(1).put(0)
      .put(0).put(0).put(0).put(1).put(1).put(0)
      .put(0).put(0).put(0).put(1).put(1).put(0)
      .put(0).put(0).put(0).put(1).put(1).put(0)
      .put(0).put(0).put(0).put(1).put(1).put(0)
      .put(0).put(0).put(0).put(1).put(1).put(0)
      .put(0).put(0).put(0).put(1).put(1).put(0)
      .put(0).put(0).put(0).put(1).put(1).put(0)
      .put(0).put(0).put(0).put(1).put(1).put(0)
      .put(0).put(0).put(0).put(1).put(1).put(0)
      .put(0).put(0).put(0).put(1).put(1).put(0);
    
    vTargetBlockFrame.position(0);
    vTargetBlockFrame.limit(vTargetBlockFrame.capacity());
    
    cTargetBlockFrame.position(0);
    cTargetBlockFrame.limit(cTargetBlockFrame.capacity());
    
    glBindBuffer(GL_ARRAY_BUFFER, vBuffTargetBlockFrame);
    glBufferSubData(GL_ARRAY_BUFFER, 0, vTargetBlockFrame);
    glVertexAttribPointer(attribPos, 3, GL_FLOAT, false, 0, 0);
  
    glBindBuffer(GL_ARRAY_BUFFER, cBuffTargetBlockFrame);
    glBufferSubData(GL_ARRAY_BUFFER, 0, cTargetBlockFrame);
    glVertexAttribPointer(attribText, 3, GL_FLOAT, false, 0, 0);
    
    glDrawArrays(GL_LINES, 0, vTargetBlockFrame.capacity() / 3);
  }
}