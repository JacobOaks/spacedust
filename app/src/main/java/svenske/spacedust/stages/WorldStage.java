package svenske.spacedust.stages;

import android.opengl.GLES20;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

import svenske.spacedust.R;
import svenske.spacedust.gameobject.Bar;
import svenske.spacedust.gameobject.Enemy;
import svenske.spacedust.gameobject.GameObject;
import svenske.spacedust.gameobject.HUD;
import svenske.spacedust.gameobject.JoyStick;
import svenske.spacedust.gameobject.Player;
import svenske.spacedust.gameobject.World;
import svenske.spacedust.graphics.BlendMode;
import svenske.spacedust.graphics.Font;
import svenske.spacedust.graphics.Sprite;
import svenske.spacedust.graphics.TextSprite;
import svenske.spacedust.graphics.TextureAtlas;
import svenske.spacedust.utils.Global;
import svenske.spacedust.utils.Node;

/**
 * This stage represents the actual game world. The WorldStage basically consists of a World and a
 * HUD, updated and rendered separately.
 */
public class WorldStage implements Stage {

    // WorldStage attributes
    World world;
    HUD hud;

    // Important GameObjects
    Player player;
    GameObject FPS_text;
    Bar player_hp_bar;

    // Create's the World and the HUD of the WorldStage as well as its starting GameObjects.
    @Override
    public void init(Node previous_continuous_data) {

        // Initialize world and HUD
        this.world = new World(previous_continuous_data);
        this.world.get_camera().set_zoom(0.25f);
        this.hud = new HUD();

        // TODO: need to reposition on resize
        // Create objects
        this.create_player();
        this.create_joysticks();
        this.create_text();
    }

    // Creates the player and the player's health bar
    private void create_player() {

        // Create player health bar
        this.player_hp_bar = new Bar(new float[] { 0f, 1f, 0f, 0.5f}, new float[] { 1f, 0f, 0f, 0.8f},
                new float[] { 0.5f, 0.5f, 0.5f, 0.3f }, 1f, 0.1f, 0f, 0f);
        this.hud.add_object(player_hp_bar,
                null, HUD.RelativePlacement.BELOW,
                // If landscape, place in upper-right corner. If portrait, place in upper-left
                ((float) Global.VIEWPORT_WIDTH / (float) Global.VIEWPORT_HEIGHT) >= 1.0f ?
                        HUD.Alignment.LEFT : HUD.Alignment.RIGHT,
                0.1f);

        // Create player
        TextureAtlas ta = new TextureAtlas(R.drawable.texture_sheet, 16, 16);
        this.player = new Player(ta,0f, 0f, this.player_hp_bar, this.world, this.world);
        this.world.add_game_object(this.player);

        // Create enemy
        Enemy e = new Enemy(ta, 5f, 5f, true, this.world, this.world);
        e.set_target(this.player);
        this.world.add_game_object(e);
        e = new Enemy(ta, 3f, 9f, true, this.world, this.world);
        e.set_target(this.player);
        this.world.add_game_object(e);
    }

    // Creates the JoySticks
    private void create_joysticks() {

        // Create movement joystick
        JoyStick movement_stick = new JoyStick("movement",
                0f, 0f, 0.25f, player);
        movement_stick.set_scale(0.6f, 0.6f);
        this.hud.add_object(movement_stick,
                null, HUD.RelativePlacement.ABOVE, HUD.Alignment.LEFT, 0.1f);

        // Create shooting joystick
        JoyStick rotation_stick = new JoyStick("shooting",
                0f, 0f, 0.25f, player);
        rotation_stick.set_scale(0.6f, 0.6f);
        this.hud.add_object(rotation_stick,
                null, HUD.RelativePlacement.ABOVE, HUD.Alignment.RIGHT, 0.1f);
    }

    // Creates text to put on the HUD
    private void create_text() {

        // Create title
        Font font = new Font(R.drawable.font, R.raw.font_info);
        Sprite title_sprite = new TextSprite(font, new float[] { 1f, 1f, 1f, 1f },
                BlendMode.MULTIPLICATIVE, "Space Dust").solidify();
        title_sprite.set_color(new float[] { 1f, 1f, 1f, 0.6f });
        title_sprite.set_blend_mode(BlendMode.MULTIPLICATIVE);
        GameObject title = new GameObject(title_sprite, 0f, 0f);
        title.set_scale(0.14f, 0.14f);
        this.hud.add_object(title,
                // If landscape, place on top-left screen edge. If portrait, place below HP bar
                ((float) Global.VIEWPORT_WIDTH / (float) Global.VIEWPORT_HEIGHT) >= 1.0f ?
                        this.player_hp_bar : null,
                HUD.RelativePlacement.BELOW, HUD.Alignment.LEFT, 0.05f);

        // Create version info
        Sprite version_sprite = new TextSprite(font, new float[] { 1f, 1f, 1f, 1f },
                BlendMode.MULTIPLICATIVE, "(prototype 1)").solidify();
        version_sprite.set_color(new float[] { 1f, 1f, 1f, 0.6f });
        version_sprite.set_blend_mode(BlendMode.MULTIPLICATIVE);
        GameObject version_text = new GameObject(version_sprite, 0f, 0f);
        version_text.set_scale(0.05f, 0.05f);
        this.hud.add_object(version_text,
                title, HUD.RelativePlacement.BELOW, HUD.Alignment.LEFT, 0.05f);

        // Create FPS text
        Sprite fps_text_sprite = new TextSprite(font, new float[] { 1f, 1f, 1f, 0.6f },
                BlendMode.MULTIPLICATIVE, "FPS: ");
        this.FPS_text = new GameObject(fps_text_sprite, 0f,0f);
        this.FPS_text.set_scale(0.07f, 0.07f);
        this.hud.add_object(FPS_text,
                version_text, HUD.RelativePlacement.BELOW, HUD.Alignment.LEFT, 0.05f);

    }

    // Responds to input by allowing the HUD and the World to respond to it.
    @Override
    public boolean input(MotionEvent me) {
        List<Integer> ignore_idx = new ArrayList<>();
        this.hud.input(me, ignore_idx);
        this.world.input(me, ignore_idx);
        return true;
    }

    // Updates the objects in the world and the HUD, and ensures the camera's position is correct.
    @Override
    public void update(float dt) {
        this.world.update(dt);
        this.hud.update(dt);
        float[] player_pos = this.player.get_pos();
        this.world.get_camera().set_position(player_pos[0], player_pos[1]);
    }

    // Responds to FPS updates by reflecting the new FPS via some text on the screen.
    @Override
    public void fps_update(float fps) {
        ((TextSprite)this.FPS_text.get_sprite()).set_text("FPS: " + fps);
    }

    // Renders the world, then the HUD.
    @Override
    public void render() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        this.world.render();
        this.hud.render();
    }

    // Notifies the world and the HUD of the resize.
    @Override
    public void resized() {
        this.world.resized();
        this.hud.resized();
    }

    @Override
    public Node get_continuous_data() {
        return this.world.get_continuous_data();
    }
}
