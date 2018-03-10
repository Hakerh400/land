package land;

import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

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
  
  public int attribPos, attribCol;
  public int[] uniforms = new int[4];
  
  float[] translationMat = Matrix.identity();
  float[] rotationXMat = Matrix.identity();
  float[] rotationYMat = Matrix.identity();
  
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

    glViewport(0, 0, w, h);
    glClearColor(0f, 0f, 0f, 1f);

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

    uniforms[0] = glGetUniformLocation(program, "translation");
    uniforms[1] = glGetUniformLocation(program, "rotationX");
    uniforms[2] = glGetUniformLocation(program, "rotationY");
    uniforms[3] = glGetUniformLocation(program, "projection");
    
    glUniformMatrix4fv(uniforms[3], false, new float[] {
      1 / (aspectRatio * fovt), 0, 0, 0,
      0, 1 / fovt, 0, 0,
      0, 0, -(far + near) / (far - near), -1,
      0, 0, -2 * far * near / (far - near), 0
    });
    
    glUniformMatrix4fv(uniforms[0], false, translationMat);
    
    attribPos = glGetAttribLocation(program, "pos");
    glEnableVertexAttribArray(attribPos);

    attribCol = glGetAttribLocation(program, "col");
    glEnableVertexAttribArray(attribCol);
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
    
    glClearColor(0f, 0f, 0f, 1f);
    
    while (!glfwWindowShouldClose(window)) {
      glfwPollEvents();
      
      processInput();
      
      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

      if(dir != 0) {
        float sp = (dir & 16) != 0 ? speed * .1f : speed;
        float x, y, z;

        if((dir & 3) != 0) {
          x = (float)(sp * Math.cos(ent.rx) * Math.sin(ent.ry));
          y = (float)(sp * Math.sin(ent.rx));
          z = (float)(sp * Math.cos(ent.rx) * Math.cos(ent.ry));

          if((dir & 1) != 0) {
            ent.x -= x;
            ent.y -= y;
            ent.z -= z;
          }
          
          if((dir & 2) != 0) {
            ent.x += x;
            ent.y += y;
            ent.z += z;
          }
        }

        if((dir & 12) != 0) {
          x = (float)(sp * Math.sin(ent.ry));
          z = (float)(sp * Math.cos(ent.ry));

          if((dir & 4) != 0) {
            ent.x -= z;
            ent.z += x;
          }
          
          if((dir & 8) != 0) {
            ent.x += z;
            ent.z -= x;
          }
        }
      }
        
      translationMat[12] = -ent.x;
      translationMat[13] = -ent.y;
      translationMat[14] = -ent.z;

      glUniformMatrix4fv(uniforms[0], false, translationMat);
      
      rotationXMat[9] = -(rotationXMat[6] = (float)Math.sin(ent.rx));
      rotationXMat[5] = rotationXMat[10] = (float)Math.cos(ent.rx);

      glUniformMatrix4fv(uniforms[1], false, rotationXMat);
      
      rotationYMat[8] = -(rotationYMat[2] = (float)Math.sin(ent.ry));
      rotationYMat[0] = rotationYMat[10] = (float)Math.cos(ent.ry);

      glUniformMatrix4fv(uniforms[2], false, rotationYMat);
      
      drawChunks();
      
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
      
      if(!chunk.hasBuffs) {
        chunk.initBuffs();
      }
      
      chunk.draw();
    }
  }
}