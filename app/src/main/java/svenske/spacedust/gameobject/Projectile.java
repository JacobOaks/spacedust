package svenske.spacedust.gameobject;

import svenske.spacedust.gameobject.NPC.NPC;
import svenske.spacedust.graphics.BlendMode;
import svenske.spacedust.graphics.LightSource;
import svenske.spacedust.graphics.Sprite;
import svenske.spacedust.physics.PhysicsObject;

/**
 * A projectile is a game object that:
 * - may emit a small light
 * - deletes itself when colliding into a target entity
 * - deal damages to a target entity it collides into
 * - can apply a status effect to a target entity it collides into
 * - moves at a specified speed in a specified direction
 */
public class Projectile extends GameObject implements LightEmitter, PhysicsObject {

    // Creates a generic bullet projectile
    public static Projectile create_bullet(float[] color, float x, float y, float v_angle,
                                           float v_magnitude, World world,
                                           boolean hostile, float damage) {

        // Create sprite, light source, and projectile
        Sprite b_sprite = new Sprite(null, -1, -1, color,
                BlendMode.JUST_COLOR, null, null);
        LightSource b_ls = new LightSource(new float[] { color[0] / 2f, color[1] / 2f, color[2] / 2f },
                0.8f, 1f, null);
        Projectile b = new Projectile(b_sprite, b_ls, x, y, v_angle, v_magnitude, world, hostile, damage);

        // Scale and return bullet projectile
        b.set_scale(0.05f, 0.2f);
        return b;
    }

    // Projectile properties
    private LightSource light_source;
    private World world; // Used to delete projectile when it hits a target
    private boolean hostile;                   // Whether the bullet is hostile or not
    private float damage;                      // How much the projectile damages target entity on hit

    /**
     * Constructs the projectile.
     * @param light_source a light source to give the bullet
     * @param v_angle the angle of the bullet's movement
     * @param v_magnitude the magnitude of the bullet's velocity
     * @param world a reference to the world to use to delete the bullet
     * @param hostile whether the bullet is hostile. If so, will only harm the player upon collision
     *                and if not, will only harm enemies upon collision
     * @param damage how much damage to deal to target ships upon collision
     * The rest of the arguments are the same as those in the superclass constructor
     */
    public Projectile(Sprite sprite, LightSource light_source, float x, float y, float v_angle,
                      float v_magnitude, World world, boolean hostile, float damage) {
        super(sprite, x, y);

        // Save/set attributes
        this.world        = world;
        this.hostile      = hostile;
        this.damage       = damage;
        this.rot          = v_angle;
        this.light_source = light_source;

        // Set appropriate velocity
        this.vx = (float)Math.cos(v_angle + Math.PI / 2) * v_magnitude;
        this.vy = (float)Math.sin(v_angle + Math.PI / 2) * v_magnitude;
    }

    // Responds to collisions by dealing damage and removing projectile if collision was with target
    @Override
    public void on_collide(PhysicsObject other) {

        // Enemy projectile hits player
        if (other instanceof Player && this.hostile) {
            ((Player)other).damage(this.damage);
            this.world.on_object_delete(this);
        }

        // Player projectile hits enemy
        else if (other instanceof NPC && !this.hostile) {
            ((NPC)other).damage(this.damage);
            this.world.on_object_delete(this);
        }
    }

    // Return the projectile's light source
    @Override
    public LightSource get_light() { return this.light_source; }

    /**
     *
     * @return bounds for the projectile.
     * Projectile bounds are defined by a circle as wide as their small dimension.
     * In general, this should function correctly, even with oblong items.
     */
    @Override
    public float[] get_bounds() {
        float[] size = this.get_size();
        float min_dimension_radius = Math.min(size[0], size[1]) / 2;
        return new float[]{this.x, this.y, min_dimension_radius};
    }
}
