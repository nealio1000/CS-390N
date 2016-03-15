/*  the GJK class holds a bunch of information relating
    to performing a single GJK algorithm on two given bodies,
    with supporting methods

    The bodies must be non-degenerate 3D convex polyhedra,
    which are actually the convex hull of the given vertices
*/

import java.util.ArrayList;
import java.io.*;

public class GJK
{
  private static double tol1 = 0.0000001;

  private static Body bodyA, bodyB;  // these are the bodies we're working with
  private static ArrayList<Triple> vertsA, vertsB;  // current world coords vertices
                                             // of the two bodies
  private static int[][] adjA, adjB;  // vertex adjacency info for the bodies

  public static void setVertices( ArrayList<Triple> vA, ArrayList<Triple> vB )
  {
    vertsA = vA;  vertsB = vB;
  }

  // do GJK algorithm with given start direction
  // and the two bodies
  public static GJKResult gjk( Triple start, Body a, Body b )
  {
    bodyA = a;  bodyB = b;

    vertsA = a.getActualVertices();  adjA = a.getAllAdjInfo();
    vertsB = b.getActualVertices();  adjB = b.getAllAdjInfo();

    Triple v = start;

    ArrayList<IntPair> bigW = new ArrayList<IntPair>();
    ArrayList<IntPair> bigY = new ArrayList<IntPair>();

    IntPair w = null; 

    GJKResult result = null;
    boolean done = false;
    int itnCount = 0;
    
    do{
      itnCount++;
System.out.println("************* gjk iteration " + itnCount + " ********");
System.out.println("v = " + v + " and W = " + bigW );

      w = minDotProduct( v, w );  // s_{A-B}(-v)

      Triple wp = makePoint( w );
System.out.println("find support point " + w + " which is " + wp );

      double vTv = v.dotProduct( v );

      if( vTv - v.dotProduct( wp ) <= tol1 * vTv )
      {// v is close enough to closest point to origin of A-B
        done = true;
        System.out.println("exited gjk with optimal v = " + v );
        if( itnCount == 1 )
        {// v = wp, I believe, simple case
          bigW.add( w );
          return new GJKResult( bigW,
                                v, null, null, null,
                                w, null, null, null,
                                1, 0, 0, 0 );
        }
        else
          return result; // result from immediately preceding johnson work
      }
      else if( member( w, bigY ) )
      {// degenerate case, halt unhappy and return v as best approx
        done = true;
        System.out.println("Hey, degenerate GJK?");
        if( itnCount == 1 )
        {
          return new GJKResult( bigW,
                                v, null, null, null,
                                w, null, null, null,
                                1, 0, 0, 0 );
        }
        else
          return result;
      }

      bigY = append( w, bigW );  // just for the exit condition
                                 // next time around

      result = johnsonDistanceAlgorithm( bigY );

      testingDisplay( result );

      // transfer info from distance algorithm into local variables
      v = result.v;
      bigW = result.bigW;

      if( result.n == 4 || v.norm() < Constants.collTolHigh )
      {
        done = true;
        return result;
      }

      if( itnCount > 100 )
      {
        System.out.println("too many iterations in GJK");
        done = true;
      }

    }while( !done );

    return null;
   
  }// gjk

