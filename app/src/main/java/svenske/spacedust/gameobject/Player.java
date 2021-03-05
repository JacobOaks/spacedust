package svenske.spacedust.gameobject;

import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import svenske.spacedust.graphics.AnimatedSprite;
import svenske.spacedust.graphics.Animation;
import svenske.spacedust.graphics.BlendMode;
import svenske.spacedust.graphics.LightSource;
import svenske.spacedust.graphics.TextureAtlas;

// A specific GameObject representing the player
public class Player extends GameObject implements JoyStick.JoystickReceiver, LightEmitter,
        AnimatedSprite.FrameChangeCallback {

    /**
     * Joystick data. Important to keep track of because the idea is to point the ship in the
     * direction it is shooting, unless it is not shooting, then to point it in the direction it
     * is accelerating.
     */
    private float acceleration_angle;     // The angle of the left joystick
    private boolean accelerating = false; // Is the left joystick active?
    private boolean shooting = false;     // Is the right joystick active?

    // Acceleration data. The left joystick is used to accelerate in a specific direction.
    private final float max_v = 6f;    // Maximum velocity in one axis (unit/s)
    private final float max_a = 0.13f; // Maximum acceleration in one axis (unit/s^2)
    private float ax, ay;              // Current acceleration in both axes (unit/s^2)

    // Light info
    private LightSource ls;
    private int sprite_light_frame = 6;
    private final float[] default_light_glow = new float[] { 0f, 0f, 0f };
    private final float[] sprite_light_glow = new float[] { 0.12f, 0.12f, -0.12f };

    // Shooting info
    private float shooting_accuracy = 0.97f; // 0 - bullet dir random; 1 - bullet dir perfect
    private float shooting_cooldown = 0.28f; // Cool down between shots (s)
    private float shooting_timer    = 0f;    // Time left before another shot can be fired
    private float bullet_speed      = 16f;   // Bullet speed (units/s)
    private List<Bullet> bullets;            // A reference to the World's list of bullets to add to

    // Health info
    private final float max_health = 10f;     // What's the maximum health I can have?
    private float health = 10f;               // How much health do I currently have?
    private float health_regen_rate = 0.5f;   // How quickly health should regenerate (health/s);
    private float health_regen_cooldown = 5f; // Time after being damaged where regeneration begins
    private float health_regen_counter  = 0f; // Counter to health regeneration beginning
    private Bar hp_bar;                       // A reference to the bar to update with HP changes

    /**
     * Constructs the player with the given atlas and starting position.
     * @param atlas the atlas containing the player textures. The animations are parsed/created
     *              in this class so if the layout on the texture atlas changes, those changes
     *              should be reflected here in creating the correct animations.
     * @param bullets this should be the list of bullets from the World.
     * @param hp_bar a bar to use to reflect the Player's health
     */
    public Player(TextureAtlas atlas, List<Bullet> bullets, Bar hp_bar, float x, float y) {
        super(null, x, y);
        this.setup_sprite(atlas); // Setup animations and sprite
        this.ls = new LightSource(default_light_glow, 5f, 4f, null);
        this.bullets = bullets;
        this.hp_bar = hp_bar;
        this.set_health(this.max_health);
    }

    // Sets up the player's animations and sprite
    private void setup_sprite(TextureAtlas atlas) {

        // Idle animation
        Animation idle = new Animation(0.1f, 12, new int[] { 0 },
                new int[] { 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 2 }, new float[][] { null },
                new BlendMode[] { BlendMode.JUST_TEXTURE });
        Map<String, Animation> anims = new HashMap<>();
        anims.put("idle", idle);
        this.sprite = new AnimatedSprite(atlas, anims, "idle",
                null, null);
        ((AnimatedSprite)this.sprite).set_frame_change_callback(this);
    }

    // Magnifies the player's light on specific frames
    @Override
    public void on_frame_change(int frame) {
        if ((frame + 1) % this.sprite_light_frame == 0) { // If lights blinking on sprite
            this.ls.set_intensity(5.3f);
            this.ls.set_reach(4.2f);
            this.ls.set_glow(this.sprite_light_glow);
        } else { // If no lights blinking on sprite
            this.ls.set_intensity(5f);
            this.ls.set_reach(4f);
            this.ls.set_glow(this.default_light_glow);
        }
    }

    // Applies acceleration and calls GameObject's update
    @Override
    void update(float dt) {

        // Update shooting
        this.shooting_timer -= dt;
        if (this.shooting && this.shooting_timer <= 0f) {
            this.shooting_timer += this.shooting_cooldown;
            this.generate_bullet();
        } else if (this.shooting_timer < 0f) this.shooting_timer = 0f;

        // Update LightSource
        this.ls.update(dt);

        // Update velocity and position
        this.vx = Math.max(-this.max_v, Math.min(this.vx + this.ax, this.max_v));
        this.vy = Math.max(-this.max_v, Math.min(this.vy + this.ay, this.max_v));
        super.update(dt);

        // Update health
        if (this.health_regen_counter > 0f) this.health_regen_counter -= dt;
        else this.heal(this.health_regen_rate * dt);
    }

    // Generates a new bullet shot from the player
    private void generate_bullet() {
        float max_bullet_dir_offset = (float)Math.PI *  (1f - this.shooting_accuracy);
        float bullet_dir_offset = (float)Math.random() * 2 * max_bullet_dir_offset - max_bullet_dir_offset;
        this.bullets.add(new Bullet(new float[] { 0f, 0f, 1f, 1f }, this.x, this.y,
                this.rot + bullet_dir_offset, this.bullet_speed));
        this.deal_damage(0.4f);
    }

    // Sets the Player's health to the given health
    public void set_health(float health) {
        this.health = Math.min(this.max_health, health);
        this.hp_bar.set_fill(this.health / this.max_health);
        if (this.health <= 0f) Log.d("[spdt/player]", "player has died!");
    }

    // Deal the given amount of damage to the player's health
    public void deal_damage(float damage) {
        if (damage < 0f) this.heal(-damage); // Count as healing if damage is negative
        else {
            this.set_health(this.health - damage);
            this.health_regen_counter = health_regen_cooldown; // Reset regeneration counter
        }
    }

    // Heal the given amount of health to the player's health
    public void heal(float health) {
        if (health < 0f) this.deal_damage(-health); // Count as damage if healing is negative
        else this.set_health(this.health + health);
    }

    /**
     * Responds to JoyStick input:
     * - movement stick: sets the appropriate acceleration angle and magnitudes.
     * - rotation: sets the appropriate rotation of the ship so it faces where it shoots.
     */
    @Override
    public void receive_dir_vec(String id, float x, float y, float magnitude) {

        // Movement stick
        if (id.equals("movement")) {
            this.accelerating = true;
            this.acceleration_angle = (float)Math.atan2(y, x) - (float)(Math.PI / 2f);
            if (!this.shooting) this.rot = this.acceleration_angle;
            this.ax = this.max_a * magnitude *
                    (float)Math.cos(this.acceleration_angle + (float)(Math.PI / 2f));
            this.ay = this.max_a * magnitude *
                    (float)Math.sin(this.acceleration_angle + (float)(Math.PI / 2f));

        // Shooting stick
        } else if (id.equals("shooting")) {
            this.shooting = true;
            this.rot = (float)Math.atan2(y, x) - (float)(Math.PI / 2f);
        }
    }

    // Responds to JoyStick input ending by resetting state
    @Override
    public void input_ended(String id) {

        // Movement stick lifted
        if (id.equals("movement")) { // No longer accelerating
            this.accelerating = false;
            this.ax = this.ay = 0f;
        }

        // Shooting stick lifted
        else if (id.equals("shooting")) { // No longer shooting, set rotation to acceleration angle
            this.shooting = false;
            if (this.accelerating) this.rot = this.acceleration_angle;
        }
    }

    // Return the Player's LightSource
    @Override
    public LightSource get_light() { return this.ls; }
}
