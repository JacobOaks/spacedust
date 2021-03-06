package svenske.spacedust.gameobject;

import java.util.List;

import svenske.spacedust.graphics.AnimatedSprite;
import svenske.spacedust.graphics.TextureAtlas;

/**
 * Players are an extension of Ships that use Joystick input to be controlled.
 *
 * Players count accelerating as "moving", and the movement JoyStick directly controls acceleration,
 * not velocity.
 */
public class Player extends Ship implements JoyStick.JoystickReceiver {

    // Acceleration info
    private float ax, ay;
    private float acceleration_angle;
    private final float max_a = 0.13f; // Maximum acceleration in one axis (unit/s^2)

    // Additional shooting info
    private boolean shooting;
    private float shooting_cooldown = 0.28f; // Cool down between shots (s)
    private float shooting_timer    = 0f;    // Time left before another shot can be fired (s)

    // Other info
    private Bar hud_hp_bar;

    /**
     * Constructs the player
     *
     * @param atlas the atlas containing the ship textures
     * @param bullets this should be the list of bullets from the World.
     * @param hp_bar a health bar (ideally displayed on the HUD) for the player to update
     */
    public Player(TextureAtlas atlas, List<Bullet> bullets, float x, float y, Bar hp_bar) {
        super(atlas,0, bullets, x, y, false);
        this.hud_hp_bar = hp_bar;
        this.set_health(this.get_max_health());
        this.max_v = 6.0f;
    }

    @Override
    public void receive_dir_vec(String id, float x, float y, float magnitude) {

        // Movement stick
        if (id.equals("movement")) {

            // Change animations if movement just started
            if (!this.moving)
                ((AnimatedSprite)this.sprite).change_animation("moving", true);

            // Set rotation angle to acceleration angle if not shooting
            this.acceleration_angle = (float)Math.atan2(y, x) - (float)(Math.PI / 2f);
            if (!this.shooting) this.rot = this.acceleration_angle;
            this.moving = true;

            // Calculate both components of acceleration
            this.ax = this.max_a * magnitude *
                    (float)Math.cos(this.acceleration_angle + (float)(Math.PI / 2f));
            this.ay = this.max_a * magnitude *
                    (float)Math.sin(this.acceleration_angle + (float)(Math.PI / 2f));

        // Shooting stick
        } else if (id.equals("shooting")) {
            this.rot = (float)Math.atan2(y, x) - (float)(Math.PI / 2f);
            this.shooting = true;
        }
    }

    @Override
    void update(float dt) {

        // Update velocity and all super components
        this.vx = Math.max(-this.max_v, Math.min(this.vx + this.ax, this.max_v));
        this.vy = Math.max(-this.max_v, Math.min(this.vy + this.ay, this.max_v));
        super.update(dt);

        // Update shooting timer
        if (this.shooting) this.shooting_timer -= dt;
        if (this.shooting && this.shooting_timer <= 0f) {
            this.shoot();
            this.shooting_timer += this.shooting_cooldown;
        }
    }

    @Override
    public void set_health(float health) {
        super.set_health(health);
        if (this.hud_hp_bar != null)
            this.hud_hp_bar.set_fill(this.get_health() / this.get_max_health());
    }

    @Override
    public void set_velocity(float vx, float vy) {
        super.set_velocity(
                Math.max(Math.min(vx, this.max_v), -this.max_v),
                Math.max(Math.min(vy, this.max_v), -this.max_v));
    }

    @Override
    public void stop() {
        this.ax = this.ay = 0f;
        super.stop();
    }

    // Responds to JoyStick input ending by updating player state
    @Override
    public void input_ended(String id) {

        // Movement stick lifted
        if (id.equals("movement")) {// No longer accelerating
            this.moving = false;
            this.ax = this.ay = 0f;
            ((AnimatedSprite)this.sprite).change_animation("idle", true);

        // Shooting stick lifted
        } else if (id.equals("shooting")) { // No longer shooting
            this.shooting = false;
            if (this.moving) this.rot = this.acceleration_angle;
        }
    }
}
