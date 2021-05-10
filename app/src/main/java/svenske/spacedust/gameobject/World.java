package svenske.spacedust.gameobject;

import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

import svenske.spacedust.R;
import svenske.spacedust.graphics.BlendMode;
import svenske.spacedust.graphics.Camera;
import svenske.spacedust.graphics.ShaderProgram;
import svenske.spacedust.graphics.Sprite;
import svenske.spacedust.graphics.TextureAtlas;
import svenske.spacedust.physics.PhysicsEngine;
import svenske.spacedust.physics.PhysicsObject;
import svenske.spacedust.utils.Global;
import svenske.spacedust.utils.Node;

// Encapsulation for all that is to be considered part of the World (as opposed to the HUD).
public class World implements GameObject.ObjectCreator, GameObject.ObjectDeleter {

    // Area settings (TODO: delegate to individual Zone class instances)
    public final float WORLD_WIDTH = 100f;      // How wide is the world?
    public final float WORLD_HEIGHT = 75f;      // How tall is the world?
    public final float AMBIENT_LIGHT = 0.85f;   // An ambient light multiplier
    public final int MAX_LIGHTS = 64;           // This must be in-sync with the shader program

    // World attributes
    private ShaderProgram sp;                   // A shader program to render the world
    private Camera cam;                         // A camera to view into the world
    private PhysicsEngine physics_engine;       // A physics engine for bullets, etc.
    private Player player;                      // The player in the world

    // Enemy info
    private float current_enemies = 0f;         // How many enemies are currently in the world
    private float killed_enemies = 0f;          // How many enemies have been killed
    private float max_enemies = 10f;            // Maximum enemies before they stop spawning
    private float enemy_spawn_cooldown = 5f;    // Cool-down between enemy spawns
    private float enemy_spawn_timer = 5f;       // Timer for enemy spawns

    /**
     * A multiplier applied to the camera's view to get a scope of relevance. Collisions outside
     * of this scope will not be checked by the physics engine, and bullets outside of this scope
     * will be removed.
     */
    private final float RELEVANCE_SCOPE_MUL = 1.4f;

    // World objects
    private GameObject background;
    private List<GameObject> world_objects;      // List of all objects in the world
    private List<PhysicsObject> physics_objects; // Sublist of world_objects for physics_objects

    // Queues for object adding or deleting from other objects
    private List<GameObject> to_add;             // A queue for objects to add
    private List<GameObject> to_delete;          // A queue for objects to delete
    private float bullet_management_timer = 2f;  // Bullets dynamically removed every so often

    // Constructs the world with the given continuous data from a previous destruction of context.
    public World(Node continuous_data) {

        // Setup shader program, camera, physics engine
        this.sp = new ShaderProgram(R.raw.vertex_world, R.raw.fragment_world);
        this.cam = new Camera(0f, 0f, 1f);
        this.cam.set_bounds(-WORLD_WIDTH / 2f, WORLD_WIDTH / 2f,
                -WORLD_HEIGHT / 2f, WORLD_HEIGHT / 2f);
        this.physics_engine = new PhysicsEngine(this.cam, RELEVANCE_SCOPE_MUL);

        // Initialize object lists
        this.world_objects = new ArrayList<>();
        this.physics_objects = new ArrayList<>();
        this.to_add = new ArrayList<>();
        this.to_delete = new ArrayList<>();

        // Create background
        TextureAtlas background_atlas = new TextureAtlas(R.drawable.background, 1, 1);
        Sprite background_sprite = new Sprite(background_atlas, 0, 0,
                new float[] { 0.1f, 0f, 0.1f, 1f }, BlendMode.AVG, null, null);
        this.background = new GameObject(background_sprite, 0f, 0f);
        this.background.set_scale(WORLD_WIDTH, WORLD_HEIGHT);

        // TODO: Re-instantiate GameObjects
        if (continuous_data !=  null) {}
    }

    /**
     * Responds to input by allowing any GameObjects that are InputReceivers to respond to it.
     * @param ignore_idx a cumulative list of pointer indices to NOT respond to.
     */
    public void input(MotionEvent me, List<Integer> ignore_idx) {
        for (GameObject go : this.world_objects)
            if (go instanceof InputReceiver) ((InputReceiver)go).input(me, ignore_idx);
    }

    // Updates all of the world's objects and check for collisions using the physics engine.
    public void update(float dt) {

        // Update world objects
        this.background.update(dt);
        for (GameObject go : this.world_objects) go.update(dt);

        // Add or remove objects if created/deleted during other object updates
        for (GameObject go : this.to_add) this.add_game_object(go);
        this.to_add.clear();
        for (GameObject go : this.to_delete) this.remove_game_object(go);
        this.to_delete.clear();

        // Check for collisions
        this.physics_engine.check_collisions(this.physics_objects);

        // Manage bullets every so often
        this.bullet_management_timer -= dt;
        if (this.bullet_management_timer <= 0f) {
            this.bullet_management_timer = 2f;
            this.manage_bullets();
        }

        // Enemy spawning TODO
        /* this.enemy_spawn_timer -= dt;
        if (this.enemy_spawn_timer < 0f) {
            this.enemy_spawn_timer += this.enemy_spawn_cooldown;
            if (this.current_enemies < this.max_enemies) { // Spawn new enemy
                float x = this.cam.get_x();
                float y = this.cam.get_y();
                while (!this.cam.out_of_view(x, y, 1.5f)) {
                    x = this.WORLD_WIDTH * (float)Math.random() - (this.WORLD_WIDTH / 2f);
                    y = this.WORLD_HEIGHT * (float)Math.random() - (this.WORLD_HEIGHT / 2f);
                }
                Enemy e = new Enemy(WorldStage.ship_ta, x, y, this);
                e.set_target(this.player);
                this.add_game_object(e);
            }
        }
         */
    }

