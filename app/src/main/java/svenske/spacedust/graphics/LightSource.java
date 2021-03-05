package svenske.spacedust.graphics;

// TODO: Document and support

import android.util.Log;

public class LightSource {

    private float glow[];
    private float intensity;
    private float reach;

    // 3 values: [min_reach, max_reach, reach_change_rate (units/second)]
    private float flicker[];

    public LightSource(float[] glow, float intensity, float reach, float[] flicker) {
        if (glow == null | glow.length != 3)
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
}
