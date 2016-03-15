public class RGB
{
  public int red, green, blue;

  public RGB( int r, int g, int b )
  {  
    red = r;  
    green = g;  
    blue = b;  
  }

  public RGB( java.util.Scanner input )
  {  
    red = input.nextInt();
    green = input.nextInt();;  
    blue = input.nextInt();;  
  }

  public String toString()
  {
    return "color(" + red + "," + green + "," + blue + ")";
  }

  public static RGB RED = new RGB( 255, 0, 0 );
  public static RGB GREEN = new RGB( 0, 255, 0 );
  public static RGB BLUE = new RGB( 0, 0, 255 );

  public static RGB YELLOW = new RGB(255,255,0);
  public static RGB MAGENTA = new RGB( 255, 0, 255 );
  public static RGB CYAN = new RGB(0,255,255);

  public static RGB ORANGE = new RGB( 255, 128, 0 );
  public static RGB PINK = new RGB( 255, 0, 128 );
  public static RGB CHARTREUSE = new RGB( 128, 255, 0 );
  public static RGB SPRINGGREEN = new RGB( 0, 255, 128 );
  public static RGB PURPLE = new RGB( 128, 0, 255 );
  public static RGB TURQUOISE = new RGB( 0, 128, 255 );

  public static RGB MAROON = new RGB( 128, 0, 0 );
  public static RGB DARKGREEN = new RGB( 0, 128, 0 );
  public static RGB DARKBLUE = new RGB( 0, 0, 128 );

  public static RGB OLIVE = new RGB( 128, 128, 0 );
  public static RGB DARKMAGENTA = new RGB( 128, 0, 128 );
  public static RGB TEAL = new RGB( 0, 128, 128 );

  public static RGB TAN = new RGB( 255, 165, 79 );
  public static RGB GOLD = new RGB( 255, 215, 0 );
  
  public static RGB WHITE = new RGB( 255, 255, 255 );
  public static RGB BLACK = new RGB( 0, 0, 0 );
  public static RGB GRAY = new RGB( 128, 128, 128 );
  public static RGB DARKGRAY = new RGB( 90, 90, 90 );
}