    // Dynamically manage bullets in the world (remove them if too far away)
    private void manage_bullets() {

        // Remove bullets out of view
        List<Bullet> to_remove = new ArrayList<>();
        for (GameObject go : this.world_objects) {
            if (go instanceof Bullet) {
                Bullet b = (Bullet)go;
                float[] pos = b.get_pos();
                if (cam.out_of_view(pos[0], pos[1], RELEVANCE_SCOPE_MUL))
                    to_remove.add(b);
            }
        }

        // Remove bullets from the full list of objects and the list of physics objects
        if (to_remove.size() > 0) {
            this.world_objects.removeAll(to_remove);
            this.physics_objects.removeAll(to_remove);
        }
    }

    // Uses the World's ShaderProgram to render all of the world objects
    public void render() {
        this.sp.bind();                                              // Bind
        this.set_lighting_uniforms();                                // Set lighting uniforms
        this.cam.set_uniforms(this.sp);                              // Set camera uniforms
        this.background.render(this.sp);                             // Render background first
        for (GameObject go : this.world_objects) go.render(this.sp); // Then render game objects
        ShaderProgram.unbind_any_shader_program();                   // Unbind
    }

    // Sets lighting uniforms in the shader program pre-render
    private void set_lighting_uniforms() {

        // Set lighting uniforms from generic GameObjects
        this.sp.set_uniform("ambient_light", this.AMBIENT_LIGHT);
        this.sp.set_uniform("max_brightness", 10f);
        int i = 0;
        for (GameObject go : this.world_objects) {
            if (go instanceof LightEmitter) {
                if (i >= this.MAX_LIGHTS)
                    Log.e("[spdt/world]",
                            "Maximum light count exceeded! Ignoring remaining lights.");
                else {
                    float[] pos = go.get_pos();
                    this.sp.set_light_uniform("lights", i, ((LightEmitter) go).get_light(),
                            pos[0], pos[1]);
                    i++;
                }
            }
        }

        // Fill remaining light slots with null
        for (int j = i; j < MAX_LIGHTS; j++)
            this.sp.set_light_uniform("lights", j, null, -1, -1);
    }

    /**
     * Responds to a resize by:
     * - updating aspect ration uniform in shader program
     * - re-calculating the correct camera bounds
     */
    public void resized() {

        // Update shader program
        this.sp.bind();
        this.sp.set_uniform("aspect_ratio",
                ((float) Global.VIEWPORT_WIDTH / (float)Global.VIEWPORT_HEIGHT));
        ShaderProgram.unbind_any_shader_program();

        // Notify camera and physics engine
        this.cam.update_bounds();
        this.physics_engine.resized();
    }

    // Add a new GameObject to the World
    public void add_game_object(GameObject go) {
        this.world_objects.add(go);      // Add to list of game objects
        if (go instanceof PhysicsObject) // Add to sublist of physics objects if it is one
            this.physics_objects.add((PhysicsObject)go);
        /* TODO
        if (go instanceof Enemy) {
            this.current_enemies += 1;
            ((TextSprite) WorldStage.enemy_text.get_sprite()).set_text("Enemies: " +
                    this.current_enemies);
        }
        */
        if (go instanceof Player) this.player = (Player)go;
    }

    // Remove a GameObject from the World
    public void remove_game_object(GameObject go) {
        boolean removed = this.world_objects.remove(go);
        if (!removed)
            Log.e("spdt/world", "attempted to remove an object not present in the World");
        if (go instanceof PhysicsObject) this.physics_objects.remove(go);
        /* TODO
        if (go instanceof Enemy) {
            this.current_enemies -= 1;
            this.killed_enemies += 1;
            ((TextSprite) WorldStage.enemy_text.get_sprite()).set_text("Enemies: " +
                    this.current_enemies);
            ((TextSprite) WorldStage.kills_text.get_sprite()).set_text("Kills: " +
                    this.killed_enemies);
        }
         */
    }

    // Responds to newly produced objects by adding them to the world
    @Override
    public void on_object_create(GameObject new_object) { this.to_add.add(new_object); }

    // Responds to object deletions by removing them from the world
    @Override
    public void on_object_delete(GameObject to_delete) { this.to_delete.add(to_delete); }

    // Returns the World's camera
    public Camera get_camera() { return this.cam; }

    // Return import information for reloading the World after a context destroy.
    public Node get_continuous_data() {
        // TODO: Save GameObjects
        return null;
    }
}
