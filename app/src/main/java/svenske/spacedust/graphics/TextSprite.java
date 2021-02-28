package svenske.spacedust.graphics;

import android.opengl.GLES20;

import java.nio.ByteBuffer;

import svenske.spacedust.R;
import svenske.spacedust.utils.Global;
import svenske.spacedust.utils.Utils;

import static android.opengl.GLES20.GL_COLOR_ATTACHMENT0;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_RGBA;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.glDeleteFramebuffers;
import static android.opengl.GLES20.glViewport;

/**
 * A sprite that can show text. The text can be changed on-demand. This class is more expensive to
 * render than a normal sprite, so if the text won't be changed, convert it into a normal Sprite
 * through the solidify() function.
 */
public class TextSprite extends Sprite {

    private String text; // The current text
    private float width;

    /**
     * Constructs the TextSprite using the given font, text color, blend mode, and starting text.
     */
    public TextSprite(Font font, float[] color, BlendMode blend_mode, String text) {

        // Pass dummy values to super constructor for now
        super(null, 0, 0, color == null ? new float[4] : color,
                BlendMode.JUST_COLOR, null, null);

        if (blend_mode == BlendMode.JUST_COLOR)
            throw new RuntimeException("[spdt/textsprite] " +
                    "Creating a TextSprite but blend mode doesn't use texture");

        // Save actual data and update buffers for first time
        this.atlas = font;
        this.text = text;
        this.blend_mode = blend_mode;
        this.update_buffers();
    }

    /**
     * Update the TextSprite's text. This is not a trivial operation. If you do not plan to do this
     * ever, solidify the TextSprite.
     */
    public void set_text(String text) {
        if (text.equals(this.text)) return;
        this.text = text;
        this.update_buffers();
    }

    /**
     * Updates the Sprites vertex positions, texture coordinates, and draw order based off of the
     * currently set text
     */
    private void update_buffers() {

        // Create arrays
        float[] vertex_positions = new float[8 * this.text.length()];
        float[] tex_coords       = new float[8 * this.text.length()];
        short[] draw_order       = new short[6 * this.text.length()];

        // Fill out vertex positions, texture coordinates, and draw order for each character
        float x = 0f;
        for (int i = 0; i < this.text.length(); i++) {
            char current_char = this.text.charAt(i);

            // Vertex positions
            float[] next_vertex_positions = ((Font)this.atlas).get_vertex_positions_for_char(current_char, true);
            x += next_vertex_positions[6];

            // Set x positions
            for (int j = 0; j < 8; j += 2)
                vertex_positions[i * 8 + j] = next_vertex_positions[j] + x;
            // Set y positions
            for (int j = 1; j < 8; j += 2)
                vertex_positions[i * 8 + j] = next_vertex_positions[j];

            x += next_vertex_positions[6]; // increment total x

            // Texture coordinates
            float[] next_tex_coords = ((Font)this.atlas).get_tex_coords_for_char(current_char, true);
            for (int j = 0; j < next_tex_coords.length; j++)
                tex_coords[i * 8 + j] = next_tex_coords[j];

            // Draw order
            draw_order[i * 6 + 0] = (short)(i * 4 + 0);
            draw_order[i * 6 + 1] = (short)(i * 4 + 1);
            draw_order[i * 6 + 2] = (short)(i * 4 + 2);
            draw_order[i * 6 + 3] = (short)(i * 4 + 0);
            draw_order[i * 6 + 4] = (short)(i * 4 + 2);
            draw_order[i * 6 + 5] = (short)(i * 4 + 3);
        }

        this.width = vertex_positions[vertex_positions.length - 2];
        if (this.text.length() > 0) {
            // Translate left to center-align the text
            float trans_left = this.width / 2;
            for (int i = 0; i < vertex_positions.length; i += 2)
                vertex_positions[i] -= trans_left;
        }

        // Create buffers
        this.vertex_positions = Utils.get_float_buffer_from(vertex_positions);
        this.texture_coordinates = Utils.get_float_buffer_from(tex_coords);
        this.draw_order = Utils.get_short_buffer_from(draw_order);
        this.vertex_count = draw_order.length;
    }

    /**
     * Turns the TextSprite into a normal sprite. Obviously this means the text can no longer be
     * changed, but increases rendering efficiency greatly.
     */
    public Sprite solidify() {

        // Calculate the width/height of the new texture
        int new_texture_width = ((Font)this.atlas).get_pixel_width_for_text(this.text, true);
        int new_texture_height = ((Font)this.atlas).get_pixel_height_for_text();

        // Generate and bind the FBO
        int fbo[] = new int[1];
        GLES20.glGenFramebuffers(1, fbo, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0]);

        // Generate the final texture and attach it to the FBO
        int texture_id[] = new int[1];
        GLES20.glGenTextures(1, texture_id, 0);
        GLES20.glBindTexture(GL_TEXTURE_2D, texture_id[0]); // bind
        GLES20.glTexImage2D(GL_TEXTURE_2D, 0, GLES20.GL_RGBA, new_texture_width,
                new_texture_height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        GLES20.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture_id[0], 0);
        GLES20.glBindTexture(GL_TEXTURE_2D, 0); // unbind

        // Create solidification shader program
        ShaderProgram sp = new ShaderProgram(R.raw.vertex_solidify, R.raw.fragment_solidify);

        // Set viewport to final texture size and set clear color to full transparency
        GLES20.glViewport(0, 0, new_texture_width, new_texture_height);
        GLES20.glClearColor(0f, 0f, 0f, 0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Bind shader program and render
        sp.bind();
        sp.set_uniform("aspect_ratio", (float)new_texture_width / (float)new_texture_height);
        this.render(sp, 0f, 0f, 2f, -2f);

        // Cleanup
        ShaderProgram.unbind_any_shader_program();
        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glDeleteFramebuffers(1, fbo, 0);

        // Revert to old viewport and clear color
        glViewport(0, 0, Global.VIEWPORT_WIDTH, Global.VIEWPORT_HEIGHT);
        GLES20.glClearColor(Global.CLEAR_COLOR[0], Global.CLEAR_COLOR[1], Global.CLEAR_COLOR[2],
                Global.CLEAR_COLOR[3]);

        // Create the new texture and Sprite and return them
        TextureAtlas new_texture = new TextureAtlas(texture_id[0], 1, 1, new_texture_width, new_texture_height);
        return new Sprite(new_texture, 0, 0, null, BlendMode.JUST_TEXTURE,
                new float[] {
                    -this.width / 2,  0.5f, // top left
                    -this.width / 2, -0.5f, // bottom left
                     this.width / 2, -0.5f, // bottom right
                     this.width / 2,  0.5f, // top right
                }, null);
    }

    // Returns the TextSprite's current text
    public String getText() { return this.text; }
}
