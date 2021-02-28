package svenske.spacedust.stages;

import android.opengl.GLES20;
import android.view.MotionEvent;

import svenske.spacedust.R;
import svenske.spacedust.graphics.BlendMode;
import svenske.spacedust.graphics.Camera;
import svenske.spacedust.graphics.Font;
import svenske.spacedust.graphics.ShaderProgram;
import svenske.spacedust.graphics.Sprite;
import svenske.spacedust.graphics.TextSprite;
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

        Font font = new Font(R.drawable.font, R.raw.font_info);

        this.sprite = new TextSprite(font, new float[] { 0f, 1f, 0f, 1f }, BlendMode.MULTIPLICATIVE, "");

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
        ((TextSprite)this.sprite).set_text(
                "(" +
                Float.toString(x).substring(0, 5) + ", " + Float.toString(y).substring(0, 5) +
                ")");
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
        this.sprite.render(this.shader_program, x, y, 0.5f, 0.5f);

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
