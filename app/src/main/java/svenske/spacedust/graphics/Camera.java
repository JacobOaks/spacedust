package svenske.spacedust.graphics;

// Represents a camera viewing a World
public class Camera {

    // Camera position and zoom
    private float x, y;
    private float zoom;

    // Constructs the camera with the given starting positions and zoom
    public Camera(float x, float y, float zoom) {
        this.x = x;
        this.y = y;
        this.zoom = zoom;
    }

    // Sets camera uniforms in a ShaderProgram based on this camera's configuration
    public void set_uniforms(ShaderProgram shader_program) {
        shader_program.set_uniform("cam_x", this.x);
        shader_program.set_uniform("cam_y", this.y);
        shader_program.set_uniform("cam_zoom", this.zoom);
    }

    // Accessors
    public float getX() { return this.x; }
    public float getY() { return this.y; }
    public float getZoom() { return this.zoom; }

    // Mutators
    public void set_zoom(float zoom) { this.zoom = zoom; }
}
