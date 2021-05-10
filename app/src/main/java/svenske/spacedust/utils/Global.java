package svenske.spacedust.utils;

import svenske.spacedust.graphics.Font;

// A class to store global constants/other info
public class Global {

    // Global font reference
    public static Font font = null;

    // Color to pass to glClearColor()
    public static final float[] CLEAR_COLOR = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };

    // Width and height of the viewport (in pixels)
    public static int VIEWPORT_WIDTH = 0;
    public static int VIEWPORT_HEIGHT = 0;
}
