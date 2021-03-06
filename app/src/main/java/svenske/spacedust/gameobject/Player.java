package svenske.spacedust.gameobject;

import svenske.spacedust.graphics.AnimatedSprite;
import svenske.spacedust.graphics.TextureAtlas;

/**
 * A player is a ship that:
 * - is controlled by JoySticks
 * - counts movement as acceleration rather than velocity
 */
public class Player extends Ship implements JoyStick.JoystickReceiver {

    // Acceleration info
    private float ax, ay;              // Current player acceleration
    private float acceleration_angle;  // Current angle of acceleration (angle of left joystick)
    private final float max_a = 0.13f; // Maximum acceleration in one axis (unit/s^2)

    // Other info
    private boolean shooting; // Whether player is currently shooting (right joystick in use)
    private Bar hud_hp_bar;   // Reference to an HP bar on the HUD to update

    /**
     * Constructs the player
     * @param atlas the atlas containing the ship textures
     * @param x the starting x for the player
     * @param y the starting y for the player
     * @param hp_bar a health bar (ideally displayed on the HUD) for the player to update
     * @param world a reference to the world to use as an object creator/deleter
     */
    public Player(TextureAtlas atlas, float x, float y, Bar hp_bar, World world) {
        super(atlas,0, x, y, false, world, world);
        this.hud_hp_bar = hp_bar;               // Use HUD health bar instead of overhead one
        this.set_health(this.get_max_health()); // Set health to max_health
        this.max_v = 6.0f;                      // Players slightly faster than enemies
    }

    // Responds to joystick input by updating movement or shooting activity
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
            this.rot = (float)Math.atan2(y, x) - (float)(Math.PI / 2f); // Face shooting direction
            this.shooting = true;                                       // Flag that shooting now
        }
    }

    // Updates the player
    @Override
    void update(float dt) {

        // Update velocity and all super components
        this.vx = Math.max(-this.max_v, Math.min(this.vx + this.ax, this.max_v));
        this.vy = Math.max(-this.max_v, Math.min(this.vy + this.ay, this.max_v));
        super.update(dt);

        // Attempt to shoot if shooting
        if (this.shooting) this.shoot(false);
    }

    // Sets the player's health and updates the HUD health bar's fill
    @Override
    public void set_health(float health) {
        super.set_health(health);
        if (this.hud_hp_bar != null)
            this.hud_hp_bar.set_fill(this.get_health() / this.get_max_health());
    }

    // Sets velocity without messing with moving flags (which relate to acceleration for the player)
    @Override
    public void set_velocity(float vx, float vy) {
        super.set_velocity(
                Math.max(Math.min(vx, this.max_v), -this.max_v),
                Math.max(Math.min(vy, this.max_v), -this.max_v));
    }

    // Stops the player completely, including acceleration
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
