public class House
{
  public static void main(String[] args)
  {
   double angle = 0;

   Triple b1Actual = new Triple( 3, 4, 5 );
   Triple b0Actual = new Triple( 4, 5, 6 );
   Triple n = b0Actual.vectorTo( b1Actual ).normalized();
System.out.println( "n: " + n );

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
                       "n.h2 = " + n.dotProduct(h2) +
                       "n.h3 = " + n.dotProduct(h3) +
                       "h2.h2 = " + h2.dotProduct(h2) +
                       "h3.h3 = " + h3.dotProduct(h3) 
                      );

    double ang = Math.toRadians( angle );
    double cos = Math.cos( ang );
    double sin = Math.sin( ang );

    Triple h = (h2.scalarProduct( cos )).add( h3.scalarProduct( sin ) );
    System.out.println("h.h= " + h.dotProduct(h) );
    Triple eye = b1Actual.add( h );

    Triple up = (h2.scalarProduct( cos )).add( h3.scalarProduct( sin ) );

  }

}

