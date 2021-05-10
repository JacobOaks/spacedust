package svenske.spacedust.graphics;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * An encapsulation for customizable animation data.
 */
public class Animation {

    // Static method to get standard animations for ships
    public static Map<String, Animation> get_generic_ship_animations(int idle_atlas_row) {
        Map<String, Animation> anims = new HashMap<>();

        // Idle animation
        Animation idle = new Animation(0.1f, 12, new int[] { idle_atlas_row },
                new int[] { 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 2 }, new float[][] { null },
                new BlendMode[] { BlendMode.JUST_TEXTURE });
        anims.put("idle", idle);

        // Engines animation
        Animation engines = new Animation(0.1f, 12, new int[] { idle_atlas_row + 1 },
                new int[] { 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 2 }, new float[][] { null },
                new BlendMode[] { BlendMode.JUST_TEXTURE });
        anims.put("thrust", engines);

        return anims;
    }

    // Constant describing the interval at which the generic ship animation has its lights on
    public static final int GENERIC_SHIP_ANIMATION_LIGHT_INTERVAL = 6;

    // Animation attributes
    public float frame_time;
    public int frames;
    public int[] atlas_rows;
    public int[] atlas_cols;
    public float[][] colors;
    public BlendMode[] blend_modes;

    /**
     * Creates a new animation. Each array parameter must be at least length 1. If an array's length
     * is less than the amount of frames, the pattern will be repeated using the modulo operator.
     * @param colors can contain null values to represent no color
     * @param atlas_rows can contain the value -1 to represent no texture
     * @param atlas_cols can contain the value -1 to represent no texture
     * @param frame_time how long each frame should last, in seconds.
     * @param frames how many frames there are.
     */
    public Animation(float frame_time, int frames, int[] atlas_rows, int[] atlas_cols,
                     float[][] colors, BlendMode[] blend_modes) {
        this.frame_time  = frame_time;
        this.frames      = frames;
        this.atlas_rows  = atlas_rows;
        this.atlas_cols  = atlas_cols;
        this.colors      = colors;
        this.blend_modes = blend_modes;
        this.check_integrity();
    }

    /**
     * Does some basic checks to make sure the given animation information is valid
     */
    public void check_integrity() {

        // Check frame time valid
        if (this.frame_time < 0)
            Log.d("[spdt/animation]", "negative frame time. Undefined behavior.");

        // Check all colors are valid length
        for (int i = 0; i < this.colors.length; i++)
            if (this.colors[i] != null && this.colors[i].length != 4)
                throw new RuntimeException("[spdt/animation]: color " + i + " is not length 4");

        // Check blends of all frames make sense
        for (int i = 0; i < this.frames; i++) {
            boolean has_color = this.colors[i % this.colors.length] != null;
            boolean has_atlas = this.atlas_cols[i % this.atlas_cols.length] != -1;
            has_atlas = has_atlas && this.atlas_rows[i % this.atlas_rows.length] != -1;
            Sprite.check_blend_mode(has_atlas, has_color, this.blend_modes[
                    i % this.blend_modes.length]);
        }
    }
}
