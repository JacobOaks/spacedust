package svenske.spacedust.physics;

import android.util.Log;

public class CircularBounds implements Bounds {


    float cx, cy;
    float r;

    public CircularBounds(float cx, float cy, float r) {
        this.cx = cx;
        this.cy = cy;
        this.r = r;
    }

    @Override
    public void set_center(float cx, float cy) {
        this.cx = cx;
        this.cy = cy;
    }

    @Override
    public boolean within(float x, float y) {
        float dx = x - this.cx;
        float dy = y - this.cy;
        float d = (float)Math.sqrt((dx * dx) + (dy * dy));
        return d <= this.r;
    }

    @Override
    public boolean collides(Bounds other) {
        if (other instanceof CircularBounds) {
            CircularBounds other_cb = (CircularBounds)other;
            float dx = other_cb.cx - this.cx;
            float dy = other_cb.cy - this.cy;
            float d = (float)Math.sqrt((dx * dx) + (dy * dy));
            return d <= (this.r + other_cb.r);
        } else if (other instanceof RectangularBounds) {
            return PhysicsEngine.rb_cb_collision((RectangularBounds)other, this);
        } else {
            Log.d("spdt/circularbounds", "unsupported bounds type for collision");
            return false;
        }
    }

}
