package svenske.spacedust.graphics;

import svenske.spacedust.utils.Utils;

/**
 * A sprite that can show text. The text can be changed on-demand. This class is more expensive to
 * render than a normal sprite, so if the text won't be changed, convert it into a normal Sprite
 * through the solidify() function.
 */
public class TextSprite extends Sprite {

    private String text; // The current text

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

        if (this.text.length() > 0) {
            // Translate left to center-align the text
            float trans_left = vertex_positions[vertex_positions.length - 2] / 2;
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
        return null;
        // TODO
    }
}