  // display lots of info about r
  private static void testingDisplay( GJKResult r )
  {
    System.out.println( r );

    if( r.n == 1 )
    {// show closest points a and b
      Triple a1p = vertsA.get( r.bigW.get(0).a );
      Triple b1p = vertsB.get( r.bigW.get(0).b );
      System.out.println("\nClosest points in bodies are a= " + a1p +
                         " and b= " + b1p );
    }

    else if( r.n == 2 )
    {// show closest points a and b
      Triple a1p = vertsA.get( r.bigW.get(0).a );
      Triple a2p = vertsA.get( r.bigW.get(1).a );
      Triple ap = Triple.convexComb( r.alpha, a1p, r.beta, a2p );
      Triple b1p = vertsB.get( r.bigW.get(0).b );
      Triple b2p = vertsB.get( r.bigW.get(1).b );
      Triple bp = Triple.convexComb( r.alpha, b1p, r.beta, b2p );
      System.out.println("\nClosest points in bodies are a= " + ap +
                         " and b= " + bp );
    }

    else if( r.n == 3 )
    {
      Triple a1p = vertsA.get( r.bigW.get(0).a );
      Triple a2p = vertsA.get( r.bigW.get(1).a );
      Triple a3p = vertsA.get( r.bigW.get(2).a );
      Triple ap = Triple.convexComb( r.alpha, a1p, r.beta, a2p, r.gamma, a3p );
      Triple b1p = vertsB.get( r.bigW.get(0).b );
      Triple b2p = vertsB.get( r.bigW.get(1).b );
      Triple b3p = vertsB.get( r.bigW.get(2).b );
      Triple bp = Triple.convexComb( r.alpha, b1p, r.beta, b2p, r.gamma, b3p );
      System.out.println("\nClosest points in bodies are a= " + ap +
                         " and b= " + bp );
    }

    else if( r.n == 4 )
    {
      Triple a1p = vertsA.get( r.bigW.get(0).a );
      Triple a2p = vertsA.get( r.bigW.get(1).a );
      Triple a3p = vertsA.get( r.bigW.get(2).a );
      Triple a4p = vertsA.get( r.bigW.get(3).a );
      Triple ap = Triple.convexComb( r.alpha, a1p, r.beta, a2p, r.gamma, a3p,
                                        r.delta, a4p );
      Triple b1p = vertsB.get( r.bigW.get(0).b );
      Triple b2p = vertsB.get( r.bigW.get(1).b );
      Triple b3p = vertsB.get( r.bigW.get(2).b );
      Triple b4p = vertsB.get( r.bigW.get(3).b );
      Triple bp = Triple.convexComb( r.alpha, b1p, r.beta, b2p, r.gamma, b3p,
                                         r.delta, b4p );
      System.out.println("\nClosest points in bodies are a= " + ap +
                         " and b= " + bp );
    }
  }

  //************************* stuff for Johnson's Distance Algorithm *****

  // use Johnson's algorithm to determine smallest set of
  // points in bigW such that closest point to the origin
  // can be expressed as a convex combination of those points
  // and returns all the detailed information, including 
  // the new bigW
  //
  // Uses dynamic programming to only figure quantities when
  // needed, and then only once

  private static int[] pow2 = {1,2,4,8,16};
  
  private static int jn;  // # of points in bigW
  private static Triple[] jpt;  // all the points
  private static Triple[][] jdiff;  // the necessary differences
  private static Double[][][] jprod;  // the necessary x.(y-z) products
  private static Double[][] jdelta;  // the necessary Delta values
  private static Double[] jtotal;  // the coefficient denominators
  private static Boolean[] jgood;  // whether a row of jdelta is all positive
  
  // is x in s?
  // (x is 0,1,2,3, index of point)
  private static boolean in( int x, int s )
  {
    return (s & pow2[x]) > 0;
  }

  // are all the coefficients for feature s positive?
  private static boolean good( int s )
  {
    if( s >= pow2[jn] )
      return false;

    if( jgood[ s ] == null )
    {// need to do this
      jgood[s] = true;
      for( int x=0; jgood[s] && x<jn; x++ )
      {
        if( in(x,s) && delta( s, x ) <= 0 )
          jgood[s] = false;
      }
    }

    return jgood[s];
  }// good

  // compute (z-x).y if not already known
  // Note:  must have z > x
  private static double prod( int z, int x, int y )
  {
    if( jprod[y][z][x] == null )
    {
      // diff(z,x) = z-x
      jprod[y][z][x] = jpt[y].dotProduct( diff( z, x ) );
//      System.out.println("compute prod("+z+","+x+","+y+")="+ jprod[y][z][x] );
    }

    return jprod[y][z][x];
  }

  // compute point z- point x
  // Must have z > x
  private static Triple diff( int z, int x )
  {
    if( jdiff[z][x] == null )
    {
      jdiff[z][x] = jpt[z].subtract( jpt[x] );
//      System.out.println("computed diff("+z+","+x+")="+jdiff[z][x] );
    }
    return jdiff[z][x];
  }

