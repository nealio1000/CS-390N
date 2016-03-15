/*  a body is the fundamental unit of physics,
    containing both geometrical and physical 
    properties of the block

    every block has a body, along with other features
    (for example, a block manages its display somewhat
     separately from its body, might have non-convex arms
     and legs sticking out, say,
     but it's collision detection and other physics
     will come from its body)

   a body has ability to minimally display itself for
   test/demo/debug purposes

   for starters, only allow true 3D convex polyhedra
   (others have 0 volume, sort of don't fit, will just be
    irritating)

*/

import java.util.*;
import java.io.*;
import java.nio.DoubleBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Body
{
  // set up the shared standard face colors (development/debug colors)
  public static RGB[] faceColors = new RGB[20];
  static 
  {
    faceColors[0] = RGB.RED;
    faceColors[1] = new RGB( 0, 225, 0 ); // RGB.GREEN;
    faceColors[2] = RGB.BLUE;
    faceColors[3] = RGB.YELLOW;
    faceColors[4] = RGB.MAGENTA;
    faceColors[5] = RGB.CYAN;
    faceColors[6] = RGB.ORANGE;
    faceColors[7] = RGB.PINK;
    faceColors[8] = new RGB( 150, 255, 0 );  // RGB.CHARTREUSE;
    faceColors[9] = RGB.SPRINGGREEN;
    faceColors[10] = RGB.PURPLE;
    faceColors[11] = RGB.TURQUOISE;
    faceColors[12] = RGB.MAROON;
    faceColors[13] = RGB.DARKGREEN;
    faceColors[14] = RGB.DARKBLUE;
    faceColors[15] = RGB.OLIVE;
    faceColors[16] = RGB.DARKMAGENTA;
    faceColors[17] = RGB.TEAL;
    faceColors[18] = RGB.TAN;
    faceColors[19] = RGB.GOLD;
  }

  private static int nextId = 0;

  private int id;  // unique id for debugging

  //------------------------------------------------------------
  // data for the convex polyhedron model
  // relative to the origin (i.e., vertices are vectors
  // offset from origin)

  // note:  indices into verts are the vertex numbers

  private Triple[] verts; // list of all the model 
                          // vertex offset vectors

  private int[][] adj;  // for each vertex number, there is an
                        // array of length >= 0 holding the
                        // adjacent vertex numbers
  
  private int[][] faces;  // for 0 or more faces in the body,
                          // have list of vertices forming that
                          // face (as a planar convex polygon,
                          // listed in ccw order from outside the body)

  //------------------------------------------------------------
  // physics quantities

  private double inverseMass;  // 1/total mass of the body
  private Mat3by3 inverseInertia;  // inverse of I_model --- inertia tensor in model coords

  private Triple center;     // current center of mass of body
                             // (origin translated by center
                             //  vector)
  private Quat ori;          // current orientation of body
    private Mat3by3 rotMat;  //   rotation matrix corresponding to ori

  private Triple vel;        // current translational 
                             // velocity of body

  // rotational velocity (omega) is stored as spinRate (rads per sec)
  // times normalized vector spinAxis
  private double spinRate;    
  private Triple spinAxis;

  // for efficiency, compute and store center and
  // orientation (which together give all info about
  // the body geometry at some elapsed time theta from current
  // where center is center and orientation is ori)
  private double theta;       // remember theta at which things have been computed
  private Triple centerTheta;
  private Quat oriTheta;

  //------------------------------------------------------------

  // construct Body from data file
  // with convenience options
  public Body( Scanner input )
  {
    nextId++;
    id = nextId;

    input.nextLine();  // read and toss the comment line
    
    // non-model data:

    center = new Triple( input );  input.nextLine();  // allow comments
    double angle = input.nextDouble();
    Triple axis = new Triple( input );
    ori = new Quat( angle, axis );                   input.nextLine();
        rotMat =  ori.formRotationMatrix();
    vel = new Triple( input );       input.nextLine();
    spinRate = Math.toRadians( input.nextDouble() );       input.nextLine();
    spinAxis = (new Triple( input )).normalized();       input.nextLine();
       
System.out.println("spin rate: " + spinRate + " axis " + spinAxis );

    inverseMass = input.nextDouble();       input.nextLine();

    // model data (vertices, adjacencies, faces):
    // depending on kind of body

    String kind = input.nextLine();  // box, tetra, file, custom, ...
System.out.println( kind );

    if( kind.equals( "file" ) )
    {// get data from file whose name is given
      String fileName = input.nextLine(); 
      try{
        Scanner dataInput = new Scanner( new File( fileName ) );

        // get data from data file just like "custom" does from main file

        dataInput.nextLine();  // toss comment line starting file

        int numVerts = dataInput.nextInt();    dataInput.nextLine();
        verts = new Triple[ numVerts ];

        for( int k=0; k<numVerts; k++ )
        {
          verts[k] = new Triple( dataInput );   dataInput.nextLine();
        }

        adj = new int[verts.length][];
        for( int k=0; k<verts.length; k++ )
        {
          adj[k] = new int[ dataInput.nextInt() ];  // number of edges out of vk
          for( int j=0; j<adj[k].length; j++ )
            adj[k][j] = dataInput.nextInt();
          dataInput.nextLine();
        }

        int numFaces = dataInput.nextInt();  dataInput.nextLine();
        faces = new int[numFaces][];
  
        for( int k=0; k<numFaces; k++ )
        {// build face k
          faces[k] = new int[ dataInput.nextInt() ];
          for( int j=0; j<faces[k].length; j++ )
            faces[k][j] = dataInput.nextInt();
          dataInput.nextLine();  // toss line to allow for comment
        }// build face k
      }
      catch(Exception e)
      {
        System.out.println("Could not open body data file [" + fileName + "]" );
        System.exit(1);
      } 

    }// file
    else if( kind.equals( "custom" ) )
    {// one of a kind, data embedded in main file

      int numVerts = input.nextInt();    input.nextLine();
System.out.println("got " + numVerts + " vertices");
      verts = new Triple[ numVerts ];

      for( int k=0; k<numVerts; k++ )
      {
        verts[k] = new Triple( input );   input.nextLine();
      }

      adj = new int[verts.length][];
      for( int k=0; k<verts.length; k++ )
      {
        adj[k] = new int[ input.nextInt() ];  // number of edges out of vk
        for( int j=0; j<adj[k].length; j++ )
          adj[k][j] = input.nextInt();
        input.nextLine();
      }

      int numFaces = input.nextInt();  input.nextLine();
System.out.println("got " + numFaces + " faces");
      faces = new int[numFaces][];

      for( int k=0; k<numFaces; k++ )
      {// build face k
        faces[k] = new int[ input.nextInt() ];
        for( int j=0; j<faces[k].length; j++ )
          faces[k][j] = input.nextInt();
        input.nextLine();  // toss line to allow for comment
      }// build face k

    }// custom
    else if( kind.equals( "box" ) )
    {
      double sx= input.nextDouble(), sy=input.nextDouble(), 
             sz=input.nextDouble();
      input.nextLine();

System.out.println("sizes are " + sx + " " + sy + " " + sz );
      verts = new Triple[8];
      // note:  use 3 bits (x is 1's bit, y is 2's, z is 4's)
      //        to number vertices, 0 is negative, 1 is positive
      verts[0] = new Triple( -sx, -sy, -sz );
      verts[1] = new Triple( sx, -sy, -sz );
      verts[2] = new Triple( -sx, sy, -sz );
      verts[3] = new Triple( sx, sy, -sz );
      verts[4] = new Triple( -sx, -sy, sz );
      verts[5] = new Triple( sx, -sy, sz );
      verts[6] = new Triple( -sx, sy, sz );
      verts[7] = new Triple( sx, sy, sz );

      // set up the adjacency information
      adj = new int[verts.length][3];
      adj[0][0] = 1; adj[0][1] = 2; adj[0][2] = 4;
      adj[1][0] = 0; adj[1][1] = 3; adj[1][2] = 5;
      adj[2][0] = 0; adj[2][1] = 3; adj[2][2] = 6;
      adj[3][0] = 1; adj[3][1] = 2; adj[3][2] = 7;
      adj[4][0] = 0; adj[4][1] = 5; adj[4][2] = 6;
      adj[5][0] = 1; adj[5][1] = 4; adj[5][2] = 7;
      adj[6][0] = 2; adj[6][1] = 4; adj[6][2] = 7;
      adj[7][0] = 3; adj[7][1] = 5; adj[7][2] = 6;

      // set up the face information
      faces = new int[6][4];
      faces[0][0]=4; faces[0][1]=5; faces[0][2]=7; faces[0][3]=6; // top (blue)
      faces[1][0]=1; faces[1][1]=3; faces[1][2]=7; faces[1][3]=5; // right (green)
      faces[2][0]=0; faces[2][1]=2; faces[2][2]=3; faces[2][3]=1; // bottom (yellow)
      faces[3][0]=0; faces[3][1]=4; faces[3][2]=6; faces[3][3]=2; // left (magenta)
      faces[4][0]=0; faces[4][1]=1; faces[4][2]=5; faces[4][3]=4; // front (red)
      faces[5][0]=2; faces[5][1]=6; faces[5][2]=7; faces[5][3]=3; // back (cyan)
      
System.out.println("finished reading a box");
    }// box
    else if( kind.equals( "tetra" ) )
    {
      double s = input.nextDouble();  input.nextLine();

      verts = new Triple[4];
  
      double s2 = Math.sqrt( 2 ), s3 = Math.sqrt(3),  // convenience
             s6 = Math.sqrt( 6 );
  
      verts[0] = new Triple(0,0,0);
      verts[1] = new Triple(s*1,0,0);
      verts[2] = new Triple(0.5*s, (s3/2)*s, 0);
      verts[3] = new Triple(0.5*s, (s3/6)*s, (s2/s3)*s );
  
      Triple shift = new Triple( 0.5*s, (s3/6)*s, (s6/12)*s );
  
      // shift model vertices so center of tetrahedron is at origin
      for( int k=0; k<4; k++ )
        verts[k] = verts[k].subtract( shift );
  
      // set up the adjacency information
      adj = new int[verts.length][3];
      adj[0][0] = 1; adj[0][1] = 2; adj[0][2] = 3;
      adj[1][0] = 0; adj[1][1] = 2; adj[1][2] = 3;
      adj[2][0] = 0; adj[2][1] = 1; adj[2][2] = 3;
      adj[3][0] = 0; adj[3][1] = 1; adj[3][2] = 2;
  
      // set up the face information
      faces = new int[4][3];
      faces[0][0] = 0; faces[0][1]=1; faces[0][2]=3;
      faces[1][0] = 1; faces[1][1]=2; faces[1][2]=3;
      faces[2][0] = 2; faces[2][1]=0; faces[2][2]=3;
      faces[3][0] = 0; faces[3][1]=2; faces[3][2]=1;

    }// tetra

    else if( kind.equals( "cylinder" ) )
    {// cylinder
      double radius = input.nextDouble();  input.nextLine();
      double height = input.nextDouble();  input.nextLine();
      int numSides = input.nextInt();  input.nextLine();

      double dAngle = 360.0/numSides;

      verts = new Triple[ 2*numSides ];

      double ang = 0;
      double h = height/2;

      for( int k=0; k<numSides; k++ )
      {// build vk and guy above
        double c = radius*Math.cos( Math.toRadians( ang ) );
        double s = radius*Math.sin( Math.toRadians( ang ) );

        verts[ k ] = new Triple( c, s, -height/2 );
        verts[ k + numSides ] = new Triple( c, s, height/2 );

        ang += dAngle;
      }

      // build adjacency information

      adj = new int[ verts.length ][3];

      // build for guys on bottom and top in parallel
      for( int k=0; k<numSides; k++ )
      {
        // following neighbors
        if( k < numSides-1 )
        {
          adj[k][0] = k+1;
          adj[k+numSides][0] = k+1 + numSides;
        }
        else
        {
          adj[k][0] = 0;
          adj[k+numSides][0] = 0 + numSides;
        }

        // preceding neighbor
        if( k > 0 )
        {
          adj[k][1] = k-1;
          adj[k+numSides][1] = k-1 + numSides;
        }
        else
        {
          adj[k][1] = numSides-1;
          adj[k+numSides][1] = numSides-1 + numSides;
        }

        // vertex above/below
        adj[ k ][2] = k + numSides;
        adj[ k+numSides ][2] = k;
      }

      // build the faces:
      
      faces = new int[ numSides + 2 ][];
      for( int k=0; k<numSides; k++ )
      {// build face with corner at vk on bottom left
    
        faces[ k ] = new int[4];

        if( k < numSides-1 )
        {
          faces[k][0] = k;
          faces[k][1] = k+1;
          faces[k][2] = k+1 + numSides;
          faces[k][3] = k + numSides;
        }
        else
        {
          faces[k][0] = k;
          faces[k][1] = 0;
          faces[k][2] = 0 + numSides;
          faces[k][3] = k + numSides;
        }
      }

      // build bottom and top faces
      faces[ numSides ] = new int[ numSides ];
      faces[ numSides+1 ] = new int[ numSides ];

      for( int k=0; k<numSides; k++ )
      {
        faces[numSides][k] = k;
        faces[numSides+1][k] = k + numSides;
      }

    }// cylinder

    // use Mirtich paper to compute inverseInertia matrix:

    inverseInertia = null;

    computeAtElapsedTime( 0 );   // make sure the "at theta" derived quantities are good

  }// construct Body from Scanner

  // convenience constructor:
  // construct a box
  // with given half-sizes sx, sy, sz size along each axis in the model
  // of desired physical properties
  public Body( double sx, double sy, double sz, // cube half-sizes
                Triple centerOfMass, Quat orientation, Triple translationalVelocity,
                    double rateOfSpin, Triple axisOfSpin,
               double oneOverMass )
  {
    nextId++;
    id = nextId;

    verts = new Triple[8];
    // note:  use 3 bits (x is 1's bit, y is 2's, z is 4's)
    //        to number vertices, 0 is negative, 1 is positive
    verts[0] = new Triple( -sx, -sy, -sz );
    verts[1] = new Triple( sx, -sy, -sz );
    verts[2] = new Triple( -sx, sy, -sz );
    verts[3] = new Triple( sx, sy, -sz );
    verts[4] = new Triple( -sx, -sy, sz );
    verts[5] = new Triple( sx, -sy, sz );
    verts[6] = new Triple( -sx, sy, sz );
    verts[7] = new Triple( sx, sy, sz );

    // set up the adjacency information
    adj = new int[verts.length][3];
    adj[0][0] = 1; adj[0][1] = 2; adj[0][2] = 4;
    adj[1][0] = 0; adj[1][1] = 3; adj[1][2] = 5;
    adj[2][0] = 0; adj[2][1] = 3; adj[2][2] = 6;
    adj[3][0] = 1; adj[3][1] = 2; adj[3][2] = 7;
    adj[4][0] = 0; adj[4][1] = 5; adj[4][2] = 6;
    adj[5][0] = 1; adj[5][1] = 4; adj[5][2] = 7;
    adj[6][0] = 2; adj[6][1] = 4; adj[6][2] = 7;
    adj[7][0] = 3; adj[7][1] = 5; adj[7][2] = 6;

    // set up the face information
    faces = new int[6][4];
    faces[0][0]=4; faces[0][1]=5; faces[0][2]=7; faces[0][3]=6; // top (blue)
    faces[1][0]=1; faces[1][1]=3; faces[1][2]=7; faces[1][3]=5; // right (green)
    faces[2][0]=0; faces[2][1]=2; faces[2][2]=3; faces[2][3]=1; // bottom (yellow)
    faces[3][0]=0; faces[3][1]=4; faces[3][2]=6; faces[3][3]=2; // left (magenta)
    faces[4][0]=0; faces[4][1]=1; faces[4][2]=5; faces[4][3]=4; // front (red)
    faces[5][0]=2; faces[5][1]=6; faces[5][2]=7; faces[5][3]=3; // back (cyan)

    center = centerOfMass;
    ori = orientation;
        rotMat =  ori.formRotationMatrix();
    vel = translationalVelocity;
    spinRate = Math.toRadians( rateOfSpin );  // user thinks in degrees, physics wants radians
    spinAxis = axisOfSpin.normalized();  // user might forget to normalize

    inverseMass = oneOverMass;

    // compute inverse body inertia tensor:
    double sx2 = sx*sx, sy2 = sy*sy, sz2 = sz*sz;
    inverseInertia = new Mat3by3( 3*inverseMass/(sy2+sz2), 0, 0,
                                  0, 3*inverseMass/(sx2+sz2), 0,
                                  0,   0,   3*inverseMass/(sx2+sy2) );
System.out.println("inertia tensor is " + inverseInertia );

    computeAtElapsedTime( 0 );   // make sure the "at theta" derived quantities are good

  }// constructor (homogeneous density box symmetric about origin)

  // convenience constructor:
  // construct a regular tetrahedron of side length s
  // with model centered at origin
  public Body( double s, // side length
          Triple centerOfMass, Quat orientation, Triple translationalVelocity,
                    double rateOfSpin, Triple axisOfSpin,
               double oneOverMass )
  {
    nextId++;
    id = nextId;

    // hard-code vertices in convenient location
    verts = new Triple[4];

    double s2 = Math.sqrt( 2 ), s3 = Math.sqrt(3),  // convenience
           s6 = Math.sqrt( 6 );
    
    verts[0] = new Triple(0,0,0);
    verts[1] = new Triple(s*1,0,0);
    verts[2] = new Triple(0.5*s, (s3/2)*s, 0);
    verts[3] = new Triple(0.5*s, (s3/6)*s, (s2/s3)*s );

    Triple shift = new Triple( 0.5*s, (s3/6)*s, (s6/12)*s );

/*  DEBUG
    System.out.println(
         "Verify convenient tetra---make sure all verts are "+ s + " apart:");
    System.out.println( "v0->v1: " + verts[0].vectorTo(verts[1]).norm() + 
                        "\nv0->v2: " + verts[0].vectorTo(verts[2]).norm() + 
                        "\nv0->v3: " + verts[0].vectorTo(verts[3]).norm() + 
                        "\nv1->v2: " + verts[1].vectorTo(verts[2]).norm() + 
                        "\nv1->v3: " + verts[1].vectorTo(verts[3]).norm() + 
                        "\nv2->v3: " + verts[2].vectorTo(verts[3]).norm() + 
                         "\n" );
    System.out.println(
         "Verify center---make sure all verts same distance from center, " +
          "\n namely square root of 3/8 times s = " + (Math.sqrt(3/8.0)*s) );
    System.out.println( "v0: " + verts[0].vectorTo(shift).norm() + 
                        "\nv1: " + verts[1].vectorTo(shift).norm() + 
                        "\nv2: " + verts[2].vectorTo(shift).norm() + 
                        "\nv3: " + verts[3].vectorTo(shift).norm() );
*/


    // shift model vertices so center of tetrahedron is at origin
    for( int k=0; k<4; k++ )
      verts[k] = verts[k].subtract( shift );

    // set up the adjacency information
    adj = new int[verts.length][3];
    adj[0][0] = 1; adj[0][1] = 2; adj[0][2] = 3;
    adj[1][0] = 0; adj[1][1] = 2; adj[1][2] = 3;
    adj[2][0] = 0; adj[2][1] = 1; adj[2][2] = 3;
    adj[3][0] = 0; adj[3][1] = 1; adj[3][2] = 2;

    // set up the face information
    faces = new int[4][3];
    faces[0][0] = 0; faces[0][1]=1; faces[0][2]=3;
    faces[1][0] = 1; faces[1][1]=2; faces[1][2]=3;
    faces[2][0] = 2; faces[2][1]=0; faces[2][2]=3;
    faces[3][0] = 0; faces[3][1]=2; faces[3][2]=1;

    center = centerOfMass;
    ori = orientation;
        rotMat =  ori.formRotationMatrix();
    vel = translationalVelocity;
    spinRate = Math.toRadians( rateOfSpin );  // user thinks in degrees, physics wants radians
    spinAxis = axisOfSpin.normalized();  // user might forget to normalize

    inverseMass = oneOverMass;

    // compute inverse body inertia tensor:
    double sx2 = s*s, sy2 = s*s, sz2 = s*s;
    inverseInertia = new Mat3by3( 3*inverseMass/(sy2+sz2), 0, 0,
                                  0, 3*inverseMass/(sx2+sz2), 0,
                                  0,   0,   3*inverseMass/(sx2+sy2) );
System.out.println("inertia tensor is " + inverseInertia );

    computeAtElapsedTime( 0 );   // make sure the "at theta" derived quantities are good

  }// constructor (homogeneous density box symmetric about origin)

  public int getId()
  {
    return id;
  }

  // draw the body in its primitive appearance
  // using translation of origin to center
  // and rotation by ori
  public void draw()
  {
    // must be in modelview matrix mode when this is called
    glPushMatrix();  // to protect others from these changes

      // do view transforms to rotate model vertices by q()q*
      // and then translate so origin moves to c

      glTranslated( center.x, center.y, center.z );

      for( int k=0; k<faces.length; k++ )
      {// draw face k

        Lib.setColor( faceColors[ k % Body.faceColors.length ] );

        // draw filled polygon with rotated vertices computed through quats
        glBegin( GL_POLYGON );
          for( int v=0; v<faces[k].length; v++ )
          {
            Triple r = ori.rotate( verts[ faces[k][v] ] );
            glVertex3d( r.x, r.y, r.z );
          }
        glEnd();

      }

    glPopMatrix();

  }// draw
  
  // advance this body ballistically---free motion in space---for
  // the given amount of time t
  // Note:  this method actually advances, whereas computeAtElapsedTime
  //        figures how body would be, hypothetically, at the given
  //        time.
  public void advance( double t )
  {
    center = translate( t );
    ori = rotate( t );    
        rotMat = ori.formRotationMatrix();
    centerTheta = new Triple( center );
    theta = 0;
    oriTheta = rotate( theta );
  }

  // compute new center after time interval t
  public Triple translate( double t )
  {
    Triple total = center.add( vel.scalarProduct( t ) );
    return total;
  }

  // compute new orientation after rotating through
  // time interval t
  public Quat rotate( double t )
  {
    return spinBy( t ).mult( ori );
  }

  // compute the quat corresponding to rotation by current spin
  // through time interval t
  public Quat spinBy( double t )
  {
    double angle = (spinRate/2)*t;
    Triple axis = spinAxis.scalarProduct( Math.sin(angle) );
    
    return new Quat( Math.cos(angle), axis.x, axis.y, axis.z );
  }

  // compute center and orientation at elapsed time t
  // corresponding to spinning from 
  // current orientation for an elapsed time of t
  // and store in the instance variables centerTheta,oriTheta
  public void computeAtElapsedTime( double t )
  {
    theta = t;  // note time for speculative advancement
    centerTheta = translate( theta );  // after moving for time theta
    oriTheta = rotate( theta );  // after spinning for time theta
  }

  // given a point p in world coords, find 
  // offset vector (like verts) that transforms to p
  // using the current (at "theta") values of
  // center and orientation
  public Triple worldToModel( Triple p )
  {
System.out.println("debugging worldToModel with p: " + p );

    // get w = vector from current center of body to p
    Triple w = centerTheta.vectorTo( p );
System.out.println("     vector p-centerTheta is " + w );

    // inverse rotate w by bar(oriTheta) (the total rotation from model at time theta)
    // to give desired offset vector
    Quat oriThetaBar = oriTheta.conjugate();

    Triple r = oriThetaBar.rotate( w );
System.out.println("after rotation by oriThetaBar get " + r );
    return r;
  }

  // given a vector v in model coords, return point it maps to
  // in current (elapsed time theta) world coords
  public Triple modelToWorld( Triple v )
  {
    return centerTheta.add( oriTheta.rotate( v ) );
  }

  // compute and return all the actual vertices of this body at
  // time theta
  public ArrayList<Triple> getActualVertices()
  {
    ArrayList<Triple> vList = new ArrayList<Triple>();

    for( int k=0; k<verts.length; k++ )
    {
      vList.add( getActualVertex( k ) );
    }

    return vList;

  }// getActualVertices

  // compute and return a actual vertex of this body as
  // most recently computed at some time by
  // a call to computeAtElapsedTime( whatever )
  public Triple getActualVertex( int k )
  {
    return modelToWorld( verts[ k ] );
  }// getActualVertex

  // return list of indices in verts of all the
  // vertices adjacent to index
  public int[] getAdjVerticesIndices( int index )
  {
    return adj[index];  // okay to not produce copy because never
                         // change this array
  }

  // get the entire adjacency information
  public int[][] getAllAdjInfo()
  {
    return adj;
  }

  public int prevVert( int index )
  {
    if( index > 0 )
      return index-1;
    else
      return verts.length-1;
  }

  public int nextVert( int index )
  {
    if( index < verts.length-1 )
      return index+1;
    else
      return 0;
  }

  public int getNumberVertices()
  {
    return verts.length;
  }

  public double getSpinRate()
  {
    return spinRate;
  }

  // apply the impulse j at the point s, both in the
  // model coordinates, and change translational and
  // rotational velocities correspondingly
  public void applyModelImpulse( Triple j, Triple s )
  {
    // compute change in translational velocity
    Triple jWorld = rotMat.mult( j );  // get impulse in world coords
    Triple deltaTransVel = jWorld.scalarProduct( inverseMass );

    // add to vel:
    vel = vel.add( deltaTransVel );
System.out.println("vel is now " + vel );

    // compute change in rotational velocity
    Triple temp = s.crossProduct( j );
System.out.println(" s X j = " + temp );

System.out.println("inverseInertiaModel is " + inverseInertia );
System.out.println("rot mat is " + rotMat );

    temp = inverseInertia.mult( temp );
System.out.println(" Imodelinverse ( s X j ) = " + temp );
    temp = rotMat.mult( temp );
System.out.println(" multiplied by rot matrix " + rotMat + " is " + temp );

    // update omega (combination of spinAxis and spinRate)
    Triple omega = spinAxis.scalarProduct( spinRate );
System.out.println("omega is " + omega );

    omega = omega.add( temp );
System.out.println("So after adding delta omega = " + temp + " get omega = " +
  omega );

    spinRate = omega.norm();
    if( spinRate > 0 )
      spinAxis = omega.normalized();
    else
      spinAxis = new Triple( 1, 0, 0 );  // kind of meaningless

System.out.println("rep'd as spin rate = " + spinRate + " and axis = " + spinAxis);    
  }//------------------------------ applyModelImpulse -----------------

  // apply the impulse j at the point r, both in world
  // coordinates, and change translational and
  // rotational velocities correspondingly
  public void applyWorldImpulse( Triple j, Triple r )
  {
    // compute change in translational velocity
    Triple deltaTransVel = j.scalarProduct( inverseMass );
    // add to vel:
    vel = vel.add( deltaTransVel );

System.out.println("vel is now " + vel );

    // compute change in rotational velocity
    //  (temp holds successive products from the right
    //   in R invImodel R^T ( r X j )

    Triple temp = r.crossProduct( j );

System.out.println(" r X j = " + temp );

    Mat3by3 invRotMat = rotMat.transpose();
System.out.println("inverse rot mat is " + invRotMat );

     temp = invRotMat.mult( temp );

     temp = inverseInertia.mult( temp );

     temp = rotMat.mult( temp );

    // update omega (combination of spinAxis and spinRate)
    Triple omega = spinAxis.scalarProduct( spinRate );
System.out.println("omega is " + omega );

    omega = omega.add( temp );
System.out.println("So after adding delta omega = " + temp + " get omega = " +
  omega );

    spinRate = omega.norm();
    if( spinRate > 0 )
      spinAxis = omega.normalized();
    else
      spinAxis = new Triple( 1, 0, 0 );  // kind of meaningless

System.out.println("rep'd as spin rate = " + spinRate + " and axis = " + spinAxis);
  }//------------------------------ applyWorldImpulse -----------------

  // reset---center at (a,b,c), all else to model
  public void reset( double a, double b, double c )
  {
    center = new Triple( a, b, c );
    ori = new Quat( 1, 0, 0, 0 );
        rotMat =  ori.formRotationMatrix();
    vel = new Triple( 0, 0, 0 );
    spinRate = 0;
    spinAxis = new Triple(1,0,0);
    computeAtElapsedTime( 0 );   // make sure the "a
  }

  public Triple getCenter()
  {
    return center;
  }

  public String toString()
  {
    return "<" + center.x + "," + center.y + "," + center.z + ">";
  }

  public static void main(String[] args)
  {
    Body a = new Body( 17, 
                       Triple.zero, Quat.id, Triple.zero,
                       0, Triple.xAxis,
                       1 );
  }

}// class Body
