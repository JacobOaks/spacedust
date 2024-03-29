package svenske.spacedust.stages;

import android.opengl.GLES20;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

import svenske.spacedust.R;
import svenske.spacedust.gameobject.Bar;
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

import static svenske.spacedust.gameobject.HUD.Alignment.LEFT;

/**
 * This stage represents the actual game world. The WorldStage basically consists of a World and a
 * HUD, updated and rendered separately.
 */
public class WorldStage implements Stage {

    // WorldStage attributes
    World world;
    HUD hud;

    // Important GameObjects
    Player player;                       // Reference to the game's player
    Bar player_hp_bar;                   // Player's health bar on the HUD
    GameObject FPS_text;                 // FPS text on the HUD
    public static GameObject score_text; // Text displaying how the current score

    // Creates the World and the HUD of the WorldStage as well as its starting GameObjects.
    @Override
    public void init(Node previous_continuous_data) {

        // Initialize global texture atlases
        Global.font = new Font(R.drawable.font, R.raw.font_info);
        Global.ta = new TextureAtlas(R.drawable.texture_sheet, 16, 16);

        // Initialize world and HUD
        this.world = new World(previous_continuous_data);
        this.world.get_camera().set_zoom(0.25f);
        this.hud = new HUD();

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
                        LEFT : HUD.Alignment.RIGHT,
                0.1f);

        // Create player
        this.player = new Player(Global.ta,0f, 0f, this.player_hp_bar, this.world);
        this.world.add_game_object(this.player);
    }

    // Creates the JoySticks
    private void create_joysticks() {

        // Create movement joystick
        JoyStick movement_stick = new JoyStick("movement",
                0f, 0f, 0.25f, player);
        movement_stick.set_scale(0.6f, 0.6f);
        this.hud.add_object(movement_stick,
                null, HUD.RelativePlacement.ABOVE, LEFT, 0.1f);

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
        Sprite title_sprite = new TextSprite(Global.font, new float[] { 1f, 1f, 1f, 1f },
                BlendMode.MULTIPLICATIVE, "Space Dust").solidify();
        title_sprite.set_color(new float[] { 1f, 1f, 1f, 0.6f });
        title_sprite.set_blend_mode(BlendMode.MULTIPLICATIVE);
        GameObject title = new GameObject(title_sprite, 0f, 0f);
        title.set_scale(0.14f, 0.14f);
        this.hud.add_object(title, null, HUD.RelativePlacement.BELOW, LEFT, 0.05f);

        // Create version info
        Sprite version_sprite = new TextSprite(Global.font, new float[] { 1f, 1f, 1f, 1f },
                BlendMode.MULTIPLICATIVE, "(prototype 2)").solidify();
        version_sprite.set_color(new float[] { 1f, 1f, 1f, 0.6f });
        version_sprite.set_blend_mode(BlendMode.MULTIPLICATIVE);
        GameObject version_text = new GameObject(version_sprite, 0f, 0f);
        version_text.set_scale(0.05f, 0.05f);
        this.hud.add_object(version_text,
                title, HUD.RelativePlacement.BELOW, LEFT, 0.03f);

        // Create FPS text
        Sprite fps_text_sprite = new TextSprite(Global.font, new float[] { 1f, 1f, 1f, 0.6f },
                BlendMode.MULTIPLICATIVE, "FPS: ");
        this.FPS_text = new GameObject(fps_text_sprite, 0f,0f);
        this.FPS_text.set_scale(0.07f, 0.07f);
        this.hud.add_object(FPS_text,
                version_text, HUD.RelativePlacement.BELOW, LEFT, 0.05f);

        // Create kills text
        Sprite score_text_sprite = new TextSprite(Global.font, new float[] { 0f, 1f, 0f, 0.8f },
                BlendMode.MULTIPLICATIVE, "Score: 0");
        WorldStage.score_text = new GameObject(score_text_sprite, 0f, 0f);
        WorldStage.score_text.set_scale(0.06f, 0.06f);
        this.hud.add_object(WorldStage.score_text,
                this.FPS_text, HUD.RelativePlacement.BELOW, LEFT, 0.03f);
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
