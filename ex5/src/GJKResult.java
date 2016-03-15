/*  a GJKResult instance holds all the
    information returned by the gjk algorithm
*/

import java.util.*;

public class GJKResult
{
  public Triple v;  // closest point in conv(W) to origin
  // v is a convex combination of some points in W, with:

  public int n;  // number of points used to express v
  public Triple ap, bp, cp, dp;  // the points 
  public IntPair a, b, c, d;  // the A and B vertices used to make the points
  public double alpha, beta, gamma, delta; //the convex combination coefficients
  public ArrayList<IntPair> bigW;  // W

  // does not make copies of the objects, so they need to be freshly
  // created or from a class/primitive with no mutators
  //  (is only given reference to bigW so it can empty the list and add
  //   the new points)
  public GJKResult( ArrayList<IntPair> bigWIn,
                    Triple apIn, Triple bpIn, Triple cpIn, Triple dpIn,
                    IntPair aIn, IntPair bIn, IntPair cIn, IntPair dIn,
                    double al, double bet, double gam, double del )
  {
    n = bigWIn.size();
    bigW = bigWIn;
System.out.println("input W: " + bigW );
    ap = apIn;
    bp = bpIn;
    cp = cpIn;
    dp = dpIn;
    a = aIn;
    b = bIn;
    c = cIn;
    d = dIn;
    alpha = al;
    beta = bet;
    gamma = gam;
    delta = del;

    // construct v from the given information
    if( n==1 )
    {
      v = new Triple( ap );
    }
    else if( n==2 )
    {
      v = Triple.convexComb( alpha, ap, beta, bp );
    }
    else if( n==3 )
    {
      v = Triple.convexComb( alpha, ap, beta, bp, gamma, cp );
    }
    else if( n==4 )
    {
      v = Triple.convexComb( alpha, ap, beta, bp, gamma, cp, delta, dp );
    }
    else
    {
      System.out.println("W has " + n + " points, which is impossible");
      System.exit(1);
    }

    // construct the new list of points needed to express the closest point
    bigW.clear();
    bigW.add(a); 
    if( n>1 )  bigW.add( b );
    if( n>2 )  bigW.add( c );
    if( n>3 )  bigW.add( d );

  }

  public double distance()
  {
    return v.norm();
  }

  public String toString()
  {
    String s = "closest point to origin v = " + v + "\n";

    if( n == 1 )
    {
      s += "closest point is convex combination of 1 vertex:\n";
      s += alpha + "*" + GJK.makePoint( a ) + "\n";
      s += "a is " + a;
    }
    else if( n == 2 )
    {
      s += "closest point is convex combination of 2 points:\n";
      s += alpha + "*" + GJK.makePoint( a ) + " + " + beta + "*" + GJK.makePoint(b) + "\n";
      s += "a is " + a + " b is " + b;
    }
    else if( n == 3 )
    {
      s += "closest point is convex combination of 3 points:\n";
      s += alpha + "*" + GJK.makePoint( a ) + " + " + beta + "*" + GJK.makePoint(b) + " + " + 
            gamma + "*" + GJK.makePoint( c ) + "\n";
      s += "a is " + a + " b is " + b + " c is " + c;
    }
    else if( n == 4 )
    {
      s += "closest point is convex combination of all 4 points:\n";
      s += alpha + "*" + GJK.makePoint( a ) + " + " + beta + "*" + GJK.makePoint(b) + " + " + 
            gamma + "*" + GJK.makePoint( c ) + 
            " + " + delta + "*" + GJK.makePoint(d) + "\n";
      s += "a is " + a + " b is " + b + " c is " + c + " d is " + d;
    }
    else
    {
      System.out.println("can't have n = " + n );
      System.exit(1);
    }
           
    s += "\noutput W: " + bigW;
    return s;

  }//toString

