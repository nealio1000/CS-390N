/* this class is similar to GJK, but the
   findClosest method uses brute force approach,
   with dynamic programming, checking all the
   Johnson style quantities to see which
   point in A-B is closest to origin
*/

public class BruteClosest
{
  // given bodies A and B, find closest point in A-B to
  // origin and return information 
  public static BruteClosestResult findClosest( Body a, Body b )
  {
    // create list of all vertices of A-B as
    // a "vertex cloud" ---don't care about adjacency
    // structure
    aVerts = a.getActualVertices();
    bVerts = b.getActualVertices();

    aMinusB = new ArrayList<IntPair>();
    for( int j=0; j<aVerts.size(); j++ )
      for( int k=0; k<bVerts.size(); k++ )
        aMinusB.add( new IntPair( j, k ) );

    // set up the completely null tables for the
    // dynamic programming calculations
    int n = aMinusB.size();  // just for convenience in typing
    deltaW = new Double[ n ];

    deltaWXsubW = new Double[ n ][ n ];
    deltaWXsubX = new Double[ n ][ n ];

    deltaWXYsubW = new Double[ n ][ n ][ n ];
    deltaWXYsubX = new Double[ n ][ n ][ n ];
    deltaWXYsubY = new Double[ n ][ n ][ n ];

    deltaWXYZsubW = new Double[ n ][ n ][ n ][ n ];
    deltaWXYZsubX = new Double[ n ][ n ][ n ][ n ];
    deltaWXYZsubY = new Double[ n ][ n ][ n ][ n ];
    deltaWXYZsubZ = new Double[ n ][ n ][ n ][ n ];

    // now go through vertices, edges, triangles, and tetrahedrons,
    // monitoring the closest

    // best holds current winning list of vertices in A-B
    // with convex combination closest
    ArrayList<IntPair> best = new ArrayList<IntPair>();
        best.add( aMinusB.get( 0 ) );  // initialize to first vertex closest
    double bestScore = makeVertex( 0 ).norm();

    // examine all individual vertices
    for( int w=1; w<n; w++ )
    {
      if( delta( w ) 
    }
    
    
 
    
  }

  // make actual vertex in A-B (as a point in space)
  // from indexed list at given index
  private static Triple makeVertex( int index )
  {
    IntPair p = aMinusB.get( index );
    return aVerts.get( p.a ).subtract( bVerts.get( p.b ) );
  }

  // these methods give the various Johnson algorithm quantities,
  // using dynamic programming

  // return delta^w_w 
  private static double deltaSub( int w )
  {
  }

  private static double deltaSub( int w, int x )
  {
    if( deltaWXsub
  }

  // static data:  store all information shared amoung the
  //               various static methods

  // current vertices of A and B
  private static ArrayList<Triple> aVerts;
  private static ArrayList<Triple> bVerts;
  private static ArrayList<IntPair> aMinusB;  // all vertices in A-B

  // dynamic programming arrays, indexed into aMinusB
  // each of these holds null until computed, after which
  // contains the corresponding Johnson algorithm value 
  private static Double[] deltaW;
  private static Double[][] deltaWXsubW, deltaWXsubX;
  private static Double[][][] deltaWXYsubW, deltaWXYsubX, deltaWXYsubY;
  private static Double[][][][] deltaWXYZsubW, deltaWXYZsubX, 
                                deltaWXYZsubY, deltaWXYZsubZ;

}
