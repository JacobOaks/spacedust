package svenske.spacedust.utils;

import svenske.spacedust.graphics.Camera;

/**
 * A class with static methods used to transform coordinates between spaces.
 *
 * Spaces to consider:
 *
 * Screen Space: the final view presented to the user. Theoretically as wide and as tall as there
 * are pixels along the width and height of the screen.
 * - x: 0 (left) - w (right)
 * - y: 0 (top) - h (bottom)
 *
 * Normalized Space: simple normalized space - the space OpenGL expects at the end of the vertex
 * shader.
 * - x: -1 (left) - +1 (right)
 * - y: -1 (bottom) - +1 (top)
 *
 * Aspect Space: normalized space where the aspect ratio of the corresponding screen is taken into
 * account. Is synonymous to world space with a camera centered at 0f, 0f and with zoom 1f. In
 * aspect space, a width is the same physical length as a height of the same value. This is the
 * space that HUD objects live in.
 *
 * World Space: aspect space with the ability to react to a camera with zoom and position. This is
 * the space that world objects live in.
 */
public class Transform {

    public static float[] screen_to_aspect(float x, float y) {
        float w = (float)Global.VIEWPORT_WIDTH;
        float h = (float)Global.VIEWPORT_HEIGHT;

        // Screen -> Normalized
        x = x * 2 / w - 1;
        y = (h - y) * 2 / h - 1;

        // Normalized -> Aspect
        float ar = w / h;
        if (ar > 1f) x *= ar;
        else y /= ar;

        return new float[] { x, y };
    }

    /**
     * Transforms screen coordinates to world coordinates
     * @param x the pixel/screen x
     * @param y the pixel/screen y
     * @return a length-2 array [x_world, y_world]
     */
    public static float[] screen_to_world(float x, float y, Camera cam) {

        // Screen -> Aspect
        float[] ar_points = Transform.screen_to_aspect(x, y);
        x = ar_points[0];
        y = ar_points[1];

        // Aspect -> World
        return new float[] { (x / cam.get_zoom()) + cam.get_x(), (y / cam.get_zoom()) + cam.get_y() };
    }
}
