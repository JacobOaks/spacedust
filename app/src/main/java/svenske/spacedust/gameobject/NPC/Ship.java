package svenske.spacedust.gameobject.NPC;

import svenske.spacedust.gameobject.LightEmitter;
import svenske.spacedust.gameobject.Projectile;
import svenske.spacedust.gameobject.World;
import svenske.spacedust.graphics.AnimatedSprite;
import svenske.spacedust.graphics.Animation;
import svenske.spacedust.graphics.LightSource;
import svenske.spacedust.graphics.TextureAtlas;
import svenske.spacedust.utils.Global;

// TODO: ship particles

/**
 * A ship is an NPC that:
 * - follows a specific texture/animation regiment
 * - has a light source that reacts to animation
 */
public class Ship extends NPC implements AnimatedSprite.FrameChangeCallback, LightEmitter {

    // Attributes
    private LightSource light_source; // Ship's light source
    private boolean moving;           // Whether the ship is currently moving

    // Bullet attributes
    private float bullet_accuracy; // 0 - random bullet dir; 1 - perfect bullet dir;
    private float bullet_speed;    // Bullet speed (units/s)
    private float bullet_damage;   // Bullet damage

    /**
     * Constructs the ship
     * @param atlas_row the row of the idle animation for the ship desired
     * @param speed the speed of the ship
     * The rest of the arguments follow superclass constructors
     */
    public Ship(int atlas_row, float x, float y, World world, float speed,
                String name, float max_hp, float hp_regen_rate, float hp_regen_cooldown,
                float bullet_accuracy, float bullet_speed, float bullet_damage) {
        super(new AnimatedSprite(Global.ta, Animation.get_generic_ship_animations(atlas_row),
                        "idle", null, null), x, y, speed, name,
                max_hp, hp_regen_rate, hp_regen_cooldown, world);
        ((AnimatedSprite)this.sprite).set_frame_change_callback(this);

        // Create ship light source
        this.light_source = new LightSource(new float[] { 0f, 0f, 0f }, 5f, 4f, null);

        // Save bullet attributes
        this.bullet_accuracy = bullet_accuracy;
        this.bullet_speed    = bullet_speed;
        this.bullet_damage   = bullet_damage;
    }

    // Updates the ship's position, moving flag, and light source
    @Override
    public void update(float dt) {
        super.update(dt);
        this.moving = (this.vx > 0f || this.vy > 0f);
        this.light_source.update(dt);
    }

    // Generates a bullet with the ship's bullet properties, and aims it towards the target
    protected void shoot(float dir) {

        // Figure out an accuracy offset
        float max_offset = (float)Math.PI *  (1f - this.bullet_accuracy);
        float offset = (float)Math.random() * 2 * max_offset - max_offset;

        // Create bullet
        Projectile b = Projectile.create_bullet(new float[] { 0.5f, 0.5f, 0.5f, 1f }, this.x, this.y,
                dir + offset, this.bullet_speed, this.world, true,
                this.bullet_damage);
        this.world.on_object_create(b);
    }

    // Responds to frame changes by setting adjusting the ship's light source
    @Override
    public void on_frame_change(int new_frame) {

        // Baseline lighting settings
        float reach = 3f;
        float intensity = 3f;
        float[] glow = new float[] { 0f, 0f, 0f };

        // If light is on in Sprite, add a little yellow light
        if ((new_frame + 1) % Animation.GENERIC_SHIP_ANIMATION_LIGHT_INTERVAL == 0) {
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

    // Return the ship's light source
    @Override
    public LightSource get_light() { return this.light_source; }
}
