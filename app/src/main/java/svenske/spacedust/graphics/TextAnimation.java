package svenske.spacedust.graphics;

import android.util.Log;

// An encapsulation for customizable text animation data.
public class TextAnimation {

    // Text animation attributes
    public float frame_time;
    public int frames;
    public Font[] fonts;
    public float[][] colors;
    public String[] texts;
    public BlendMode[] blend_modes;

    /**
     * Creates a new text animation. Each array parameter must be at least length 1. If an array's
     * length is less than the amount of frames, the pattern will be repeated using the modulo
     * operator.
     * @param colors can contain null values to represent no color.
     * @param frame_time how long each frame should last, in seconds.
     * @param frames how many frames there are.
     */
    public TextAnimation(float frame_time, int frames, Font[] fonts, float[][] colors,
                         String[] texts, BlendMode[] blend_modes) {
        this.frame_time  = frame_time;
        this.frames      = frames;
        this.fonts       = fonts;
        this.colors      = colors;
        this.texts       = texts;
        this.blend_modes = blend_modes;
        this.check_integrity();
    }

    /**
     * Does some basic checks to make sure the given text animation information is valid
     */
    public void check_integrity() {

        // Check frame time valid
        if (this.frame_time < 0)
            Log.d("[spdt/textanimation]", "negative frame time. Undefined behavior.");

        // Check all colors are valid length
        for (int i = 0; i < this.colors.length; i++)
            if (this.colors[i] != null && this.colors[i].length != 4)
                throw new RuntimeException("[spdt/textanimation]: color " + i + " is not length 4");

        // Check blends of all frames make sense
        for (int i = 0; i < this.frames; i++) {
            boolean has_color = this.colors[i % this.colors.length] != null;
            Sprite.check_blend_mode(true, has_color, this.blend_modes[
                    i % this.blend_modes.length]);
        }
    }
}
