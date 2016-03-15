/* a separation tracker keeps track of a separating axis rigidly
   mounted on the second body (known as "B" in notes and thoughts),
   advances time until the closest point a1 on first body (A)
   gets within collision envelope along n, or some ak adjacent to a1
   gets closer along n than a1, in which case it replaces a1 in that
   role.

   When the gap closes (a1 gets close along n), use GJK to get a new
   separating axis or realize have contact.

  Note that the "first" body is what I call A in all the theory and
  pictures, while "second" body is B.  As two bodies approach each
  other, which one is "first" and which is "second" can change

  This is July 30, 2014 version which realizes a lot of the earlier
  work was kind of stupid!

  Unlike earlier versions, there is only one kind of separation:
  a body-fixed vector n from b1 separating from a1 closest

*/

import java.util.ArrayList;
import java.io.PrintWriter;

public class SeparationTracker
{
  private Body first, second;
  private double elapsedTime;  // current elapsed time from start of step

  // indices of special vertex for each body
  private int ia1, ib1;
  // indices of ak's adjacent to a1
  private int[] adj;
  // offset vector so that n = b1 - b0 in body coords
  private Triple b0;
  private Triple b0Actual;  // mostly for debug output
  private Triple b1Actual;  // mostly for debug output

  // remember kind of contact for picking up later, if this pair
  // is tied for earliest and have to do physics
  private String contactKind;

  private boolean haveContact;  // note when have contact

  // build separation
  // between the two given bodies
  // or realize they overlap,
  // given starting guess for GJK
  public SeparationTracker( Body one, Body two, Triple start )
  {
    one.computeAtElapsedTime(0);
    two.computeAtElapsedTime(0);
    initSeparationByGJK( start, one, two );
  }

  // use GJK with starting guess given by a1-b1 in bodies one and two
  private void initSeparationByGJK( int ia, int ib, Body one, Body two )
  {
    Triple aActual = one.getActualVertex( ia );
    Triple bActual = two.getActualVertex( ib );
    initSeparationByGJK( aActual.vectorTo( bActual ), one, two );
  }

  // use GJK (with start as initial v) to get initial separation
  // Bodies one and two must have had computeAtElapsedTime called
  private void initSeparationByGJK( Triple start, Body one, Body two )
  {

    System.out.println("Creating initial separation by GJK-------------");

    ArrayList<Triple> firstVerts, secondVerts;
    firstVerts = one.getActualVertices();
    secondVerts = two.getActualVertices();

    GJKResult result = GJK.gjk( start, one, two );

    // set up separation depending on result:

    haveContact = false;

    if( result.distance() <  Constants.collTolHigh )
    {// bodies are within collision envelope
      haveContact = true;
    }
    else if( result.n == 4 )
    {// must be touching
      System.out.println("gjk gave impossible result: " + result );
      System.exit(1);
    }
    else
    {// not touching, can pretty much use any reasonable choices for a1, b1
     // so use some heuristics

     // depending on results of GJK, set first, second, ia1, ib1
     // appropriately, and note whether switched,
     // then build adj, b0 uniformly

     boolean switched = false;  // first is one, second is two if not switched

     // count # of distinct points occurring in W for one, two
     int oneCount = result.countNumberDistinct( true );
     int twoCount = result.countNumberDistinct( false );
   
     if( (oneCount == twoCount && 
            Math.abs( one.getSpinRate() ) >= Math.abs( two.getSpinRate() ) )
         ||
         twoCount > oneCount
       )
     {// second is two
       first = one;  second = two;
       ia1 = result.a.a;
       ib1 = result.a.b;
     }
     else
     {// second is one
       first = two;  second = one;       switched = true;
       ia1 = result.a.b;
       ib1 = result.a.a;
     }

System.out.println("ia1: " + ia1 + " " + " ib1: " + ib1 + " switched: " +
                    switched );

System.out.println("a1 actual is " + first.getActualVertex( ia1 ) );

     // result.v is the separating axis, pointing from two (B) to one (A),
     // set remaining separation variables:

     adj = first.getAdjVerticesIndices( ia1 );
     System.out.println("vers adj to a1: " );
     for( int k=0; k<adj.length; k++ ) 
       System.out.print( adj[k] + " " );
     System.out.println();

     Triple b1Actual = second.getActualVertex( ib1 );
     System.out.println("b1Actual: " + b1Actual );
     Triple n = new Triple( result.v );
     n = n.normalized();
   
     // roles of one and two have switched (GJK produces v pointing from
     // two to one, if switched, want v pointing from one to two)
     if( switched )
       n = n.scalarProduct( -1 );

     System.out.println("separating axis (n) = " + n );

     // b0Actual is point in world coords such that vector
     // n is b1Actual-b0Actual:
     b0Actual = n.vectorTo( b1Actual );
     System.out.println("b0Actual: " + b0Actual );
     // store as instance variable
     b0 = second.worldToModel( b0Actual );
     System.out.println("b0: " + b0);
     Triple check = second.modelToWorld( b0 );
     System.out.println("this should be b0Actual: " + check );

    }// not touching
   
    System.out.println( "Initial separation: " + toString() );

  }// initSeparationByGJK

