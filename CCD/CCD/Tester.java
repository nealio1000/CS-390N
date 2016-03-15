/*  this application provides a test framework
*/

import java.util.*;
import java.io.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import static org.lwjgl.opengl.GL11.*;

public class Tester extends Basic
{
  public static void main(String[] args)
  {
    Tester app = new Tester();
    app.start();
  }

  private static double size = 5;

  // instance variables:

  private double time;
  private double timeStep;

  private int currentPair;  

  private SeparationTracker tracker;
  private double mouseX, mouseY;
  private Camera camera;
  private ArrayList<Body> list;

  public Tester()
  {
    super( "Test and development application", 
             550, 550, 30 );

    // create a camera
    camera = new Camera( null,
                 new Triple(50,-100,50), new Triple(50,0,50), new Triple(0,0,1),
                  50,  // size = half-width = half-height of view rectangle
                 100, 200,   // near, far
                0, 0, getPixelWidth(), getPixelHeight(),    // entire window
                RGB.WHITE );

    // gravity---each body has its own

    list = new ArrayList<Body>();

    // hard-code a few bodies

    Body b = new Body( size, 2*size, 3*size,  // sx, sy, sz
                       new Triple( 50, 50, 50 ), // initial location
//                        new Quat(1,0,0,0), // initial orientation---standard
      new Quat( 180, new Triple(1,0,0) ), // ori is 180 about x
                       new Triple(0,0,0), // initial translational velocity
                       0,  // initial spin in degrees per second
                       new Triple(1,0,0), // axis of rotation
                       1/(size*size*size)   // inverse mass 
                     );
    // tested axes (1,1,1), (0,1,0), (1,0,0), (0,0,1)
    list.add(b);

    timeStep = 1.0/getFPS();

/*  // stuff for CCD
    time = 0;
    currentPair = 0;
    tracker = new SeparationTracker( list.get(currentPair), 
                                     list.get(currentPair+1), 
         list.get(currentPair).getActualVertex(0).vectorTo(
            list.get(currentPair+1).getActualVertex(0) ) );
*/

  }// Tester constructor

  public void init()
  {
    // configure the keys
    Keys.enableShifting( true );
    Keys.enableRepeating( true );
    Keys.setRepeatDelay( 100000000L );

    // set background color once and for all
    glClearColor( 0.5f, 0.5f, 0.5f, 0.0f );
    
    // enable depth testing
    glEnable( GL_DEPTH_TEST );
  }

  public void display()
  {
    // clear the screen to background color and clear the depth buffer:
    glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );

    camera.activate();

    // display the bodies
    for( int k=0; k<list.size(); k++ )
    {
      list.get( k ).draw();
    }

  }// display

  public void update()
  {

    // stub for simply observing moving bodies:
    for( int k=0; k<list.size(); k++ )
    {
      list.get( k ).advance( timeStep );
    }

/*
    if( ! keepMoving ) // don't update anymore once keepMoving has been set
      return;

    // inner loop to advance time by timeStep
    double timeRemaining = timeStep;

    while( timeRemaining > 0 && !tracker.foundContact() )
    {
      // narrow phase collision work:  for each pair, determine
      // how far we could advance time, up to remaining time,
      // and monitor the smallest such time

      double earliestTime = tracker.computeTimeUntilTouch( timeRemaining );

      if( tracker.foundContact() )
      {
        System.out.println("CONTACT!!!!");
        keepMoving = false;
      }
      else
      {
        System.out.println("FULL STEP WITHOUT CONTACT");
      }

      // advance the bodies
      list.get(currentPair).advance( earliestTime );
      list.get(currentPair+1).advance( earliestTime );
       
      timeRemaining -= earliestTime;
System.out.println("\n\n -------- Time remaining = " + timeRemaining );

    }// inner loop to advance everything through stepTime
*/

  }// update

  public void processInputs()
  {
    Keys.update();

    double impMag = size*size*size;
    Body b = list.get(0);

    while( Keys.hasNext() )
    {
      int code = Keys.next();

      // reset
      if( code == Keyboard.KEY_HOME )
        b.reset( 50, 50, 50 );

      // non-spinny thrusters
      else if( code == Keyboard.KEY_UP )
        b.applyModelImpulse( new Triple(0,impMag,0), new Triple(0,-size,0) );
      else if( code == Keyboard.KEY_DOWN )
        b.applyModelImpulse( new Triple(0,-impMag,0), new Triple(0,size,0) );
      else if( code == Keyboard.KEY_RIGHT )
        b.applyModelImpulse( new Triple(impMag,0,0), new Triple(-size,0,0) );
      else if( code == Keyboard.KEY_LEFT )
        b.applyModelImpulse( new Triple(-impMag,0,0), new Triple(size,0,0) );
      else if( code == Keyboard.KEY_PRIOR )  // labeled PAGE_UP typically
        b.applyModelImpulse( new Triple(0,0,impMag), new Triple(0,0,-size) );
      else if( code == Keyboard.KEY_NEXT )  // labeled PAGE_DOWN typically
        b.applyModelImpulse( new Triple(0,0,-impMag), new Triple(0,0,size) );

      // 4 bottom thrusters
      else if( code == Keyboard.KEY_NUMPAD1 )
        b.applyModelImpulse( new Triple(0,0, impMag ), 
                            new Triple(-size,-size,-size) );
      else if( code == Keyboard.KEY_NUMPAD2 )
        b.applyModelImpulse( new Triple(0,0, impMag ), 
                            new Triple(size,-size,-size) );
      else if( code == Keyboard.KEY_NUMPAD4 )
        b.applyModelImpulse( new Triple(0,0, impMag ), 
                            new Triple(-size,size,-size) );
      else if( code == Keyboard.KEY_NUMPAD5 )
        b.applyModelImpulse( new Triple(0,0, impMag ), 
                            new Triple(size,size,-size) );

      else if( code == Keyboard.KEY_L )
        b.applyWorldImpulse( new Triple(-impMag,0,0), new Triple(0,0,0) );
      else if( code == Keyboard.KEY_R )
        b.applyWorldImpulse( new Triple(impMag,0,0), new Triple(0,0,0) );
      else if( code == Keyboard.KEY_D )
        b.applyWorldImpulse( new Triple(0,0,-impMag), new Triple(0,0,0) );
      else if( code == Keyboard.KEY_U )
        b.applyWorldImpulse( new Triple(0,0,impMag), new Triple(0,0,0) );
      else if( code == Keyboard.KEY_B )
        b.applyWorldImpulse( new Triple(0,impMag,0), new Triple(0,0,0) );
      else if( code == Keyboard.KEY_F )
        b.applyWorldImpulse( new Triple(0,-impMag,0), new Triple(0,0,0) );

      else if( code == Keyboard.KEY_P )
      {// pivot, without translating, about, say, the (0,1,0) axis
       // by applying two simultaneous impulses properly
        b.applyModelImpulse( new Triple(impMag,2*impMag,3*impMag), 
                               new Triple(-size,size,size) );
        b.applyModelImpulse( new Triple(-impMag,-2*impMag,-3*impMag), 
                               new Triple(size,-size,-size) );
      }

      else if( code == Keyboard.KEY_O )
      {// opposite pivot
        b.applyModelImpulse( new Triple(-impMag,-2*impMag,-3*impMag), 
                               new Triple(-size,size,size) );
        b.applyModelImpulse( new Triple(impMag,2*impMag,3*impMag), 
                               new Triple(size,-size,-size) );
      }

    }// loop to process all waiting key input events


  
  }// processInputs

}
