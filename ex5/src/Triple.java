/*
  this class is immutable
*/

public class Triple
{
  public double x, y, z;

  public Triple( double xIn, double yIn, double zIn )
  {
    x = xIn;
    y = yIn;
    z = zIn;
  }

  public Triple( Triple other )
  {
    x = other.x;
    y = other.y;
    z = other.z;
  }

  public Triple( java.util.Scanner input )
  {
    x = input.nextDouble();
    y = input.nextDouble();
    z = input.nextDouble();
  }

  public Triple vectorTo( Triple other )
  {
    return new Triple( other.x - x, other.y - y, other.z - z );
  }

  public Triple scalarProduct( double s )
  {
    return new Triple( s*x, s*y, s*z );
  }

  public double dotProduct( Triple other )
  {
    return x*other.x + y*other.y + z*other.z;
  }

  public Triple crossProduct( Triple other )
  {
    return new Triple( y*other.z - z*other.y,
                       z*other.x - x*other.z,
                       x*other.y - y*other.x );
  }

  // compute the point on the line from this point s of the way
  // along the vector d
  public Triple pointOnLine( double lambda, Triple d )
  {
    return new Triple( x + lambda*d.x, y + lambda*d.y, z + lambda*d.z );
  }

  // compute point lambda of the way from this point to q
  public Triple ofTheWay( double lambda, Triple q )
  {
    return new Triple( x + lambda*(q.x-x),
                       y + lambda*(q.y-y),
                       z + lambda*(q.z-z) );
  }

  public double norm()
  {
    return Math.sqrt( x*x + y*y + z*z );
  }

  // create a new triple this is a normalized version
  // of this triple
  public Triple normalized()
  {
    double len = norm();
    return new Triple( x/len, y/len, z/len );
  }

  // make a new triple that is this triple plus v
  public Triple add( Triple v )
  {
    return new Triple( x+v.x, y+v.y, z+v.z );
  }

  // make a new triple that is this triple minus v
  public Triple subtract( Triple v )
  {
    return new Triple( x-v.x, y-v.y, z-v.z );
  }

  public String toString()
  {
    return "<" + x + " " + y + " " + z + ">";
  }

  public Quat toQuat()
  {
    return new Quat( 0, x, y, z );
  }

  public static Triple convexComb( double alpha, Triple a,
                                   double beta, Triple b )
  {
    return new Triple( alpha*a.x + beta*b.x, 
                       alpha*a.y + beta*b.y,
                       alpha*a.z + beta*b.z );
  }

  public static Triple convexComb( double alpha, Triple a,
                                   double beta, Triple b,
                                   double gamma, Triple c )
  {
    return new Triple( alpha*a.x + beta*b.x + gamma*c.x, 
                       alpha*a.y + beta*b.y + gamma*c.y,
                       alpha*a.z + beta*b.z + gamma*c.z );
  }

  public static Triple convexComb( double alpha, Triple a,
                                   double beta, Triple b,
                                   double gamma, Triple c,
                                   double delta, Triple d )
  {
    return new Triple( alpha*a.x + beta*b.x + gamma*c.x + delta*d.x, 
                       alpha*a.y + beta*b.y + gamma*c.y + delta*d.y,
                       alpha*a.z + beta*b.z + gamma*c.z + delta*d.z );
  }

  public static double zCoordCrossProduct( Triple a, Triple b )
  {
    return a.x * b.y  - b.x * a.y;
  }

  // since Triple is immutable, makes sense to have "constants"
  // ("final" probably doesn't do anything, since no method can change)
  public final static Triple zero = new Triple(0,0,0);
  public final static Triple xAxis = new Triple(1,0,0);
  public final static Triple yAxis = new Triple(0,1,0);
  public final static Triple zAxis = new Triple(0,0,1);

}
