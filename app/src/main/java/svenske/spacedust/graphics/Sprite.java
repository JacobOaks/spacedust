package svenske.spacedust.graphics;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static svenske.spacedust.utils.Utils.get_float_buffer_from;
import static svenske.spacedust.utils.Utils.get_short_buffer_from;

/**
 * The Sprite class represents a single displayable graphical unit. It can have texture, color, or
 * both. It can have any shape, though it defaults to square. It can be drawn wherever and with
 * any shader program.
 */
public class Sprite {

    // Static Data

    /**
     * Since the Sprite defaults to being square in shape, square data is stored here statically
     * to be reused
     */
    public static FloatBuffer SQUARE_VERTEX_POSITIONS = null;
    public static ShortBuffer SQUARE_DRAW_ORDER       = null;
    public static final int   SQUARE_VERTEX_COUNT     = 6;

    /**
     * Here are some methods to get the default square vertex position buffer and the default square
     * draw order buffer. This will create those buffers if it hasn't been done yet
     */
    public static FloatBuffer get_square_vertex_positions_buffer() {
        if (SQUARE_VERTEX_POSITIONS == null) {
            float[] positions = {
                    -0.5f,  0.5f, // top left
                    -0.5f, -0.5f, // bottom left
                     0.5f, -0.5f, // bottom right
                     0.5f,  0.5f, // top right
            };
            SQUARE_VERTEX_POSITIONS = get_float_buffer_from(positions);
        }

        return SQUARE_VERTEX_POSITIONS;
    }
    public static ShortBuffer get_square_draw_order_buffer() {
        if (SQUARE_DRAW_ORDER == null)
            SQUARE_DRAW_ORDER = get_short_buffer_from(new short[]{ 0, 1, 2, 0, 2, 3 });

        return SQUARE_DRAW_ORDER;
    }

    /**
     * Buffers/rendering data for the sprite
     */
    protected FloatBuffer vertex_positions;    // actual vertex positions in model space
    protected FloatBuffer texture_coordinates; // texture coordinates
    protected ShortBuffer draw_order;          // draw order in triangles
    protected int vertex_count;                // total amount of drawn vertices

    // Sprite Attributes
    TextureAtlas atlas;
    float[] color;
    BlendMode blend_mode;

    /**
     * Constructs a new Sprite
     * @param atlas if the Sprite is to be textured, pass the corresponding TextureAtlas through
     *              here. If not, pass null
     * @param atlas_row the row of the atlas corresponding to this Sprite's texture
     * @param atlas_col the column of the atlas corresponding to this Sprite's texture
     * @param color if the Sprite is to be colored, pass the color in as a length-4 float array
     *              here. The floats should be in [0f, 1f]. If not, pass null
     * @param blend_mode a blend mode to describe how texture and color should interact when
     *                   rendering the Sprite
     * @param vertex_positions for default square model, pass null. Otherwise pass custom vertex
     *                         positions here
     * @param draw_order for default draw square model, pass null. Otherwise pass custom draw order
     */
    public Sprite(TextureAtlas atlas, int atlas_row, int atlas_col, float[] color,
                  BlendMode blend_mode, float[] vertex_positions, short[] draw_order) {

        // Save color
        this.color = color;
        if (this.color != null && this.color.length != 4)
            throw new RuntimeException("[spdt/sprite]: " +
                    " invalid color array length " + this.color.length);

        // Save vertex positions
        if (vertex_positions == null) // default to square vertex positions
            this.vertex_positions = get_square_vertex_positions_buffer();
        else
            this.vertex_positions = get_float_buffer_from(vertex_positions);

        // Save texture atlas and get coordinates
        this.atlas = atlas;
        if (this.atlas != null)
            this.texture_coordinates = TextureAtlas.get_tex_coords_buffer(this.atlas,
                    atlas_row, atlas_col);

        // Save blend mode and verify it
        this.blend_mode = blend_mode;
        check_blend_mode(this.atlas, this.color, this.blend_mode);

        // Save draw order and count vertices
        if (draw_order == null) { // default to square draw order
            this.vertex_count = SQUARE_VERTEX_COUNT;
            this.draw_order = get_square_draw_order_buffer();
        } else {
            this.vertex_count = draw_order.length;
            this.draw_order = get_short_buffer_from(draw_order);
        }
    }

    /**
     * Verifies that an atlas and color are valid given a blend mode
     */
    protected static void check_blend_mode(TextureAtlas atlas, float[] color, BlendMode blend_mode) {
        if ((blend_mode != BlendMode.JUST_COLOR) && atlas == null)
            throw new RuntimeException("[spdt/sprite]" +
                    "BlendMode " + blend_mode + " given but no texture given");
        if ((blend_mode != BlendMode.JUST_TEXTURE) && color == null)
            throw new RuntimeException("[spdt/sprite]" +
                    "BlendMode " + blend_mode + " given but no color given");
    }

    /**
     * Updates the sprite. Doesn't actually do anything but the purposes it to be extended by
     * more complicated classes that may need to update every loop
     */
    public void update(float dt) {}

    /**
     * Renders the sprite
     * The given position means different things depending on the shader program used. This method
     * assumes the given shader program always has "obj_x" and "obj_y" uniforms
     */
    public void render(ShaderProgram shader_program, float x, float y) {

        // Set vertex position attribute data
        int position_attrib_loc = shader_program.get_attribute_location("vertex_position");
        GLES20.glEnableVertexAttribArray(position_attrib_loc);
        GLES20.glVertexAttribPointer(position_attrib_loc, 2, GLES20.GL_FLOAT, false,
                2 * 4, this.vertex_positions);

        // Set texture info if included in blend
        int tex_coords_attrib_loc = -1;
        if (this.blend_mode != BlendMode.JUST_COLOR) {

            // Set texture bank info
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.atlas.getID());
            shader_program.set_uniform("texture_sampler", 0);

            // Set texture coordinate info
            tex_coords_attrib_loc = shader_program.get_attribute_location("tex_coords");
            GLES20.glEnableVertexAttribArray(tex_coords_attrib_loc);
            GLES20.glVertexAttribPointer(tex_coords_attrib_loc, 2, GLES20.GL_FLOAT, false,
                    2 * 4, this.texture_coordinates);
        }

        // Set color and blend data
        if (this.blend_mode != BlendMode.JUST_TEXTURE)
            shader_program.set_uniform("vertex_color", this.color);
        shader_program.set_uniform("blend_mode", this.blend_mode.ordinal());

        // Pass in given position
        shader_program.set_uniform("obj_x", x);
        shader_program.set_uniform("obj_y", y);

        // Draw
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, this.vertex_count, GLES20.GL_UNSIGNED_SHORT, this.draw_order);

        // Disable vertex position handle
        GLES20.glDisableVertexAttribArray(position_attrib_loc);
        if (tex_coords_attrib_loc != -1)
            GLES20.glDisableVertexAttribArray(tex_coords_attrib_loc);
    }
}