  // show essentials of this separation tracker
  public String toString()
  {
    return "base body is " + second.getId() + " with origin b1: " + ib1 +
           " other body has closest vertex a1: " + ia1;
  }

  // a condition has been violated so we need to note the
  // new value of ia1 and update adj
  public double[] transition( int ia1New )
  {
    ia1=ia1New;

    adj = first.getAdjVerticesIndices( ia1 );

    System.out.println("transition to " + this.toString() );

    return computeValues( elapsedTime );
    
  }
              
  // compute elapsed time, up to max, when touch
  // (actually until they are within collision envelope and
  //  getting closer leading to interpenetration),
  // where if they don't touch before elapsed time max,
  // return  max
  public double computeTimeUntilTouch( double max )
  {
System.out.println("New step, advance time until " + max + " with separation: " + toString() );

    elapsedTime = 0;
    double[] currentValues;

    do{// outer loop to repeatedly advance time

      // advance time until gap closes or condition violated or out of time
      String reasonStopped = advanceTime( max );

      if( reasonStopped.equals( "full step" ) )
      {// ran out of time
        System.out.println("         reached max with no changes");
      }

      else if( reasonStopped.equals( "gap closing" ) )
      {// gap closing---either detect contact or get new separation by GJK

        System.out.println("         gap closing at elapsed time " + elapsedTime );

        initSeparationByGJK( ia1, ib1, first, second );
       
      }// gap closing
      else
      {// for most violated condition do transition
       // (since not explicitly handling degeneracy, we only
       //  use one violating vertex at a time)

        System.out.println(
            "         condition violated at elapsed time " + elapsedTime );

        // do a little extra work for simplicity 
        // (can't easily get these values from advanceTime)

        double[] newValues = computeValues( elapsedTime );

        // find the most violating guy and use it
        int worst=-1;
        double worstViolation = 0;

        System.out.println("gap value is " + newValues[0] );

        System.out.println("searching for worst violator:");
        for( int k=1; k<newValues.length; k++ )
        {
          System.out.println("k: " + k + " value: " + newValues[k] );
          if( newValues[k] < worstViolation )
          {
            worst = k;
            worstViolation = newValues[k];
          }
        }

        if( worstViolation < 0 )
        {// found one so transition depending on the current kind of separation
          // note shift:  f_k has position k in values, is adj[k-1]
          newValues = transition( adj[worst-1] );
        }
        else
        {
          System.out.println("no violation in computeTimeUntilTouch");
          System.exit(1);
        }

      }// for most violated condition do transition
        
    }while( elapsedTime < max && !haveContact );
    // end of outer loop to repeatedly advance time

    return elapsedTime;  // is instance variable, acting like accessor to outside world

  }// computeTimeUntilTouch

