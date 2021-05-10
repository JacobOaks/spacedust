package svenske.spacedust.gameobject;

import svenske.spacedust.graphics.BlendMode;
import svenske.spacedust.graphics.LightSource;
import svenske.spacedust.graphics.Sprite;
import svenske.spacedust.physics.PhysicsObject;

/**
 * A bullet:
 * - emits a small light
 * - deletes itself when colliding into a target entity
 * - deals damage to a target entity it collides into
 * - moves at a specified speed in a specified direction
 */
public class Bullet extends GameObject implements LightEmitter, PhysicsObject {

    // Bullet properties
    private LightSource ls;        // Bullet's light
    private ObjectDeleter deleter; // Used to delete bullet when it hits a target ship
    // Hostile bullets will damage only Player objects. Non-hostiles will damage only Enemy objects.
    private boolean hostile;       // Whether the bullet is hostile or not
    private float damage;          // How much damage the bullet does on collision with target ship

    /**
     * Constructs the bullet.
     * @param color the color to be used for the sprite itself and its glow.
     * @param x the starting x position for the bullet.
     * @param y the starting y position for the bullet.
     * @param v_angle the angle of the bullet's movement.
     * @param v_magnitude the magnitude of the bullet's velocity.
     * @param deleter a reference to a deleter (the World) to use to delete the bullet from the
     * @param hostile whether the bullet is hostile. If so, will only harm the player upon collision
     *                and if not, will only harm enemies upon collision
     * @param damage how much damage to deal to target ships upon collision
     */
    public Bullet(float[] color, float x, float y, float v_angle, float v_magnitude,
                  ObjectDeleter deleter, boolean hostile, float damage) {
        super(new Sprite(null, -1, -1, color, BlendMode.JUST_COLOR,
                null, null), x, y);

        // Save/set attributes
        this.deleter = deleter;
        this.hostile = hostile;
        this.damage = damage;
        this.rot = v_angle;
        this.sy = 0.2f;
        this.sx = 0.05f;

        // Set appropriate velocity
        this.vx = (float)Math.cos(v_angle + Math.PI / 2) * v_magnitude;
        this.vy = (float)Math.sin(v_angle + Math.PI / 2) * v_magnitude;

        // Create light source
        this.ls = new LightSource(new float[] { color[0] / 2f, color[1] / 2f, color[2] / 2f },
                0.8f, 1f, null);
    }

    // Responds to collisions by dealing damage and removing bullet if collision was with a ship
    @Override
    public void on_collide(PhysicsObject other) {
        if (other instanceof Player && this.hostile) {       // Enemy bullet hits player
            ((Player)other).damage(this.damage);
            this.deleter.on_object_delete(this);
        }

        /* TODO
        else if (other instanceof Enemy && !this.hostile) { // Player bullet hits enemy
            ((Enemy)other).damage(this.damage);
            this.deleter.on_object_delete(this);
        }
        */
    }

    // Return the bullet's light source
    @Override
    public LightSource get_light() { return this.ls; }

    // Bullet bounds are defined by a circle with diameter equal to their height
    @Override
    public float[] get_bounds() { return new float[] { this.x, this.y, this.get_size()[1] / 2f }; }
}
