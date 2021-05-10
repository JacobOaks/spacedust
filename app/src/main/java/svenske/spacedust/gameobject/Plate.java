package svenske.spacedust.gameobject;

import svenske.spacedust.graphics.BlendMode;
import svenske.spacedust.graphics.ShaderProgram;
import svenske.spacedust.graphics.Sprite;
import svenske.spacedust.graphics.TextSprite;
import svenske.spacedust.utils.Global;

/**
 * A render-able nameplate that displays an update-able HP bar and a single-line name
 */
public class Plate {

    // Attributes
    private Bar hp_bar;      // The nameplate's health bar
    private float inner_pad; // The padding between the health bar and the name

    // Name attributes
    private Sprite name;          // The name on the plate
    private float x, y;           // Position of the plate
    private float name_x, name_y; // Position of the name specifically
    // Scale for the name (is very large by default)
    public static final float NAME_SCALE = 0.22f;

    /**
     * Creates the plate
     * @param starting_fill the starting fill for the hp bar [0, 1]
     * The rest of the arguments are described in the attribute declarations
     */
    public Plate(String name, float starting_fill, float x, float y, float inner_pad) {

        // Create name sprite and health bar
        this.name = new TextSprite(Global.font, new float[] {1f, 1f, 1f, 1f},
                BlendMode.MULTIPLICATIVE, name).solidify();
        this.hp_bar = new Bar(new float[] {0f, 1f, 0f, 0.5f}, new float[] {1f, 0f, 0f, 1f},
                new float[] {0.5f, 0.5f, 0.5f, 0.5f}, 1.3f, 0.08f, 0, 0);

        // Save and calculation positions
        this.x = x;
        this.y = y;
        this.inner_pad = inner_pad;
        this.calculate_positions();

        // Set initial HP bar fill
        this.set_hp_bar_fill(starting_fill);
    }

    // Recalculate the positions for the name sprite and the health bar based off of the plate's pos
    private void calculate_positions() {
        float half_height = this.get_size()[1] / 2;
        name_x = this.x;
        name_y = (this.y + half_height) - (this.NAME_SCALE * this.name.get_size()[1] / 2);
        this.hp_bar.set_pos(this.x, (this.y - half_height) + (this.hp_bar.get_size()[1] / 2));
    }

    // Renders the plate
    public void render(ShaderProgram sp) {

        // Render health bar and string of text
        this.hp_bar.render(sp);
        this.name.render(sp, this.name_x, this.name_y, this.NAME_SCALE, this.NAME_SCALE, 0f);
    }

    // Updates the fill of the health bar
    public void set_hp_bar_fill(float fill) {
        this.hp_bar.set_fill(fill);
    }

    // Sets and updates positions for the plate
    public void set_pos(float x, float y) {
        this.x = x;
        this.y = y;
        this.calculate_positions();
    }

    // Calculates the size of the entire plate
    public float[] get_size() {
        float[] text_sz = this.name.get_size();
        text_sz[0] *= Plate.NAME_SCALE;
        text_sz[1] *= Plate.NAME_SCALE;
        float[] bar_sz  = this.hp_bar.get_size();
        return new float[] { Math.max(text_sz[0], bar_sz[0]), text_sz[1] + bar_sz[1] + this.inner_pad };
    }
}