  // return (computing first if not known) jdelta[s][x]
  private static Double delta( int s, int x )
  {
    if( jdelta[s][x] == null )
    {// need to compute this
      if( s == pow2[x] )
      {
        jdelta[s][x] = 1.0;    // base case
//        System.out.println("delta(" + s + ", " + x + ")=" + jdelta[s][x] );
      }
      else
      {// compute by the big theorem
// System.out.println("Start computing delta at " + s + " and " + x );
        int z=-1;  // the special "front" point, set when first see
                  // a legal choice
        double coeff = 0; // accumulate the terms here
        for( int y=0;  y<jn; y++ )
        {// scan all y in s       
          if( in( y, s ) && y != x)
          {// y is in s and is not x
// System.out.println("       " + "using y=" + y );
            // set the front point if not already set
            if( z < 0 )
              z = y;
         
            if( z > x )
              coeff += prod( z, x, y ) * delta( s-pow2[x], y );   
            else
              coeff -= prod( x, z, y ) * delta( s-pow2[x], y );   
          }
        }// scan all y in s

        jdelta[s][x] = coeff;
//        System.out.println("delta(" + s + ", " + x + ")=" + jdelta[s][x] );
      }
    }

    return jdelta[s][x];
  } 

  // given winning feature coded up in s,
  // produce the GJKResult
  private static GJKResult reportWinner( int s, ArrayList<IntPair> bigW )
  {
    ArrayList<IntPair> rW = new ArrayList<IntPair>(); // the "result W"
    Triple[] pts = new Triple[4];
    IntPair[] ips = new IntPair[4];
    double[] tops = new double[4];
    double bottom = 0;  // the Delta^s value

    // scan s and for each component, store info
    int count = 0;  // number of components encountered
    for( int k=0; k<jn; k++ )
    {
      if( in( k, s ) )
      {// point k is a member of the feature
        rW.add( bigW.get(k) );
        pts[count] = jpt[k];  
        ips[count] = bigW.get(k);
        tops[count] = delta( s, k );
          bottom += tops[count];
        count++;
      }
    }

    return new GJKResult( rW,
                          pts[0], pts[1], pts[2], pts[3],
                          ips[0], ips[1], ips[2], ips[3],
                          tops[0]/bottom, tops[1]/bottom, tops[2]/bottom,
                           tops[3]/bottom );
  }

  // return true if either s uses points not existing
  // or delta( s, x) < 0
  private static boolean notPos( int s, int x )
  {
    return s >= pow2[jn] || delta(s,x)<=0;
  }

