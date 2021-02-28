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
    Sprite sprite1, sprite2;

    @Override
    public void init(Node previous_continuous_data) {

        this.shader_program = new ShaderProgram(R.raw.vertex_world, R.raw.fragment_world);
        this.camera = new Camera(0f, 0f, 0.2f);

        Font font = new Font(R.drawable.font, R.raw.font_info);
        TextAnimation ta = new TextAnimation(0.8f, 4,
                new Font[]{ font },
                new float[][] { new float[] { 1.0f, 0.0f, 0.0f, 1.0f } },
                new String[] { "Hey", "Dude" },
                new BlendMode[] { BlendMode.MULTIPLICATIVE, BlendMode.ADDITIVE,
                        BlendMode.SUBTRACTIVE, BlendMode.AVG });

        Map<String, TextAnimation> anims = new HashMap<>();
        anims.put("sole", ta);
        this.sprite1 = new AnimatedTextSprite(anims, "sole");

        TextureAtlas texture_atlas = new TextureAtlas(R.drawable.example_atlas, 3, 3);
        Animation anim = new Animation(0.2f, 9,
                new int[] { 0, 0, 0, 1, 1, 1, 2, 2, 2 },
                new int[] { 0, 1, 2 },
                new float[][] { null }, new BlendMode[] { BlendMode.JUST_TEXTURE } );
        Map<String, Animation> anims2 = new HashMap<>();
        anims2.put("sole", anim);
        this.sprite2 = new AnimatedSprite(texture_atlas, anims2, "sole");
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
        this.sprite1.update(dt);
        this.sprite2.update(dt);
    }

    @Override
    public void render() {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        this.shader_program.bind();
        this.camera.set_uniforms(this.shader_program);
        this.sprite1.render(this.shader_program, x, y, 1f, 1f);
        this.sprite2.render(this.shader_program, x, y + 2f, 1f, 1f);

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
