package svenske.spacedust.gameobject.NPC;

import java.util.Map;

import svenske.spacedust.gameobject.LightEmitter;
import svenske.spacedust.gameobject.Projectile;
import svenske.spacedust.gameobject.World;
import svenske.spacedust.graphics.AnimatedSprite;
import svenske.spacedust.graphics.Animation;
import svenske.spacedust.graphics.BlendMode;
import svenske.spacedust.graphics.LightSource;
import svenske.spacedust.utils.Global;

/**
 * Snipers are enemy ships that fire high-speed/high-dmg bullets in between episodes of erratic
 * movement/dodging around the player.
 */
public class Sniper extends NPC implements AnimatedSprite.FrameChangeCallback, LightEmitter {

    // Sniper skill & behavior settings
    private static final float SNIPER_SPEED           = 10f;
    private static final float SNIPER_MAX_HP          = 4f;
    private static final float SNIPER_HP_REGEN_RATE   = 0.7f;
    private static final float SNIPER_HP_REGEN_CD     = 6f;
    private static final float SNIPER_BULLET_ACCURACY = 0.98f;
    private static final float SNIPER_BULLET_SPEED    = 26f;
    private static final float SNIPER_BULLET_DMG      = 4f;
    private static final float SNIPER_FOCUS_RANGE     = 8f;
    private static final float SNIPER_ERR_MVMT_CD     = 0.8f; // Length in s of each erratic mvmt
    private static final float SNIPER_ERR_MVMT_ACC    = 0.6f; // How towards target err mvmt is
    private static final float SNIPER_SHOOT_CHANCE    = 0.4f; // Chance after erratic mvmt of shoot
    private static final int   SNIPER_LIGHT_INTERVAL  = 7;

    // AI Attributes
    private enum AIState { IDLE, WANDERING, FOCUSED, ATTACKING }
    private Sniper.AIState ai_state;
    private float poll_cooldown_timer    = 1f;
    private float erratic_cooldown_timer = SNIPER_ERR_MVMT_CD;

    // Attributes
    private LightSource light_source; // Sniper's light source
    private boolean moving;           // Whether the sniper is currently moving

    // Constructs the sniper with arguments from the superclass constructor
    public Sniper(float x, float y, World world) {
        super(new AnimatedSprite(Global.ta, Sniper.get_animations(), "idle",
                        null, null), x, y, SNIPER_SPEED, "Sniper",
                SNIPER_MAX_HP, SNIPER_HP_REGEN_RATE, SNIPER_HP_REGEN_CD, world);
        ((AnimatedSprite)this.sprite).set_frame_change_callback(this);

        // Create light source
        this.light_source = new LightSource(new float[] { 0f, 0f, 0f }, 5f, 4f, null);
    }

    // Adds an attacking animation to the generic ship animations for the sniper
    private static Map<String, Animation> get_animations() {
        Map<String, Animation> anims = Animation.get_generic_ship_animations(4, SNIPER_LIGHT_INTERVAL);
        anims.put("attack", new Animation(0.12f, 9,
                new int[]{6, 6, 6, 7, 7, 7, 7, 7, 7, 7},
                new int[]{0, 1, 2, 0, 1, 0, 1, 0, 1, 0},
                new float[][]{null}, new BlendMode[]{BlendMode.JUST_TEXTURE}));
        return anims;
    }

    // Updates Sniper's AI and animation state
    @Override
    public void update(float dt) {
        super.update(dt);
        this.update_animation();
        this.light_source.update(dt);
        this.update_ai_state(dt);
        if (this.ai_state == Sniper.AIState.FOCUSED) this.update_focus_ai(dt);
        if (this.ai_state == AIState.ATTACKING) this.rot = this.get_target_info()[0];
    }

