/*  an IntPair is simply a pair of ints
    (where a and b are indices in the vertex lists of two bodies)
*/

public class IntPair
{
  public int a, b;

  public IntPair( int aIn, int bIn )
  {
    a=aIn;  b=bIn;
  }

  public boolean equals( IntPair other )
  {
    return a==other.a && b==other.b;
  }

  public String toString()
  {
    return "(" + a + "," + b + ")";
  }

}