  // compute the values of the gap and all the conditions at elapsed time
  // theta
  public double[] computeValues( double theta )
  {
    // tell bodies to compute and store centers and
    // rotation matrices for
    // elapsed time of theta from current time
    first.computeAtElapsedTime( theta );
    second.computeAtElapsedTime( theta );

    Triple a1Actual = first.getActualVertex( ia1 );
    Triple b1Actual = second.getActualVertex( ib1 );
    b0Actual = second.modelToWorld( b0 );

    Triple n = b0Actual.vectorTo( b1Actual );

    double[] vals = new double[ adj.length + 1 ];
 
    double nDotA1 = n.dotProduct( a1Actual );

    // compute gap function
    vals[0] = nDotA1 - n.dotProduct( b1Actual );

    for( int k=0; k<adj.length; k++ )
    {
      Triple akActual = first.getActualVertex( adj[k] );
      vals[k+1] = n.dotProduct( akActual ) - nDotA1;
    }

    return vals;

  }// computeValues

  // allow outside world to realize contact has been found
  public boolean foundContact()
  {
    return haveContact;
  }

  private void trace( String message, double t, double[] array )
  {
    System.out.print( message );
    System.out.printf("fk's( %12.7f ): ", t );
    
    for( int k=0; k<array.length; k++ )
      System.out.printf("%12.7g ", array[k] );
    System.out.println();
  }

  private void trace( String message, double[] array )
  {
    System.out.print( message + " " );
    
    for( int k=0; k<array.length; k++ )
      System.out.printf("%12.7g ", array[k] );
    System.out.println();
  }

  private void trace( String message, double[] topArray, double[] botArray )
  {
    System.out.print( message + " " );
    
    for( int k=0; k<topArray.length; k++ )
      System.out.printf("%12.7g ", topArray[k] / botArray[k] );
    System.out.println();
  }

