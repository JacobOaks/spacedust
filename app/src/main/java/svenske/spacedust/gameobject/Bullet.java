package svenske.spacedust.gameobject;

import android.util.Log;

import svenske.spacedust.graphics.BlendMode;
import svenske.spacedust.graphics.LightSource;
import svenske.spacedust.graphics.Sprite;
import svenske.spacedust.physics.PhysicsObject;

// A bullet shot in a specific direction at a specific speed that emits a light
public class Bullet extends GameObject implements LightEmitter, PhysicsObject {

    // The bullet emits a small light.
    private LightSource ls;

    // A deleter (the world) to notify when to delete the bullet (if it hits a ship)
    private ObjectDeleter deleter;

    // Hostile bullets will damage only Player objects. Non-hostiles will damage only Enemy objects.
    private boolean hostile;

    private float damage;

    /**
     * Constructs the bullet.
     * @param color the color to be used for the sprite itself and its glow.
     * @param x the starting x position for the bullet.
     * @param y the starting y position for the bullet.
     * @param v_angle the angle of the bullet's movement.
     * @param v_magnitude the magnitude of the bullet's velocity.
     * @param deleter a reference to a deleter (the World) to use to delete the bullet from the
     */
    public Bullet(float[] color, float x, float y, float v_angle, float v_magnitude,
                  ObjectDeleter deleter, boolean hostile, float damage) {
        super(new Sprite(null, -1, -1, color, BlendMode.JUST_COLOR,
                null, null), x, y);
        this.deleter = deleter;
        this.hostile = hostile;
        this.damage = damage;

        // Set rotation and scale of bullet model
        this.sy = 0.2f;
        this.sx = 0.05f;
        this.rot = v_angle;

        // Set appropriate velocity
        this.vx = (float)Math.cos(v_angle + Math.PI / 2) * v_magnitude;
        this.vy = (float)Math.sin(v_angle + Math.PI / 2) * v_magnitude;

        // Set light source
        this.ls = new LightSource(new float[] { color[0] / 2f, color[1] / 2f, color[2] / 2f },
                0.8f, 1f, null);
    }

    // Return the bullet's light source
    @Override
    public LightSource get_light() { return this.ls; }

    // Since bullets rotate, their bounds are defined by a circle
    @Override
    public float[] get_bounds() {
        return new float[] { this.x, this.y, this.get_size()[1] / 2f };
    }

    // Responds to collisions by dealing damage and removing bullet if collision was with a ship
    @Override
    public void on_collide(PhysicsObject other) {
        if (other instanceof Player && this.hostile) {       // Enemy bullet hits player
            ((Player)other).deal_damage(this.damage);
            this.deleter.on_object_delete(this);
        } else if (other instanceof Enemy && !this.hostile) { // Player bullet hits enemy
            ((Enemy)other).deal_damage(this.damage);
            this.deleter.on_object_delete(this);
        }
    }
}
