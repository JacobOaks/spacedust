package svenske.spacedust.graphics;

import android.util.Log;

/**
 * An encapsulation for hugely customizable animation data.
 */
public class Animation {

    /**
     * The arrays below must be as long as the amount of frames this animation has. Or, if texture
     * isn't used in any frame, atlas_rows and atlas_cols can be null; If color is not used in any
     * frame, colors can be null.
     */
    public float frame_time;
    public int frames;
    public int[] atlas_rows;
    public int[] atlas_cols;
    public float[][] colors;
    public BlendMode[] blend_modes;

    /**
     * Creates a new Animation
     * @param frame_time how long each frame should last, in seconds
     * @param frames how many frames there are
     * @param atlas_rows the texture atlas row for each frame. set values to -1 when no texture is
     *                   in that frame
     * @param atlas_cols the texture atlas column for each frame. set values to -1 when no texture
     *                   is in that frame
     * @param colors the color for each frame. set values to null when no color is in that frame
     * @param blend_modes the blend modes for each frame
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
        if (this.atlas_rows != null && this.atlas_rows.length != this.frames)
            throw new RuntimeException("[spdt/animation]: invalid amount of atlas rows");
        if (this.atlas_cols != null && this.atlas_cols.length != this.frames)
            throw new RuntimeException("[spdt/animation]: invalid amount of atlas cows");
        if (this.colors != null && this.colors.length != this.frames)
            throw new RuntimeException("[spdt/animation]: invalid amount of colors");
        if (this.blend_modes.length != this.frames)
            throw new RuntimeException("[spdt/animation]: invalid amount of blend_modes");
        if (this.frame_time < 0)
            Log.d("[spdt/animation]", "negative frame time. Undefined behavior.");
        if (this.colors != null)
            for (int i = 0; i < this.colors.length; i++)
                if (this.colors[i] != null && this.colors[i].length != 4)
                    throw new RuntimeException("[spdt/animation]: color " + i + " is not length 4");
    }
}
