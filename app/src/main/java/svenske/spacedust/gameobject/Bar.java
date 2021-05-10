package svenske.spacedust.gameobject;

import svenske.spacedust.graphics.BlendMode;
import svenske.spacedust.graphics.ShaderProgram;
import svenske.spacedust.graphics.Sprite;

// A generic class representing any sort of horizontal bar (e.g., health, progress, etc.)
public class Bar extends GameObject {

    // Foreground properties
    Sprite foreground;                 // The foreground's sprite (just a colored/scaled square)
    private float foreground_sx;       // The foreground sprite's x-scale (based on fill)
    private float foreground_offset_x; // Offset relative to the bar's x for rendering foreground

    // Fill/color properties
    private float fill;          // 0.0: Bar is empty; 1.0: Bar is full
    // The actual color of the foreground of the bar is interpolated between these two:
    private float[] empty_color; // The color when the bar is empty
    private float[] full_color;  // The color when the bar is full

    /**
     * Constructs the bar. The bar's actual color is interpolated between full_color and empty_color
     * based on its current fill
     * @param full_color the color when the bar is full
     * @param empty_color the color when the bar is empty
     * @param background_color the color for the background of the bar
     * @param sx the x-scale for the bar
     * @param sy the y-scale for the bar
     * @param x the x-position for the bar
     * @param y the y-position for the bar
     */
    public Bar(float[] full_color, float[] empty_color, float[] background_color, float sx, float sy, float x, float y) {

        // Initialize background and superclass
        super(new Sprite(null, -1, -1, background_color,
                BlendMode.JUST_COLOR, null, null), x, y);

        // Initialize foreground sprite
        this.foreground = new Sprite(null, -1, -1, full_color,
                BlendMode.JUST_COLOR, null, null);

        // Save attributes
        this.empty_color = empty_color;
        this.full_color = full_color;
        this.sx = sx;
        this.sy = sy;

        // Set bar to full at beginning
        this.foreground_offset_x = 0f;
        this.set_fill(1f);
    }

    // Updates the bar's fill and reloads the foreground of the bar
    public void set_fill(float fill) {
        this.fill = Math.min(1f, Math.max(0f, fill));
        this.reload();
    }

    // Ensures the foreground is colored and positioned correctly based on the bar's fill
    private void reload() {

        // Update foreground scale and offset
        this.foreground_sx = fill * this.sx;
        float width = this.get_size()[0];
        float foreground_width = width * fill;
        float left_x = -(width / 2);
        this.foreground_offset_x = left_x + (foreground_width / 2);

        // Update foreground color
        this.foreground.set_color(new float[] {
                this.empty_color[0] * (1 - this.fill) + this.full_color[0] * this.fill,
                this.empty_color[1] * (1 - this.fill) + this.full_color[1] * this.fill,
                this.empty_color[2] * (1 - this.fill) + this.full_color[2] * this.fill,
                this.empty_color[3] * (1 - this.fill) + this.full_color[3] * this.fill,
        });
    }

    // Responds to scale changes by reloading the bar
    @Override
    public void set_scale(float sx, float sy) {
        super.set_scale(sx, sy);
        this.reload();
    }

    // Renders the background and the foreground of the bar
    @Override
    void render(ShaderProgram sp) {
        super.render(sp);
        this.foreground.render(sp, this.x + this.foreground_offset_x, this.y, this.foreground_sx, this.sy, this.rot);
    }
}