  // advance time as far as possible, from the current elapsedTime,
  // up to and including max,
  // until gap closes, a condition is violated, or
  // time runs out, and return time when one of these happens,
  // leaving both bodies computed at that time
  //
  // Use finite difference Newton's method with trust region, modified
  // somewhat to try to find first root from start time
  public String advanceTime( double max )
  {
    // at start, both bodies have been computed at the current
    // elapsed time and conditions have been set up for current
    // separation

    double x = elapsedTime;

    double delta = Math.max( Constants.initTrustRadiusFraction * max,
                             Constants.minInitTrustRadius );

    // believe first root is in [left,right], maintain these throughout
    double left = x;
    double right = max;

    double[] f = computeValues( x );
    double[] fp = computeDerivs( x, f );

    trace("-------Start of advanceTime ", x, f );
    trace("                     derivs ", x, fp );

    // if gap is small and closing, done
    // (can happen if had smallish gap and transitioned,
    //  leading to small enough gap with closing)

    if( Constants.collTolLow <= f[0] && f[0] < Constants.collTolHigh &&
        fp[0] < 0 )
    {// gap small and closing, so done
      return "gap closing";
    }

    // check (as a matter of debugging, really)
    // that f is valid at initial time
    // (all fk are above their target interval, or
    //  if in the target interval, have positive derivative)

    boolean validStart = true;
    for( int k=0; k<f.length; k++ )
    {
      if( (k==0 && 
                   (f[k]<Constants.collTolLow || f[k]<Constants.collTolHigh && fp[k]<=0)
          )
           ||
         (k>0 && 
                   (f[k]<-Constants.collTolHigh || f[k]<-Constants.collTolLow && fp[k]<=0)
          )
        )
      {
        System.out.println(
        "SeparationTracker.advanceTime couldn't start due to function " + k );
        validStart = false;
      }
    }

    if( !validStart )
    {// error---somehow being called incorrectly
      System.out.println("error in SeparationTracker.advanceTime at initial time " + x );
      System.exit(1);
    }

    boolean done = false;
    double xplus;
    double[] fplus, fplusp;

    while( !done )
    {

System.out.println("    start outer loop, time: " + x );

      if( x >= max )
      {// have reached the end of time
System.out.println("reached max time");
        elapsedTime = max;
        return "full step";
      }

      // take step---inner loop to find accurate enough step
      // to valid time, or realize have hit target interval 
      boolean goodStep = false;

      do{ 
        // compute trial step xplus
        
System.out.println("        INNER LOOP top, delta = " + delta + " stay in [" + left + ","
                          + right + "]" );

        xplus = Math.min( x+delta, right );  // move here unless some fk wants to do
                          // shorter positive newton step or even
                          // a backward newton step
        double rightLimit = xplus;
        String stepType = "advance to limit"; 
        int specialK = -1;  // the fk, if any, that forced "midpoint" type step

System.out.println("            advance to limit step is: " + xplus );

        double newt, midpt;

        for( int k=0; k<f.length; k++ )
        {// update xplus due to fk
         
System.out.print("              f_" + k + " wants step to ");

          if( (k==0 && f[k]>Constants.collTolHigh) // safely away from target
              ||
              (k==0 && f[k]>= Constants.collTolLow && fp[k]>=0) // in target but separating
              ||
              (k>0 && f[k]> -Constants.collTolLow ) // safely away from target
            )
          {// fk is above top or f0 is in target interval but separating
            if( fp[k] < 0 )
            {// above target and going down, might use newton step

              if( k==0 )
                newt = x - (f[k]-Constants.collTol)/fp[k];
              else
                newt = x - (f[k]+Constants.collTol)/fp[k];

System.out.println( newt + " (advancing newt step)");

              if( newt < xplus )
              {
                xplus = newt;
                stepType = "advancing newt";
              }
            }
            else
            {// above target and going up, so go to right limit

              // leave xk at xplus
System.out.println( rightLimit + " (f,f' >= 0 so go to right limit");

            }// above target and going up, so go to right limit
            
          }// fk is above top or f0 is in target interval but separating
          else if( (k==0 && f[k] >= Constants.collTolLow)  
                   ||
                   (k>0 && f[k] >= -Constants.collTolHigh)
                 )
          {// fk is above bottom (hence between bottom and top)
           // so realize have reached target

System.out.println("-------- f_" + k + " in target interval" );

            elapsedTime = x;

            if( k==0 )
              return "gap closing";
            else
              return "condition violated";
   
          }// fk is above bottom (hence between bottom and top)
          else
          {// fk is below bottom
            
            if( fp[k] >= 0 )
            {// fk is below, going up, definitely need to do midpoint
              midpt = (left+right)/2;
 
System.out.println( midpt + " (below bottom, f'>=0, wants midpoint");

              if( midpt < xplus )
              {
                xplus = midpt;
                stepType = "midpoint";
              }
            }
            else
            {// fk' negative, move back to newt or midpoint
              if( k==0 )
                newt = x - (f[k]-Constants.collTol)/fp[k];
              else
                newt = x - (f[k]+Constants.collTol)/fp[k];

              if( newt > left )
              {
System.out.println( newt + " (below bottom, f'<0, newt step not too far back");
                if( newt < xplus )
                {
                  xplus = newt;
                  stepType = "retreating newt";
                }
              }
              else
              {// retreating newt too far back, try midpoint
                midpt = (left+right)/2;

System.out.println( midpt + " (midpt---newt retreats too far");

                if( midpt < xplus )
                {
                  xplus = midpt;
                  stepType = "midpoint";
                }
              }
            }
                        
          }// fk is below bottom
 
        }// update xplus due to fk

System.out.println("          winning xplus is " + xplus + " (" + stepType + ")" );

        // see if xplus has good accuracy

        fplus = computeValues( xplus );
trace("       trial xplus ", xplus, fplus );

        fplusp = computeDerivs( xplus, fplus );
trace("           derivs: ", xplus, fplusp );

        // update things depending on the step type

        // no matter what's going on (whether xplus has enough accuracy or is
        // midpoint or whatever), if any fplus_k is below bottom, update right
        // because we've discovered a time we can't go to
        for( int k=0; k<f.length; k++ )
        {
          if( ( k==0 && fplus[k] < Constants.collTolLow ) 
              ||
              ( k>0 && fplus[k] < -Constants.collTolHigh )
            )
            right = Math.min( right, xplus );
        }

        if( stepType.equals( "midpoint" ) )
        {// doing bisection step, so accuracy unchanged, not required to do step

          goodStep = true;
          x = xplus;
          f = fplus;
          fp = fplusp;

          if( (specialK==0 && fplus[ specialK ] > Constants.collTolHigh)
              ||
              (specialK>0 && fplus[ specialK ] > -Constants.collTolLow)
            )
          {// advance left to midpoint
            left = xplus;
          }
          else if( (specialK==0 && fplus[ specialK ] < Constants.collTolLow)
                   ||
                   (specialK>0 && fplus[ specialK ] < -Constants.collTolHigh)
                 )
          {// retreat right to midpoint
            right = xplus;
          }
          // else f[specialK] is in target interval, will exit at top of loop

        }
        else
        {// didn't go to midpoint so need accuracy check

          double accuracy = 0;
          for( int k=0; k<f.length; k++ )
          {
            accuracy = Math.max( accuracy, Math.abs( fplus[k]-f[k]-fp[k]*(xplus-x) ) );
          }

System.out.println("accuracy: " + accuracy + " delta: " + delta );

          if( accuracy < Constants.acceptableAccuracy )
          {// all linear models match actual values at xplus closely enough
         
            if( stepType.equals( "advance to limit" ) )
              left = xplus;

System.out.println("accept xplus= " + xplus );

             goodStep = true;
             x = xplus;
             f = fplus;
             fp = fplusp;

             // see if delta should be increased
             if( accuracy < Constants.goodAccuracy )
               delta = 2*delta;

          }// linear model matches actual
          else
          {// linear model and actual value differ too much, shrink trust region
           // and try again
            delta = 0.5*delta;
System.out.println("TRUST REGION SHRINKING");
          }

        }// didn't go to midpoint so need accuracy check

      }while( !goodStep );

    }// until done

    return "error";

  }// advanceTime

