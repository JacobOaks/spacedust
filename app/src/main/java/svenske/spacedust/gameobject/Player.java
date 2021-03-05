package svenske.spacedust.gameobject;

import java.util.HashMap;
import java.util.Map;

import svenske.spacedust.graphics.AnimatedSprite;
import svenske.spacedust.graphics.Animation;
import svenske.spacedust.graphics.BlendMode;
import svenske.spacedust.graphics.TextureAtlas;

// A specific GameObject representing the player
public class Player extends GameObject implements JoyStick.JoystickReceiver {

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

    /**
     * Constructs the player with the given atlas and starting position.
     * @param atlas the atlas containing the player textures. The animations are parsed/created
     *              in this class so if the layout on the texture atlas changes, those changes
     *              should be reflected here in creating the correct animations.
     */
    public Player(TextureAtlas atlas, float x, float y) {
        super(null, x, y);
        this.setup_sprite(atlas); // Setup animations and sprite
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
    }

    // Applies acceleration and calls GameObject's update
    @Override
    void update(float dt) {
        this.vx = Math.max(-this.max_v, Math.min(this.vx + this.ax, this.max_v));
        this.vy = Math.max(-this.max_v, Math.min(this.vy + this.ay, this.max_v));
        super.update(dt);
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
}
