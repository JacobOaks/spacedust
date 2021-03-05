package svenske.spacedust.stages;

import android.opengl.GLES20;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import svenske.spacedust.R;
import svenske.spacedust.gameobject.GameObject;
import svenske.spacedust.gameobject.HUD;
import svenske.spacedust.gameobject.JoyStick;
import svenske.spacedust.gameobject.World;
import svenske.spacedust.graphics.AnimatedTextSprite;
import svenske.spacedust.graphics.BlendMode;
import svenske.spacedust.graphics.Font;
import svenske.spacedust.graphics.Sprite;
import svenske.spacedust.graphics.TextAnimation;
import svenske.spacedust.graphics.TextSprite;
import svenske.spacedust.graphics.TextureAtlas;
import svenske.spacedust.utils.Node;

/**
 * This stage represents the actual game world. The WorldStage basically consists of a World and a
 * HUD, updated and rendered separately.
 */
public class WorldStage implements Stage {

    // WorldStage attributes
    World world;
    HUD hud;
    GameObject FPSText;

    // Create's the World and the HUD of the WorldStage.
    @Override
    public void init(Node previous_continuous_data) {
        this.world = new World(previous_continuous_data);
        this.world.get_camera().set_zoom(0.33f);
        this.hud = new HUD();

        // Create player
        TextureAtlas ta = new TextureAtlas(R.drawable.example_atlas, 1, 1);
        Sprite player_sprite = new Sprite(ta, 0, 0, null,
                BlendMode.JUST_TEXTURE, null, null);
        GameObject player = new GameObject(player_sprite, 0f, 0f);
        this.world.add_game_object(player);

        // Create scratch object
        Sprite scratch_sprite = new Sprite(ta, 0, 0, new float[] { 1f, 0, 1f, 1f },
                BlendMode.AVG, null, null);
        GameObject scratch_obj = new GameObject(scratch_sprite, 2f, 2f);
        scratch_obj.set_scale(0.8f, 0.8f);
        this.world.add_game_object(scratch_obj);

        // Create joystick
        JoyStick joy_stick = new JoyStick("movement_stick", 0f, 0f, null);
        this.hud.add_object(joy_stick, null, HUD.RelativePlacement.ABOVE, HUD.Alignment.LEFT, 0.1f);

        // Create title
        Font font = new Font(R.drawable.font, R.raw.font_info);
        TextSprite ts = new TextSprite(font, new float[] { 1f, 1f, 1f, 1f },
                BlendMode.MULTIPLICATIVE, "Space Dust");
        GameObject title = new GameObject(ts, 0f, 0f);
        title.set_scale(0.14f, 0.14f);
        this.hud.add_object(title, null,
                HUD.RelativePlacement.BELOW, HUD.Alignment.LEFT, 0.05f);

        // Create FPS text
        ts = new TextSprite(font, new float[] { 1f, 1f, 1f, 1f },
                BlendMode.MULTIPLICATIVE, "FPS: ");
        FPSText = new GameObject(ts, 0f,0f);
        FPSText.set_scale(0.07f, 0.07f);
        this.hud.add_object(FPSText, title, HUD.RelativePlacement.BELOW, HUD.Alignment.LEFT, 0.05f);
    }

    @Override
    public boolean input(MotionEvent me) {
        List<Integer> ignore_idx = new ArrayList<>();
        this.hud.input(me, ignore_idx);
        this.world.input(me, ignore_idx);
        return true;
    }

    @Override
    public void update(float dt) {
        this.world.update(dt);
        this.hud.update(dt);
    }

    @Override
    public void fps_update(float fps) {
        ((TextSprite)this.FPSText.get_sprite()).set_text("FPS: " + fps);
    }

    @Override
    public void render() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        this.world.render();
        this.hud.render();
    }

    @Override
    public void resized() {
        this.world.resized();
        this.hud.resized();
    }

    @Override
    public Node get_continuous_data() {
        return null;
    }
}