  private boolean member( double x, double left, double right )
  {
    return left<=x && x<=right;
  }

  // compute and return finite difference derivative of family
  // of functions at x, using already computed function values f(x)
  private double[] computeDerivs( double x, double[] f )
  {
    double[] feps = computeValues( x+Constants.fdeps );
    double[] fp = new double[ f.length ];

    for( int k=0; k<f.length; k++ )
      fp[k] = ( feps[k] - f[k] ) / Constants.fdeps;

    return fp;
  }

  // setup viewing for the giving camera so as to
  // look from eye point on unit circle centered at b1Actual
  // in the plane with normal vector b1Actual-b0Actual,
  public void setView( Camera cam, double angle, double distance )
  {
    Triple b1Actual = second.getActualVertex( ib1 );
System.out.println("b1: " + b1Actual );
    Triple b0Actual = second.modelToWorld( b0 );
System.out.println("b0: " + b0Actual );
    Triple n = b0Actual.vectorTo( b1Actual ).normalized();
System.out.println("n: " + n );

    // do Householder transformation to get vectors in plane
    // normal to n (see yellow page 413)

    double s = n.x >= 0 ? 1 : -1;   // s = sign(n1)
    double d = -(1+s*n.x);
    Triple h2 = new Triple( (n.x*n.y + s*n.y)/d,
                            (n.y*n.y)/d + 1,
                            (n.y*n.z)/d 
                          );
System.out.println("h2: " + h2 );
    Triple h3 = new Triple( (n.x*n.z + s*n.z)/d,
                            (n.y*n.z)/d,
                            (n.z*n.z)/d + 1
                          );
System.out.println("h3: " + h3 );
    System.out.println("h2.h3 = " + h2.dotProduct(h3) +
                       " n.h2 = " + n.dotProduct(h2) +
                       " n.h3 = " + n.dotProduct(h3) +
                       " h2.h2 = " + h2.dotProduct(h2) +
                       " h3.h3 = " + h3.dotProduct(h3) 
                      );

    double ang = Math.toRadians( angle );
    double cos = distance*Math.cos( ang );
    double sin = distance*Math.sin( ang );

    Triple h = (h2.scalarProduct( cos )).add( h3.scalarProduct( sin ) );
    System.out.println("h: " + h + " h.h= " + h.dotProduct(h) );
    Triple eye = b1Actual.add( h );

    Triple up = (h2.scalarProduct( -sin )).add( h3.scalarProduct( cos ) );

    System.out.println("View separation from eye: " + eye +
       " looking at b1: " + b1Actual + " with up: " + up );
    cam.setView( eye, b1Actual, up );
  }

}// SeparationTracker