  public static GJKResult johnsonDistanceAlgorithm( ArrayList<IntPair> bigW )
  {
    System.out.println("input W for Johnson: " + bigW );

    jn = bigW.size();  

    // jpt holds all the points---jn of them
    // (always need all of these so do up front)
    jpt = new Triple[ jn ];
    for( int k=0; k<jn; k++ )
    {// build point k
      IntPair ip = bigW.get(k);
      jpt[k] = makePoint( ip );
//      System.out.println("point " + k + " is " + jpt[k] );
    }

    // set up full memory space for these, will be
    // filled as needed, dynamically
    jdiff = new Triple[jn][];
    for( int row=0; row<jn; row++ )
      jdiff[row] = new Triple[ row+1 ];
    jprod = new Double[jn][jn][];
    for( int lev=0; lev<jn; lev++ )
      for( int row=0; row<jn; row++ )
        jprod[lev][row] = new Double[ row+1 ];
    jdelta = new Double[pow2[jn]][jn];
    jtotal = new Double[pow2[jn]];
    jgood = new Boolean[pow2[jn]];

    // DEBUG (inefficient to leave in)

    // compute (if not already done) and show entire 15 rows of table
    for( int s=1; s<pow2[jn]; s++ )
    {
      String temp = "";
      for( int x=0; x<jn; x++ )
        if( in( x, s ) )
          temp += (char) ('a'+x);
      System.out.print( String.format("%2d %4s:", s, temp ) );
      for( int x=0; x<jn; x++ )
      {
        if( in( x, s ) )
          System.out.print( String.format("%13.4f ", delta(s,x) ) );
        else
          System.out.print( "------------- " );
      }
      System.out.println();
    }
    System.out.println();

    // Johnson's distance algorithm:  try all features until
    // find one that is "good" (all coefficients nonnegative)
    // with all extension features not "good"

    if( good( 1 ) && notPos(3,1) && notPos(5,2) && notPos(9,3) )
    {// a wins
      return reportWinner( 1, bigW );
    }

    if( good( 2 ) && notPos(3,0) && notPos(6,2) && notPos(10,3) )
    {// b wins
      return reportWinner( 2, bigW );
    }

    if( good( 4 ) && notPos(5,0) && notPos(6,1) && notPos(12,3) )
    {// c wins
      return reportWinner( 4, bigW );
    }

    if( good( 8 ) && notPos(9,0) && notPos(10,1) && notPos(12,2) )
    {// d wins
      return reportWinner( 8, bigW );
    }

    if( good( 3 ) && notPos(7,2) && notPos(11,3) )
    {// ab wins
      return reportWinner( 3, bigW );
    }

    if( good( 5 ) && notPos(7,1) && notPos(13,3) )
    {// ac wins
      return reportWinner( 5, bigW );
    }

    if( good( 9 ) && notPos(11,1) && notPos(13,2) )
    {// ad wins
      return reportWinner( 9, bigW );
    }

    if( good( 6 ) && notPos(7,0) && notPos(14,3) )
    {// bc wins
      return reportWinner( 6, bigW );
    }

    if( good( 10 ) && notPos(11,0) && notPos(14,2) )
    {// bd wins
      return reportWinner( 10, bigW );
    }

    if( good( 12 ) && notPos(13,0) && notPos(14,1) )
    {// cd wins
      return reportWinner( 12, bigW );
    }

    if( good( 7 ) && notPos(15,3) )
    {// abc wins
      return reportWinner( 7, bigW );
    }

    if( good( 11 ) && notPos(15,2) )
    {// abd wins
      return reportWinner( 11, bigW );
    }

    if( good( 13 ) && notPos(15,1) )
    {// acd wins
      return reportWinner( 13, bigW );
    }

    if( good( 14 ) && notPos(15,0) )
    {// bcd wins
      return reportWinner( 14, bigW );
    }

    if( good( 15 ) )
    {// abcd wins
      return reportWinner( 15, bigW );
    }

    // must be degenerate problem, so resort to
    // checking all good rows for giving closest to origin

    GJKResult degBest = null;   // monitor degenerate best
    double degScore = Double.MAX_VALUE, score;
    GJKResult res;

    for( int s=1; s<pow2[jn]; s++ )
    {
      if( good(s) )
      {
        res = reportWinner( s, bigW ); 
        score = res.v.norm();
        if( score < degScore )
        {
          degBest = res;
          degScore = score;
        }
      }
    }

    if( degBest != null )
    {
      System.out.println("Degenerate case");
      System.out.println( degBest );
      System.out.println();
      return degBest;
    }

    System.out.println("johnsonDistanceAlgorithm failed---no case returned");

    System.out.println("points in space are:" );
    for( int k=0; k<jn; k++ )
      System.out.println( jpt[k] );
        
    System.exit(1);
    return null;

  }// johnsonDistanceAlgorithm

  // ------------------------------
  // utilities

  private static boolean member( IntPair x, ArrayList<IntPair> list )
  {
    for( int k=0; k<list.size(); k++ )
      if( x.equals( list.get(k) ) )
        return true;
    return false;
  }

  // make a new object as copy of list with x appended
  private static ArrayList<IntPair> append( IntPair x, ArrayList<IntPair> list )
  {
    ArrayList<IntPair> result = new ArrayList<IntPair>();

    for( int k=0; k<list.size(); k++ )
      result.add( list.get(k) );

    result.add( x );
    return result;
  }

  // find the point in A-B (expressed as (i,j) being a_i - b_j)
  // giving the min of vTp over all p in A-B
  // (start with vertex w.a in A and w.b in B as a guess, unless
  //  w is null, in which case use (0,0))

