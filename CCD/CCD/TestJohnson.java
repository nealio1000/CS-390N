/*  
    interactively move a hard-coded tetrahedron around and
    visualize closest point to origin, by way of
    testing Johnson distance algorithm
*/

import java.util.*;
import java.io.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import static org.lwjgl.opengl.GL11.*;

public class TestJohnson extends Basic
{
  public static void main(String[] args)
  {
    TestJohnson app = new TestJohnson();
    app.start();
  }

  // instance variables:
  private Camera camera1, camera2, camera3;

  public TestJohnson()
  {
    super( "Test Johnson", 
             1700, 600, 30 );

    // set up the test tetrahedron
    a = new Triple( 2, 1, 1 ); 
    b = new Triple( 5, 3, 2 ); 
    c = new Triple( 3, 6, 3 ); 
    d = new Triple( 4, 3, 7 ); 

    shift = new Triple(0,0,0);
    
    // create a camera
    camera1 = new Camera( null,
                 new Triple(0,-20,0), new Triple(0,0,0), new Triple(0,0,1),
                  10,
                 10, 30,
                50, 50, 500, 500,
                RGB.WHITE );

    camera2 = new Camera( null,
               new Triple(20,0,0), new Triple(0,0,0), new Triple(0,0,1),
                  10,
                 10, 30,
                600, 50, 500, 500,
                RGB.WHITE );

    camera3 = new Camera( null,
             new Triple(0,0,20), new Triple(0,0,0), new Triple(0,1,0),
                  10,
                 10, 30, 
                1150, 50, 500, 500,
                RGB.WHITE );

  }// TestJohnson constructor

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

    //*************************************
    camera1.activate();

    scene();

    //*************************************
    camera2.activate();

    scene();

    //*************************************
    camera3.activate();

    scene();

  }// display

  // simple hard-coding of 4 vertices with shift vector
  private Triple shift;  // the shift vector
  private Triple a, b, c, d;
  
  private void scene()
  {
    // draw the origin
    glColor3d( 1, 0, 0 );
    glPointSize( 3 );
    glBegin( GL_POINTS );
      glVertex3d( 0, 0, 0 );
    glEnd();

    // draw the tetrahedron shifted 

    // abc
    glColor3d( 0, 0, 0 );
    glBegin( GL_LINE_LOOP );
      Lib.vertex( a.add(shift) );
      Lib.vertex( b.add(shift) );
      Lib.vertex( c.add(shift) );
    glEnd();

/*
    // abd
    glColor3d( 0, 0, 0 );
    glBegin( GL_LINE_LOOP );
      Lib.vertex( a.add(shift) );
      Lib.vertex( b.add(shift) );
      Lib.vertex( d.add(shift) );
    glEnd();

    // acd
    glColor3d( 0, 0, 0 );
    glBegin( GL_LINE_LOOP );
      Lib.vertex( a.add(shift) );
      Lib.vertex( c.add(shift) );
      Lib.vertex( d.add(shift) );
    glEnd();

    // bcd
    glColor3d( 0, 0, 0 );
    glBegin( GL_LINE_LOOP );
      Lib.vertex( b.add(shift) );
      Lib.vertex( c.add(shift) );
      Lib.vertex( d.add(shift) );
    glEnd();
*/

    // use Johnson to figure closest point to origin and
    // draw it
    ArrayList<Triple> vA = new ArrayList<Triple>();
    ArrayList<Triple> vB = new ArrayList<Triple>();
    vA.add( a.add(shift) );
    vA.add( b.add(shift) );
    vA.add( c.add(shift) );
 //   vA.add( d.add(shift) );
    vB.add( new Triple(0,0,0) );
    GJK.setVertices( vA, vB );
    ArrayList<IntPair> w = new ArrayList<IntPair>();
    w.add( new IntPair( 0, 0 ) );
    w.add( new IntPair( 1, 0 ) );
    w.add( new IntPair( 2, 0 ) );
//    w.add( new IntPair( 3, 0 ) );
//  System.out.println("doing johnson with shift " + shift );
    GJKResult result = GJK.johnsonDistanceAlgorithm( w );
    glColor3d( 0, 1, 0 );
    glPointSize( 3 );
    glBegin( GL_POINTS );
       Lib.vertex( result.v );
    glEnd();
    
  }// scene

  public void update()
  {
  }// update

  public void processInputs()
  {
    double a = 0.1;

    Keys.update();

    while( Keys.hasNext() )
    {
      int code = Keys.next();

      if( code == Keyboard.KEY_LEFT )
      {
        shift.x -= a;        
      }
      else if( code == Keyboard.KEY_RIGHT )
      {
        shift.x += a;        
      }
      else if( code == Keyboard.KEY_DOWN )
      {
        shift.y -= a;        
      }
      else if( code == Keyboard.KEY_UP )
      {
        shift.y += a;        
      }
      else if( code == Keyboard.KEY_HOME )
      {
        shift.z += a;        
      }
      else if( code == Keyboard.KEY_END )
      {
        shift.z -= a;        
      }

    }// loop to process all waiting key input events
  
  }// processInputs

}
