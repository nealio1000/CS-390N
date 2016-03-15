/*  Put various useful methods here that
    don't fit in some specific class
*/

import java.nio.DoubleBuffer;
import org.lwjgl.BufferUtils;  // for making DoubleBuffer

import static org.lwjgl.opengl.GL11.*;

import java.util.Scanner;

public class Lib
{
  public static Scanner keys = new Scanner( System.in );

  public static void setColor( RGB color )
  {
    glColor3f( color.red/255f, color.green/255f, color.blue/255f );   
  }

  // send a Triple to OpenGL as a vertex
  public static void vertex( Triple p )
  {
    glVertex3d( p.x, p.y, p.z );
  }

  // gluLookAt (compute the 4 by 4 matrix here and glMultMatrix it)
  public static void lookAt( Triple eye, Triple center, Triple up )
  {
    Triple w = eye.vectorTo( center ).normalized();
    Triple u = up.normalized();
    Triple s = w.crossProduct( u );  // s will be length 1 since w and u are
    Triple r = s.crossProduct( w );  // similarly, r of length 1

    // now compute the desired 4 by 4 matrix as 16 values using column major order
    // and transfer a DoubleBuffer
    DoubleBuffer db = BufferUtils.createDoubleBuffer( 16 );

    db.put( s.x );  db.put( r.x );  db.put ( -w.x ); db.put( 0.0 );  // column 1
    db.put( s.y );  db.put( r.y );  db.put ( -w.y ); db.put( 0.0 );  // column 2
    db.put( s.z );  db.put( r.z );  db.put ( -w.z ); db.put( 0.0 );  // column 3
    db.put(-s.dotProduct(eye)); db.put(-r.dotProduct(eye)); db.put(w.dotProduct(eye)); db.put(1.0); // col 4

    db.rewind();

    glMultMatrix( db );
    
  }

}
