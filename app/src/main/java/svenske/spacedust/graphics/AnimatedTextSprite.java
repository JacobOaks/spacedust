package svenske.spacedust.graphics;

import java.util.Map;

public class AnimatedTextSprite extends TextSprite {

    /**
     * A map from strings (animation) names to actual TextAnimation info.
     */
    private Map<String, TextAnimation> anims;
    private TextAnimation current_anim; // currently active animation

    // Current frame and how many seconds it has left
    private int current_frame;
    private float time_left;

    public AnimatedTextSprite(Map<String, TextAnimation> anims, String starting_anim) {
        super(
                anims.get(starting_anim).fonts[0],
                anims.get(starting_anim).colors != null ? anims.get(starting_anim).colors[0] : null,
                anims.get(starting_anim).blend_modes[0],
                anims.get(starting_anim).texts[0]
                );

        this.anims = anims;
        this.current_anim = this.anims.get(starting_anim);
        this.current_frame = 0;
        this.time_left = this.current_anim.frame_time;
    }

    /**
     * Checks for necessary frame changes
     */
    @Override
    public void update(float dt) {
        super.update(dt);
        this.time_left -= dt;
        while (this.time_left < 0f) {
            this.time_left += this.current_anim.frame_time;
            this.switch_frames(this.current_frame + 1);
        }
    }

    /**
     * Switches the currently active frame
     */
    public void switch_frames(int frame) {
        this.current_frame = frame % this.current_anim.frames;

        boolean buffers_need_updated = false;

        // Update font
        TextureAtlas old_atlas = this.atlas;
        this.atlas = this.current_anim.fonts[this.current_frame % this.current_anim.fonts.length];
        if (old_atlas != this.atlas) buffers_need_updated = true;

        // Update color
        this.color = null;
        if (this.current_anim.colors != null)
            this.color = this.current_anim.colors[
                    this.current_frame % this.current_anim.colors.length];

        // Update blend mode
        this.blend_mode = this.current_anim.blend_modes[
                this.current_frame % this.current_anim.blend_modes.length];

        // Update text
        String new_text = this.current_anim.texts[
                this.current_frame % this.current_anim.texts.length];
        if (!this.text.equals(new_text)) {
            this.set_text(new_text);
            buffers_need_updated = false;
        }

        // Update buffers if necessary
        if (buffers_need_updated) this.update_buffers();
    }

    /**
     * Changes the currently active animation on the Sprite
     * @param animation_name the name of the new animation
     * @param carry_over if `true`, an attempt to keep the same frame will be made (modulo will be
     *                   used, so this is a safe operation)
     */
    public void change_animation(String animation_name, boolean carry_over) {
        this.current_anim = this.anims.get(animation_name);
        this.switch_frames(carry_over ? this.current_frame : 0);
    }
}
