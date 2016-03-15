/*  this class should be extended to provide
    an event-driven, graphical application

   The extending class should have its own constructor
   where things can be set up before the window is prepared,
   with a call to super( window title, fps )

   Override these methods:

   init:  do things to be done once at the begining after the window
          is ready to go

   display:  draw the game world

   processInputs:  scan InputInfo queue and process all waiting input events
                   with appropriate changes to game objects
    
   update:  advance the simulation one full time step

*/

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;
 
public class Basic{
 
  // We need to strongly reference callback instances.
  private GLFWKeyCallback keyCallback;
  private GLFWCursorPosCallback cursorPosCallback;
  private GLFWMouseButtonCallback mouseButtonCallback;
 
  // The window handle
  private long window;

  private String title;
  private int width, height;
  private int stepNumber;
  private long timeStep;   // amount of time in a step in nanoseconds

  private long startTime;
 
  private int mouseX, mouseY;  // current mouse cursor position

  // create window given title, size in pixels, step time in nanoseconds
  public Basic( String windowLabel, int pw, int ph, long timeInNanos )
  {
    title = windowLabel;
    width = pw;  height = ph;
    stepNumber = 0;

    timeStep = timeInNanos;
    startTime = System.nanoTime();
  }

  public void start()
  {
    try{
      setup();   // get window ready to go
      //init();    // let extending app set up some things
      loop();
 
      // Release window and window callbacks
      glfwDestroyWindow(window);
      keyCallback.release();
    } finally {
      // Terminate GLFW and release the GLFWerrorfun
      glfwTerminate();
    }
  }
 
  private void setup()
  {
    // Initialize GLFW. Most GLFW functions will not work before doing this.
    if( glfwInit() != GL11.GL_TRUE )
      throw new IllegalStateException("Unable to initialize GLFW");
 
    // Configure the window
    glfwDefaultWindowHints(); // optional, the current window hints are already the default
    glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
    glfwWindowHint(GLFW_RESIZABLE, GL_FALSE); // the window will not be resizable

    // Get the usable resolution of the primary monitor
    /*  could use this to detect full screen size on any monitor
    ByteBuffer vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
    width = GLFWvidmode.width(vidmode) - 900;
    height = GLFWvidmode.height(vidmode) - 120; // avoid the dock at the bottom
    */

    // Create the window
    window = glfwCreateWindow(width, height, title, NULL, NULL);
    if( window == NULL )
      throw new RuntimeException("Failed to create the GLFW window");
 
    // Set up a key callback. 
    // It will be called every time a key is pressed, repeated or released.
    glfwSetKeyCallback(window, 
      keyCallback = new GLFWKeyCallback() 
      {
        public void invoke(long window, int key, 
                           int scancode, int action, 
                           int mods)
        {
          // whenever a key is pressed, the callback function
          // puts it in the InputInfo queue for processing later in an app
          InputInfo.add( new InputInfo( 'k', key, action, mods ) );
        }
      }
    );

    glfwSetCursorPosCallback( window,
       cursorPosCallback = new GLFWCursorPosCallback()
       {
         public void invoke(long window, double xpos, double ypos )
         {
           // whenever cursor moves, add input info
           InputInfo.add( new InputInfo( 'm', (int) Math.round(xpos),
                                              (int) Math.round(ypos) ) );
         }
       }
     );
 
    glfwSetMouseButtonCallback( window,
       mouseButtonCallback = new GLFWMouseButtonCallback()
       {
         public void invoke(long window, int button, int action, int mods )
         {
           // whenever mouse button is pressed, released, or repeated, make input info
           InputInfo.add( new InputInfo( 'b', button, action, mods ) );
         }
       }
    );

    glfwSetWindowPos( window, 0, 0 );
 
    // Make the OpenGL context current
    glfwMakeContextCurrent(window);
    // Enable v-sync
    glfwSwapInterval(1);

    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL );

    // Make the window visible
    glfwShowWindow(window);
  }
 
  private final static int NUMDELAYSPERYIELD = 5;  // hope not important

  private void loop()
  {
    // This line is critical for LWJGL's interoperation with GLFW's
    // OpenGL context, or any context that is managed externally.
    // LWJGL detects the context that is current in the current thread,
    // creates the ContextCapabilities instance and makes the OpenGL
    // bindings available for use.
    GLContext.createFromCurrent();
 
    init();

    // Run the rendering loop until the window wants to close
    long prevTime = System.nanoTime();
    long overTime = 0L;

    int numDelays = 0;

    while( glfwWindowShouldClose(window) == GL_FALSE )
    {
      stepNumber++;

      display();
      processInputs();
      update();

      long currentTime = System.nanoTime();

      // figure time spent on this step already in nanoseconds
      long elapsedTime = currentTime - prevTime;

      long sleepTime = timeStep - elapsedTime - overTime;

      if( sleepTime > 0 )
      {// have some time to sleep
        try{
          Thread.sleep( sleepTime/1000000L ); // sleep this many milliseconds
        }
        catch(InterruptedException ie)
        {}
        prevTime = System.nanoTime();  // will be the new before step time
        // figure how much too long it took with overhead from sleeping
        overTime = prevTime - currentTime - sleepTime;
      }
      else
      {// step took longer than timeStep
        overTime = 0L;
        prevTime = System.nanoTime();
        numDelays++;

        if( numDelays >= NUMDELAYSPERYIELD )
        {// give another thread a chance
          Thread.yield();
          numDelays = 0;
        }
      }
      
      glfwSwapBuffers(window); // swap the color buffers
 
      // Poll for window events. The key callback above will only be
      // invoked during this call.
      glfwPollEvents();
    }
  }
 
  // ---------------------- methods that can be called in app  -------

  public int getStepNumber()
  {
    return stepNumber;
  }

  // return time from start of application
  // in milliseconds
  public long getTime()
  {
    return (System.nanoTime() - startTime) / 1000000L;
  }

  public double getTimeStep()
  {
    return timeStep/1e9;  //convert from nanoseconds to seconds
  }

  public void restartStepNumbering()
  {
    stepNumber = 0;
  }

  public int getMouseX()
  {
    return mouseX;
  }

  public int getMouseY()
  {
    return mouseY;
  }

  // ---------------------- methods that can and should be overridden -------

  protected void init()
  {
    // Set the clear color once and for all
    glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
  }

  protected void processInputs()
  {
    while( InputInfo.size() > 0 )
    {// process next input info
      InputInfo info = InputInfo.get();
      System.out.println( info );
    }
    
  }

  protected void update()
  {
  }

  protected void display()
  {
    // clear the framebuffer
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); 

//    System.out.println("Step " + getStepNumber() );
  }

  public static void main(String[] args)
  {
    Basic t = new Basic( "Basic App (should be extended)", 800, 600, 33333333L );
    t.start();
  }
 
}
