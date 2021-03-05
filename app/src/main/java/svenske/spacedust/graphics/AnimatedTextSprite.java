package svenske.spacedust.graphics;

import android.opengl.GLES20;

import java.util.HashMap;
import java.util.Map;

import svenske.spacedust.R;
import svenske.spacedust.utils.Global;
import svenske.spacedust.utils.Utils;

import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.glDeleteFramebuffers;
import static android.opengl.GLES20.glViewport;

/**
 * A TextSprite that can be animated. Just like TextSprite, these can be solidified, but into
 * AnimatedSprites. This is highly advised when possible as those will be less computationally
 * expensive to update and render.
 */
public class AnimatedTextSprite extends TextSprite {

    /**
     * A map from strings (animation) names to actual TextAnimation info.
     */
    private Map<String, TextAnimation> anims;
    private TextAnimation current_anim; // currently active animation
    private String starting_anim;       // stored in case of solidification

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
        this.starting_anim = starting_anim;
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
        this.time_left = this.current_anim.frame_time;
        this.switch_frames(carry_over ? this.current_frame : 0);
    }

    /**
     * Converts the AnimatedTextSprite into a normal TextSprite using FBOs. Note the resulting
     * AnimatedSprite will start at frame 0 of the starting animation.
     */
    @Override
    public Sprite solidify() {

        // Save previous current frame and animation to revert to at the end
        int previous_frame = this.current_frame;
        TextAnimation previous_animation = this.current_anim;

        // Calculate max texture width, and max animation frames (over all animations)
        int max_texture_width = 0;
        int max_frames = 0;
        for (String s : this.anims.keySet()) {
            if (this.anims.get(s).frames > max_frames) max_frames = this.anims.get(s).frames;
            for (int i = 0; i < this.anims.get(s).texts.length; i++) {
                int text_width = ((Font)this.atlas).get_pixel_width_for_text(
                        this.anims.get(s).texts[i], true);
                if (text_width > max_texture_width) max_texture_width = text_width;
            }
        }

        // New width in pixels
        int new_texture_width = max_texture_width * max_frames;
        // Height of one row in pixels
        int new_texture_row_height = ((Font)this.atlas).get_pixel_height_for_text();
        // New height in pixels
        int new_texture_height = new_texture_row_height * this.anims.size();
        // Max width of any animation state in normalized space
        float max_width = ((float)max_texture_width / (float)new_texture_row_height);

        // Create new FBO and bound texture
        int[] ids = Utils.get_new_fbo_and_bound_texture(new_texture_width, new_texture_height);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, ids[0]);

        // Create solidification shader program
        ShaderProgram sp = new ShaderProgram(R.raw.vertex_solidify, R.raw.fragment_solidify);

        // Set viewport to final texture size and set clear color to full transparency
        GLES20.glViewport(0, 0, new_texture_width, new_texture_height);
        GLES20.glClearColor(0f, 0f, 0f, 0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        sp.bind();
        int sheet_row = 0;
        Map<String, Animation> new_anims = new HashMap<>();

        // One row of new sheet -> one animation
        for (String s : this.anims.keySet()) {
            TextAnimation anim = this.anims.get(s);
            this.change_animation(s, true);

            // Calculate center y of this row
            float y = (2f * ((float)sheet_row + 0.5f)) / (float)(2 * this.anims.size()); // [0, 1]
            y *= 2; // [0, 1] -> [0, 2]
            y -= 1; // [0, 2] -> [-1, 1]

            // One col of new sheet -> one frame of animation
            for (int i = 0; i < anim.frames; i++) {
                this.switch_frames(i);

                // Calculate center x of this column
                float x = (2f * ((float)i + 0.5f)) / (float)(2 * max_frames); // [0, 1]
                x *= 2; // [0, 1] -> [0, 2]
                x -= 1; // [0, 2] -> [-1, 1]

                // Render this frame of this animation at this row and col of new atlas
                this.render(sp, x, y,
                        // Calculate the correct x-scaling to fit the whole text into one atlas spot
                        2f / (max_width * (float)max_frames),
                        // Calculate the correct y-scaling to fit the whole text into one atlas spot
                        -2f / (float)this.anims.size());
            }

            // Create new animation object and put in map
            int[] atlas_cols = new int[anim.frames];
            for (int i = 0; i < atlas_cols.length; i++) atlas_cols[i] = i;
            Animation new_anim = new Animation(anim.frame_time, anim.frames,
                    new int[] { sheet_row }, atlas_cols, new float[][] { null },
                    new BlendMode[] { BlendMode.JUST_TEXTURE });
            new_anims.put(s, new_anim);

            sheet_row++;
        }

        // Cleanup
        ShaderProgram.unbind_any_shader_program();
        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glDeleteFramebuffers(1, ids, 0);

        // Revert to old viewport, clear color, animation, and frame
        glViewport(0, 0, Global.VIEWPORT_WIDTH, Global.VIEWPORT_HEIGHT);
        GLES20.glClearColor(Global.CLEAR_COLOR[0], Global.CLEAR_COLOR[1], Global.CLEAR_COLOR[2],
                Global.CLEAR_COLOR[3]);
        this.current_anim = previous_animation;
        this.switch_frames(previous_frame);

        // Create the new texture and AnimatedSprite and return them
        TextureAtlas new_atlas = new TextureAtlas(ids[1], this.anims.size(), max_frames,
                new_texture_width, new_texture_height);
        return new AnimatedSprite(new_atlas, new_anims, this.starting_anim,
                new float[] {
                    -max_width / 2f,  0.5f, // top left
                    -max_width / 2f, -0.5f, // bottom left
                     max_width / 2f, -0.5f, // bottom right
                     max_width / 2f,  0.5f, // top right
                }, null);
    }
}
