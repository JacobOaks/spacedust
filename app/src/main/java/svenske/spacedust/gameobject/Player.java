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

    // State data
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

    // Shooting info
    private float shooting_accuracy = 0.97f; // 0 - bullet dir random; 1 - bullet dir perfect
    private float shooting_cooldown = 0.28f; // Cool down between shots (s)
    private float shooting_timer    = 0f;    // Time left before another shot can be fired (s)
    private float bullet_speed      = 16f;   // Bullet speed (units/s)
    private List<Bullet> bullets;            // A reference to the World's list of bullets to add to

    // Health info
    private final float max_health = 10f;     // What's the maximum health I can have?
    private float health = 10f;               // How much health do I currently have?
    private float health_regen_rate = 1f;     // How quickly health should regenerate (health/s);
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
        this.ls = new LightSource(new float[] { 0f, 0f, 0f }, 5f, 4f, null);
        this.bullets = bullets;
        this.hp_bar = hp_bar;
        this.set_health(this.max_health);
    }

    // Sets up the player's animations and sprite
    private void setup_sprite(TextureAtlas atlas) {
        Map<String, Animation> anims = new HashMap<>();

        // Idle animation
        Animation idle = new Animation(0.1f, 12, new int[] { 0 },
                new int[] { 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 2 }, new float[][] { null },
                new BlendMode[] { BlendMode.JUST_TEXTURE });
        anims.put("idle", idle);

        // Shooting animation
        Animation shooting = new Animation(this.shooting_cooldown / 6f, 7,
                new int[] { 0 }, new int[] { 3, 0, 0, 0, 0, 0, 0 }, new float[][] { null },
                new BlendMode[] { BlendMode.JUST_TEXTURE });
        anims.put("shooting", shooting);

        // Accelerating animation
        Animation accelerating = new Animation(0.1f, 12, new int[] { 1 },
                new int[] { 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 2 }, new float[][] { null },
                new BlendMode[] { BlendMode.JUST_TEXTURE });
        anims.put("accelerating", accelerating);

        // Shooting and accelerating animation
        Animation shooting_and_accelerating = new Animation(this.shooting_cooldown / 6f, 6,
                new int[] { 1 }, new int[] { 3, 0, 0, 0, 0, 0 }, new float[][] { null },
                new BlendMode[] { BlendMode.JUST_TEXTURE });
        anims.put("shooting_and_accelerating", shooting_and_accelerating);

        // Create sprite
        this.sprite = new AnimatedSprite(atlas, anims, "idle",
                null, null);
        ((AnimatedSprite)this.sprite).set_frame_change_callback(this);
    }

    // Responds to frame changes by appropriately setting light information, and generating bullets
    @Override
    public void on_frame_change(int frame) {

        // Baseline lighting settings
        float reach = 3f;
        float intensity = 3f;
        float[] glow = new float[] { 0f, 0f, 0f };

        // If a shot is fired, emity a powerful orange light
        if (this.shooting && frame == 1) {
            reach += 0.5f;
            intensity += 0.5f;
            glow[0] += 0.7f;
            glow[2] -= 0.2f;

        // If the little flashing lights come on, add a little yellow light
        } else if (!this.shooting && (frame + 1) % this.sprite_light_frame == 0) {
            reach += 0.1f;
            intensity += 0.1f;
            glow[0] += 0.2f;
            glow[1] += 0.2f;
            glow[2] -= 0.2f;
        }

        // If accelerating, add some orange light from the engines
        if (this.accelerating) {
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
     * Updates the player:
     * - update the light source
     * - update velocity and position
     * - update the animation
     * - update shooting timer
     * - update the health regeneration counter, or regenerate health if cool-down over
     */
    @Override
    void update(float dt) {

        // Update LightSource
        this.ls.update(dt);

        // Update velocity, position, sprite
        this.vx = Math.max(-this.max_v, Math.min(this.vx + this.ax, this.max_v));
        this.vy = Math.max(-this.max_v, Math.min(this.vy + this.ay, this.max_v));
        super.update(dt);

        // Update shooting timer
        if (this.shooting) this.shooting_timer -= dt;
        if (this.shooting && this.shooting_timer <= 0f) this.generate_bullet();

        // Update health
        if (this.health_regen_counter > 0f) this.health_regen_counter -= dt;
        else this.heal(this.health_regen_rate * dt);
    }

    // Generates a new bullet shot from the player
    private void generate_bullet() {

        // Figure out an accuracy offset
        float max_offset = (float)Math.PI *  (1f - this.shooting_accuracy);
        float offset = (float)Math.random() * 2 * max_offset - max_offset;

        // Create bullet
        this.bullets.add(new Bullet(new float[] { 0.5f, 0.5f, 0.5f, 1f }, this.x, this.y,
                this.rot + offset, this.bullet_speed));
        this.deal_damage(0.4f); // TODO: remove
        this.shooting_timer += this.shooting_cooldown; // Reset cool-down
        ((AnimatedSprite)this.sprite).switch_frames(0);
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
     * - Movement stick: accelerate in appropriate direction and face shit appropriate direction if
     *   not also shooting. Update state to ensure smooth animation transitions
     * - Shooting stick: face the ship in the direction of shooting and update the state to ensure
     *   smooth animation transition
     */
    @Override
    public void receive_dir_vec(String id, float x, float y, float magnitude) {

        // Movement stick
        if (id.equals("movement")) {
            this.set_state(true, this.shooting);

            // Set rotation angle to acceleration angle if not shooting
            this.acceleration_angle = (float)Math.atan2(y, x) - (float)(Math.PI / 2f);
            if (!this.shooting) this.rot = this.acceleration_angle;

            // Calculate both components of acceleration
            this.ax = this.max_a * magnitude *
                    (float)Math.cos(this.acceleration_angle + (float)(Math.PI / 2f));
            this.ay = this.max_a * magnitude *
                    (float)Math.sin(this.acceleration_angle + (float)(Math.PI / 2f));

        // Shooting stick
        } else if (id.equals("shooting")) {
            this.rot = (float)Math.atan2(y, x) - (float)(Math.PI / 2f);
            this.set_state(this.accelerating, true);
        }
    }

    // Responds to JoyStick input ending by updating player state
    @Override
    public void input_ended(String id) {

        // Movement stick lifted
        if (id.equals("movement")) // No longer accelerating
            this.set_state(false, this.shooting);

        // Shooting stick lifted
        else if (id.equals("shooting")) // No longer shooting
            this.set_state(this.accelerating, false);
    }

    /**
     * If the given state differs from the current player state, this method will ensure a smooth/
     * correct transition between the two states in terms of animation and other properties
     */
    private void set_state(boolean accelerating, boolean shooting) {

        // Ignore if state isn't changing
        if (accelerating == this.accelerating && shooting == this.shooting) return;

        // What's the correct animation given the new state?
        String correct_animation = "idle";
        if (accelerating && shooting)  correct_animation = "shooting_and_accelerating";
        else if (accelerating) correct_animation = "accelerating";
        else if (shooting) correct_animation = "shooting";

        // Change animation
        ((AnimatedSprite)this.sprite).change_animation(correct_animation, true);

        // Revert to acceleration angle if accelerating while shooting stops
        if (this.shooting && !shooting && this.accelerating)
            this.rot = this.acceleration_angle;

        // Set acceleration to 0 if acceleration has stopped
        if (this.accelerating && !accelerating) this.ax = this.ay = 0;

        // Update state flags
        this.accelerating = accelerating;
        this.shooting = shooting;
    }

    // Return the Player's LightSource
    @Override
    public LightSource get_light() { return this.ls; }
}
