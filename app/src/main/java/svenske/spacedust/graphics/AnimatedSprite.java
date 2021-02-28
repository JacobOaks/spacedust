package svenske.spacedust.graphics;

import java.util.Map;

/**
 * Extends Sprite by supporting Animations. It's not super computationally complex, but static
 * objects that can be represented by normal Sprites should continue to be. The one restriction to
 * note for AnimatedSprites is that all textures used must be on the same atlas.
 */
public class AnimatedSprite extends Sprite {

    /**
     * A map from strings (animation) names to actual Animation info.
     * e.g., a ship could have "idle", "shooting", etc. different animations.
     */
    private Map<String, Animation> anims;
    private Animation current_anim; // currently active animation

    // Current frame and how many seconds it has left
    private int current_frame;
    private float time_left;

    /**
     * Creates the AnimatedSprite with the given atlas and animation info (mapped from name to
     * animation).
     * @param starting_anim the name of the animation to start with/
     */
    public AnimatedSprite(TextureAtlas atlas, Map<String, Animation> anims, String starting_anim) {
        super(atlas,
                anims.get(starting_anim).atlas_rows != null ? anims.get(starting_anim).atlas_rows[0] : null,
                anims.get(starting_anim).atlas_cols != null ? anims.get(starting_anim).atlas_cols[0] : null,
                anims.get(starting_anim).colors != null ? anims.get(starting_anim).colors[0] : null,
                anims.get(starting_anim).blend_modes[0],
                null, null);

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
     * Switches the currently active frame and does some basic safety checks
     */
    public void switch_frames(int frame) {
        this.current_frame = frame % this.current_anim.frames;

        int atlas_row = -1;
        if (this.current_anim.atlas_rows != null)
            atlas_row = this.current_anim.atlas_rows[this.current_frame];

        int atlas_col = -1;
        if (this.current_anim.atlas_cols != null)
            atlas_col = this.current_anim.atlas_cols[this.current_frame];

        this.color = null;
        if (this.current_anim.colors != null)
            this.color = this.current_anim.colors[this.current_frame];

        this.blend_mode = this.current_anim.blend_modes[this.current_frame];
        Sprite.check_blend_mode((atlas_row != -1 && atlas_col != -1) ? this.atlas : null,
                this.color, this.blend_mode);

        this.texture_coordinates = TextureAtlas.get_tex_coords_buffer(
                this.atlas, atlas_row, atlas_col);
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
