package svenske.spacedust.gameobject;

import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

import svenske.spacedust.R;
import svenske.spacedust.graphics.BlendMode;
import svenske.spacedust.graphics.Camera;
import svenske.spacedust.graphics.ShaderProgram;
import svenske.spacedust.graphics.Sprite;
import svenske.spacedust.graphics.TextureAtlas;
import svenske.spacedust.utils.Global;
import svenske.spacedust.utils.Node;

// Encapsulation for all that is to be considered part of the World (as opposed to the HUD).
public class World {

    public final float WORLD_WIDTH = 100f;
    public final float WORLD_HEIGHT = 75f;

    // World attributes
    ShaderProgram sp;
    Camera cam;
    List<GameObject> game_objects;

    // Constructs the world with the given continuous data from a previous destruction of context.
    public World(Node continuous_data) {

        this.sp = new ShaderProgram(R.raw.vertex_world, R.raw.fragment_world);
        this.cam = new Camera(0f, 0f, 1f);
        this.cam.set_bounds(-WORLD_WIDTH / 2f, WORLD_WIDTH / 2f,
                -WORLD_HEIGHT / 2f, WORLD_HEIGHT / 2f);
        this.game_objects = new ArrayList<>();

        TextureAtlas background_atlas = new TextureAtlas(R.drawable.background, 1, 1);
        Sprite background_sprite = new Sprite(background_atlas, 0, 0,
                new float[] { 0.1f, 0f, 0.1f, 1f }, BlendMode.AVG, null, null);
        GameObject background = new GameObject(background_sprite, 0f, 0f);
        background.set_scale(WORLD_WIDTH, WORLD_HEIGHT);
        this.game_objects.add(background);

        if (continuous_data !=  null) {

            // TODO: Re-instantiate GameObjects

        }
    }

    /**
     * Responds to input by allowing any GameObjects that are InputReceivers to respond to it.
     * @param ignore_idx a cumulative list of pointer indices to NOT respond to.
     */
    public void input(MotionEvent me, List<Integer> ignore_idx) {
        for (GameObject go : this.game_objects) {
            if (go instanceof InputReceiver) ((InputReceiver)go).input(me, ignore_idx);
        }
    }

    // Updates all of the World's GameObjects.
    public void update(float dt) {
        for (GameObject go : this.game_objects) go.update(dt);
    }

    // Uses the World's ShaderProgram to render all of its GameObjects
    public void render() {
        this.sp.bind();
        this.cam.set_uniforms(this.sp);
        for (GameObject go : this.game_objects) go.render(this.sp);
        ShaderProgram.unbind_any_shader_program();
    }

    // Updates the World's ShaderProgram's aspect ratio uniform in response to a screen size update.
    public void resized() {
        this.sp.bind();
        this.sp.set_uniform("aspect_ratio",
                ((float) Global.VIEWPORT_WIDTH / (float)Global.VIEWPORT_HEIGHT));
        ShaderProgram.unbind_any_shader_program();
        this.cam.update_bounds();
    }

    // Add a new GameObject to the World
    public void add_game_object(GameObject go) {
        this.game_objects.add(go);
    }

    // Returns the World's camera
    public Camera get_camera() { return this.cam; }

    // Return import information for reloading the World after a context destroy.
    public Node get_continuous_data() {

        // TODO: Save GameObjects

        return null;
    }
}
