package svenske.spacedust.graphics;

import android.opengl.GLES20;

import java.io.InputStream;

import svenske.spacedust.GameActivity;
import svenske.spacedust.utils.Utils;

// A general class encapsulating an OpenGL shader program.
public class ShaderProgram {

    // OpenGL shader program ID
    int program;

    /**
     * Loads the shader program by loading and compiling source code in the given vertex shader
     * and fragment shader source files resource IDs.
     */
    public ShaderProgram(int vertex_shader_resource_id, int fragment_shader_resource_id) {

        // Vertex shader
        InputStream is = GameActivity.app_resources.openRawResource(vertex_shader_resource_id);
        String vertex_shader_code = Utils.input_stream_to_string(is);
        int vertex_shader = compile_shader(GLES20.GL_VERTEX_SHADER, vertex_shader_code);

        // Fragment shader
        is = GameActivity.app_resources.openRawResource(fragment_shader_resource_id);
        String fragment_shader_code = Utils.input_stream_to_string(is);
        int fragment_shader = compile_shader(GLES20.GL_FRAGMENT_SHADER, fragment_shader_code);

        // Create empty OpenGL ES shader program
        this.program = GLES20.glCreateProgram();
        if (this.program == 0)
            throw new RuntimeException("[spdt/shaderprogram]: " +
                    " shader program couldn't be created");

        // Attach vertex and fragment shaders
        GLES20.glAttachShader(this.program, vertex_shader);
        GLES20.glAttachShader(this.program, fragment_shader);

        // Link program together
        GLES20.glLinkProgram(this.program);
    }

    public void bind() {
        GLES20.glUseProgram(this.program);
    }

    // Return the location of the attribute with the given name in the shader program.
    public int get_attribute_location(String name) {
        int loc = GLES20.glGetAttribLocation(this.program, name);
        if (loc == -1)
            throw new RuntimeException("[spdt/shaderprogram]: " +
                    " no attribute named " + name);
        return loc;
    }

    // Return the location of the uniform with the given name in the shader program.
    private int get_uniform_location(String name) {
        int loc = GLES20.glGetUniformLocation(this.program, name);
        if (loc == -1)
            throw new RuntimeException("[spdt/shaderprogram]: " +
                    " no uniform named " + name);
        return loc;
    }

    // Return whether the uniform with the given name exists in this shader program.
    public boolean uniform_exists(String name) {
        return (GLES20.glGetUniformLocation(this.program, name) != -1);
    }

    // Sets the uniform with the given name to the given array of floats.
    public void set_uniform(String name, float[] value) {
        int loc = this.get_uniform_location(name);
        if (value.length == 4)
            GLES20.glUniform4fv(loc, 1, value, 0);
        else if (value.length == 3)
            GLES20.glUniform3fv(loc, 1, value, 0);
        else
            throw new RuntimeException("[spdt/shaderprogram]: " +
                    "invalid length array given in set_uniform: " + value.length);
    }

    // Sets the uniform with the given name to the given float.
    public void set_uniform(String name, float value) {
        GLES20.glUniform1f(this.get_uniform_location(name), value);
    }

    // Sets the uniform with the given name to the given int.
    public void set_uniform(String name, int value) {
        GLES20.glUniform1i(this.get_uniform_location(name), value);
    }

    // Sets the uniform with the given name to the given LightSource.
    public void set_light_uniform(String array_name, int i, LightSource ls, float x, float y) {
        this.set_uniform(array_name + "[" + i + "].reach", ls.get_reach());
        this.set_uniform(array_name + "[" + i + "].intensity", ls.get_intensity());
        this.set_uniform(array_name + "[" + i + "].glow", ls.get_glow());
        this.set_uniform(array_name + "[" + i + "].x", x);
        this.set_uniform(array_name + "[" + i + "].y", y);
    }

    // Only one shader program can be bound at a time. This method unbinds any/all.
    public static void unbind_any_shader_program() {
        GLES20.glUseProgram(0);
    }

    /**
     * Creates and compiles an individual shader program of the given type
     * @param type either GLES20.GL_VERTEX_SHADER or GLES20.GL_FRAGMENT_SHADER
     * @return the compiled shader's ID.
     */
    private static int compile_shader(int type, String code) {

        // Create shader
        int shader = GLES20.glCreateShader(type);

        // Add source code, compile, return
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
