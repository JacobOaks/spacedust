package svenske.spacedust.gameobject;

import android.util.Log;

import java.util.List;

import svenske.spacedust.graphics.TextureAtlas;

public class Enemy extends Ship {

    // AI info & settings
    GameObject target;
    private float min_hone_distance = 3f;
    private float max_focus_distance = 18f;
    private float max_shoot_distance = 6f;
    boolean honing = false;
    boolean focus = false;

    private float hone_poll_cooldown = 1f;
    private float hone_poll_timer    = 0.0f;
    private float shooting_cooldown  = 1f;
    private float shooting_timer     = 0.0f;
    private float focus_poll_timer   = 1f;
    private float focus_poll_cooldown = 1f;

    /**
     * Constructs the enemy
     *
     * @param atlas      the atlas containing the ship textures
     * @param bullets    this should be the list of bullets from the World.
     * @param health_bar whether to give the Ship an overhead health bar
     */
    public Enemy(TextureAtlas atlas, List<Bullet> bullets, float x, float y, boolean health_bar) {
        super(atlas, 2, bullets, x, y, health_bar);
    }

    @Override
    void update(float dt) {
        super.update(dt);

        this.hone_poll_timer -= dt;
        this.shooting_timer  -= dt;
        this.focus_poll_timer -= dt;

        if (this.focus_poll_timer <= 0f) poll_for_focus();

        if (this.focus)
            this.poll_for_action();
        else {
            // TODO: wandering AI
        }
    }

    private void poll_for_focus() {

        float[] target_pos = this.target.get_pos();
        float dx = target_pos[0] - this.x;
        float dy = target_pos[1] - this.y;
        float d = (float)Math.sqrt((dx * dx) + (dy * dy));
        if (d < max_focus_distance) this.focus = true;
        this.focus_poll_timer = this.focus_poll_cooldown;
    }

    private void poll_for_action() {

        float[] target_pos = this.target.get_pos();
        float dx = target_pos[0] - this.x;
        float dy = target_pos[1] - this.y;
        float d = (float)Math.sqrt((dx * dx) + (dy * dy));
        float angle = (float)Math.atan2(dy, dx);

        if (d > max_focus_distance) {
            this.focus = false;
            this.stop();
            return;
        }

        if (honing) {

            if (d < min_hone_distance) {
                this.honing = false;
                this.stop();
                this.hone_poll_timer = this.hone_poll_cooldown;
            } else {
                this.set_velocity((float)Math.cos(angle) * this.max_v,
                        (float)Math.sin(angle) * this.max_v);

            }
        } else {
            this.rot = angle - (float)Math.PI / 2f;;
        }

        if (this.hone_poll_timer <= 0f) {
            this.hone_poll_timer = this.hone_poll_cooldown;

            if (!honing) {
                if (d > min_hone_distance) {
                    this.honing = true;
                    this.hone_poll_timer = this.hone_poll_cooldown;
                }
            }
        }

        if (this.shooting_timer <= 0f) {
            this.shooting_timer = this.shooting_cooldown;
            if (d < this.max_shoot_distance) this.shoot();
        }
    }

    public void set_target(GameObject target) {
        this.target = target;
    }
}
