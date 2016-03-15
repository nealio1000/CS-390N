/*  holds a 3 by 3 matrix
*/

import java.nio.DoubleBuffer;
import org.lwjgl.BufferUtils;  // for making DoubleBuffer 

public class Mat3by3
{
  public double[][] data;

  public Mat3by3( double a11, double a12, double a13,
                  double a21, double a22, double a23,
                  double a31, double a32, double a33 )
  {
    data = new double[3][3];
    data[0][0] = a11;
    data[0][1] = a12;
    data[0][2] = a13;
    data[1][0] = a21;
    data[1][1] = a22;
    data[1][2] = a23;
    data[2][0] = a31;
    data[2][1] = a32;
    data[2][2] = a33;
  }

  // create a direct, native order
  // (using org.lwjgl.BufferUtils) 
  // DoubleBuffer, ready to use in OpenGL,
  //  from a 4 by 4 matrix
  // (add 4th row/col of identity,
  //  store in column major order)
  public DoubleBuffer toOpenGL()
  {
    double[] a = new double[16];  // all components set to 0
                                  // so don't need to set
                                  // added 4th row and col
    int index = 0;
    for( int col=0; col<3; col++ )
    {// copy col over
      for( int r=0; r<3; r++ )
      {
        a[index] = data[r][col];
        index++;
      }
      index++;  // skip the 4th row in this column
    }
  
    a[15] = 1;

    // now transfer contents of the array into a DoubleBuffer
    DoubleBuffer db = BufferUtils.createDoubleBuffer( 16 );
    for( int k=0; k<16; k++ )
      db.put( a[k] );

    db.rewind();

/*
    for( int k=0; k<16; k++ )
      System.out.println( db.get() );
*/

    return db;
    
  }

  public String toString()
  {
    return "\n" +
        data[0][0] +" "+ data[0][1] +" "+ data[0][2] + "\n" +
        data[1][0] +" "+ data[1][1] +" "+ data[1][2] + "\n" +
        data[2][0] +" "+ data[2][1] +" "+ data[2][2] + "\n" ;
  }

  // return this matrix times v
  public Triple mult( Triple v )
  {
    return new Triple( data[0][0]*v.x + data[0][1]*v.y + data[0][2]*v.z,
                       data[1][0]*v.x + data[1][1]*v.y + data[1][2]*v.z,
                       data[2][0]*v.x + data[2][1]*v.y + data[2][2]*v.z );
  }

  public Mat3by3 transpose()
  {
    return new Mat3by3( data[0][0], data[1][0], data[2][0],     
                        data[0][1], data[1][1], data[2][1],     
                        data[0][2], data[1][2], data[2][2] );     
  }

}
