package svenske.spacedust.graphics;

import android.opengl.GLES20;

import java.io.InputStream;

import svenske.spacedust.GameActivity;
import svenske.spacedust.utils.Utils;

public class ShaderProgram {

    int program;

    // TODO: Load from resources
    public ShaderProgram(int vertex_shader_resource_id, int fragment_shader_resource_id) {

        // Load vertex shader
        InputStream is = GameActivity.app_resources.openRawResource(vertex_shader_resource_id);
        String vertex_shader_code = Utils.input_stream_to_string(is);
        int vertex_shader = load_shader(GLES20.GL_VERTEX_SHADER, vertex_shader_code);

        // Load fragment shader
        is = GameActivity.app_resources.openRawResource(fragment_shader_resource_id);
        String fragment_shader_code = Utils.input_stream_to_string(is);
        int fragment_shader = load_shader(GLES20.GL_FRAGMENT_SHADER, fragment_shader_code);

        // create empty OpenGL ES shader program
        this.program = GLES20.glCreateProgram();
        if (this.program == 0)
            throw new RuntimeException("[spdt/shaderprogram]: " +
                    " shader program couldn't be created");

        // attach vertex and fragment shaders
        GLES20.glAttachShader(this.program, vertex_shader);
        GLES20.glAttachShader(this.program, fragment_shader);

        // link program together
        GLES20.glLinkProgram(this.program);
    }

    public void bind() {
        GLES20.glUseProgram(this.program);
    }

    public int get_attribute_location(String name) {
        int loc = GLES20.glGetAttribLocation(this.program, name);
        if (loc == -1)
            throw new RuntimeException("[spdt/shaderprogram]: " +
                    " no attribute named " + name);
        return loc;
    }

    private int get_uniform_location(String name) {
        int loc = GLES20.glGetUniformLocation(this.program, name);
        if (loc == -1)
            throw new RuntimeException("[spdt/shaderprogram]: " +
                    " no uniform named " + name);
        return loc;
    }

    public void set_uniform(String name, float[] value) {
        int loc = this.get_uniform_location(name);
        if (value.length == 4)
            GLES20.glUniform4fv(loc, 1, value, 0);
        else
            throw new RuntimeException("[spdt/shaderprogram]: " +
                    "invalid length array given in set_uniform: " + value.length);
    }

    public void set_uniform(String name, float value) {
        GLES20.glUniform1f(this.get_uniform_location(name), value);
    }

    public void set_uniform(String name, int value) {
        GLES20.glUniform1i(this.get_uniform_location(name), value);
    }

    public static void unbind_any_shader_program() {
        GLES20.glUseProgram(0);
    }

    // type is either GLES20.GL_VERTEX_SHADER or GLES20.GL_FRAGMENT_SHADER
    private static int load_shader(int type, String code) {

        // create shader
        int shader = GLES20.glCreateShader(type);

        // add source code, compile, return
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);
        int[] status = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] == GLES20.GL_FALSE)
            throw new RuntimeException("[spdt/shaderprogram]: " +
                    "can't compile shader: " + GLES20.glGetShaderInfoLog(shader));
        return shader;
    }
}
