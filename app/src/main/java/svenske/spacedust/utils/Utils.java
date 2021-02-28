package svenske.spacedust.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import svenske.spacedust.GameActivity;

/**
 * A class with generic static utility methods.
 */
public class Utils {

    /**
     * Converts an InputStream (say, from a raw resource) into a single String.
     */
    public static String input_stream_to_string(InputStream is) {

        // Create empty ByteArrayOutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;

        // Read from input stream, write to BAOS
        try {
            while ((length = is.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }

            // Convert BAOS to string and return
            return baos.toString("UTF-8");
        } catch (IOException ioe) {
            throw new RuntimeException("[spdt/utils]: " +
                    " can't convert InputStream to String:" + ioe.getMessage());
        }
    }

    /**
     * Splits a string into a list of strings by splitting at the given char
     */
    public static List<String> split_string(String value, char split) {
        List<String> result = new ArrayList<>();
        int beginning = 0;
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == split) {
                result.add(value.substring(beginning, i));
                beginning = i + 1;
            }
        }
        result.add(value.substring(beginning));
        return result;
    }

    /**
     * Reads a resource with the given ID, returning it as a list of Strings
     */
    public static List<String> read_resource(int resource_id) {
        InputStream is = GameActivity.app_resources.openRawResource(resource_id);
        String data = Utils.input_stream_to_string(is);
        return split_string(data, '\n');
    }

    /**
     * Turns a float array into a FloatBuffer.
     */
    public static FloatBuffer get_float_buffer_from(float[] values) {

        // Num coordinates * 4 bytes per float
        ByteBuffer bb = ByteBuffer.allocateDirect(values.length * 4);
        bb.order(ByteOrder.nativeOrder()); // use the device hardware's native byte order
        FloatBuffer fb = bb.asFloatBuffer(); // create float buffer from byte buffer
        fb.put(values); // add coordinates to float buffer
        fb.position(0); // set buffer to read first position
        return fb;
    }

    /**
     * Turns a short (as in the data type) array into a ShortBuffer.
     */
    public static ShortBuffer get_short_buffer_from(short[] values) {

        // Num coordinates * 2 bytes per short
        ByteBuffer bb = ByteBuffer.allocateDirect(values.length * 2);
        bb.order(ByteOrder.nativeOrder()); // use the device hardware's native byte order
        ShortBuffer sb = bb.asShortBuffer(); // create short buffer from byte buffer
        sb.put(values); // add coordinates to short buffer
        sb.position(0); // set buffer to read first position
        return sb;
    }
}
