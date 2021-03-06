package svenske.spacedust.physics;

public interface Bounds {

    boolean within(float x, float y);

    boolean collides(Bounds other);

    void set_center(float cx, float cy);

}
