package svenske.spacedust.graphics;

import java.util.HashMap;
import java.util.Map;

import svenske.spacedust.utils.Node;

// An extension of TextureAtlas with some useful functions tailored towards displaying text
public class Font extends TextureAtlas {

    // Attributes
    private Map<Character, Integer> cutoffs;
    private int standard_cutoff = 0;
    private char starting_char  = 0;

    /**
     * Creates the font
     * @param atlas_resource_id the resource ID of the atlas (the actual font Image)
     * @param info_resource_id the resource ID of the font info node file
     */
    public Font(int atlas_resource_id, int info_resource_id) {
        super(atlas_resource_id, 0, 0);
        Node font_data = Node.read_node(info_resource_id);
        this.starting_char = (char)Integer.parseInt(font_data.get_child("starting_char").get_value());
        this.rows = Integer.parseInt(font_data.get_child("rows").get_value());
        this.cols = Integer.parseInt(font_data.get_child("cols").get_value());

        // Load all cutoffs
        this.cutoffs = new HashMap<>();
        for (Node child : font_data.get_child("cutoffs").get_children()) {
            if (child.get_name().toUpperCase().equals("STANDARD"))
                this.standard_cutoff = Integer.parseInt(child.get_value());
            else if (child.get_name().toUpperCase().equals("COLON"))
                this.cutoffs.put(':', Integer.parseInt(child.get_value()));
            else if (child.get_name().toUpperCase().equals("NUMBER"))
                this.cutoffs.put('#', Integer.parseInt(child.get_value()));
            else this.cutoffs.put(child.get_name().charAt(0), Integer.parseInt(child.get_value()));
        }
    }

    // Makes sure the given character can be represented by this font
    private void check_char_validity(char c) {
        // Check for out-of-bounds character
        if (c < this.starting_char)
            throw new RuntimeException("[spdt/font] " +
                    "the given chararacter '" + c + "' is before the starting character '" +
                    this.starting_char + "'");
        else if (c > this.starting_char + this.rows * this.cols)
            throw new RuntimeException("[spdt/font]" +
                    "the given character '" + c + "' is after the ending character '" +
                    (this.starting_char + (this.rows * this.cols)) + "'");
    }

    /**
     * Calculates how many pixels wide a given string would be in this Font.
     * @param cutoff whether to calculate as if cutoff were applied
     */
    public int get_pixel_width_for_text(String s, boolean cutoff) {
        int total_width = 0;
        int char_width = (int)((float)this.width / (float)this.cols);
        for (int i = 0; i < s.length(); i++) {
            total_width += char_width;
            if (cutoff) total_width -= (2 * this.get_cutoff(s.charAt(i)));
        }
        return total_width;
    }

    public int get_pixel_height_for_text() { return (int)((float)this.height / (float)this.rows); }

    // Height always 1.0f, width varies to make height 1.0f
    public float[] get_vertex_positions_for_char(char c, boolean cutoff) {
        this.check_char_validity(c);

        // Calculate character width and height in pixels
        float char_width = (float)this.width / (float)this.cols;
        float char_height = (float)this.height / (float)this.rows;

        // Calculate half-width and half-height necessary to make total height 1f
        if (cutoff) char_width -= (2 * this.get_cutoff(c));
        float cw = (char_width / char_height) / 2f;
        float ch = 1f / 2f;

        return new float[] {
                -cw,  ch, // top left
                -cw, -ch, // bottom left
                 cw, -ch, // bottom right
                 cw,  ch, // top right
        };
    }

    /**
     * Calculate the texture coordinates for the given character of this Font
     * @param c the character whose texture coordinates to receive
     * @param cutoff whether to cutoff extra whitespace
     */
    public float[] get_tex_coords_for_char(char c, boolean cutoff) {
        this.check_char_validity(c);

        // Calculate row and column
        int c_index = (c - this.starting_char);
        float rowf = (float)(c_index / this.cols);          // zero-indexed
        float colf = (float)(c_index - (rowf * this.cols)); //zero-indexed

        // Calculate texture coordinates
        float[] texture_coordinates = {
                colf / this.cols,       rowf / this.rows,        // top-left
                colf / this.cols,       (rowf + 1f) / this.rows, // bottom-left
                (colf + 1) / this.cols, (rowf + 1f) / this.rows, // bottom-right
                (colf + 1) / this.cols, rowf / this.rows,        // top-right
        };

        // Account for cutoff
        if (cutoff) {
            float cutoffFactor = (float)get_cutoff(c) / (float)this.width;
            texture_coordinates[0] += cutoffFactor;
            texture_coordinates[2] += cutoffFactor;
            texture_coordinates[4] -= cutoffFactor;
            texture_coordinates[6] -= cutoffFactor;
        }

        // Return final coordinates
        return texture_coordinates;
    }

    // Returns the correct horizontal cutoff for the given character
    public int get_cutoff(char c) {
        Integer cutoff = this.cutoffs.get(c);
        if (cutoff == null) cutoff = this.standard_cutoff;
        return cutoff;
    }
}