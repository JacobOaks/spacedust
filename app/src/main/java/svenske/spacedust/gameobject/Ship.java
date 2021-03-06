package svenske.spacedust.gameobject;

import java.util.HashMap;
import java.util.Map;

import svenske.spacedust.graphics.AnimatedSprite;
import svenske.spacedust.graphics.Animation;
import svenske.spacedust.graphics.BlendMode;
import svenske.spacedust.graphics.LightSource;
import svenske.spacedust.graphics.ShaderProgram;
import svenske.spacedust.graphics.TextureAtlas;
import svenske.spacedust.physics.PhysicsObject;

/**
 * A generic Ship class. Ships have:
 * - health & health regeneration
 * - shooting capability
 * - idle and moving animation
 * - (optionally) overhead health bars
 * - light emitting depending on ship state
 */
public class Ship extends GameObject implements LightEmitter, AnimatedSprite.FrameChangeCallback,
        PhysicsObject {

    // Light info
    private LightSource ls;             // The ship's LightSource's properties change by state
    private int sprite_light_frame = 6; // Which frames of the animation have the lights on?

    // Shooting properties
    protected float bullet_speed      = 16f;   // Bullet speed (units/s)
    protected float bullet_damage     = 1.0f;  // Bullet damage
    protected float shooting_accuracy = 0.95f; // 0 - random bullet dir; 1 - perfect bullet dir;
    protected float shooting_cooldown = 0.4f;  // Minimum time in between shots
    protected float shooting_timer    = 0.0f;  // Timer for shooting

    // Health info
    private final float health_regen_cooldown = 5f;  // Time after damage where regeneration begins
    private final float health_regen_rate     = 1f;  // How quickly health regenerates (health/s);
    private final float max_health            = 10f; // Maximum health the ship can have
    private float health                      = 10f; // How much health the ship currently has
    private float health_regen_counter        = 0f;  // Counter to health regeneration beginning
    private Bar overhead_hp_bar;                     // An HP bar rendered above the ship.

    // Movement info
    protected float max_v    = 5.0f;  // Maximum velocity the ship can go
    protected boolean moving = false; // Flag denoting whether the ship is moving

    // Object creator/deleter callbacks
    private final ObjectCreator obj_creator; // Used to create bullets in the world
    private final ObjectDeleter obj_deleter; // Used to delete the ship if it dies

    /**
     * Constructs the ship
     * @param atlas the atlas containing the ship textures
     * @param atlas_row the first row of the ship's sprites in the atlas
     * @param health_bar whether to give the Ship an overhead health bar
     */
    public Ship(TextureAtlas atlas, int atlas_row, float x, float y, boolean health_bar,
                ObjectCreator obj_creator, ObjectDeleter obj_deleter) {
        super(null, x, y);

        // Create sprite and light source
        this.setup_sprite(atlas, atlas_row);
        this.ls = new LightSource(new float[] { 0f, 0f, 0f }, 5f, 4f, null);

        // Save object creator and deleter
        this.obj_creator = obj_creator;
        this.obj_deleter = obj_deleter;

        // Setup health
        this.set_health(this.max_health);
        if (health_bar) {
            this.overhead_hp_bar = new Bar(new float[]{0f, 1f, 0f, 0.5f}, new float[]{1f, 0f, 0f, 1f},
                    new float[]{0.5f, 0.5f, 0.5f, 0.5f}, 1.4f, 0.1f, this.x, this.y);
            this.update_health_bar();
        }
    }

    // Updates the ship's overhead health bar's position if there is one
    private void update_health_bar() {
        if (this.overhead_hp_bar != null) {
            this.overhead_hp_bar.x = this.x;
            this.overhead_hp_bar.y = this.y + this.get_size()[1] / 2 + 0.15f;
        }
    }

    // Sets up the ships's animations and sprite
    private void setup_sprite(TextureAtlas atlas, int atlas_row) {
        Map<String, Animation> anims = new HashMap<>();

        // Idle animation
        Animation idle = new Animation(0.1f, 12, new int[] { atlas_row },
                new int[] { 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 2 }, new float[][] { null },
                new BlendMode[] { BlendMode.JUST_TEXTURE });
        anims.put("idle", idle);

        // Accelerating animation
        Animation accelerating = new Animation(0.1f, 12, new int[] { atlas_row + 1 },
                new int[] { 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 2 }, new float[][] { null },
                new BlendMode[] { BlendMode.JUST_TEXTURE });
        anims.put("moving", accelerating);

        // Create sprite
        this.sprite = new AnimatedSprite(atlas, anims, "idle",
                null, null);
        ((AnimatedSprite)this.sprite).set_frame_change_callback(this);
    }

    // Responds to animation frame changes by appropriately setting light information
    @Override
    public void on_frame_change(int frame) {

        // Baseline lighting settings
        float reach = 3f;
        float intensity = 3f;
        float[] glow = new float[] { 0f, 0f, 0f };

        // If light is on in Sprite, add a little yellow light
        if ((frame + 1) % this.sprite_light_frame == 0) {
            reach += 0.1f;
            intensity += 0.1f;
            glow[0] += 0.2f;
            glow[1] += 0.2f;
            glow[2] -= 0.2f;
        }

        // If moving, add some orange light from the engines
        if (this.moving) {
            reach += 0.1f;
            intensity += 0.1f;
            glow[0] += 0.2f;
        }

        // Set final light information
        this.ls.set_reach(reach);
        this.ls.set_glow(glow);
        this.ls.set_intensity(intensity);
    }

    /**
     * Updates the ship:
     * - update the light source
     * - update position
     * - update overhead health bar
     * - update the animation
     * - update the health regeneration counter, or regenerate health if cool-down over
     */
    @Override
    void update(float dt) {

        // Update LightSource
        this.ls.update(dt);

        // Update position and sprite
        super.update(dt);

        // Update overhead health bar
        this.update_health_bar();

        // Update health
        if (this.health_regen_counter > 0f) this.health_regen_counter -= dt;
        else this.heal(this.health_regen_rate * dt);

        // Update shooting timer
        if (this.shooting_timer > 0f) this.shooting_timer -= dt;
    }

    // Renders the ship and its overhead bar if it has one
    @Override
    void render(ShaderProgram sp) {
        super.render(sp);
        if (this.overhead_hp_bar != null) this.overhead_hp_bar.render(sp);
    }

    /**
     * Generates a new bullet traveling in the direction of the ship's rotation if the shooting
     * cooldown is over
     * @param hostile whether the bullet is hostile (harms the player) or not (harms enemies)
     */
    public void shoot(boolean hostile) {

        // Make sure cool-down is over and reset it if it is
        if (this.shooting_timer > 0f) return;
        this.shooting_timer = this.shooting_cooldown;

        // Figure out an accuracy offset
        float max_offset = (float)Math.PI *  (1f - this.shooting_accuracy);
        float offset = (float)Math.random() * 2 * max_offset - max_offset;

        // Create bullet
        Bullet b = new Bullet(new float[] { 0.5f, 0.5f, 0.5f, 1f }, this.x, this.y,
                this.rot + offset, this.bullet_speed, this.obj_deleter, hostile,
                this.bullet_damage);
        this.obj_creator.on_object_create(b);
    }

    // Sets the ship's health to the given health
    public void set_health(float health) {
        this.health = Math.min(this.max_health, health);
        if (this.overhead_hp_bar != null)       // Update health bar
            this.overhead_hp_bar.set_fill(this.health / this.max_health);
        if (this.health <= 0f) this.has_died(); // Check for death
    }

    // Deletes the ship when it dies
    protected void has_died() {
        this.obj_deleter.on_object_delete(this);
    }

    // Deal the given amount of damage to the ship's health
    public void deal_damage(float damage) {
        if (damage < 0f) this.heal(-damage); // Count as healing if damage is negative
        else {
            this.set_health(this.health - damage);
            this.health_regen_counter = health_regen_cooldown; // Reset regeneration counter
        }
    }

    // Heal the given amount of health to the ship's health
    public void heal(float health) {
        if (health < 0f) this.deal_damage(-health); // Count as damage if healing is negative
        else this.set_health(this.health + health);
    }

    // Responds to velocity setting by bounding the velocity, updating animation, and rotating
    @Override
    public void set_velocity(float vx, float vy) {
        super.set_velocity(
                Math.max(Math.min(vx, this.max_v), -this.max_v),
                Math.max(Math.min(vy, this.max_v), -this.max_v));
        if (vx == 0 && vy == 0) this.stop();
        else {
            if (!this.moving)
                ((AnimatedSprite)this.sprite).change_animation("moving", true);
            this.moving = true;
            this.rot = ((float)Math.atan2(vy, vx)) - (float)Math.PI / 2f;
        }
    }

    // Stops the ship completely
    public void stop() {
        this.vx = this.vy = 0f;
        if (this.moving) {
            ((AnimatedSprite) this.sprite).change_animation("idle", true);
            this.moving = false;
        }
    }

    // Return the ship's LightSource
    @Override
    public LightSource get_light() { return this.ls; }

    // Ship bounds are defined by a circle with radius 0.45f around the ship's center
    @Override
    public float[] get_bounds() {
        float[] size = this.get_size();
        return new float[] { this.x, this.y, size[0] * 0.45f};
    }

    // Return the ship's health
    public float get_health() { return this.health; }

    // Return the ship's maximum health
    public float get_max_health() { return this.max_health; }

    // Collisions with bullets are handled in the bullet's on_collide method
    @Override
    public void on_collide(PhysicsObject other) {}
}