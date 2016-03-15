/*  a camera is mounted in the world or on a block,
    maintains viewing setup,
    including pixel grid rectangle to map to
    and frustum info
*/

import java.awt.*;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;

public class Camera 
{
  private static int nextId = 0;
  public final int id;

  private RGB backColor;

  // view setup information

  private Block owner;  // the block this camera is attached to,
                        // or null if this camera is sitting in the world
                        // not attached to any block

  private String type;  // "ortho" or "persp"

  // either in model coords of body of owner, 
  // or in world coords,
  // set up view transformation
  private Triple eye;   // camera location
  private Triple center;  // center point of view rectangle
  private Triple up;    // up direction
                        
  // set up view volume
//  private double fovy;  // vertical angle of view frustum
//  private double aspect;  // width of view rectangle / height
  private double size;
  private double near;  // distance from eye to view rectangle
  private double far;  // distance from eye to far clipping plane

  private int pl, pb, pw, ph;  // pixel values of pixel grid rectangle in 
                               // the entire window

  // build a camera with specified viewing setup
  public Camera( Block ownerIn, String typeIn,
                 Triple eyeIn, Triple centerIn, Triple upIn,
                 double sizeIn,
                 double nearIn, double farIn,
                 int l, int b, int w, int h,   // pixel grid region
                 RGB backColorIn
               )
  {
    ++nextId;  id = nextId;

    owner = ownerIn;
    type = typeIn;

    eye = eyeIn;  center = centerIn;  up = upIn;
//    fovy = fovyIn;  aspect = aspectIn;  
    size = sizeIn;
    near = nearIn;  far = farIn;
    pl = l;  pb = b;  pw = w;  ph = h;
    backColor = backColorIn;
  }

  public void activate()
  {
    // specify part of pixel grid that this camera owns:
    glViewport( pl, pb, pw, ph );

    // draw the background rectangle with simple setup:
    glMatrixMode( GL_PROJECTION );
    glLoadIdentity();
    glOrtho( 0, 1, 0, 1, 1, 3 );
    glMatrixMode( GL_MODELVIEW );
    glLoadIdentity();
    Lib.setColor( backColor );
    glBegin( GL_POLYGON );
      glVertex3d( 0, 0, -2 );
      glVertex3d( 1, 0, -2 );
      glVertex3d( 1, 1, -2 );
      glVertex3d( 0, 1, -2 );
    glEnd();

    glClear( GL_DEPTH_BUFFER_BIT ); // ignore background fragments

    // now set up for drawing blocks:

    glMatrixMode( GL_PROJECTION );
    glLoadIdentity();
    if( type.equals( "persp" ) )
      glFrustum( -size, size, -size, size, near, far );
    else if( type.equals( "ortho" ) )
      glOrtho( -size, size, -size, size, near, far );
    else
    {
      System.out.println("unknown camera type [" + type + "]" );
      System.exit(1);
    }
    
    glMatrixMode( GL_MODELVIEW );
    glLoadIdentity();

    if( owner == null )
    {
      Lib.lookAt( eye, center, up );
    }
    else
    {
      System.out.println("need to use owner's info to set gluLookAt");
      System.out.println("but not implemented yet");
      System.exit(1);
    }

  }// activate

  public void setView( Triple e, Triple c, Triple u )
  {
    eye = e;  center = c;  up = u;
  }

}// Camera