  private static IntPair minDotProduct( Triple v, IntPair w )
  {
    System.out.println("find min dot product along " + v );

    int n;                // these three values are used twice
    boolean[] computed;
    double[] value;

    IntPair result = new IntPair( 0, 0 );

    if( w == null )
      w = new IntPair(0,0);

    // find min dot product v*a for vertex a of A

    n = vertsA.size();
    computed = new boolean[ n ];  // all set to false
    value = new double[ n ];      // all set to 0

    int j = w.a;   // vertex j is the current winner, 
                   // start at given guess

    boolean done = false;
    double bestScore = dotProd( v, j, vertsA, computed, value );  
    int bestVert = j;
    
// System.out.println("------------- find vertex of A with min dot product");
    do{

      // check j's neighbors for best one:

      // look for vertex k adjacent to j that has a better score
      for( int k=0; k<adjA[j].length; k++ )
      {
        double score = dotProd( v, adjA[j][k], vertsA, computed, value );
        if( score < bestScore )
        {
          bestScore = score;
          bestVert = adjA[j][k];
        }
      }

      if( bestVert == j )
      {// no adjacent vertex beat j, so it's the best
        done = true;
        result.a = bestVert;
      }
      else
      {// move to better vertex and repeat
        j = bestVert;
        // System.out.println("found vertex " + j + " best" );
      }

    }while( !done );

//System.out.println("first body has min dot product of " + bestScore + 
//                      " achieved by vertex " + result.a );

    // DEBUG check:  compare to brute force, flag if different
/*
    if( Math.abs( bestScore - bruteForceMinDotProd( v, vertsA ) )
        > 1e-7 )
    {
      System.out.println("oops in GJK.minDotProduct, vertsA, brute force disagrees");
      System.out.println("first body has min dot product of " + bestScore
                         + " achieved by vertex " + vertsA.get(result.a) );
      System.out.println("while brute force gives " +
         bruteForceMinDotProd( v, vertsA ) + " at vertex " +
            bruteForceWinner );
      System.exit(1);
    }
*/

    // find max dot product v*b for vertex b of B

    n = vertsB.size();
    computed = new boolean[ n ];  // all set to false
    value = new double[ n ];      // all set to 0

    j = w.b;   // vertex j is the current winner, 
                   // start at given guess

    done = false;
    bestScore = dotProd( v, j, vertsB, computed, value );  
    bestVert = j;
    
// System.out.println("------------- find vertex of B with max dot product");
    do{

      // check j's neighbors for best one:

      // look for vertex k adjacent to j that has a better score
      for( int k=0; k<adjB[j].length; k++ )
      {
        double score = dotProd( v, adjB[j][k], vertsB, computed, value );
        if( score > bestScore )
        {
          bestScore = score;
          bestVert = adjB[j][k];
        }
      }

      if( bestVert == j )
      {// no adjacent vertex beat j, so it's the best
        done = true;
        result.b = bestVert;
      }
      else
      {// move to better vertex and repeat
        j = bestVert;
//  System.out.println("found vertex " + j + " best" );
      }

    }while( !done );

// System.out.println("second body has max dot product of " + bestScore + 
//                      " achieved by vertex " + result.b );

    // DEBUG check:  compare to brute force, flag if different
/*
    if( Math.abs( bestScore - bruteForceMaxDotProd( v, vertsB ) )
        > 1e-7 )
    {
      System.out.println("oops in GJK.maxDotProduct, vertsB, brute force disagrees");
      System.out.println("first body has max dot product of " + bestScore
                         + " achieved by vertex " + vertsB.get(result.b) );
      System.out.println("while brute force gives " +
         bruteForceMaxDotProd( v, vertsB ) + " at vertex " +
            bruteForceWinner );

      System.exit(1);
    }
*/

//  System.out.println("determined " + result + " is best");
    return result;

  }

  // do brute force---compute v . a  for all a in list

  private static Triple bruteForceWinner;

  private static double bruteForceMinDotProd( Triple v, ArrayList<Triple> list)
  {
    double best = v.dotProduct( list.get(0) );
    for( int k=1; k<list.size(); k++ )
    {
      double temp = v.dotProduct( list.get(k) );
      if( temp < best )
      {
        best = temp;
        bruteForceWinner = list.get(k);
      }
    }

    return best;
  }

  // do brute force---compute v . a  for all a in list
  private static double bruteForceMaxDotProd( Triple v, ArrayList<Triple> list)
  {
    double best = v.dotProduct( list.get(0) );
    for( int k=1; k<list.size(); k++ )
    { 
      double temp = v.dotProduct( list.get(k) ); 
      if( temp > best )
      {
        best = temp;
        bruteForceWinner = list.get(k);
     }
    }
    return best;
  }

