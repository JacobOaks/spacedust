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

public class TextureAtlas {

    private static Map<Integer, Map<Integer, FloatBuffer[][]>> tex_coords_buffers;

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
        if (buffer_array[row][col] == null) {
            float colf = (float)col;
            float rowf = (float)row;
            float[] texture_coordinates = {
                    colf / ta.cols, (rowf + 1f) / ta.rows,      // top-left
                    colf / ta.cols, rowf / ta.rows,             // bottom-left
                    (colf + 1) / ta.cols, rowf / ta.rows,       // bottom-right
                    (colf + 1) / ta.cols, (rowf + 1) / ta.rows, // top-right
            };

            buffer_array[row][col] = get_float_buffer_from(texture_coordinates);
        }

        return buffer_array[row][col];
    }

    // TextureAtlas attributes
    private int rows, cols;
    private int width, height;
    private int[] id;

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

        //set width and height
        this.rows = rows;
        this.cols = cols;
        this.width = bmp.getWidth();
        this.height = bmp.getHeight();//generate gl texture and bind it
        this.id = new int[1];

        GLES20.glGenTextures(1, id, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id[0]);

        //set minification and magnification filter parameters
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);

        //set wrapping
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        //bind bitmap to texture
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);

        //recycle bitmap
        bmp.recycle();
    }

    public int getID() { return this.id[0]; }

    // TODO: Utility methods for merging, stacking, cropping, etc. TextureAtlases into new ones
}
