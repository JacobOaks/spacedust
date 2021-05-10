package svenske.spacedust.gameobject;

import svenske.spacedust.graphics.AnimatedSprite;
import svenske.spacedust.graphics.Animation;
import svenske.spacedust.graphics.LightSource;
import svenske.spacedust.graphics.TextureAtlas;
import svenske.spacedust.physics.PhysicsObject;

// TODO: set particles

/**
 * A player is an entity that:
 * - is controlled by a movement and a shooting joystick
 * - has acceleration in addition to position and velocity
 */
public class Player extends Entity implements JoyStick.JoystickReceiver,
        AnimatedSprite.FrameChangeCallback, LightEmitter {

    // Movement info
    private float ax, ay;                   // Current player acceleration
    private float acceleration_angle;       // Current angle of acceleration (angle of left joystick)
    private final float max_a = 0.13f;      // Maximum acceleration in one axis (unit/s^2)
    private final float max_v = 4f;         // Maximum velocity in one axis (unit/s)
    protected boolean accelerating = false; // Flag denoting whether the player is accelerating

    // Shooting info
    protected float bullet_speed            = 16f;   // Bullet speed (units/s)
    protected float bullet_damage           = 1.0f;  // Bullet damage
    protected float shooting_accuracy       = 0.95f; // 0 - random bullet dir; 1 - perfect bullet dir;
    protected float shooting_cooldown       = 0.4f;  // Minimum time in between shots
    protected float shooting_cooldown_timer = 0.0f;  // Timer for shooting
    private boolean shooting; // Whether player is currently shooting (right joystick in use)

    // Other info
    private LightSource light_source; // Player's light source
    private Bar hud_hp_bar;           // Reference to an HP bar on the HUD to update

    /**
     * Constructs the player
     * @param atlas the atlas containing the ship textures
     * @param hp_bar a health bar (ideally displayed on the HUD) for the player to update
     * The rest of the attributes are the same as in the superclass constructor
     */
    public Player(TextureAtlas atlas, float x, float y, Bar hp_bar, World world) {

        // Call super, setup animation and sprite
        super(new AnimatedSprite(atlas, Animation.get_generic_ship_animations(0),
                "idle", null, null), x, y, "Player",
                20f, 1f, 5f, world);
        ((AnimatedSprite)this.sprite).set_frame_change_callback(this);

        // Create light source
        this.light_source = new LightSource(new float[] { 0f, 0f, 0f }, 5f, 4f, null);

        // Save reference to HUD HP bar
        this.hud_hp_bar = hp_bar; // Use HUD health bar instead of overhead one
    }

    // Responds to joystick input by updating movement or shooting activity
    @Override
    public void receive_dir_vec(String id, float x, float y, float magnitude) {

        // Movement stick
        if (id.equals("movement")) {

            // Change animations if movement just started
            if (!this.accelerating)
                ((AnimatedSprite)this.sprite).change_animation("thrust", true);

            // Set rotation angle to acceleration angle if not shooting
            this.acceleration_angle = (float)Math.atan2(y, x) - (float)(Math.PI / 2f);
            if (!this.shooting) this.rot = this.acceleration_angle;
            this.accelerating = true;

            // Calculate both components of acceleration
            this.ax = this.max_a * magnitude *
                    (float)Math.cos(this.acceleration_angle + (float)(Math.PI / 2f));
            this.ay = this.max_a * magnitude *
                    (float)Math.sin(this.acceleration_angle + (float)(Math.PI / 2f));

        // Shooting stick
        } else if (id.equals("shooting")) {
            this.rot = (float)Math.atan2(y, x) - (float)(Math.PI / 2f); // Face shooting direction
            this.shooting = true;                                       // Flag that shooting now
        }
    }

    // Updates the player
    @Override
    public void update(float dt) {

        // Update light source
        this.light_source.update(dt);

        // Update velocity and all super components
        this.vx = Math.max(-this.max_v, Math.min(this.vx + this.ax, this.max_v));
        this.vy = Math.max(-this.max_v, Math.min(this.vy + this.ay, this.max_v));
        super.update(dt);

        // Attempt to shoot if shooting
        if (this.shooting_cooldown_timer > 0f) this.shooting_cooldown_timer -= dt;
        if (this.shooting) this.shoot();
    }

    // Generates a new bullet traveling in the direction of the player's rotation
    public void shoot() {

        // Make sure cool-down is over and reset it if it is
        if (this.shooting_cooldown_timer > 0f) return;
        this.shooting_cooldown_timer = this.shooting_cooldown;

        // Figure out an accuracy offset
        float max_offset = (float)Math.PI *  (1f - this.shooting_accuracy);
        float offset = (float)Math.random() * 2 * max_offset - max_offset;

        // Create bullet
        Projectile b = Projectile.create_bullet(new float[] { 0.5f, 0.5f, 0.5f, 1f }, this.x, this.y,
                this.rot + offset, this.bullet_speed, this.world, false,
                this.bullet_damage);
        this.world.on_object_create(b);
    }

    // Responds to JoyStick input ending by updating player state
    @Override
    public void input_ended(String id) {

        // Movement stick lifted
        if (id.equals("movement")) {// No longer accelerating
            this.accelerating = false;
            this.ax = this.ay = 0f;
            ((AnimatedSprite)this.sprite).change_animation("idle", true);

            // Shooting stick lifted
        } else if (id.equals("shooting")) { // No longer shooting
            this.shooting = false;
            if (this.accelerating) this.rot = this.acceleration_angle;
        }
    }

    // Player projectile interaction handled in projectile collision method
    @Override
    public void on_collide(PhysicsObject other) {}

    // Responds to frame changes by setting adjusting the player's light source
    @Override
    public void on_frame_change(int new_frame) {

        // Baseline lighting settings
        float reach = 3f;
        float intensity = 3f;
        float[] glow = new float[] { 0f, 0f, 0f };

        // If light is on in Sprite, add a little yellow light
        if ((new_frame + 1) % Animation.GENERIC_SHIP_ANIMATION_LIGHT_INTERVAL == 0) {
            reach += 0.1f;
            intensity += 0.1f;
            glow[0] += 0.2f;
            glow[1] += 0.2f;
            glow[2] -= 0.2f;
        }

        // If moving, add some orange light from the engines
        if (this.accelerating) {
            reach += 0.1f;
            intensity += 0.1f;
            glow[0] += 0.2f;
        }

        // Set final light information
        this.light_source.set_reach(reach);
        this.light_source.set_glow(glow);
        this.light_source.set_intensity(intensity);
    }

    // Sets the player's health and updates the HUD health bar's fill
    @Override
    public void set_health(float health) {
        super.set_health(health);
        if (this.hud_hp_bar != null)
            this.hud_hp_bar.set_fill(this.get_hp() / this.get_max_hp());
    }

    // Sets velocity bounded by player's maximum velocity
    @Override
    public void set_velocity(float vx, float vy) {
        super.set_velocity(
                Math.max(Math.min(vx, this.max_v), -this.max_v),
                Math.max(Math.min(vy, this.max_v), -this.max_v));
    }

    // Return the player's LightSource
    @Override
    public LightSource get_light() { return this.light_source; }
}
