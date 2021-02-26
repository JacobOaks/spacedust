package svenske.spacedust.stages;

import android.opengl.GLES20;
import android.util.Log;
import android.view.MotionEvent;

import svenske.spacedust.R;
import svenske.spacedust.graphics.ShaderProgram;
import svenske.spacedust.graphics.Sprite;
import svenske.spacedust.utils.Node;

/**
 * This stage represents the actual game world.
 */
public class WorldStage implements Stage {

    Sprite sprite;
    ShaderProgram shader_program;

    @Override
    public void init(Node previous_continuous_data) {

        if (previous_continuous_data != null) {
            // TODO: Load data pre-context destroy
        } else {
            // TODO: Starting from scratch
        }

        this.sprite = new Sprite(new float[] {0.9f, 0.1f, 0.9f, 1.0f}, null, null);
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

        /**
         * TODO: Update World
         * TODO: Update HUD
         */

        this.sprite.update(dt);
    }

    @Override
    public void render() {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        this.shader_program.bind();
        this.shader_program.set_uniform("cam_x", 1f);
        this.shader_program.set_uniform("cam_y", 1f);
        this.shader_program.set_uniform("cam_zoom", 0.5f);

        /**
         * TODO: Render World
         * TODO: Render HUD
         */

        this.sprite.render(this.shader_program, 0f, 0f);
        ShaderProgram.unbind_any_shader_program();
    }

    @Override
    public void resize(float width, float height) {
        Log.d("spdt/worldstage", "new aspect ratio: " + (width / height));
        this.shader_program.bind();
        this.shader_program.set_uniform("aspect_ratio", (width / height));
        ShaderProgram.unbind_any_shader_program();
    }

    @Override
    public Node get_continuous_data() {

        //TODO: Return important information from the World and/or HUD

        return null;
    }
}
