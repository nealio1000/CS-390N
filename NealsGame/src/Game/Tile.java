package Game;

import Graphics.Shader;
import Math.Matrix4f;
import Math.Vector3f;

public class Tile extends GameObject {

    public static float radius = 0.8f;
    public static int numSides = 40;

    private static float[] vertices = generateVertices(radius, numSides);

    private static float[] texCoords = generateTexCords(radius, numSides);

    private static byte[] indices = generateIndices(numSides);

    private static String[] texPaths = new String[]{
            "assets/green.png"
    };

    public Tile() {
        super(vertices, indices, texCoords, texPaths);
    }

    @Override
    public void render() {
        tex[0].bind();
        Shader.shader1.enable();
        for (int i = 0; i < 40; i++) {
            Shader.shader1.setUniformMat4f("ml_matrix", Matrix4f.translate(new Vector3f(-10.0f + (i + 1.0f), -4.7f, 0.0f)));
            VAO.render();
        }

        Shader.shader1.disable();
        tex[0].unbind();
    }

}
