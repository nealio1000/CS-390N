/* 
  quaternion class
*/

public class Quat
{
  private double s;
  private Triple v;

  // high convenience constructor:
  // create a quaternion from an angle alpha in degrees
  // and
  // an axis w (which doesn't have to be normalized)
  // corresponding to rotation by alpha about w
  public Quat( double alpha, Triple w )
  {
    double rads = Math.toRadians(alpha/2);
    s = Math.cos( rads );
    v = w.scalarProduct( Math.sin( rads )/w.norm() );
  }

  // more flexible, efficient constructor:
  // create a quaternion [s,<x,y,z>]
  // Note that it might not be a unit quaternion
  public Quat( double scalar, double x, double y, double z )
  {
    s = scalar;
    v = new Triple( x, y, z );
  }

  public Quat( Quat other )
  {
    s=other.s;
    v = new Triple( other.v );
  }

  // construct a quat from a vector
  public Quat( Triple w )
  {
    s = 0;
    v = new Triple( w );
  }

  // construct a quat from a scanner
  public Quat( java.util.Scanner input )
  {
    s = input.nextDouble();
    v = new Triple( input );
  }

  // make a quaternion that is this one
  // normalized 
  public Quat normalized()
  {
    double len = Math.sqrt( s*s + v.x*v.x + v.y*v.y + v.z*v.z );
    return new Quat( s/len, v.x/len, v.y/len, v.z/len );
  }

  // form the rotation matrix corresponding to this
  // unit quaternion
  public Mat3by3 formRotationMatrix()
  {
    // note that all the terms occur with a factor of 2
    double x2 = 2*v.x*v.x;
    double y2 = 2*v.y*v.y;
    double z2 = 2*v.z*v.z;
    double xy = 2*v.x*v.y;
    double xz = 2*v.x*v.z;
    double yz = 2*v.y*v.z;
    double sx = 2*s*v.x;
    double sy = 2*s*v.y;
    double sz = 2*s*v.z;
    Mat3by3 mat = new Mat3by3( 1-y2-z2, xy-sz, xz+sy,
                     xy+sz, 1-x2-z2, yz-sx,
                     xz-sy, yz+sx, 1-x2-y2 );
    return mat;
  }

  public String toString()
  {
    return "[" + s + "," + v.toString() + "]";
  }

  // multiply this quat by the other
  // on the right
  public Quat mult( Quat other )
  {
    Triple sv = (other.v).scalarProduct(s).add( v.scalarProduct( other.s ) );
    Triple vp = sv.add( v.crossProduct( other.v ) );
    return new Quat( s*other.s - v.dotProduct( other.v),
                     vp.x, vp.y, vp.z );
  }

  public Quat conjugate()
  {
    return new Quat( s, -v.x, -v.y, -v.z );
  }

  public Triple toVector()
  {
    return new Triple( v.x, v.y, v.z );
  }

  // rotate the vector p by this quaternion by
  // doing q p qbar, essentially
  public Triple rotate( Triple p )
  {
    return (this.mult( p.toQuat()).mult( this.conjugate() )).toVector();
  }

  public static void main(String[] args)
  {
    Triple xaxis = new Triple(1,0,0);
    Triple zaxis = new Triple(0,0,1);

    Quat q1 = new Quat( 30, zaxis );
    Quat q2 = new Quat( 45, zaxis );
    System.out.println( q1.rotate( xaxis ) + " is [1,0,0] rotated by 30 degrees about z axis");
    System.out.println( q2.rotate( xaxis ) + " is [1,0,0] rotated by 45 degrees about z axis");
    Quat q3 = q1.mult( q2 );
    System.out.println( q3.rotate( xaxis ) + " is [1,0,0] rotated by 75 degrees about z axis");

    Quat q1bar = q1.conjugate();
    Quat q2bar = q2.conjugate();
    Quat q3bar = q3.conjugate();
    System.out.println(" q1 q1bar is " + q1.mult( q1bar ) );
    System.out.println(" q2 q2bar is " + q2.mult( q2bar ) );
    System.out.println(" q3 q3bar is " + q3.mult( q3bar ) );
 
    Triple v = new Triple(1,2,3);
    System.out.println("[1,2,3] as a quat is " + v.toQuat() );

    Quat p = q3.mult( v.toQuat() );
    System.out.println("p= q3 [1,2,3] is " + p );
    System.out.println(" q3bar p = " + q3bar.mult( p ) );
    
  }

  // since Quat is immutable, makes sense to have constants
  public final static Quat id = new Quat(1,0,0,0);  // identity---no rotation
}
