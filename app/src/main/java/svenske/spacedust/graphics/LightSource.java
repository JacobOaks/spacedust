package svenske.spacedust.graphics;

import android.util.Log;

// Represents a light source used in shader programs for lighting calculations
public class LightSource {

    // Attributes
    private float glow[];    // The color of the light source
    private float intensity; // How intense the light source is
    private float reach;     // How far reaching the light source is
    // Flicker provides a way for the light to randomly dim/enlighten quickly and erratically
    // 3 values: [min_reach, max_reach, reach_change_rate (units/second)]
    private float flicker[];

    // Constructs the LightSource by providing each attribute as detailed above
    public LightSource(float[] glow, float intensity, float reach, float[] flicker) {
        if (glow == null || glow.length != 3)
            throw new RuntimeException("[spdt/lightsource] " +
                    "light glow must be length 3.");
        this.glow = glow;
        this.intensity = intensity;
        this.reach = reach;
        this.flicker = flicker;
        if (flicker != null && flicker.length != 3)
            throw new RuntimeException("[spdt/lightsource] " +
                    "flicker must be length 3 (see class for specification).");
    }

    // Updates the LightSource by updating flicker if it has it.
    public void update(float dt) {
        if (this.flicker != null) {
            float direction = Math.random() < 0.5 ? -1f : 1f;
            float delta_reach = dt * this.flicker[2] * direction;
            this.reach = Math.min(Math.max(this.flicker[0], this.reach + delta_reach), this.flicker[1]);
            Log.d("[spdt/lightsource]", "reach: " + this.reach);
        }
    }

    // Accessors
    public float get_reach() { return this.reach; }
    public float get_intensity() { return this.intensity; }
    public float[] get_glow() { return this.glow; }

    // Mutators
    public void set_reach(float reach) { this.reach = reach; }
    public void set_intensity(float intensity) { this.intensity = intensity; }
    public void set_glow(float[] glow) { this.glow = glow; }
}
