package svenske.spacedust.graphics;

public class Camera {

    private float x, y;
    private float zoom;

    public Camera(float x, float y, float zoom) {
        this.x = x;
        this.y = y;
        this.zoom = zoom;
    }

    public void set_uniforms(ShaderProgram shader_program) {
        shader_program.set_uniform("cam_x", this.x);
        shader_program.set_uniform("cam_y", this.y);
        shader_program.set_uniform("cam_zoom", this.zoom);
    }
}
