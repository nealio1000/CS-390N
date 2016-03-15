package Game;

import Graphics.Shader;
import Graphics.Texture;
import Graphics.VertexArray;
import Math.Matrix4f;
import Math.Vector3f;

public class GameObject {

    public static Shader shader;
    public String[] texPaths;
    public VertexArray VAO;
    public Texture[] tex;
    public float[] vertices, texCoords;
    public byte[] indices;

    //	private static int vbo = 0; // Vertex Buffer Object
    //	private static int vao = 0; // Vertex Array Object

    public Vector3f position = new Vector3f();

    public float delta = 0.01f;

    public GameObject(float[] vertices, byte[] indices, float[] texCoords, String[] texPath) {
        this.texPaths = texPath;
        this.vertices = vertices;
        this.indices = indices;
        this.texCoords = texCoords;
        tex = new Texture[texPaths.length];
        for (int i = 0; i < texPath.length; i++)
            tex[i] = new Texture(texPath[i]);
        VAO = new VertexArray(this.vertices, this.indices, this.texCoords);
    }

    public static float[] generateVertices(float r, int numSides) {

        numSides = 40;

        float x;
        float y;

        float[] vertices = new float[numSides * 3];
        float partition = 360 / (float) numSides;


        for (int i = 0; i < numSides * 3; i += 3) {
            x = (float) (r * Math.cos(Math.toRadians((i + 1) * partition)));
            vertices[i] = x;
            y = (float) (r * Math.sin(Math.toRadians((i + 1) * partition)));
            vertices[i + 1] = y;
            vertices[i + 2] = 0;
        }

        return vertices;
    }

    public static float[] generateTexCords(float r, int numSides) {
        float[] vertices = new float[numSides * 2];
        float partition = 360 / (float) numSides;


        for (int i = 0; i < numSides * 2; i += 2) {
            vertices[i] = (float) (r * Math.cos(Math.toRadians((i + 1) * partition)));
            vertices[i + 1] = (float) (r * Math.sin(Math.toRadians((i + 1) * partition)));
        }
        return vertices;
    }

    public static byte[] generateIndices(int numSides) {
        byte[] indices = new byte[numSides + (numSides / 2)];
        int counter = 0;
        int k = 0;
        for (int i = 0; i < indices.length; i++) {
            indices[i] = (byte) k;

            if (counter == 2)
                counter = 0;
            else {
                k++;
                counter++;
            }
        }
        indices[indices.length - 1] = 0;

        return indices;
    }

    public void loadShader() {
        shader = new Shader("shaders/bg.vert", "shaders/bg.frag");
    }

    public void translate(Vector3f vector) {
        position.x += vector.x;
        position.y += vector.y;
        position.z += vector.z;
    }

    public void sinUpdate() {
        position.y += (float) Math.sin(delta) / 105.0f;
    }

    public void cosUpdate() {
        position.y += (float) Math.cos(delta) / 105.0f;
    }

    public void render() {
        tex[0].bind();
        Shader.shader1.enable();
        Shader.shader1.setUniformMat4f("ml_matrix", Matrix4f.translate(position));
        VAO.render();
        Shader.shader1.disable();
        tex[0].unbind();
    }

    public void update() {
        // our default update function
    }

}
