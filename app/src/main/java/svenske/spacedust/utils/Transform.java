package svenske.spacedust.utils;

public class Transform {

    public static float[] screen_to_norm(float x, float y, float w, float h) {
        return new float[] { x * 2 / w - 1, (h - y) * 2 / h - 1 };
    }

    public static float[] norm_to_aspect(float x, float y, float ar) {
        // if ar > 1 x * ar
        if (ar > 1f) x *= ar;
        else y /= ar;
        return new float[] { x, y };
    }

    public static float[] aspect_to_world(float x, float y, float cam_x, float cam_y, float cam_zoom) {
        return new float[] { (x / cam_zoom) + cam_x, (y / cam_zoom) + cam_y };
    }
}