  public Triple[] getClosestPoints( ArrayList<Triple> vertsA, 
                                    ArrayList<Triple> vertsB )
  {
    Triple[] answer = new Triple[2];

    if( n==1 )
    {// a=(k,j) is vertex of A-B closest to origin
      answer[0] = new Triple( vertsA.get( a.a ) );
      answer[1] = new Triple( vertsB.get( a.b ) );
    }
    else if( n==2 )
    {// a=(k,j), b=(p,q) are vertices of A-B for edge that has
     //  closest point to origin on it
      answer[0] = Triple.convexComb( alpha, vertsA.get( a.a ),
                                     beta, vertsA.get( b.a )  );
      answer[1] = Triple.convexComb( alpha, vertsB.get( a.b ),
                                     beta, vertsB.get( b.b )  );
    }
    else if( n==3 )
    {// a=(k,j), b, c are vertices of A-B for triangle that has
     //  closest point to origin in it
      answer[0] = Triple.convexComb( alpha, vertsA.get( a.a ),
                                     beta, vertsA.get( b.a ),
                                     gamma, vertsA.get( c.a ) );
      answer[1] = Triple.convexComb( alpha, vertsB.get( a.b ),
                                     beta, vertsB.get( b.b ),
                                     gamma, vertsB.get( c.b )  );
    }
    else if( n==4 )
    {// a=(k,j), b, c, d are vertices of A-B for tetrahedron that has
     //  closest point to origin in it
      answer[0] = Triple.convexComb( alpha, vertsA.get( a.a ),
                                     beta, vertsA.get( b.a ),
                                     gamma, vertsA.get( c.a ),
                                     delta, vertsA.get( d.a )  );
      answer[1] = Triple.convexComb( alpha, vertsB.get( a.b ),
                                     beta, vertsB.get( b.b ),
                                     gamma, vertsB.get( c.b ),
                                     delta, vertsA.get( d.b )  );
    }

    return answer;
  }

  // report all dot products of v with
  // vertices of the two bodies, where
  // the order matches the order in the IntPair's
  public void reportDotProds( ArrayList<Triple> vertsA, 
                              ArrayList<Triple> vertsB )
  {
    System.out.println("Dot products with " + v );
    
    System.out.println("and vertices of first body:");
    for( int k=0; k<vertsA.size(); k++ )
      System.out.println( k + ": " + vertsA.get(k).dotProduct( v ) );

    System.out.println("and vertices of second body:");
    for( int k=0; k<vertsB.size(); k++ )
      System.out.println( k + ": " + vertsB.get(k).dotProduct( v ) );
  }

  // check ordering of dot products of v with
  // special and other vertices of the two bodies, where
  // the order matches the order in the IntPair's
  public void checkDotProds( ArrayList<Triple> vertsA,
                              ArrayList<Triple> vertsB )
  {
    for( int k=0; k<vertsA.size(); k++ )
      System.out.println( k + ": " + vertsA.get(k).dotProduct( v ) );

    System.out.println("and vertices of second body:");
    for( int k=0; k<vertsB.size(); k++ )
      System.out.println( k + ": " + vertsB.get(k).dotProduct( v ) );
  }

  public int countNumberDistinct( boolean countFirst )
  {
    if( n==1 )
      return 1;
    else if( n==2 )
    {
      int x, y;
      if( countFirst )
      { x=bigW.get(0).a; y=bigW.get(1).a; }
      else
      { x=bigW.get(0).b; y=bigW.get(1).b; }

      if( x==y )
        return 1;
      else
        return 2;
    }
    else if( n==3 )
    {// n==3
      
      int x, y, z;
      if( countFirst )
      { x=bigW.get(0).a; y=bigW.get(1).a; z=bigW.get(2).a; }
      else
      { x=bigW.get(0).b; y=bigW.get(1).b; z=bigW.get(2).b; }

      if( x==y && y==z )
        return 1;
      else
      {
        if( x==y || x==z || y==z )
          return 2;
        else
          return 3;
      }
    }
    else
    {
      System.out.println("Shouldn't be calling GJKResult.countNumberDistinct");
      System.out.println("when n=" + n );
      System.exit(1);
      return -1;
    }
  }

  public static void main(String[] args)
  {
    Random rng = new Random( 1 );

    do{
    ArrayList<IntPair> bigW = new ArrayList<IntPair>();
    IntPair a=null, b=null, c=null, d=null;
    a=new IntPair(rng.nextInt(3),rng.nextInt(3));
    bigW.add( a );
    b=new IntPair(rng.nextInt(3),rng.nextInt(3));
    bigW.add( b );
/*
    c=new IntPair(rng.nextInt(3),rng.nextInt(3));
    bigW.add( c );
*/

    GJKResult r = new GJKResult( bigW, 
       Triple.xAxis, Triple.xAxis, Triple.xAxis, Triple.xAxis,
                          a, b, c, d,
                          0,0,0,0 );

    System.out.println( "number distinct in first spots: " + 
                            r.countNumberDistinct( true ) );
    System.out.println( "number distinct in second spots: " + 
                            r.countNumberDistinct( false ) );

      Lib.keys.nextLine();
    }while( true );

  }

}// GJKResult    
