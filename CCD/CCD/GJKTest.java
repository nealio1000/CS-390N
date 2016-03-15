/*  interactively test GJK
      two polygons (possibly degenerate to segment or point)
      are created, and as one is
      controlled by user input, GJK is constantly
      checking overlap and displaying the two closest
      points on the two bodies.
*/

import java.util.*;
import java.io.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import static org.lwjgl.opengl.GL11.*;

public class GJKTest extends Basic
{
  public static void main(String[] args)
  {
    GJKTest app = new GJKTest();
    app.start();
  }

  // instance variables:

  private Camera camera;

  private Triple center;  // origin for body A
  private double angle;   // orientation for A
  private ArrayList<Triple> verts;  // model vertices for A

  private  ArrayList<Triple> a, b;

  private PrintWriter pictexFile;

  public GJKTest()
  {
    super( "Interactively test GJK", 
             500, 500, 30 );

    Keyboard.enableRepeatEvents( false );

    // create a camera
    camera = new Camera( //
           0, 0, getPixelWidth(), getPixelHeight(),    // entire window
           0, 100, 0, 100,        // area of graph paper being viewed
           1, 100,               //  distance to near and far clipping planes
           RGB.WHITE );

    // hard-code various kinds of bodies
    center = new Triple( 20, 15 );
    angle = 0;
    verts = new ArrayList<Triple>();
/* // rectangle for A:
      verts.add( new Triple( 3, 2 ));
      verts.add( new Triple( -3, 2 ));
      verts.add( new Triple( -3, -2 ));
      verts.add( new Triple( 3, -2 ));
*/

    // general convex quadrilateral for A:
      verts.add( new Triple( 2,1 ));
      verts.add( new Triple( -1,4 ));
//      verts.add( new Triple( -3,-3 ));
 //     verts.add( new Triple( 3,-2 ));

    a = updateA();

    b = new ArrayList<Triple>();
/*  // rectangle for B:
         b.add( new Triple( 40, 45, -2 ) );
         b.add( new Triple( 55, 50, -2 ) );
         b.add( new Triple( 50, 65, -2 ) );
         b.add( new Triple( 35, 60, -2 ) );
*/

    b.add( new Triple( 65, 55 ));
    b.add( new Triple( 60, 70));
//    b.add( new Triple( 45,75));
//    b.add( new Triple( 35,65));
//    b.add( new Triple( 40,50));
//    b.add( new Triple( 55,45));


    try{
      pictexFile = new PrintWriter( new File( "pic.tex" ) );
      pictexFile.println("\\input pictex");
      pictexFile.println("\\nopagenumbers \\parindent 0true in");
    }
    catch(Exception e)
    {}

  }// GJKTest constructor

  // update a to reflect current values of center, angle, using model verts of A
  private ArrayList<Triple> updateA()
  {
    ArrayList<Triple> v = new ArrayList<Triple>();
    double rads = Math.toRadians( angle );
    double cos = Math.cos( rads );
    double sin = Math.sin( rads );

    double x,y;

    for( int k=0; k<verts.size(); k++ )
    {// compute transformed vertex k and append to v
       x = verts.get(k).x;  y = verts.get(k).y;
       v.add( new Triple( center.x + cos*x-sin*y, center.y + sin*x + cos*y, -2 ) );
    }

    return v;
  }

  public void init()
  {
    // set background color once and for all
    glClearColor( 1.0f, 1.0f, 0.0f, 0.0f );
  }

  public void display()
  {
    // clear the screen to background color and clear the depth buffer:
    glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );

    camera.activate();

    // display both polygons

    Lib.setColor( new RGB( 200, 200, 200 ) );
    display( a );
    Lib.setColor( new RGB( 200, 200, 200 ) );
    display( b );

    Triple start = a.get(0).vectorTo( b.get(0) );

    GJKResult res = GJK.gjk( start, a, b );
    // draw points on A and B that are closest to each other
    Triple[] points = res.getClosestPoints( a, b );
    Lib.setColor( new RGB( 0, 0, 0 ) );
    glPointSize( 3 );
    glBegin( GL_POINTS );
      glVertex3d( points[0].x, points[0].y, points[0].z );
      glVertex3d( points[1].x, points[1].y, points[1].z );
    glEnd();
    
  }// display

  private void display( ArrayList<Triple> a )
  {
    if( a.size() > 2 )
      glBegin( GL_LINE_LOOP );
    else if( a.size() == 2 )
      glBegin( GL_LINES );
    else
      glBegin( GL_POINTS );

      for( int k=0; k<a.size(); k++ )
      {
        Triple p = a.get(k);
        glVertex3d( p.x, p.y, p.z );
      }

    glEnd();
  }

  public void update()
  {
  }// update

  private long keyTime;
  private final static long reqDelay = 30000000;  // 1/10th second

  // simple handling of key state so that user has to
  // press and release a key to count as a press
  public void processInputs()
  {
    Keyboard.poll();

    long currentTime = System.nanoTime();

    double amount = 0.5;

    if( Keyboard.isKeyDown( Keyboard.KEY_L ) && keyTime+reqDelay <= currentTime )
    {
      center.translateBy( -amount, 0, 0 );
      a = updateA();
    }

    if( Keyboard.isKeyDown( Keyboard.KEY_R ) && keyTime+reqDelay <= currentTime )
    {
      center.translateBy( amount, 0, 0 );
      a = updateA();
    }

    if( Keyboard.isKeyDown( Keyboard.KEY_D ) && keyTime+reqDelay <= currentTime )
    {
      center.translateBy( 0, -amount, 0 );
      a = updateA();
    }

    if( Keyboard.isKeyDown( Keyboard.KEY_U ) && keyTime+reqDelay <= currentTime )
    {
      center.translateBy( 0, amount, 0 );
      a = updateA();
    }

    if( Keyboard.isKeyDown( Keyboard.KEY_P ) && keyTime+reqDelay <= currentTime )
    {
      angle += 1;
      if( angle > 360 )
        angle -= 360;
      a = updateA();
    }

    if( Keyboard.isKeyDown( Keyboard.KEY_O ) && keyTime+reqDelay <= currentTime )
    {
      angle -= 1;
      if( angle < 0 )
        angle += 360;
      a = updateA();
    }

    keyTime = currentTime;

  }// processInputs

}
