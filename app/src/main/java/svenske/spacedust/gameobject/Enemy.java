package svenske.spacedust.gameobject;

import svenske.spacedust.graphics.TextureAtlas;

/**
 * Enemies are ships that:
 * - can be assigned targets
 * - will focus on targets if within a certain range
 * - will hone in on its target if they are in focus until they get close enough
 * - will shoot at its target if it is close enough
 */
public class Enemy extends Ship {

    // AI info & settings
    GameObject target;                      // The enemy's current target
    private float min_hone_distance  = 3f;  // If enemy at least this far from target, will hone
    private float max_focus_distance = 15f; // Enemy will focus on target when this close or more
    private float max_shoot_distance = 6f;  // Enemy will shoot at target when this close or more
    boolean honing = false;                 // Whether enemy currently honing in on target
    boolean focus  = false;                 // Whether enemy currently focused on target

    // A.I Timers
    private float hone_poll_cooldown  = 1f;   // Interval for checking if honing is necessary
    private float hone_poll_timer     = 0.0f; // Timer for honing
    private float focus_poll_cooldown = 3f;   // Interval for checking target within focus range
    private float focus_poll_timer    = 1f;   // Timer for checking for focus

    /**
     * Constructs the enemy
     * @param atlas the atlas containing the ship textures
     * @param x the x-position to place the enemy
     * @param y the y-position to place the enemy
     * @param world a reference to the world to use for object creation/deletion
     */
    public Enemy(TextureAtlas atlas, float x, float y, World world) {
        super(atlas, 2, x, y, true, world, world);

        // Set enemy specific shooting settings
        this.bullet_speed      = 20f;
        this.shooting_accuracy = 0.95f;
        this.bullet_damage     = 1f;
    }

    // Updates the enemy by performing some basic A.I.
    @Override
    void update(float dt) {
        super.update(dt);

        // Update timers
        this.hone_poll_timer -= dt;
        this.shooting_timer  -= dt;
        this.focus_poll_timer -= dt;

        // If focus timer ready, poll for focus
        if (this.focus_poll_timer <= 0f) poll_for_focus();

        if (this.focus)
            this.poll_for_action();
        else {
            // TODO: wandering AI
        }
    }

    // Checks whether the enemy should start focusing on its enemy
    private void poll_for_focus() {

        // If no target, do not focus
        if (this.target == null) {
            this.focus = false;
            return;
        }

        // Do not poll if focus is true. Focus is set to false in poll_for_action()
        if (this.focus) return;

        // Calculate distance to target.
        float[] target_pos = this.target.get_pos();
        float dx = target_pos[0] - this.x;
        float dy = target_pos[1] - this.y;
        float d = (float)Math.sqrt((dx * dx) + (dy * dy));

        // If within focus distance, begin to focus on the target
        if (d < max_focus_distance) this.focus = true;

        // Reset focus poll timer
        this.focus_poll_timer = this.focus_poll_cooldown;
    }

    // Controls enemy action when it is focusing on its target
    private void poll_for_action() {

        // Calculate distance and angle to target
        float[] target_pos = this.target.get_pos();
        float dx = target_pos[0] - this.x;
        float dy = target_pos[1] - this.y;
        float d = (float)Math.sqrt((dx * dx) + (dy * dy));
        float angle = (float)Math.atan2(dy, dx);

        // If the target is now too far away to continue focusing on it, stop focusing on it
        if (d > max_focus_distance) {
            this.focus = false;
            this.stop();
            return;
        }

        // If the enemy is currently honing in on the target
        if (honing) {

            // Check if close enough to stop honing
            if (d < min_hone_distance) {
                this.honing = false;
                this.stop();
                this.hone_poll_timer = this.hone_poll_cooldown;

            // Otherwise, continue honing in on target
            } else {
                this.set_velocity((float)Math.cos(angle) * this.max_v,
                        (float)Math.sin(angle) * this.max_v);
            }

        // If not honing in on target, make sure to face it
        } else {
            this.rot = angle - (float)Math.PI / 2f;;
        }

        // If honing poll timer up, reset i
        if (this.hone_poll_timer <= 0f) {
            this.hone_poll_timer = this.hone_poll_cooldown;

            // If not honing, check if far away enough to begin honing again
            if (!honing) {
                if (d > min_hone_distance) {
                    this.honing = true;
                    this.hone_poll_timer = this.hone_poll_cooldown;
                }
            }
        }

        // If within range of target, attempt to shoot it
        if (d < this.max_shoot_distance) this.shoot(true);
    }

    // Sets the enemy's current target
    public void set_target(GameObject target) {
        this.target = target;
    }
}
