package svenske.spacedust.gameobject;

import svenske.spacedust.graphics.BlendMode;
import svenske.spacedust.graphics.ShaderProgram;
import svenske.spacedust.graphics.Sprite;

// A generic class representing any sort of bars (e.g., health, progress, etc.)
public class Bar extends GameObject {

    Sprite foreground;
    private float fill;
    private float foreground_sx;
    private float foreground_offset_x;
    private float[] empty_color;
    private float[] full_color;

    public Bar(float[] full_color, float[] empty_color, float[] background_color, float sx, float sy, float x, float y) {
        super(new Sprite(null, -1, -1, background_color,
                BlendMode.JUST_COLOR, null, null), x, y);

        this.foreground = new Sprite(null, -1, -1, full_color,
                BlendMode.JUST_COLOR, null, null);

        this.empty_color = empty_color;
        this.full_color = full_color;

        this.sx = sx;
        this.sy = sy;
        this.set_fill(1f);
        this.foreground_offset_x = 0f;
    }

    public void set_fill(float fill) {
        this.fill = Math.min(1f, Math.max(0f, fill));
        this.reload();
    }

    private void reload() {
        this.foreground_sx = fill * this.sx;
        float width = this.get_size()[0];
        float foreground_width = width * fill;
        float left_x = this.x - (width / 2);
        float foreground_x = left_x + (foreground_width / 2);
        this.foreground_offset_x = foreground_x - this.x;
        this.foreground.set_color(new float[] {
                this.empty_color[0] * (1 - this.fill) + this.full_color[0] * this.fill,
                this.empty_color[1] * (1 - this.fill) + this.full_color[1] * this.fill,
                this.empty_color[2] * (1 - this.fill) + this.full_color[2] * this.fill,
                this.empty_color[3] * (1 - this.fill) + this.full_color[3] * this.fill,
        });
    }

    @Override
    public void set_scale(float sx, float sy) {
        super.set_scale(sx, sy);
        this.reload();
    }

    @Override
    void render(ShaderProgram sp) {
        super.render(sp);
        this.foreground.render(sp, this.x + this.foreground_offset_x, this.y, this.foreground_sx, this.sy, this.rot);
    }
}
