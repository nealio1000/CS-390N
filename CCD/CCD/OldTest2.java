/*  
   test CCD with two bodies,
   obtained sequentially from a given data file,
   showing action in 3 cameras with ortho views
*/

import java.util.*;
import java.io.*;

import org.lwjgl.input.Keyboard;

import static org.lwjgl.opengl.GL11.*;

public class OldTest2 extends Basic
{
  public static void main(String[] args)
  {
    OldTest2 app = new OldTest2();
    app.start();
  }

  private static double size = 10;

  // instance variables:

  private double timeStep;

  private int currentPair;  

  private SeparationTracker tracker;
  private Camera camera1, camera2, camera3;
  private ArrayList<Body> list;

  private boolean keepMoving;

  public OldTest2()
  {
    super( "Test CCD with 2 bodies", 
             1700, 600, 30 );

    // create cameras

    // front
    camera1 = new Camera( null, "ortho",
              new Triple(50,-100,50), new Triple(50,50,50), new Triple(0,0,1),
                  50,
                 100, 300,
                50, 50, 500, 500,
                RGB.WHITE );

    // right
    camera2 = new Camera( null, "ortho",
               new Triple(200,50,50), new Triple(50,50,50), new Triple(0,0,1),
                  50,
                 100, 300,
                600, 50, 500, 500,
                RGB.WHITE );

    // top
    camera3 = new Camera( null, "ortho",
             new Triple(50,50,200), new Triple(50,50,50), new Triple(0,1,0),
                  50,
                 100, 300,
                1150, 50, 500, 500,
                RGB.WHITE );


    // gravity---each body has its own

    list = new ArrayList<Body>();

    timeStep = 1.0/getFPS();


  // Test case 1 (meet nicely in center of soundstage, edge to edge):

/*
    Body body1 = new Body( size, size, size,
                          new Triple( 10, 50, 10 ),
                          new Quat( 0, new Triple(1,0,0) ),
          new Triple( 5, 0, 5 ), 30, new Triple(0,1,0),
                           1 );
    list.add( body1 );

    Body body2 = new Body( size, size, size,
                          new Triple( 90, 50, 90 ),
                          new Quat( 0, new Triple(1,0,0) ),
          new Triple( -5, 0, -5 ), 30, new Triple(0,0,1),
                           1 );
    list.add( body2 );
*/

/*
    // Test case 2 (body 2 drops onto body 1 (no translational velocity), use various spin rates for both):

    Body body1 = new Body( size, size, size,
                          new Triple( 10, 50, 10 ),
                          Quat.id,
          Triple.zero, 19, Triple.yAxis, 
                           1 );
    list.add( body1 );
    
    Body body2 = new Body( size, size, size,
                          new Triple( 90, 50, 90 ),
                          new Quat( 0, new Triple(1,0,0) ),
          new Triple( -5, 0, -5 ), 65, new Triple(0,0,1),
                           1 );
    list.add( body2 );
*/

    // Test case 3 (like 1, but vary spins and linear velocities):
    
/*
    Body body1 = new Body( size, size, size,
                          new Triple( 10, 50, 10 ),
                          Quat.id, 
          new Triple( 7, 3, 11 ), 19, Triple.yAxis,
                           1 );
    list.add( body1 );
    
    Body body2 = new Body( size, size, size,
                          new Triple( 90, 50, 90 ),
                          new Quat( 0, new Triple(1,0,0) ),
          new Triple( -5, 0, -5 ), 65, new Triple(0,0,1),
                           1 );
    list.add( body2 );
*/

    // Test case 4 (a box and a tetrahedron):

    Body body1 = new Body( size, size, size,
                          new Triple( 10, 50, 10 ),
                          Quat.id,
          new Triple( 7, 3, 11 ), 19, Triple.yAxis,
                           1 );
    list.add( body1 );
   
    Body body2 = new Body( size, 
                          new Triple( 90, 50, 90 ),
                          new Quat( 37, new Triple(1,0,0) ),
          new Triple( -6, 1, -5 ), 65, new Triple(1,0,1),
                           1 );
    list.add( body2 );

    tracker = new SeparationTracker( body1, body2,
              body1.getActualVertex(0).vectorTo( body2.getActualVertex(0) ) );

    timeStep = 1.0/getFPS();
    keepMoving = true;

  }// OldTest2 constructor

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

    camera1.activate();
    scene();

    camera2.activate();
    scene();

    camera3.activate();
    scene();

  }// display

  private void scene()
  {
    // display the bodies
    for( int k=0; k<list.size(); k++ )
    {
      list.get( k ).draw();
    }

  }// scene

  public void update()
  {
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
      list.get(0).advance( earliestTime );
      list.get(1).advance( earliestTime );

      timeRemaining -= earliestTime;
System.out.println("\n\n -------- Time remaining = " + timeRemaining );

    }// inner loop to advance everything through stepTime

  }// update

  public void processInputs()
  {
    Keys.update();

    while( Keys.hasNext() )
    {
      int code = Keys.next();
    }// loop to process all waiting key input events
  
  }// processInputs

}
