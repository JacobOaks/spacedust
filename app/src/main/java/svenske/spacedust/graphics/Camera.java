package svenske.spacedust.graphics;

import svenske.spacedust.utils.Global;

// Represents a camera viewing a World
public class Camera {

    // Camera position and zoom
    private float x, y;
    private float zoom;

    // Camera bounds info
    private float min_x, max_x, min_y, max_y;
    private float min_c_x, max_c_x, min_c_y, max_c_y;
    private boolean bounded = false;

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
    public void set_zoom(float zoom) {
        this.zoom = zoom;
        this.update_bounds();
    }
    public void set_position(float x, float y) {
        this.x = this.bounded ? Math.min(Math.max(x, this.min_c_x), this.max_c_x) : x;
        this.y = this.bounded ? Math.min(Math.max(y, this.min_c_y), this.max_c_y) : y;
    }

    public void set_bounds(float min_x, float max_x, float min_y, float max_y) {
        this.min_x = min_x;
        this.max_x = max_x;
        this.min_y = min_y;
        this.max_y = max_y;
        this.bounded = true;
        this.update_bounds();
    }

    public void update_bounds() {
        if (this.bounded) {
            float view_width = 2f / this.zoom;
            float view_height = 2f / this.zoom;
            float ar = (float)Global.VIEWPORT_WIDTH / (float)Global.VIEWPORT_HEIGHT;
            if (ar > 1f) view_width *= ar;
            else view_height /= ar;
            this.min_c_x = this.min_x + (view_width / 2);
            this.max_c_x = this.max_x - (view_width / 2);
            this.min_c_y = this.min_y + (view_height / 2);
            this.max_c_y = this.max_y - (view_height / 2);
        }
    }
}
