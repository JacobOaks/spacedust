package svenske.spacedust.gameobject.NPC;

import svenske.spacedust.gameobject.Entity;
import svenske.spacedust.gameobject.GameObject;
import svenske.spacedust.gameobject.World;
import svenske.spacedust.graphics.Sprite;
import svenske.spacedust.utils.Global;

/**
 * An NPC is a hostile entity that:
 * - has a specified speed
 * - can be given a object to target
 */
public abstract class NPC extends Entity {

    // Attributes
    private float speed;         // Maximum combined speed of the NPC
    protected GameObject target; // A target

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

    /**
     * @return a length-2 float array where the first float is direction to the NPC's target (in
     * radians), and the second float is the distance to the target. If the NPC has no target, null
     * will be returned
     */
    protected float[] get_target_info() {
        if (this.target == null) return null;
        else return Global.get_vector_info(this.get_pos(), this.target.get_pos());
    }

    /**
     * Starts the NPC moving in a certain direction
     * @param dir the direction (in radians)
     * @param speed_multiplier multiplier for the NPC's speed attribute
     * @param rotate whether to also face the NPC in the direction of travel
     */
    public void set_movement_direction(float dir, float speed_multiplier, boolean rotate) {
        float vx = (float)Math.cos(dir + Math.PI / 2) * speed_multiplier * this.speed;
        float vy = (float)Math.sin(dir + Math.PI / 2) * speed_multiplier * this.speed;
        this.set_velocity(vx, vy);
        if (rotate) this.rot = dir;
    }

    // Sets the NPC's target
    public NPC set_target(GameObject target) { this.target = target; return this; }
}
