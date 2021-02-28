package svenske.spacedust.graphics;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import svenske.spacedust.GameActivity;

import static svenske.spacedust.utils.Utils.get_float_buffer_from;

/**
 * Represents a single texture atlas (or "sprite sheet") that may or may not have many textures
 * in one image. These should be shared amongst Sprites. They provide utility functions to get
 * correct texture coordinate buffers based off of atlas size and desired cell. These are also
 * shared, so try to use similarly-sized sheets/repeat texture coordinates. This class also
 * provides utility functions for performing operations on multiple atlases (i.e., splitting,
 * stacking, slicing, etc.)
 */
public class TextureAtlas {

    //  Maps from atlas rows -> atlas columns -> 2D array of possible texture coordinate buffers.
    private static Map<Integer, Map<Integer, FloatBuffer[][]>> tex_coords_buffers;

    /**
     * Inspects the tex_coords_buffer data structure above, reporting how many unique atlas shapes
     * are registered and how many total texture coordinate buffers are created.
     */
    public static void inspect_tex_coords_buffers() {

        // Ensure map has been initialized
        if (tex_coords_buffers == null) tex_coords_buffers = new HashMap<>();

        // Count buffers and configurations
        int float_buffers = 0;
        int configurations = 0;
        for (Integer key : tex_coords_buffers.keySet()) {
            for (Integer key_2 : tex_coords_buffers.get(key).keySet()) {
                configurations++;
                FloatBuffer[][] fb_array = tex_coords_buffers.get(key).get(key_2);
                for (int i = 0; i < fb_array.length; i++) {
                    for (int j = 0; j < fb_array[i].length; j++) {
                        if (fb_array[i][j] != null) float_buffers++;
                    }
                }
            }
        }

        // Log results
        Log.d("spdt/textureatlas", "INSPECTION: tex_coords_buffer has "
                + configurations + " unique atlas sizes");
        Log.d("spdt/textureatlas", "INSPECTION: " + float_buffers
                + " total texture coordinate buffers");
    }

    /**
     * Returns a texture coordinate buffer to use for the given row and column of the given atlas.
     * These are lazily created. Try to re-use similar texture coordinates and atlas sizes.
     */
    public static FloatBuffer get_tex_coords_buffer(TextureAtlas ta, int row, int col) {

        // Ensure map has been initialized
        if (tex_coords_buffers == null) tex_coords_buffers = new HashMap<>();

        // Get sub-map for all buffers whose texture atlas has the correct amount of rows
        if (!tex_coords_buffers.containsKey(ta.rows))
            tex_coords_buffers.put(ta.rows, new HashMap<>());
        Map<Integer, FloatBuffer[][]> for_total_rows = tex_coords_buffers.get(ta.rows);

        // Get 2D array for all buffers whose texture atlas has the correct amount of rows and cols
        if (!for_total_rows.containsKey(ta.cols))
            for_total_rows.put(ta.cols, new FloatBuffer[ta.rows][ta.cols]);
        FloatBuffer[][] buffer_array = for_total_rows.get(ta.cols);

        // Calculate texture coordinates if this is first time this buffer is needed
        try {
            if (buffer_array[row][col] == null) {
                float colf = (float) col;
                float rowf = (float) row;
                float[] texture_coordinates = {
                        colf / ta.cols,       rowf / ta.rows,        // top-left
                        colf / ta.cols,       (rowf + 1f) / ta.rows, // bottom-left
                        (colf + 1) / ta.cols, (rowf + 1f) / ta.rows, // bottom-right
                        (colf + 1) / ta.cols, rowf / ta.rows,        // top-right
                };

                buffer_array[row][col] = get_float_buffer_from(texture_coordinates);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("[spdt/textureatlas] out of bounds: " + e.getMessage());
        }

        return buffer_array[row][col];
    }

    // TextureAtlas attributes
    protected int rows, cols;
    protected int width, height;
    protected int[] id;

    /**
     * Creates a new texture atlas with the image at the given resource ID. If this is a sheet
     * with just one texture, rows = cols = 1.
     */
    public TextureAtlas(int resource_id, int rows, int cols) {

        //load texture into bitmap
        InputStream is = GameActivity.app_resources.openRawResource(resource_id);
        Bitmap bmp;
        try {
            bmp = BitmapFactory.decodeStream(is);
        } finally {
            try {
                is.close();
            } catch(IOException e) {
                throw new RuntimeException("[spdt/textureatlas]" +
                        " unable to load texture: " + e.getMessage());
            }
        }

        // Set width and height
        this.rows = rows;
        this.cols = cols;
        this.width = bmp.getWidth();
        this.height = bmp.getHeight();
        this.id = new int[1];

        // Generate and bind GL texture object
        GLES20.glGenTextures(1, id, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id[0]);

        // Set minification and magnification filter parameters
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);

        // Set wrapping parameters
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        // Put loaded  bitmap to texture
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);

        // Recycle bitmap
        bmp.recycle();
    }

    /**
     * An alternative constructor taking an already created OpenGL texture and other texture
     * information
     */
    public TextureAtlas(int texture_id, int rows, int cols, int width, int height) {
        this.id = new int[] { texture_id };
        this.rows = rows;
        this.cols = cols;
        this.width = width;
        this.height = height;
    }

    // Return the atlas' OpenGL ES texture id
    public int getID() { return this.id[0]; }

    // TODO: Utility methods for merging, stacking, cropping, etc. TextureAtlases into new ones
}
