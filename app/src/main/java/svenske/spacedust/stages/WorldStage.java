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

        Font font = new Font(R.drawable.font, R.raw.font_info);

        this.sprite1 = new TextSprite(font, new float[] { 0f, 1f, 0f, 1f }, BlendMode.MULTIPLICATIVE,
                "hi");
        this.sprite2 = ((TextSprite)this.sprite1).solidify();

        this.camera = new Camera(0f, 0f, 0.2f);
        this.shader_program = new ShaderProgram(R.raw.vertex_world, R.raw.fragment_world);

        if (previous_continuous_data != null) {
            this.x = Float.parseFloat(previous_continuous_data.get_child("x").get_value());
            this.y = Float.parseFloat(previous_continuous_data.get_child("y").get_value());
            ((TextSprite)this.sprite1).set_text(previous_continuous_data.get_child("text").get_value());
            // TODO: Load data pre-context destroy
        }
    }

    @Override
    public boolean scale_input(float scale_factor, float focal_x, float focal_y) {
        return false;
    }

    float x, y;

    @Override
    public boolean other_input(MotionEvent me) {
        float[] world_pos = Transform.screen_to_world(me.getX(), me.getY(),
                Global.VIEWPORT_WIDTH, Global.VIEWPORT_HEIGHT, this.camera);
        x = world_pos[0];
        y = world_pos[1];
        ((TextSprite)sprite1).set_text(
                "(" +
                Float.toString(x).substring(0, 4) +
                ", " +
                Float.toString(y).substring(0, 4) +
                ")"
        );
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
        this.sprite1.render(this.shader_program, 0f, 0f, 1f, 1f);
        this.sprite2.render(this.shader_program, x, y, 1f, 1f);

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
        Node cont_data = new Node("world_stage");
        cont_data.add_child("x", Float.toString(this.x));
        cont_data.add_child("y", Float.toString(this.y));
        cont_data.add_child("text", ((TextSprite)sprite1).getText());
        return cont_data;
    }
}
