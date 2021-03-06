package svenske.spacedust.physics;

import android.graphics.Rect;
import android.util.Log;

public class RectangularBounds implements Bounds {

    float cx, cy;
    float w, h;

    public RectangularBounds(float cx, float cy, float w, float h) {
        this.cx = cx;
        this.cy = cy;
        this.w = w;
        this.h = h;
    }

    @Override
    public void set_center(float cx, float cy) {
        this.cx = cx;
        this.cy = cy;
    }

    @Override
    public boolean within(float x, float y) {
        if (x < this.cx + w / 2 && x > this.cx - w / 2)
            return (y < this.cy + h / 2 && y > this.cy - h / 2);
        return false;
    }

    @Override
    public boolean collides(Bounds other) {
        if (other instanceof RectangularBounds) {
            RectangularBounds other_rb = (RectangularBounds)other;
            return  this.cx < other_rb.cx + other_rb.w &&
                    this.cx + this.w > other_rb.cx &&
                    this.cy < other_rb.cy + other_rb.h &&
                    this.cy + this.h > other_rb.cy;
        } else if (other instanceof CircularBounds) {
            return PhysicsEngine.rb_cb_collision(this, (CircularBounds)other);
        } else {
            Log.d("spdt/rectangularbounds", "unsupported bounds type for collision");
            return false;
        }
    }
}
