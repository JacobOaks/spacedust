package svenske.spacedust.graphics;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import svenske.spacedust.utils.Node;

public class Font extends TextureAtlas {

    /**
     * Font data
     */
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
        this.starting_char = font_data.get_child("starting_char").get_value().charAt(0);
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

    /**
     * Calculate the texture coordinates for the given character of this Font
     * @param c the character whose texture coordinates to receive
     * @param cutoff whether to cutoff extra whitespace
     */
    public float[] get_tex_coords_for_char(char c, boolean cutoff) {

        // Check for out-of-bounds character
        if (c < this.starting_char)
            throw new RuntimeException("[spdt/font] " +
                    "the given chararacter '" + c + "' is before the starting character '" +
                    this.starting_char + "'");
        else if (c > this.starting_char + this.rows * this.cols)
            throw new RuntimeException("[spdt/font]" +
                    "the given character '" + c + "' is after the ending character '" +
                    (this.starting_char + (this.rows * this.cols)) + "'");

        // Calculate row and column
        int c_index = (c - this.starting_char);
        float rowf = (float)(c_index / this.cols);          // zero-indexed
        float colf = (float)(c_index - (rowf * this.cols)); //zero-indexed

        // Calculate texture coordinates
        float[] texture_coordinates = {
                colf / this.cols,       rowf / this.rows,         // top-left
                colf / this.cols,       (rowf + 1f) / this.rows, // bottom-left
                (colf + 1) / this.cols, (rowf + 1f) / this.rows, // bottom-right
                (colf + 1) / this.cols, rowf / this.rows,         // top-right
        };

        //account for cutoff
        if (cutoff) {
            float cutoffFactor = (float)get_cutoff(c) / (float)this.width;
            texture_coordinates[0] += cutoffFactor;
            texture_coordinates[2] += cutoffFactor;
            texture_coordinates[4] -= cutoffFactor;
            texture_coordinates[6] -= cutoffFactor;
        }

        //return final coordinates
        return texture_coordinates;
    }

    //Accessors
    public float getCharacterHeight() { return (float)this.height / (float)this.rows; }
    public float getCharacterWidth() { return (float)this.width / (float)this.cols; }
    public int get_cutoff(char c) {
        Integer cutoff = this.cutoffs.get(c);
        if (cutoff == null) cutoff = this.standard_cutoff;
        return cutoff;
    }
}