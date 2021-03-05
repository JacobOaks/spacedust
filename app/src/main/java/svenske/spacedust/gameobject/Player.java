package svenske.spacedust.gameobject;

import java.util.HashMap;
import java.util.Map;

import svenske.spacedust.graphics.AnimatedSprite;
import svenske.spacedust.graphics.Animation;
import svenske.spacedust.graphics.BlendMode;
import svenske.spacedust.graphics.TextureAtlas;

public class Player extends GameObject implements JoyStick.JoystickReceiver {

    private float movement_angle;
    private boolean moving = false;
    private boolean shooting = false;
    private float max_v = 6f;                      // units per second
    private float ax, ay;                          // units per second^2
    private float acceleration_capability = 0.13f; // units per second^2

    public Player(TextureAtlas atlas, float x, float y) {
        super(null, x, y);
        this.setup_sprite(atlas);
    }

    private void setup_sprite(TextureAtlas atlas) {
        Animation idle = new Animation(0.1f, 12, new int[] { 0 },
                new int[] { 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 2 }, new float[][] { null },
                new BlendMode[] { BlendMode.JUST_TEXTURE });
        Map<String, Animation> anims = new HashMap<>();
        anims.put("idle", idle);
        this.sprite = new AnimatedSprite(atlas, anims, "idle",
                null, null);
    }

    @Override
    void update(float dt) {
        this.vx = Math.max(-this.max_v, Math.min(this.vx + this.ax, this.max_v));
        this.vy = Math.max(-this.max_v, Math.min(this.vy + this.ay, this.max_v));
        super.update(dt);
    }

    @Override
    public void receive_dir_vec(String id, float x, float y, float magnitude) {
        if (id.equals("movement")) {
            this.moving = true;
            this.movement_angle = (float)Math.atan2(y, x) - (float)(Math.PI / 2f);
            if (!this.shooting) this.rot = this.movement_angle;
            this.ax = this.acceleration_capability * magnitude *
                    (float)Math.cos(this.movement_angle + (float)(Math.PI / 2f));
            this.ay = this.acceleration_capability * magnitude *
                    (float)Math.sin(this.movement_angle + (float)(Math.PI / 2f));

        } else if (id.equals("rotation")) {
            this.shooting = true;
            this.rot = (float)Math.atan2(y, x) - (float)(Math.PI / 2f);
        }
    }

    @Override
    public void input_ended(String id) {
        if (id.equals("movement")) {
            this.moving = false;
            this.ax = this.ay = 0f;
        }
        else if (id.equals("rotation")) {
            this.shooting = false;
            if (this.moving) this.rot = this.movement_angle;
        }
    }
}
