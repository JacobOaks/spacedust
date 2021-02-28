package svenske.spacedust.graphics;

import svenske.spacedust.utils.Utils;

public class TextSprite extends Sprite {

    private String text;

    public TextSprite(Font font, float[] color, BlendMode blend_mode, String text) {

        // Pass dummy values to super constructor for now
        super(null, 0, 0, color == null ? new float[4] : color,
                BlendMode.JUST_COLOR, null, null);

        if (blend_mode == BlendMode.JUST_COLOR)
            throw new RuntimeException("[spdt/textsprite] " +
                    "Creating a TextSprite but blend mode doesn't use texture");

        this.atlas = font;
        this.text = text;
        this.blend_mode = blend_mode;
        this.update_buffers();
    }

    private void update_buffers() {

        float[] tex_coords = ((Font)this.atlas).get_tex_coords_for_char(this.text.charAt(0), true);
        this.texture_coordinates = Utils.get_float_buffer_from(tex_coords);
    }
}
