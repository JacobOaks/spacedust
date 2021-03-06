package svenske.spacedust.gameobject;

import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

import svenske.spacedust.R;
import svenske.spacedust.graphics.BlendMode;
import svenske.spacedust.graphics.Camera;
import svenske.spacedust.graphics.LightSource;
import svenske.spacedust.graphics.ShaderProgram;
import svenske.spacedust.graphics.Sprite;
import svenske.spacedust.graphics.TextureAtlas;
import svenske.spacedust.utils.Global;
import svenske.spacedust.utils.Node;

// Encapsulation for all that is to be considered part of the World (as opposed to the HUD).
public class World {

    // Area settings
    // TODO (post-prototype): Grab from appropriate "zone"
    public final float WORLD_WIDTH = 100f;
    public final float WORLD_HEIGHT = 75f;
    public final float AMBIENT_LIGHT = 0.85f;
    public final int MAX_LIGHTS = 64;

    // World attributes
    private ShaderProgram sp;
    private Camera cam;

    // World objects
    private GameObject background;
    private List<GameObject> world_objects;
    private List<Bullet> bullets;                  // Kept separate so can be dynamically removed
    private float bullet_management_cooldown = 2f; // Bullets dynamically removed every so often

    // Constructs the world with the given continuous data from a previous destruction of context.
    public World(Node continuous_data) {

        // Setup shader program and camera, instantiate world object list
        this.sp = new ShaderProgram(R.raw.vertex_world, R.raw.fragment_world);
        this.cam = new Camera(0f, 0f, 1f);
        this.cam.set_bounds(-WORLD_WIDTH / 2f, WORLD_WIDTH / 2f,
                -WORLD_HEIGHT / 2f, WORLD_HEIGHT / 2f);
        this.world_objects = new ArrayList<>();
        this.bullets = new ArrayList<>();

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

    // Updates all of the world objects.
    public void update(float dt) {

        // Update world objects
        this.background.update(dt);
        for (Bullet b: this.bullets) b.update(dt);
        for (GameObject go : this.world_objects) go.update(dt);

        // Manage bullets every so often
        this.bullet_management_cooldown -= dt;
        if (this.bullet_management_cooldown <= 0f) {
            this.bullet_management_cooldown = 2f;
            this.manage_bullets();
        }
    }

    // Dynamically manage bullets in the world
    private void manage_bullets() {

        // Remove bullets out of view
        List<Bullet> to_remove = new ArrayList<>();
        for (Bullet b : this.bullets) {
            float[] pos = b.get_pos();
            if (cam.out_of_view(pos[0], pos[1], 1.5f))
                to_remove.add(b);
        }
        if (to_remove.size() > 0) this.bullets.removeAll(to_remove);
    }

    // Uses the World's ShaderProgram to render all of the world objects
    public void render() {
        this.sp.bind();
        this.set_lighting_uniforms();    // Set lighting uniforms
        this.cam.set_uniforms(this.sp);  // Set camera uniforms
        this.background.render(this.sp); // Render background first obviously
        for (GameObject go : this.world_objects) go.render(this.sp); // Then render other objects
        for (Bullet b : this.bullets) b.render(this.sp); // Then render bullets
        ShaderProgram.unbind_any_shader_program();
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

        // Set bullet lighting uniforms
        for (Bullet b : this.bullets) {
            if (i >= this.MAX_LIGHTS)
                Log.e("[spdt/world]",
                        "Maximum light count exceeded! Ignoring remaining lights.");
            else {
                float[] pos = b.get_pos();
                this.sp.set_light_uniform("lights", i, b.get_light(), pos[0], pos[1]);
                i++;
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
        this.sp.bind();
        this.sp.set_uniform("aspect_ratio",
                ((float) Global.VIEWPORT_WIDTH / (float)Global.VIEWPORT_HEIGHT));
        ShaderProgram.unbind_any_shader_program();
        this.cam.update_bounds();
    }

    // Add a new GameObject to the World
    public void add_game_object(GameObject go) {
        this.world_objects.add(go);
    }

    // Returns the World's camera
    public Camera get_camera() { return this.cam; }

    // Return import information for reloading the World after a context destroy.
    public Node get_continuous_data() {
        // TODO: Save GameObjects
        return null;
    }

    // Return a reference to the bullets list to pass to bullet-creating objects
    public List<Bullet> get_bullets() { return this.bullets; }
}
