package svenske.spacedust.graphics;

// TODO: Document and support

public class LightSource {

    private float color[];
    private float intensity;
    private float reach;

    // 3 values: [min_reach, max_reach, reach_change_rate (units/second)]
    private float flicker[];

    public LightSource(float[] color, float intensity, float reach, float[] flicker) {
        if (color == null | color.length != 4)
            throw new RuntimeException("[spdt/lightsource] " +
                    "color must be length 4.");
        this.color = color;
        this.intensity = intensity;
        this.reach = reach;
        this.flicker = flicker;
        if (flicker != null && flicker.length != 3)
            throw new RuntimeException("[spdt/lightsource] " +
                    "flicker must be length 3 (see class for specification).");
    }

    public void update(float dt) {
        if (this.flicker != null) {
            float direction = Math.random() < 0.5 ? -1f : 1f;
            float delta_reach = dt * this.flicker[2] * direction;
            this.reach = Math.min(Math.max(this.flicker[0], this.reach + delta_reach), this.flicker[1]);
        }
    }

    // TODO: setUniforms() to set lighting uniforms in world shader
}
