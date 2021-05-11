package svenske.spacedust.gameobject.NPC;

import svenske.spacedust.gameobject.LightEmitter;
import svenske.spacedust.gameobject.Projectile;
import svenske.spacedust.gameobject.World;
import svenske.spacedust.graphics.AnimatedSprite;
import svenske.spacedust.graphics.Animation;
import svenske.spacedust.graphics.LightSource;
import svenske.spacedust.utils.Global;

/**
 * Marauders are enemy ships that rapid-fire low-damage bullets. They're tanky, and they encircle
 * their target when within range.
 */
public class Marauder extends NPC implements LightEmitter, AnimatedSprite.FrameChangeCallback {

    // Marauder skill & behavior settings
    private static final float MARAUDER_SPEED           = 4.8f;
    private static final float MARAUDER_MAX_HP          = 19f;
    private static final float MARAUDER_HP_REGEN_RATE   = 1f;
    private static final float MARAUDER_HP_REGEN_CD     = 8f;
    private static final float MARAUDER_BULLET_ACCURACY = 0.84f;
    private static final float MARAUDER_BULLET_SPEED    = 14f;
    private static final float MARAUDER_BULLET_DMG      = 0.17f;
    private static final float MARAUDER_SHOOT_COOLDOWN  = 0.3f;
    private static final float MARAUDER_FOCUS_RANGE     = 6f;
    private static final float MARAUDER_ENCIRCLE_RANGE  = 2.2f;
    private static final int   MARAUDER_LIGHT_INTERVAL  = 8;

    // Attributes
    private LightSource light_source; // Marauder's light source
    private boolean moving;           // Whether the marauder is currently moving

    // AI attributes
    private enum AIState { IDLE, WANDERING, FOCUSED }
    private AIState ai_state;
    private float poll_cooldown_timer = 1f;
    private float shoot_cooldown_timer = 0f;

    // Constructs the Marauder with arguments identical to superclass arguments
    public Marauder(float x, float y, World world) {
        super(new AnimatedSprite(Global.ta,
                        Animation.get_generic_ship_animations(2, MARAUDER_LIGHT_INTERVAL),
                        "idle", null, null), x, y, MARAUDER_SPEED,
                        "Marauder", MARAUDER_MAX_HP, MARAUDER_HP_REGEN_RATE,
                        MARAUDER_HP_REGEN_CD, world);
        ((AnimatedSprite)this.sprite).set_frame_change_callback(this);
        this.ai_state = AIState.IDLE;

        // Create light source
        this.light_source = new LightSource(new float[] { 0f, 0f, 0f }, 5f, 4f, null);
    }

    // Updates Marauder's AI
    @Override
    public void update(float dt) {
        super.update(dt);
        this.update_animation();
        this.light_source.update(dt);
        this.update_ai_state(dt);
        if (this.ai_state == AIState.FOCUSED) this.update_focus_ai(dt);
    }

    // Updates the animation of the Marauder based on if its moving or not
    private void update_animation() {

        // If just started moving, use thrust animation
        if ((this.vx > 0f || this.vy > 0f) && !this.moving) {
            this.moving = true;
            ((AnimatedSprite)this.sprite).change_animation("thrust", true);
        }

        // If just stopped moving, use idle animation
        else if ((this.vx < 0.0001f || this.vy < 0.0001f) && this.moving) {
            this.moving = false;
            ((AnimatedSprite)this.sprite).change_animation("idle", true);
        }
    }

    // Updates the current AI state of the Marauder
    private void update_ai_state(float dt) {

        // Every second, re-poll for the state this marauder should be in
        this.poll_cooldown_timer -= dt;
        if (this.poll_cooldown_timer <= 0f) {
            this.poll_cooldown_timer = 1f;
            float target_distance = this.target != null ? this.get_target_info()[1] : MARAUDER_FOCUS_RANGE + 1f;

            // If within distance to the target, switch state to focused
            if (target_distance < MARAUDER_FOCUS_RANGE) this.ai_state = AIState.FOCUSED;

            // If was previously focused but now out of range, set to idle and stop moving
            else if (this.ai_state == AIState.FOCUSED) {
                this.ai_state = AIState.IDLE;
                this.set_velocity(0f, 0f);

            // When not in range, alternate between idling and wandering aimlessly
            } else {
                if (Math.random() < 0.4f) { // With 40% chance, switch between the two states
                    if (this.ai_state == AIState.IDLE) {
                        this.set_movement_direction((float)(Math.random() * Math.PI * 2), 0.3f, true);
                        this.ai_state = AIState.WANDERING;
                    } else {
                        this.ai_state = AIState.IDLE;
                        this.set_velocity(0f, 0f);
                    }
                }
            }
        }
    }

    // Calculates movement and shooting when the Marauder is within focus range of its target
    private void update_focus_ai(float dt) {

        // Get direction and distance to target
        float[] target_info = this.get_target_info();

        // Shoot if cooldown for shooting is over and reset cooldown
        this.shoot_cooldown_timer -= dt;
        if (this.shoot_cooldown_timer <= 0f) {
            this.shoot_cooldown_timer = MARAUDER_SHOOT_COOLDOWN;
            this.shoot(target_info[0]);
        }

        // The closer to the target, the more perpendicular to target the motion becomes
        float perpendicular_angle = target_info[0] - (float)(Math.PI / 2);
        float dist_proportion = (target_info[1] - MARAUDER_ENCIRCLE_RANGE) / (MARAUDER_FOCUS_RANGE - MARAUDER_ENCIRCLE_RANGE);
        float movement_angle = (dist_proportion * target_info[0]) + ((1 - dist_proportion) * perpendicular_angle);
        this.rot = target_info[0];
        this.set_movement_direction(movement_angle, 1f, false);
    }

    // Generates a bullet with the ship's bullet properties, and aims it towards the target
    protected void shoot(float dir) {

        // Figure out an accuracy offset
        float max_offset = (float)Math.PI *  (1f - MARAUDER_BULLET_ACCURACY);
        float offset = (float)Math.random() * 2 * max_offset - max_offset;

        // Create bullet
        Projectile b = Projectile.create_bullet(new float[] { 0.5f, 0.5f, 0.5f, 1f }, this.x, this.y,
                dir + offset, MARAUDER_BULLET_SPEED, this.world, true,
                MARAUDER_BULLET_DMG);
        this.world.on_object_create(b);
    }

    // Responds to frame changes by setting adjusting the marauder's light source
    @Override
    public void on_frame_change(int new_frame) {

        // Baseline lighting settings
        float reach = 3f;
        float intensity = 3f;
        float[] glow = new float[] { 0f, 0f, 0f };

        // If light is on in the sprite, add a little yellow light
        if ((new_frame + 1) % MARAUDER_LIGHT_INTERVAL == 0) {
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
        this.light_source.set_reach(reach);
        this.light_source.set_glow(glow);
        this.light_source.set_intensity(intensity);
    }

    // Return the marauder's light source
    @Override
    public LightSource get_light() { return this.light_source; }
}
