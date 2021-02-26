package svenske.spacedust.graphics;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Sprite {

    // Static Data
    public static FloatBuffer SQUARE_VERTEX_POSITIONS = null;
    public static ShortBuffer SQUARE_DRAW_ORDER       = null;
    public static final int   SQUARE_VERTEX_COUNT     = 6;

    // Default square vertex positions
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

    public static FloatBuffer get_float_buffer_from(float[] values) {

        // Num coordinates * 4 bytes per float
        ByteBuffer bb = ByteBuffer.allocateDirect(values.length * 4);
        bb.order(ByteOrder.nativeOrder()); // use the device hardware's native byte order
        FloatBuffer fb = bb.asFloatBuffer(); // create float buffer from byte buffer
        fb.put(values); // add coordinates to float buffer
        fb.position(0); // set buffer to read first position
        return fb;
    }

    // Default square draw order
    public static ShortBuffer get_square_draw_order_buffer() {
        if (SQUARE_DRAW_ORDER == null)
            SQUARE_DRAW_ORDER = get_short_buffer_from(new short[]{ 0, 1, 2, 0, 2, 3 });

        return SQUARE_DRAW_ORDER;
    }

    public static ShortBuffer get_short_buffer_from(short[] values) {

        // Num coordinates * 2 bytes per short
        ByteBuffer bb = ByteBuffer.allocateDirect(values.length * 2);
        bb.order(ByteOrder.nativeOrder()); // use the device hardware's native byte order
        ShortBuffer sb = bb.asShortBuffer(); // create short buffer from byte buffer
        sb.put(values); // add coordinates to short buffer
        sb.position(0); // set buffer to read first position
        return sb;
    }

    // Buffers/rendering data
    private FloatBuffer vertex_positions;
    // TODO: private FloatBuffer texture_coordinates;
    private ShortBuffer draw_order;
    private int vertex_count;

    // Sprite Attributes
    // TODO: TextureAtlas atlas
    float[] color;
    // TODO: BlendMode bm

    public Sprite(float[] color, float[] vertex_positions, short[] draw_order) {

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

        // TODO: Texture coordinates

        // Save draw order
        if (draw_order == null) { // default to square draw order
            this.vertex_count = SQUARE_VERTEX_COUNT;
            this.draw_order = get_square_draw_order_buffer();
        } else {
            this.vertex_count = draw_order.length;
            this.draw_order = get_short_buffer_from(draw_order);
        }
    }

    public void render(ShaderProgram shader_program, float x, float y) {

        // set vertex position data
        int position_attrib_loc = shader_program.get_attribute_location("vertex_position");
        GLES20.glEnableVertexAttribArray(position_attrib_loc);
        GLES20.glVertexAttribPointer(position_attrib_loc, 2, GLES20.GL_FLOAT, false,
                              2 * 4, this.vertex_positions);

        // TODO: set texture coordinate data

        // set color data
        shader_program.set_uniform("vertex_color", this.color);
        shader_program.set_uniform("obj_x", x);
        shader_program.set_uniform("obj_y", y);

        // draw
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, this.vertex_count, GLES20.GL_UNSIGNED_SHORT, this.draw_order);

        // disable vertex position handle
        GLES20.glDisableVertexAttribArray(position_attrib_loc);
    }

    public void update(float dt) {

    }
}
