/*  
   generate random bodies, checking each one for
   overlap with existing ones with GJK

   Use 3 cameras from TestJohnson, adjusted for
   full sound stage
*/

import java.util.*;
import java.io.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import static org.lwjgl.opengl.GL11.*;

public class TestGJK extends Basic
{
  public static void main(String[] args)
  {
    TestGJK app = new TestGJK();
    app.start();
  }

  private static double size = 2;

  // instance variables:

  private double time;
  private double timeStep;

  private int currentPair;  

//  private SeparationTracker tracker;
  private double mouseX, mouseY;
  private Camera camera1, camera2, camera3;
  private ArrayList<Body> list;

  public TestGJK()
  {
    super( "Test and development application", 
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

    rng = new Random(1);

/*
    Body body1 = new Body( size, 
                 new Triple( 50, 50, 50 ), Quat.id,
                   Triple.zero, 0, Triple.xAxis,
                   1 );
    list.add( body1 );
 
    Body body2 = new Body( size, 
                 new Triple( 60, 55, 55 ), Quat.id,
                   Triple.zero, 30, new Triple(1,-Math.sqrt(3),0), 
                   1 );
    list.add( body2 );
*/

  }// TestGJK constructor

  // construct a random body and see if it overlaps
  // an bodies in the list, and if so, mention it,
  // otherwise add to the list

  private Random rng;

  private double randomCoord()
  {
    return rng.nextDouble()*(100-2*size) + size;
  }

  private double randomAngle()
  {
    return rng.nextDouble()*360;
  }

  private Triple randomAxis()
  {
    return new Triple( 2*rng.nextDouble()-1, 
                       2*rng.nextDouble()-1,
                       2*rng.nextDouble()-1 );
  }

  private void addBody()
  {

    // make boxes
    Body body = new Body( size, size, size,
     new Triple( 50, randomCoord(), randomCoord() ), // location
      Quat.id, //  new Quat( randomAngle(), randomAxis() ), // orientation
                    Triple.zero, // trans vel
                    0, Triple.xAxis,  // spin
                    1                 // inverse mass
                 );

/*

    // make regular tetrahedra
    Body body = new Body( size, 
     new Triple( randomCoord(), 50, randomCoord() ), // location
                   new Quat( randomAngle(), randomAxis() ), // orientation
                    Triple.zero, // trans vel
                    0, Triple.xAxis,  // spin
                    1                 // inverse mass
                   );
*/

System.out.println("ATTEMPTing to add " + body );

    boolean overlaps = false;
    for( int k=0; !overlaps && k<list.size(); k++ )
    {// use GJK to see if body is too close to list.get(k)

      System.out.println("\n\ncheck current body " + body.getCenter() +
                         " against " + list.get(k).getCenter() );
      GJKResult result = GJK.gjk( Triple.xAxis, body, list.get(k) );

/*      // brute force debugging code--------------

      // for pragmatic reasons (takes too long to test)
      // don't do brute force if they're clearly far apart

      if( body.getCenter().vectorTo( list.get(k).getCenter() ).norm()
          < 4*size )
      {// might be near enough to touch, do brute force
        double temp = GJK.bruteForceClosest( body, list.get(k) );
        if( result.v.norm() > temp+0.0000001 )
        {
          System.out.println("GJK got distance " + result.v.norm() +
            " whereas brute force got " + temp );
          System.exit(1);
        }
      }
*/      // brute force debugging code--------------

      if( result.v.norm() < 0.001 )
      {
        overlaps = true;
        System.out.print( "NOTE: on frame " + getFrameNumber() );
        System.out.println(" new body: " + body + " hit existing body in position " + k );
      }
    }

    if( !overlaps )
    {
      list.add( body );
      System.out.print( "NOTE: on frame " + getFrameNumber() );
      System.out.println(" added body # " + list.size() );
    }
  }

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
    addBody();  // testing gjk by adding a ton

/*
    GJKResult result = GJK.gjk( Triple.xAxis, list.get(0), list.get(1) );
    System.out.println("Frame " + getFrameNumber() + ", after GJK:\n" + result );
    result.reportDotProds( list.get(0).getActualVertices(),
                           list.get(1).getActualVertices() );
*/

    for( int k=0; k<list.size(); k++ )
    {
      list.get( k ).advance( timeStep );
    }

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
