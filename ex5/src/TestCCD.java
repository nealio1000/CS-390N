/*  
   test CCD with two bodies,
   obtained sequentially from a given data file,
   showing action in 3 cameras with ortho views
*/

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class TestCCD extends Basic
{
  public static void main(String[] args)
  {
    if( args.length == 0 )
    {
      System.out.println("Usage:  j TestCCD <test file name>");
      System.exit(1);
    }
    TestCCD app = new TestCCD( args[0] );
    app.start();
  }

  private static double size = 10;

  // instance variables:

  private double timeStep;
  private SeparationTracker tracker;
  private boolean keepMoving;

  private Camera camera1, camera2, camera3, camera4;
  private boolean separationViewing;
  private double sepViewAngle;
  private double sepViewDist;

  private ArrayList<Body> list;
  private int currentPair;

  public TestCCD( String fileName )
  {
    super( "Test CCD with 2 bodies",
             1200, 600, 33333333L );

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
//    camera3 = new Camera( null, "ortho",
//             new Triple(50,50,200), new Triple(50,50,50), new Triple(0,1,0),
//                  50,
//                 100, 300,
//                1150, 50, 500, 500,
//                RGB.WHITE );

    // looking along plane of separation
    camera4 = new Camera( null, "persp",
             new Triple(50,-100,50), new Triple(50,50,50), new Triple(0,0,1),
                  5,
                 10, 1000,
                50, 50, 500, 500,
                RGB.WHITE );
    separationViewing = false;
    sepViewAngle = 0;
    sepViewDist = 50;

    // gravity---each body has its own

    list = new ArrayList<Body>();

    // read all the bodies

    try{
      Scanner input = new Scanner( new File( fileName ) );

      int num = input.nextInt();  // number of bodies
System.out.println("number of bodies is " + num );
      input.nextLine();

      for( int k=0; k<num; k++ )
      {// read body k
System.out.println("start reading body # " + k );
        Body b = new Body( input );
        list.add( b );
System.out.println("finished reading body # " + k );
      }

      System.out.println("read " + list.size() + " bodies from file");
      input.close();

    }
    catch(Exception e)
    {
      System.out.println("problem loading the test file [" + fileName + "]" );
      System.exit(1);
    }

    tracker = new SeparationTracker( list.get(currentPair), list.get(currentPair+1),
                    list.get(currentPair).getActualVertex(0).vectorTo(
                    list.get(currentPair+1).getActualVertex(0) ) );

    timeStep = getTimeStep();
    keepMoving = true;

    currentPair = 0;

  }// TestCCD constructor

  public void init()
  {
    // set background color once and for all
    glClearColor( 0.5f, 0.5f, 0.5f, 0.0f );

    // enable depth testing
    glEnable( GL_DEPTH_TEST );

  }

  public void display()
  {
    // clear the screen to background color and clear the depth buffer:
    glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );

    // System.out.println("Step " + getStepNumber() + " with bodies " +
    //                      currentPair + " and " + (currentPair+1) );

    if( separationViewing )
    {
      tracker.setView( camera4, sepViewAngle, sepViewDist );
      camera4.activate();
      scene();
    }

    else
    {// 3 views
      camera1.activate();
      scene();

      camera2.activate();
      scene();

//      camera3.activate();
    //  scene();
    }

  }// display

  private void scene()
  {
    list.get( currentPair ).draw();
    list.get( currentPair+1 ).draw();
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
          try {
              String explosionFile = "explosion.wav";
              InputStream in = new FileInputStream(explosionFile);

              // create an audiostream from the inputstream
              AudioStream audioStream = new AudioStream(in);

              // play the audio clip with the audioplayer class
              AudioPlayer.player.start(audioStream);
          }
          catch(IOException e){
              System.out.println(e);
          }
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

  }// update

  public void processInputs()
  {
    while( InputInfo.size() > 0 )
    {
      InputInfo info = InputInfo.get();

      if( info.kind == 'k' && (info.action == GLFW_PRESS || info.action == GLFW_REPEAT) )
      {
        int code = info.code;

        if( code == GLFW_KEY_ESCAPE )
        {// start next test

          currentPair += 2;
          System.out.println("------------ another test, current = " + currentPair + " ------");
          if( currentPair+1 > list.size()-1 )
            System.exit(0);
          tracker = new SeparationTracker( list.get(currentPair),
                                           list.get(currentPair+1),
              list.get(currentPair).getActualVertex(0).vectorTo(
              list.get(currentPair+1).getActualVertex(0) ) );

          keepMoving = true;
          restartStepNumbering();
        }

        else if( code == GLFW_KEY_SPACE )
        {// pause/restart
          keepMoving = !keepMoving;
          System.out.println( "*******************************" + getStepNumber() );
        }

        else if( code == GLFW_KEY_V )
        {// toggle between 3 views and separation viewing
          separationViewing = !separationViewing;
        }
        else if( code == GLFW_KEY_R )
        {// rotate around viewing circle
          sepViewAngle += 5;
          if( sepViewAngle > 360 )
            sepViewAngle -= 360;
        }
        else if( code == GLFW_KEY_F )
        {// make distance farther
          sepViewDist += 5;
        }
        else if( code == GLFW_KEY_N )
        {// make distance nearer
          sepViewDist -= 5;
        }
      }// input event is a key

//      else if ( info.kind == 'm' )
//      {// mouse moved
//        System.out.println( info );
//      }

      else if( info.kind == 'b' )
      {// button action
        System.out.println( info );
      }

    }// loop to process all waiting input events

  }// processInputs

}