  // compute dot product of vertex k of given list against v
  // unless it's already in the chart
  private static double dotProd( Triple v, int k, ArrayList<Triple> list,
                          boolean[] computed, double[] value )
  {
    if( ! computed[k] )
    {
      Triple p = list.get(k);
      value[k] = v.dotProduct( p );
      computed[k] = true;
    }

//  System.out.println( v + " dot product with vertex " + k + " which is " +
//                        list.get(k) + " is " + value[k] );
    return value[k];
  }

  // given IntPair (i,j), create point a_i - b_j
  public static Triple makePoint( IntPair p )
  {
    Triple a = vertsA.get( p.a );
    Triple b = vertsB.get( p.b );
    return b.vectorTo( a );
  }

  // unit testing methods (each gets to be main in its turn)

  // test Johnson by using one standard tetrahedron and
  // translating it interactively to verify apparently
  // good behavior
  public static void testJohnson( String[] args )
  {
    System.out.print("enter translation vector: ");
    double x = Lib.keys.nextDouble();
    double y = Lib.keys.nextDouble();
    double z = Lib.keys.nextDouble();

    vertsA = new ArrayList<Triple>();
    vertsB = new ArrayList<Triple>();

//    /* original test tetrahedron
    vertsA.add( new Triple(x+2,y+1,z+1) );
    vertsA.add( new Triple(x+5,y+3,z+2) );
    vertsA.add( new Triple(x+3,y+6,z+3) );
//    vertsA.add( new Triple(x+4,y+3,z+7) );
//    */

/*    // standard tetrahedron
    vertsA.add( new Triple(x+1,y+0,z+0) );
    vertsA.add( new Triple(x+0,y+1,z+0) );
    vertsA.add( new Triple(x+0,y+0,z+0) );
    vertsA.add( new Triple(x+0,y+0,z+1) );
*/

    vertsB.add( new Triple(0,0,0) );

    ArrayList<IntPair> bigW = new ArrayList<IntPair>();
    bigW.add( new IntPair( 0, 0 ) );
    bigW.add( new IntPair( 1, 0 ) );
    bigW.add( new IntPair( 2, 0 ) );
//    bigW.add( new IntPair( 3, 0 ) );
    
    GJKResult answer = johnsonDistanceAlgorithm( bigW );

    System.out.println("Johnson gave answer: " + answer );
  }

  private static void testMinDotProduct( Body a, Body b, Triple v, IntPair w )
  {
    bodyA = a;  bodyB = b;

    vertsA = a.getActualVertices();  adjA = a.getAllAdjInfo();
    vertsB = b.getActualVertices();  adjB = b.getAllAdjInfo();

    IntPair result = minDotProduct( v, w ); 
    
  }
 
  // make two bodies and call method that imitates start of GJK
  // and then just calls minDotProduct
  public static void testMinDotProductMain(String[] args)
  {
    double s = 10;
    Triple centerStage = new Triple(50,50,50);
    Triple stageRight = new Triple(75,50,50);
    Triple zero = new Triple(0,0,0);
    Quat one = new Quat(1,0,0,0);  // no rotation
    Quat z45 = new Quat( 45, new Triple(0,0,1) );  // rotate 45 about z axis
    Triple xAxis=new Triple(1,0,0);
    Triple yAxis=new Triple(0,1,0);
    Triple zAxis=new Triple(0,0,1);

    //   Body(half sizes, center, ori, vel, spinrate, spinaxis, 1/mass )
    Body a = new Body(s,s,s,centerStage,z45,zero,0,xAxis,1/(s*s*s));
    Body b = new Body(s,s,s,stageRight,one,zero,0,xAxis,1/(s*s*s));
    // tested with a,b standard, a rotated by z45,

    testMinDotProduct( a, b, new Triple(3,-1,2), new IntPair(0,0) );
  }

