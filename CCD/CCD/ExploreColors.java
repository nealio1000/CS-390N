/*  
   Try out Body.faceColors
*/

import java.util.*;
import java.io.*;

import org.lwjgl.input.Keyboard;

import static org.lwjgl.opengl.GL11.*;

public class ExploreColors extends Basic
{
  public static void main(String[] args)
  {
    ExploreColors app = new ExploreColors();
    app.start();
  }

  private static double size = 50;

  public ExploreColors()
  {
    super( "Explore Colors",
             600, 600, 30 );
  }// ExploreColors constructor

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

    glMatrixMode( GL_PROJECTION );
    glLoadIdentity();
    glOrtho( 0, 600, 0, 600, 1, 3 );

    glMatrixMode( GL_MODELVIEW );
    glLoadIdentity();

    int index = 0;
    double x=size, y=size;

    for( int k=1; k<=4; k++ )
    {
      for( int j=1; j<=5; j++ )
      {
        Lib.setColor( Body.faceColors[ index ] );
        glBegin(GL_POLYGON);
          glVertex3d( x, y, -2 );
          glVertex3d( x+size, y, -2 );
          glVertex3d( x+size, y+size, -2 );
          glVertex3d( x, y+size, -2 );
        glEnd();

        index++;
        x += 2*size;
      }
 
      y += 2*size;
      x = size;
    }
  
  }// display

}
