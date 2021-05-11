package svenske.spacedust.gameobject;

import svenske.spacedust.graphics.ShaderProgram;
import svenske.spacedust.graphics.Sprite;
import svenske.spacedust.physics.PhysicsEngine;
import svenske.spacedust.physics.PhysicsObject;
import svenske.spacedust.utils.Global;

/**
 * An Entity is a game object with:
 * - Physics capabilities (bounds)
 * - Name (& a nameplate)
 * - Health (& health regen)
 * - Some particle emission capabilities
 */
public abstract class Entity extends GameObject implements PhysicsObject {

    // Basic Attributes
    private String name;      // Entity name
    private Plate nameplate;  // Nameplate shown above the entity
    // Padding between the entity and its nameplate
    public static final float NAMEPLATE_PADDING = 0.08f;
    private float kb_x, kb_y; // Knockback x and y

    // Reference to the world for creating/deleting objects
    protected World world;

    // Health attributes
    private float hp;                      // How much health does the entity have?
    private float max_hp;                  // What's the entity's maximum possible health?
    private float hp_regen_rate;           // How quickly does the entity regenerate health (hp/s)?
    private float hp_regen_cooldown;       // How long after being damaged can the entity regen?
    private float hp_regen_cooldown_timer; // Current timer for regen cooldown

    // TODO: Particles
    // private ParticleSpawner death_particles; // TODO
    // private ParticleSpawner damaged_particles; // TODO
    // private ParticleSpawner movement_particles; // TODO
    // private float movement_particles_cooldown; // TODO
    // private float movement_particles_cooldown_timer; // TODO

    /**
     * Constructs the Entity
     * @param world a reference to the world which will be used to add objects (i.e., projectiles)
     *              and remove them (i.e., projectiles or dead entities)
     * The rest of the parameters are described in the superclass constructors or above in the
     *              attributes declarations
     */
    public Entity(Sprite sprite, float x, float y, String name, float max_hp,
                  float hp_regen_rate, float hp_regen_cooldown, World world) {
        super(sprite, x, y);

        // Save basic entity info and set initial values
        this.name              = name;
        this.max_hp            = max_hp;
        this.set_health(this.max_hp);
        this.hp_regen_rate     = hp_regen_rate;
        this.hp_regen_cooldown = this.hp_regen_cooldown_timer = hp_regen_cooldown;

        // Save reference to world for obj creation/deletion
        this.world = world;

        // TODO: save particles

        // Create and position nameplate
        this.nameplate = new Plate(this.name, this.hp / this.max_hp, 0f, 0f, NAMEPLATE_PADDING);
        this.update_plate_position();
    }

    // Updates the entity's position, nameplate, and health regen status
    @Override
    public void update(float dt) {

        // Update position and sprite
        super.update(dt);
        this.update_knockback(dt);

        // Update plate
        this.update_plate_position();

        // TODO: Update movement particles

        // Update health regeneration
        if (this.hp_regen_cooldown_timer > 0f) this.hp_regen_cooldown_timer -= dt;
        else this.heal(this.hp_regen_rate * dt);
    }

    // Applies knock-back to entity's position and degrades the effects of knockback over time
    private void update_knockback(float dt) {
        this.x += this.kb_x * dt;
        this.y += this.kb_y * dt;
        this.kb_x *= 0.98f;
        this.kb_y *= 0.98f;
    }

    // Updates the entity's overhead health bar's position if there is one
    private void update_plate_position() {
        if (this.nameplate != null) {
            float new_nameplate_y = (this.y + this.get_size()[1] / 2) +
                                    (this.nameplate.get_size()[1] / 2) + NAMEPLATE_PADDING;
            this.nameplate.set_pos(this.x, new_nameplate_y);
        }
    }

    // Renders the entity and its plate if it has one
    @Override
    void render(ShaderProgram sp) {
        super.render(sp);
        if (this.nameplate != null) this.nameplate.render(sp);
    }

    // Deal the given amount of damage to the ship's health
    public void damage(float hp) {
        if (hp < 0f) this.heal(-hp); // Count as healing if damage is negative
        else {
            this.set_health(this.hp - hp);
            this.hp_regen_cooldown_timer = this.hp_regen_cooldown; // Reset regeneration counter
            // TODO: Spawn damage particles
        }
    }

    // Heal the given amount of health to the ship's health
    public void heal(float hp) {
        if (hp < 0f) this.damage(-hp); // Count as damage if healing is negative
        else this.set_health(this.hp + hp);
    }

    // Sets the ship's health to the given health
    public void set_health(float health) {
        this.hp = Math.min(this.max_hp, health);
        if (this.nameplate != null)
            this.nameplate.set_hp_bar_fill(this.hp / this.max_hp);
        if (this.hp <= 0f) this.died(); // Check for death
    }

    // Called when the entity dies. By default, just deletes the entity
    protected void died() {
        this.world.on_object_delete(this);
        // TODO: Spawn death particles
    }

    // Handles collision with other entities
    @Override
    public void on_collide(PhysicsObject other) {
        if (other instanceof Entity) {

            // Damage this entity for crashing
            this.damage(0.05f);

            // Apply some knockback on both entities
            Entity e = (Entity)other;
            float dir = Global.get_vector_info(e.get_pos(), this.get_pos())[0];
            float v = e.get_full_v();
            this.kb_x = v * (float)Math.cos(dir + Math.PI / 2);
            this.kb_y = v * (float)Math.sin(dir + Math.PI / 2);
        }
    }

    // Get entity's bounds
    @Override
    public float[] get_bounds() {
        // Entity bounds are defined by a circle with diameter equal to 9/10 of the sprite size
        // Important to define entities with this in mind (oblong entities should override this)
        float[] size = this.get_size();
        return new float[] { this.x, this.y, size[0] * 0.45f };
    }

    // Return the entity's health
    public float get_hp() { return this.hp; }

    // Return the entity's maximum health
    public float get_max_hp() { return this.max_hp; }
}
