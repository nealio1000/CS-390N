/*  Wrapper to provide slightly easier access
    to the LWJGL Keyboard class

    If user foolishly presses too many keys at
    one time, results are unpredictable, and this
    class makes no attempt to warn or deal with
    this except to halt if tries to add too many
    down keys or finds a key to be released that
    is not down

    Physical keys on the keyboard use int codes
    like  Keyboard.KEY_A  for the "a" key, and
    so on

    In the "processInputs" method of Basic, 
    call Keys.update() first to notice all
    physical key events, then loop through
    with Keys.hasNext() and Keys.next()
    to get key codes of all pending key pressed
    events.

    If "shifting" is enabled,
    this class adds 1000 to the code if either
    shift key is down when it is pressed,
    and adds 2000 to the code if either control
    key is down when it is pressed
    (except, of course, for the shift and control
     keys, which are ignored as ordinary keys)

    When shifting is disabled, the several shift
    and control keys generate key pressed events

    When "repeat" is enabled, as long as a key is
    pressed and not released, a key pressed event
    is generated every so often
*/

import org.lwjgl.input.Keyboard;

import java.util.ArrayList;

public class Keys
{

  // make sure Keyboard is not trying to do
  // repeat events, and is "created" 
  // (whatever that means)
  static 
  {
    try{
      Keyboard.create();
      Keyboard.enableRepeatEvents(false);
    }
    catch(Exception e)
    {
      System.out.println("could not setup Keyboard");
      System.exit(1);
    }
  }

  // transfer all pending Keyboard events to
  // simulated key events
  public static void update()
  {
    // process all physical key events
    while( Keyboard.next() )
    {
      int code = Keyboard.getEventKey();
 
      if( Keyboard.getEventKeyState() )
      {// event is that the key was pressed
        // add this as a down key
        if( numDown <= MAX )
        {// have space to add another down key

          // store the code and time issued
          downKeys[numDown] = code;
          timeGen[numDown] = System.nanoTime();
          numDown++;

          // issue a key pressed event
          // taking shifting into account
 
          if( !shifting )
          {// shift keys are just like anybody else
            events.add( code );
          }
          else
          {// shifting
            if( !isShift(code) )
              events.add( code + haveShift() + haveControl() );            
          }
        }// have space to add another down key
        else
        {// user holding too many down at once
          System.out.println("You can only have " + MAX +
           " keys pressed at one time");
          System.exit(1);
        }// user holding too many down at once
      }
      else
      {// event is that the key was released
        // remove the key from list
        int j = -1;
        for( int k=0; k<numDown; k++ )
          if( downKeys[k] == code )
            j=k;
        if( j == -1 )
        {// key was released but is not in list?
          System.out.println("Somehow key " + code +
              " was released but not listed as down?");
          System.exit(1);
        }
        else
        {// remove item j from list
          // swap last guy into earlier spot for O(1) removal
          downKeys[ j ] = downKeys[ numDown-1 ];
          timeGen[ j ] = timeGen[ numDown-1 ];
          numDown--;
        }
      }
    }// loop to process all waiting Keyboard events

    // now generate repeat key pressed events for
    // all keys that are down, depending on when
    // last sent and current time and whether repeating
    if( repeating )
    {// see if any down keys haven't been issued for long enough
      for( int k=0; k<numDown; k++ )
      {
        long currentTime = System.nanoTime();
        if( timeGen[k] + waitTime <= currentTime )
        {// issue pressed event again
          if( !shifting )
          {// shift keys are just like anybody else
            events.add( downKeys[k] );
            timeGen[k] = currentTime;
          }
          else
          {// shifting
            if( !isShift(downKeys[k]) )
            {
              events.add( downKeys[k] + haveShift() + haveControl() );
              timeGen[k] = currentTime;
            }
          }
        }// has waited long enough
      }// go through all down keys
    }// repeating 

  }// update

  // is there another simulated key pressed
  // event?
  public static boolean hasNext()
  {
    return events.size() > 0;
  }
  
  // consume the simulated key pressed event
  // and return its code
  public static int next()
  {
    int key = events.get(0);
    events.remove(0);
    return key;
  }

  public static void enableShifting( boolean value )
  {
    shifting = value;
  }

  public static void enableRepeating( boolean value )
  {
    repeating = value;
  }

  public static void setRepeatDelay( long t )
  {
    waitTime = t;
  }

  private static final int MAX = 6;  // max # of keys that can be down at one time
  private static int numDown = 0;  // # of keys currently down
  private static int[] downKeys = new int[MAX]; // key codes of down keys
  private static long[] timeGen = new long[MAX];  // time last issued

  // simulated key pressed events
  private static ArrayList<Integer> events = new ArrayList<Integer>();

  private static long waitTime = 33333333;

  private static boolean shifting = false;
  private static boolean repeating = false;

  private static boolean isShift( int code )
  {
    return code==Keyboard.KEY_LSHIFT ||
           code==Keyboard.KEY_RSHIFT ||
           code==Keyboard.KEY_LCONTROL ||
           code==Keyboard.KEY_RCONTROL;
  }

  // return 1000 if a shift key is down
  // or 0 if not
  private static int haveShift()
  {
    for( int k=0; k<numDown; k++ )
      if( downKeys[k] == Keyboard.KEY_LSHIFT ||
          downKeys[k] == Keyboard.KEY_RSHIFT )
       return 1000;
    return 0;
  }

  // return 2000 if a control key is down
  // or 0 if not
  private static int haveControl()
  {
    for( int k=0; k<numDown; k++ )
      if( downKeys[k] == Keyboard.KEY_LCONTROL ||
          downKeys[k] == Keyboard.KEY_RCONTROL )
       return 2000;
    return 0;
  }

  public static void main(String[] args)
  {
  }
}