  // hard-code two bodies and test gjk on them
  public static void main( String[] args )
  {
    // arguments:  sx sy sz (half-sizes along axes)
    //             center of mass location, orientation,
    //             trans vel, spin rate, spin axis, 1/mass 
/*
    Body a = new Body( 1, 1, 1, Triple.zero, Quat.id,
                        Triple.zero, 0, Triple.xAxis, 1 );

    // try various choices for B
    Body b = new Body( 1, 1, 1, new Triple( 5, 5, 5 ), Quat.id,
                       Triple.zero, 0, Triple.xAxis, 1 );
    Body b = new Body(1,1,1, new Triple( 5, 5, 5 ), new Quat(45,Triple.zAxis),
                       Triple.zero, 0, Triple.xAxis, 1 );
    Body b = new Body( 1, 1, 1, new Triple( 0, 0, 10 ), 
                     new Quat(45,new Triple(1,-1,0)),
                       Triple.zero, 0, Triple.xAxis, 1 );
    Body b = new Body( 1, 1, 1, new Triple( 0, 0.5, 10 ), 
                     new Quat(45,new Triple(1,-1,0)),
                       Triple.zero, 0, Triple.xAxis, 1 );
    Body b = new Body( 1, 1, 1, new Triple( 1.2, 0, 10 ), 
                     new Quat(45,new Triple(1,-1,0)),
                       Triple.zero, 0, Triple.xAxis, 1 );
    Body b = new Body( 1, 1, 1, new Triple( 2, 0, 10 ), 
                     new Quat(45,new Triple(1,-1,0)),
                       Triple.zero, 0, Triple.xAxis, 1 );
    Body b = new Body( 1, 1, 1, new Triple( 2, 0, 0 ), 
                     new Quat(45,new Triple(1,-1,0)),
                       Triple.zero, 0, Triple.xAxis, 1 );
    Body b = new Body( 1, 1, 1, new Triple( 2, 0, 3 ), 
                     new Quat(45,new Triple(1,-1,0)),
                       Triple.zero, 0, Triple.xAxis, 1 );
*/
    Body a = new Body( 1, 1, 1, Triple.zero, new Quat(37,new Triple(1,2,3)),
                        Triple.zero, 0, Triple.xAxis, 1 );
    Body b = new Body( 1, 1, 1, new Triple( 2, 3, -2 ), 
                     new Quat(53,new Triple(1,-1,20)),
                       Triple.zero, 0, Triple.xAxis, 1 );

    GJKResult result = gjk( Triple.xAxis, a, b );

    // compare brute force
    System.out.println("\n\n------------- Brute force: -------------- ");
    bruteForceClosest( a, b );
    System.out.println( "GJK result: " + result );
    System.out.println("winning distance is " + result.v.norm() );
  }

  // given two bodies A and B, find the point in A-B that is
  // closest to the origin by brute force, simply taking all
  // possible vertices in A-B (many of which are not on the boundary,
  // of course) and then from those, form all possible triangles and
  // for each triangle apply johnson
  public static double bruteForceClosest( Body one, Body two )
  {
    // form separate lists of vertices for the bodies
    vertsA = one.getActualVertices();
    vertsB = two.getActualVertices();

    // form all vertices of A-B
    ArrayList<IntPair> all = new ArrayList<IntPair>();
    for( int j=0; j<vertsA.size(); j++ )
      for( int k=0; k<vertsB.size(); k++ )
        all.add( new IntPair( j, k ) );

    // form all triangles with vertices in A-B and apply Johnson
    ArrayList<IntPair> bigW = new ArrayList<IntPair>();
    int count = 0;
    double bestDistance = Double.MAX_VALUE, dist;
    int bestTriangle = -1;
    GJKResult result;

    for( int a=0; a<all.size(); a++ )
      for( int b=0; b<a; b++ )
        if( a != b )
          for( int c=0; c<b; c++ )
          {// have a valid triangle
            count++;
            System.out.println( "examine triangle # " + count + 
                                " with vertices " + a + " " + b + " " + c );
            bigW.clear();
            bigW.add( all.get( c ) );           
            bigW.add( all.get( b ) );           
            bigW.add( all.get( a ) );           
           
            result = johnsonDistanceAlgorithm( bigW );
            dist = result.v.norm();
            
            if( dist < bestDistance )
            {
              bestDistance = dist;
              bestTriangle = count;
            }

          }// have a valid triangle
          
    System.out.println("minimal distance is " + bestDistance );
    System.out.println("achieved by triangle # " + bestTriangle );

    return bestDistance;
  }

}