    // Updates the animation of the Sniper based on its state
    private void update_animation() {

        // If attacking, don't interrupt animation
        if (this.ai_state == AIState.ATTACKING) return;

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

    // Updates the AI state of the sniper
    private void update_ai_state(float dt) {

        // If currently attacking, don't change state until attack is done
        if (this.ai_state == AIState.ATTACKING) return;

        // Every second, re-poll for the state this sniper should be in
        this.poll_cooldown_timer -= dt;
        if (this.poll_cooldown_timer <= 0f) {
            this.poll_cooldown_timer = 1f;
            float target_distance = this.target != null ? this.get_target_info()[1] : SNIPER_FOCUS_RANGE + 1f;

            // If within distance to the target, switch state to focused
            if (target_distance < SNIPER_FOCUS_RANGE) this.ai_state = Sniper.AIState.FOCUSED;

            // If was previously focused but now out of range, set to idle and stop moving
            else if (this.ai_state == Sniper.AIState.FOCUSED) {
                this.ai_state = Sniper.AIState.IDLE;
                this.set_velocity(0f, 0f);

            // When not in range, alternate between idling and wandering aimlessly
            } else {
                if (Math.random() < 0.2f) { // With 20% chance, switch between the two states
                    if (this.ai_state == Sniper.AIState.IDLE) {
                        this.set_movement_direction((float)(Math.random() * Math.PI * 2), 0.3f, true);
                        this.ai_state = Sniper.AIState.WANDERING;
                    } else {
                        this.ai_state = Sniper.AIState.IDLE;
                        this.set_velocity(0f, 0f);
                    }
                }
            }
        }
    }

    // Updates AI when sniper is focused on its target
    private void update_focus_ai(float dt) {

        // At the end of every erratic movement
        this.erratic_cooldown_timer -= dt;
        if (erratic_cooldown_timer <= 0f) {
            this.erratic_cooldown_timer = Sniper.SNIPER_ERR_MVMT_CD;
            float rot = this.get_target_info()[0];

            // With SNIPER_SHOOT_CHANCE% probably, initiate an attack.
            if (Math.random() < SNIPER_SHOOT_CHANCE) {
                this.set_velocity(0f, 0f);
                this.rot = rot;
                ((AnimatedSprite)this.sprite).change_animation("attack", false);
                this.ai_state = AIState.ATTACKING;

            // Otherwise, initiate another erratic movement in the general direction of the target
            } else {
                float max_offset = (float)Math.PI * (1 - SNIPER_ERR_MVMT_ACC);
                float offset = (float)Math.random() * 2 * max_offset - max_offset;
                this.set_movement_direction(rot + offset, 1f, true);
            }
        }

    }

    // Generates a bullet with the ship's bullet properties, and aims it towards the target
    // TODO: Shoot two bullets from the actual guns on the texture rather than the center
    protected void shoot(float dir) {

        // Figure out an accuracy offset
        float max_offset = (float)Math.PI *  (1f - SNIPER_BULLET_ACCURACY);
        float offset = (float)Math.random() * 2 * max_offset - max_offset;

        // Create bullet
        Projectile b = Projectile.create_bullet(new float[] { 0.1f, 0.1f, 1f, 1f }, this.x, this.y,
                dir + offset, SNIPER_BULLET_SPEED, this.world, true,
                SNIPER_BULLET_DMG);
        this.world.on_object_create(b);
    }

    // Responds to frame changes by setting adjusting the sniper's light source
    @Override
    public void on_frame_change(int new_frame) {

        // Baseline lighting settings
        float reach = 3f;
        float intensity = 3f;
        float[] glow = new float[] { 0f, 0f, 0f };

        // When charging an attack, give a little more light
        if (this.ai_state == AIState.ATTACKING) {

            // When blue lights are on, make more light and emit blue glow
            if (new_frame > 2 && new_frame % 2 == 1) { // frames 3, 5, 7
                intensity += 0.35f;
                reach += 0.35f;
                glow[2] += 1f;
            }

            // If shooting animation is done, release bullet and reset animation to idle
            if (new_frame == 8) {
                this.shoot(this.get_target_info()[0]);
                this.moving    = false;
                this.ai_state  = AIState.FOCUSED;
                ((AnimatedSprite)this.sprite).change_animation("idle", false);
            }
        } else {
            // If light is on in the sprite, add some yellowish light
            if ((new_frame + 1) % SNIPER_LIGHT_INTERVAL == 0) {
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
        }

        // Set final light information
        this.light_source.set_reach(reach);
        this.light_source.set_glow(glow);
        this.light_source.set_intensity(intensity);
    }

    // Return the sniper's light source
    @Override
    public LightSource get_light() { return this.light_source; }
}