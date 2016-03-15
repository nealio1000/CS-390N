/*  randomly test GJK
    by keeping a list of bodies, keep randomly
    trying to add another
*/

import java.util.*;
import java.io.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import static org.lwjgl.opengl.GL11.*;

public class GJKRandomTest extends Basic
{
  public static void main(String[] args)
  {
    GJKRandomTest app = new GJKRandomTest();
    app.start();
  }

  // instance variables:

  private Camera camera;

  private ArrayList<Body> list;
  private ArrayList<Triple> modelVerts;
  private RGB standardColor;

  private PrintWriter out;
  private PrintWriter pictexFile;

  public GJKRandomTest()
  {
    super( "Randomly test GJK", 
             500, 500, 30 );

    Keyboard.enableRepeatEvents( false );

    // create a camera
    camera = new Camera( //
           0, 0, getPixelWidth(), getPixelHeight(),    // entire window
           0, 100, 0, 100,        // area of graph paper being viewed
           1, 100,               //  distance to near and far clipping planes
           RGB.WHITE );

     list = new ArrayList<Body>();

     // make a standard square of model vertices:
     double size = 3;
     modelVerts = new ArrayList<Triple>();
     modelVerts.add( new Triple( size, size, 0 ) ); 
     modelVerts.add( new Triple( -size, size, 0 ) ); 
     modelVerts.add( new Triple( -size, -size, 0 ) ); 
     modelVerts.add( new Triple( size, -size, 0 ) ); 

     standardColor = new RGB( 1, 0, 0 );

    try{
      out = new PrintWriter( new File( "log" ) );
    }
    catch(Exception e)
    {}

    try{
      pictexFile = new PrintWriter( new File( "pic.tex" ) );
      pictexFile.println("\\input pictex");
      pictexFile.println("\\nopagenumbers \\parindent 0true in");
    }
    catch(Exception e)
    {}

  }// GJKRandomTest constructor

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

    for( int k=0; k<list.size(); k++ )
      list.get(k).draw();

  }// display

  public static Triple zeroVec = new Triple(0,0,0);
  public static Triple zAxis = new Triple(0,0,1);

  public void update()
  {
    // make a somewhat random body, check it against all existing ones by gjk
    // until find an overlap and ditch it, otherwise add to the list

    Body b = new Body( modelVerts, standardColor );
    Triple randomCenter = new Triple( rand( 5, 95 ), rand( 5, 95 ), -2 );
    Quat randomOri = new Quat( rand( 0, 90 ), zAxis );
//    Quat randomOri = new Quat( 0, zAxis );
    b.init( randomCenter, randomOri, zeroVec, 0 );

    b.computeAtElapsedTime( 0 );

    // for each body in list, see if b hits it, and if it does,
    // stop trying to add b
    boolean hit = false;
    for( int k=0; k<list.size() && !hit; k++ )
    {
      GJKResult r = GJK.gjk( new Triple( 1, 1, 0 ), 
                                   list.get(k).getActualVertices(),
                                   b.getActualVertices() );
      if( r.v.norm() <= Constants.collTolHigh )
      {
        hit = true;
        out.println("couldn't add with " + list.size() + " bodies already");
      }
    }

    if( !hit )
      list.add( b );

  }// update

  public static Random rng = new Random(1);

  // make random double uniformly distributed in [x,y]
  // Must have 0 < x < y
  private double rand( double x, double y )
  {
    return x + rng.nextDouble()*(y-x);
  }

  private long keyTime;
  private final static long reqDelay = 30000000;  // 1/10th second

  // simple handling of key state so that user has to
  // press and release a key to count as a press
  public void processInputs()
  {
    Keyboard.poll();

    long currentTime = System.nanoTime();

    if( Keyboard.isKeyDown( Keyboard.KEY_Q ) )
    {
      out.close();
      System.exit(0);
    }

  }// processInputs

}
