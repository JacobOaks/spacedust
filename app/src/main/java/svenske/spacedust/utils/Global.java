package svenske.spacedust.utils;

import svenske.spacedust.graphics.Font;
import svenske.spacedust.graphics.TextureAtlas;

// A class to store global constants/other info
public class Global {

    // Global textures
    public static Font font = null;
    public static TextureAtlas ta = null;

    // Color to pass to glClearColor()
    public static final float[] CLEAR_COLOR = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };

    // Width and height of the viewport (in pixels)
    public static int VIEWPORT_WIDTH = 0;
    public static int VIEWPORT_HEIGHT = 0;

    /**
     * @return a length-2 float array where the first float is direction from pos1 to pos2 (in
     * radians), and the second float is the distance between the two points.
     */
    public static float[] get_vector_info(float[] pos1, float[] pos2) {
        float dx = pos2[0] - pos1[0];
        float dy = pos2[1] - pos1[1];
        float d = (float)Math.sqrt((dx * dx) + (dy * dy));
        return new float[] { (float)Math.atan2(dy, dx) - (float)(Math.PI / 2f), d };
    }
}
