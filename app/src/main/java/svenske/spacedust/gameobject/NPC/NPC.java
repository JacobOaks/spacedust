package svenske.spacedust.gameobject.NPC;

import svenske.spacedust.gameobject.Entity;
import svenske.spacedust.gameobject.GameObject;
import svenske.spacedust.gameobject.World;
import svenske.spacedust.graphics.Sprite;
import svenske.spacedust.physics.PhysicsObject;

/**
 * An NPC is a hostile entity that:
 * - has a specified speed
 * - can be given a object to target
 */
public abstract class NPC extends Entity {

    // Attributes
    private float speed;       // Maximum combined speed of the NPC
    private GameObject target; // A target

    /**
     * Constructs the NPC
     * @param base_sprite a sprite to render
     * @param speed how quick the NPC is
     * The rest of the arguments follow super arguments
     */
    public NPC(Sprite base_sprite, float x, float y, float speed, String name, float max_hp,
               float hp_regen_rate, float hp_regen_cooldown, World world) {
        super(base_sprite, x, y, name, max_hp, hp_regen_rate, hp_regen_cooldown, world);
        this.speed = speed;
    }

    // Gets the rotation from the NPC to its target
    protected float get_target_dir() {
        float[] target_pos = this.target.get_pos();
        float dx = target_pos[0] - this.x;
        float dy = target_pos[1] - this.y;
        return (float)Math.atan2(dy, dx);
    }

    /**
     * Starts the NPC moving in a certain direction
     * @param dir the direction (in radians)
     * @param speed_multiplier multiplier for the NPC's speed attribute
     * @param rotate whether to also face the NPC in the direction of travel
     */
    public void set_movement_direction(float dir, float speed_multiplier, boolean rotate) {
        this.vx = (float)Math.cos(dir + Math.PI / 2) * speed_multiplier * this.speed;
        this.vy = (float)Math.sin(dir + Math.PI / 2) * speed_multiplier * this.speed;
        if (rotate) this.rot = dir;
    }

    // NPC projectile collision handled in projectile collision method
    @Override
    public void on_collide(PhysicsObject other) {}

    // Sets the NPC's target
    public void set_target(GameObject target) { this.target = target; }
}
