package Game;


import Math.Vector3f;

public class Coin extends GameObject {

    public static float radius = 0.6f;
    public static int numSides = 40;
    public boolean alive = true;

    private static float[] vertices = generateVertices(radius, numSides);


    private static float[] texCoords = generateTexCords(radius, numSides);


    private static byte[] indices = generateIndices(numSides);


    private static String[] texPaths = new String[]{
            "assets/yellow.png"
    };

    public Coin() {
        super(vertices, indices, texCoords, texPaths);
        super.translate(new Vector3f(0.0f, -2.4f, 0.0f));
    }

    @Override
    public void update() {
        sinUpdate();
        delta += 0.1f;
    }

}
