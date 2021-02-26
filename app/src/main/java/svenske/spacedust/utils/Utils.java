package svenske.spacedust.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utils {

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

}
