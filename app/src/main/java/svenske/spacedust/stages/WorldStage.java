package svenske.spacedust.stages;

import android.opengl.GLES20;
import android.util.Log;
import android.view.MotionEvent;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

import svenske.spacedust.R;
import svenske.spacedust.graphics.AnimatedSprite;
import svenske.spacedust.graphics.Animation;
import svenske.spacedust.graphics.BlendMode;
import svenske.spacedust.graphics.Camera;
import svenske.spacedust.graphics.ShaderProgram;
import svenske.spacedust.graphics.Sprite;
import svenske.spacedust.graphics.TextureAtlas;
import svenske.spacedust.utils.Node;
import svenske.spacedust.utils.Transform;

/**
 * This stage represents the actual game world.
 */
public class WorldStage implements Stage {

    Camera camera;
    ShaderProgram shader_program;
    Sprite sprite;

    @Override
    public void init(Node previous_continuous_data) {

        if (previous_continuous_data != null) {
            // TODO: Load data pre-context destroy
        } else {
            // TODO: Starting from scratch
        }

        TextureAtlas atlas  = new TextureAtlas(R.drawable.example_atlas, 1, 1);

        Animation anim = new Animation(0.04f, 13,
                new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                new float[][] {
                        {0.0f, 0.0f, 0.0f, 1.0f},
                        {0.0f, 0.1f, 0.0f, 1.0f},
                        {0.0f, 0.2f, 0.0f, 1.0f},
                        {0.0f, 0.3f, 0.0f, 1.0f},
                        {0.0f, 0.4f, 0.0f, 1.0f},
                        {0.0f, 0.5f, 0.0f, 1.0f},
                        {0.0f, 0.6f, 0.0f, 1.0f},
                        {0.0f, 0.7f, 0.0f, 1.0f},
                        {0.0f, 0.8f, 0.0f, 1.0f},
                        {0.0f, 0.9f, 0.0f, 1.0f},
                        {0.0f, 1.0f, 0.0f, 1.0f},
                        {0.0f, (2f/3f), 0.0f, 1.0f},
                        {0.0f, (1f/3f), 0.0f, 1.0f}
                },
                new BlendMode[] {
                        BlendMode.AVG, BlendMode.AVG, BlendMode.AVG, BlendMode.AVG, BlendMode.AVG,
                        BlendMode.AVG, BlendMode.AVG, BlendMode.AVG, BlendMode.AVG, BlendMode.AVG,
                        BlendMode.AVG, BlendMode.AVG, BlendMode.AVG });
        Map<String, Animation> animations = new HashMap<>();
        animations.put("flash_green", anim);
        this.sprite = new AnimatedSprite(atlas, animations, "flash_green");

        this.camera = new Camera(0f, 0f, 0.33f);
        this.shader_program = new ShaderProgram(R.raw.vertex_shader, R.raw.fragment_shader);
    }

    @Override
    public boolean scale_input(float scale_factor, float focal_x, float focal_y) {
        return false;
    }

    float w, h;
    float x, y;

    @Override
    public boolean other_input(MotionEvent me) {
        float[] world_pos = Transform.screen_to_world(me.getX(), me.getY(), this.w, this.h, this.camera);
        x = world_pos[0];
        y = world_pos[1];
        return false;
    }

    @Override
    public void update(float dt) {
        this.sprite.update(dt);
    }

    @Override
    public void render() {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        this.shader_program.bind();
        this.camera.set_uniforms(this.shader_program);
        this.sprite.render(this.shader_program, x, y);

        ShaderProgram.unbind_any_shader_program();
    }

    @Override
    public void resize(float width, float height) {
        this.w = width;
        this.h = height;
        this.shader_program.bind();
        this.shader_program.set_uniform("aspect_ratio", (width / height));
        ShaderProgram.unbind_any_shader_program();
    }

    @Override
    public Node get_continuous_data() {
        return null;
    }
}
