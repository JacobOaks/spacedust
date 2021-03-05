package svenske.spacedust.gameobject;

import svenske.spacedust.graphics.BlendMode;
import svenske.spacedust.graphics.LightSource;
import svenske.spacedust.graphics.Sprite;

// A bullet shot in a specific direction at a specific speed that emits a light
public class Bullet extends GameObject implements LightEmitter {

    // The bullet emits a small light.
    private LightSource ls;

    /**
     * Constructs the bullet.
     * @param color the color to be used for the sprite itself and its glow.
     * @param x the starting x position for the bullet.
     * @param y the starting y position for the bullet.
     * @param v_angle the angle of the bullet's movement.
     * @param v_magnitude the magnitude of the bullet's velocity.
     */
    public Bullet(float[] color, float x, float y, float v_angle, float v_magnitude) {
        super(new Sprite(null, -1, -1, color, BlendMode.JUST_COLOR,
                null, null), x, y);

        // Set rotation and scale of bullet model
        this.sy = 0.2f;
        this.sx = 0.05f;
        this.rot = v_angle;

        // Set appropriate velocity
        this.vx = (float)Math.cos(v_angle + Math.PI / 2) * v_magnitude;
        this.vy = (float)Math.sin(v_angle + Math.PI / 2) * v_magnitude;

        // Set light source
        this.ls = new LightSource(new float[] { color[0] / 2f, color[1] / 2f, color[2] / 2f },
                0.8f, 1f, null);
    }

    // Return the bullet's light source
    @Override
    public LightSource get_light() { return this.ls; }
}
