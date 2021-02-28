package svenske.spacedust.utils;

import svenske.spacedust.graphics.Camera;

/**
 * A class with static methods used to transform coordinates.
 */
public class Transform {

    /**
     * Transforms screen coordinates to world coordinates
     * @param x the pixel/screen x
     * @param y the pixel/screen y
     * @param w the width of the screen in pixels
     * @param h the height of the screen in pixels
     * @return a length-2 array [x_world, y_world]
     */
    public static float[] screen_to_world(float x, float y, float w, float h, Camera cam) {

        // Normalize
        x = x * 2 / w - 1;
        y = (h - y) * 2 / h - 1;

        // Aspect ratio
        float ar = w / h;
        if (ar > 1f) x *= ar;
        else y /= ar;

        // Camera
        return new float[] { (x / cam.getZoom()) + cam.getX(), (y / cam.getZoom()) + cam.getY() };
    }
}
