package svenske.spacedust.stages;

import android.opengl.GLES20;
import android.util.Log;
import android.view.MotionEvent;

import org.w3c.dom.Text;

import svenske.spacedust.R;
import svenske.spacedust.graphics.BlendMode;
import svenske.spacedust.graphics.Camera;
import svenske.spacedust.graphics.ShaderProgram;
import svenske.spacedust.graphics.Sprite;
import svenske.spacedust.graphics.TextureAtlas;
import svenske.spacedust.utils.Node;

/**
 * This stage represents the actual game world.
 */
public class WorldStage implements Stage {

    Sprite[] sprites;
    Camera camera;
    ShaderProgram shader_program;

    @Override
    public void init(Node previous_continuous_data) {

        if (previous_continuous_data != null) {
            // TODO: Load data pre-context destroy
        } else {
            // TODO: Starting from scratch
        }

        TextureAtlas atlas  = new TextureAtlas(R.drawable.example_atlas, 1, 1);
        float[] color       = new float[] { 0.6f, 0.0f, 0.0f, 0.7f };

        this.sprites = new Sprite[6];
        for (int i = 0; i < this.sprites.length; i++) {
            this.sprites[i] = new Sprite(
                    atlas,
                    0,
                    0,
                    color,
                    BlendMode.values()[i],
                    null,
                    null);
        }
        this.camera = new Camera(0f, 0f, 0.33f);
        this.shader_program = new ShaderProgram(R.raw.vertex_shader, R.raw.fragment_shader);
    }

    @Override
    public boolean scale_input(float scale_factor, float focal_x, float focal_y) {
        return false;
    }

    @Override
    public boolean other_input(MotionEvent me) {
        return false;
    }

    @Override
    public void update(float dt) {
        for (int i = 0; i < this.sprites.length; i++)
            this.sprites[i].update(dt);
    }

    @Override
    public void render() {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        this.shader_program.bind();
        this.camera.set_uniforms(this.shader_program);

        for (int i = 0; i < this.sprites.length; i++) {
            this.sprites[i].render(this.shader_program,
                    -5f + 2f * (float)i, 0f);
        }
        ShaderProgram.unbind_any_shader_program();
    }

    @Override
    public void resize(float width, float height) {
        this.shader_program.bind();
        this.shader_program.set_uniform("aspect_ratio", (width / height));
        ShaderProgram.unbind_any_shader_program();
    }

    @Override
    public Node get_continuous_data() {
        return null;
    }
}
