package svenske.spacedust.stages;

import android.opengl.GLES20;
import android.view.MotionEvent;

import java.util.HashMap;
import java.util.Map;

import svenske.spacedust.R;
import svenske.spacedust.graphics.AnimatedSprite;
import svenske.spacedust.graphics.AnimatedTextSprite;
import svenske.spacedust.graphics.Animation;
import svenske.spacedust.graphics.BlendMode;
import svenske.spacedust.graphics.Camera;
import svenske.spacedust.graphics.Font;
import svenske.spacedust.graphics.ShaderProgram;
import svenske.spacedust.graphics.Sprite;
import svenske.spacedust.graphics.TextAnimation;
import svenske.spacedust.graphics.TextSprite;
import svenske.spacedust.graphics.TextureAtlas;
import svenske.spacedust.utils.Global;
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

        this.shader_program = new ShaderProgram(R.raw.vertex_world, R.raw.fragment_world);
        this.camera = new Camera(0f, 0f, 0.2f);

        Font font = new Font(R.drawable.font, R.raw.font_info);
        this.sprite = new TextSprite(font, null, BlendMode.JUST_TEXTURE, "Some text").solidify();
    }

    @Override
    public boolean scale_input(float scale_factor, float focal_x, float focal_y) {
        return false;
    }

    float x, y;

    @Override
    public boolean other_input(MotionEvent me) {
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
        this.sprite.render(this.shader_program, x, y, 2f, 2f);

        ShaderProgram.unbind_any_shader_program();
    }

    @Override
    public void resized() {
        this.shader_program.bind();
        this.shader_program.set_uniform("aspect_ratio",
                ((float)Global.VIEWPORT_WIDTH / (float)Global.VIEWPORT_HEIGHT));
        ShaderProgram.unbind_any_shader_program();
    }

    @Override
    public Node get_continuous_data() {
        return null;
    }
}
