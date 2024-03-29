package svenske.spacedust.graphics;

import svenske.spacedust.utils.Global;

// Represents a camera in a World. Cameras can be given strict bounds to restrict what can be seen.
public class Camera {

    // Camera position and zoom
    private float x, y;
    private float zoom;

    // Camera bounds info
    private float min_x, max_x, min_y, max_y;         // Strict visibility bounds
    private float min_c_x, max_c_x, min_c_y, max_c_y; // Calculated bounds for the camera's pos
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
    public float get_x() { return this.x; }
    public float get_y() { return this.y; }
    public float get_zoom() { return this.zoom; }

    // Sets the camera's zoom and updates the bounds (if there are any)
    public void set_zoom(float zoom) {
        this.zoom = zoom;
        this.update_bounds();
    }

    // Sets the camera's position, checking its bounds if it has them
    public void set_position(float x, float y) {
        this.x = this.bounded ? Math.min(Math.max(x, this.min_c_x), this.max_c_x) : x;
        this.y = this.bounded ? Math.min(Math.max(y, this.min_c_y), this.max_c_y) : y;
    }

    /**
     * Sets the strict visibility bounds of the camera. I.e., if min_x = 0.1, no x < 0.1 will ever
     * be visible with this camera. The same follows for the other three parameters.
     */
    public void set_bounds(float min_x, float max_x, float min_y, float max_y) {
        this.min_x = min_x;
        this.max_x = max_x;
        this.min_y = min_y;
        this.max_y = max_y;
        this.bounded = true;
        this.update_bounds();
    }

    /**
     * From the strict visibility bounds, calculate the bounds for the camera's position based off
     * of aspect ratio and zoom.
     */
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

    // Returns the size (width/height) of the camera's view, multiplied by some given scalar
    public float[] get_view_size(float mul) {
        // Calculate the current view times the given scalar
        float view_width = 2f * mul/ this.zoom;
        float view_height = 2f * mul / this.zoom;
        float ar = (float)Global.VIEWPORT_WIDTH / (float)Global.VIEWPORT_HEIGHT;
        if (ar > 1f) view_width *= ar;
        else view_height /= ar;
        return new float[] { view_width, view_height };
    }

    // Return if the given position is out of the camera's view (scaled by the given scalar)
    public boolean out_of_view(float x, float y, float mul) {

        // Get the size of the camera's view scaled by the given multiplier
        float[] view_size = this.get_view_size(mul);

        // Determine if given position is outside of the calculated view
        if (x < this.x - view_size[0] / 2 || x > this.x + view_size[0] / 2) {
            return y < this.y - view_size[1] / 2 || y > this.y + view_size[1] / 2;
        }
        return false;
    }
}
