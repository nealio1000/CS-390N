package Input;

import org.lwjgl.glfw.GLFWKeyCallback;

import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class Input extends GLFWKeyCallback {

    // a boolean array of all the keys.
    public static boolean[] keys = new boolean[65535];

    // Overrides GLFW's own implementation of the Invoke method
    // This gets called everytime a key is pressed.
    @Override
    public void invoke(long window, int key, int scancode, int action, int mods) {
        keys[key] = action != GLFW_RELEASE;
    }

    public static boolean isKeyDown(int keycode) {
        return keys[keycode];
    }

    public static boolean isKeyUp(int keycode) {
        return keys[keycode];
    }


}
