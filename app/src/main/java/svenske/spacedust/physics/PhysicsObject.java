package svenske.spacedust.physics;

// A generic interface for anything to be considered collidable
public interface PhysicsObject {

    /**
     * @return a float array representing the bounds/hitbox of the object. This can be defined in
     * two different ways:
     *  - Circular bounds:
     *      - length-3 array [center_x, center_y, radius]
     *  - Rectangular bounds:
     *      - length-4 array [center_x, center_y, width, height]
     */
    float[] get_bounds();

    // Gets called when the object collides with another (other) object
    void on_collide(PhysicsObject other);
}
