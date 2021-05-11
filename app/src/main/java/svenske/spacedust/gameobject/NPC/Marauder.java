package svenske.spacedust.gameobject.NPC;

import android.util.Log;

import svenske.spacedust.gameobject.World;

/**
 * Marauders are enemy ships that rapid-fire low-damage bullets. They're tanky, and they encircle
 * their target when within range.
 */
public class Marauder extends Ship {

    // Marauder skill & behavior settings
    private static final float MARAUDER_SPEED          = 4.8f;
    private static final float MARAUDER_MAX_HP         = 22f;
    private static final float MARAUDER_HP_REGEN_RATE  = 1f;
    private static final float MARAUDER_HP_REGEN_CD    = 8f;
    private static final float MARAUDER_ACCURACY       = 0.84f;
    private static final float MARAUDER_BULLET_SPEED   = 14f;
    private static final float MARAUDER_BULLET_DMG     = 0.17f;
    private static final float MARAUDER_SHOOT_COOLDOWN = 0.3f;
    private static final float MARAUDER_FOCUS_RANGE    = 6f;
    private static final float MARAUDER_ENCIRCLE_RANGE = 2.2f;

    // AI attributes
    private enum AIState { IDLE, WANDERING, FOCUSED }
    private AIState ai_state;
    private float poll_cooldown_timer = 1f;
    private float shoot_cooldown_timer = 0f;

    // Constructs the Marauder with arguments identical to superclass arguments
    public Marauder(float x, float y, World world) {
        super(2, x, y, world, MARAUDER_SPEED, "Marauder", MARAUDER_MAX_HP,
                MARAUDER_HP_REGEN_RATE, MARAUDER_HP_REGEN_CD, MARAUDER_ACCURACY,
                MARAUDER_BULLET_SPEED, MARAUDER_BULLET_DMG);
        this.ai_state = AIState.IDLE;
    }

    // Updates Marauder's AI
    @Override
    public void update(float dt) {
        super.update(dt);
        this.update_ai_state(dt);
        if (this.ai_state == AIState.FOCUSED) this.update_focus_ai(dt);
    }

    // Updates the current AI state of the Marauder
    private void update_ai_state(float dt) {

        // Every second, re-poll for the state this marauder should be in
        this.poll_cooldown_timer -= dt;
        if (this.poll_cooldown_timer <= 0f) {
            this.poll_cooldown_timer = 1f;
            float target_distance = this.target != null ? this.get_target_info()[1] : MARAUDER_FOCUS_RANGE + 1f;
            Log.d("[spdt/marauder]", "target dir: " + Math.toDegrees(this.get_target_info()[0]));

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
}
