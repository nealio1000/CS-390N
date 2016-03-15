package Game;

import Graphics.Shader;
import Input.Input;
import Math.Matrix4f;
import Math.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class Player extends GameObject {

    public static float radius = 0.8f;
    public static int numSides = 40;
    public Vector3f delta = new Vector3f();

    public boolean running = false;
    public boolean jumping = false;
    public boolean idle = true;

    private static float[] vertices = generateVertices(radius, numSides);

    private static float[] texCoords = generateTexCords(radius, numSides);

    private static byte[] indices = generateIndices(numSides);

    private static String[] texPaths = {"assets/red.png"};

    public Player() {
        super(vertices, indices, texCoords, texPaths);
    }


    //********* EXPERIMENTAL FLOOR COLLISION AREA FOR POSSIBLE PLATFORMER GAME ***********
//    public boolean checkFloorCollision() {
//        if (position.y <= 3.0f)
//            return true;
//        else
//            return false;
//    }

    @Override
    public void render() {
        tex[0].bind();
        Shader.shader1.enable();
        Shader.shader1.setUniformMat4f("ml_matrix", Matrix4f.translate(position));
        VAO.render();
        Shader.shader1.disable();
        tex[0].unbind();
    }

    @Override
    public void update() {
        position.y -= delta.y;
        position.x -= delta.x;

        if (position.y <= -3.1f) {
            position.y = -3.1f;
        }
        if (Input.isKeyDown(GLFW_KEY_SPACE)) {
            delta.y = -0.05f;

            jumping = true;
        } else if (Input.isKeyDown(GLFW_KEY_D)) {
            delta.x = -0.15f;
            idle = false;
            running = true;
        } else if (Input.isKeyDown(GLFW_KEY_A)) {
            delta.x = 0.15f;
            idle = false;
            running = true;
        } else if (Input.isKeyDown((GLFW_KEY_ESCAPE))) {
            System.out.println("\n\nYou got " + Game.coinCounter + " coins.\nGood Job!");
            System.out.println("\nGame Over");
            System.exit(0);
        } else {
            delta.y += 0.01f; // fake gravity
            delta.x = 0.0f;
        }
    }
}
