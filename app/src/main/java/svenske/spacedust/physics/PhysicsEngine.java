package svenske.spacedust.physics;

public class PhysicsEngine {

    public static boolean rb_cb_collision(RectangularBounds rb, CircularBounds cb) {

        // calculate AABB info (center, half-extents)
        float rb_hw = rb.w / 2;
        float rb_hh = rb.h / 2;

        float dx = cb.cx - rb.cx;
        float dy = cb.cy - rb.cy;

        float clamp_x = Math.max(Math.min(rb_hw, dx), -rb_hw);
        float clamp_y = Math.max(Math.min(rb_hh, dy), -rb_hh);

        float closest_x = rb.cx + clamp_x;
        float closest_y = rb.cy + clamp_y;

        float diff_x = closest_x - cb.cx;
        float diff_y = closest_y - cb.cy;

        return (float)Math.sqrt((diff_x * diff_x) + (diff_y * diff_y)) < cb.r;
    }
}
