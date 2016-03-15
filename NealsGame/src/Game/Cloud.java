package Game;

public class Cloud extends GameObject {

    public static float radius = 1.0f;
    public static int numSides = 40;


    private static float[] vertices = generateVertices(radius, numSides);


    private static float[] texCoords = generateTexCords(radius, numSides);


    private static byte[] indices = generateIndices(numSides);


    private static String[] texPaths = new String[]{"assets/grey.png"};

    public Cloud() {
        super(vertices, indices, texCoords, texPaths);
        //super.translate(new Vector3f(0.0f, -2.4f, 0.0f));
    }

    @Override
    public void update() {
        cosUpdate();
        delta += 0.1f;
    }


}
