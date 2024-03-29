package svenske.spacedust.gameobject;

import svenske.spacedust.graphics.ShaderProgram;
import svenske.spacedust.graphics.Sprite;

/**
 * A generic class that describes anything that:
 * - can be updated
 * - can be rendered
 * - has position
 * - has scale
 * - has rotation
 * - has non-rotated size
 * - has velocity
 *
 * GameObjects should assume they live in aspect/world space (can be thought of as equivalent in
 * this case).
 *
 * This class is very bare with the intent being that it is the root of a complex inheritance tree.
 *
 * Extending classes should make sure to:
 * - define any important positions relative to the GameObject's main position (so they stay synced)
 * - call superclass versions of methods
 */
public class GameObject {

    // Attributes
    protected Sprite sprite; // The GameObject's sprite
    protected float x, y;    // The position of the GameObject
    protected float sx, sy;  // The scale of the GameObject
    protected float vx, vy;  // The velocity of the GameObject in units / second
    protected float rot;     // The rotation of the GameObject in radians

    // Constructs the GameObject with the given Sprite (may be null) and position.
    public GameObject(Sprite sprite, float x, float y) {
        this.sprite = sprite;
        this.x = x;
        this.y = y;
        this.sx = this.sy = 1f;
        this.vx = this.vy = 0f;
    }

    // Updates the GameObject's Sprite if not null, and the GameObject's position
    void update(float dt) {
        if (this.sprite != null) this.sprite.update(dt);
        this.x += this.vx * dt;
        this.y += this.vy * dt;
    }

    // Renders the GameObject's sprite at its position using the given ShaderProgram
    void render(ShaderProgram sp) {
        if (this.sprite != null) this.sprite.render(sp, this.x, this.y, this.sx, this.sy, this.rot);
    }

    // Updates the GameObject's position
    public void set_pos(float x, float y) {
        this.x = x;
        this.y = y;
    }

    // Updates the GameObject's scale
    public void set_scale(float sx, float sy) {
        this.sx = sx;
        this.sy = sy;
    }

    // Updates the GameObject's velocity
    public void set_velocity(float vx, float vy) {
        this.vx = vx;
        this.vy = vy;
    }

    // Return the GameObject's current position
    public float[] get_pos() { return new float[] { this.x, this.y }; }

    // Return the GameObject's full velocity
    public float get_full_v() { return (float)Math.sqrt((this.vx * this.vx) + (this.vy * this.vy)); }

    /**
     * @return the GameObject's size.
     *
     * NOTE: This assumes a rotation of zero, which may not be the case for this GameObject.
     */
    public float[] get_size() {
        if (this.sprite != null) {
            float[] sprite_size = this.sprite.get_size();
            return new float[] { sprite_size[0] * this.sx, sprite_size[1] * this.sy };
        } else return new float[]{0f, 0f};
    }

    // Return the GameObject's Sprite (may be null)
    public Sprite get_sprite() { return this.sprite; }
}
