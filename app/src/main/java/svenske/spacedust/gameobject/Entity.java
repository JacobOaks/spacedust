package svenske.spacedust.gameobject;

import svenske.spacedust.graphics.ShaderProgram;
import svenske.spacedust.graphics.Sprite;
import svenske.spacedust.physics.PhysicsObject;

public abstract class Entity extends GameObject implements PhysicsObject {

    // Basic Attributes
    private String name;
    private Plate nameplate;
    public static final float NAMEPLATE_PADDING = 0.08f;

    // Object Creation & Deletion
    protected ObjectCreator object_creator;
    protected ObjectDeleter object_deleter;

    // HP
    private float hp;
    private float max_hp;
    private float hp_regen_rate;
    private float hp_regen_cooldown;
    private float hp_regen_cooldown_timer;

    // Particles TODO
    // private ParticleSpawner death_particles; // TODO
    // private ParticleSpawner damaged_particles; // TODO
    // private ParticleSpawner movement_particles; // TODO
    // private float movement_particles_cooldown; // TODO
    // private float movement_particles_cooldown_timer; // TODO

    public Entity(Sprite base_sprite, float x, float y, String name, float max_hp,
                  float hp_regen_rate, float hp_regen_cooldown, ObjectCreator object_creator,
                  ObjectDeleter object_deleter) {
        super(base_sprite, x, y);

        // Save basic entity info and set initial values
        this.name = name;
        this.max_hp = max_hp;
        this.set_health(this.max_hp);
        this.hp_regen_rate = hp_regen_rate;
        this.hp_regen_cooldown = this.hp_regen_cooldown_timer = hp_regen_cooldown;

        // Save references to object deleter and creator
        this.object_creator = object_creator;
        this.object_deleter = object_deleter;

        // TODO: save particles

        // Create and position nameplate
        this.nameplate = new Plate(this.name, this.hp / this.max_hp, 0f, 0f, NAMEPLATE_PADDING);
        this.update_plate_position();
    }

    /**
     * Updates the entity:
     * - update position
     * - update plate position
     * - update the health regeneration counter, or regenerate health if cool-down over
     */
    @Override
    void update(float dt) {

        // Update position and sprite
        super.update(dt);

        // Update plate
        this.update_plate_position();

        // Spawn movement particles TODO

        // Update health
        if (this.hp_regen_cooldown_timer > 0f) this.hp_regen_cooldown_timer -= dt;
        else this.heal(this.hp_regen_rate * dt);
    }

    // Updates the ship's overhead health bar's position if there is one
    private void update_plate_position() {
        if (this.nameplate != null) {
            // TODO: can optimize by only updating if entity has moved
            float new_nameplate_y = (this.y + this.get_size()[1] / 2) +
                                    (this.nameplate.get_size()[1] / 2) + NAMEPLATE_PADDING;
            this.nameplate.set_pos(this.x, new_nameplate_y);
        }
    }

    // Renders the ship and its plate if it has one
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
        this.object_deleter.on_object_delete(this);
    }

    // Get entity's bounds
    @Override
    public float[] get_bounds() {
        // Entity bounds are defined by a circle with diameter equal to 9/10 of the sprite size
        // Pretty import to define entities with this in mind (oblong entities should override this)
        float[] size = this.get_size();
        return new float[] { this.x, this.y, size[0] * 0.45f };
    }

    // Return the entity's health
    public float get_hp() { return this.hp; }

    // Return the entity's maximum health
    public float get_max_hp() { return this.max_hp; }
}
