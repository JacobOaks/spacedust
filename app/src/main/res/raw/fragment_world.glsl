
/**
 * Fragment shader program used for rendering a game world.
 * Uses: - color
 *       - texture
 *       - blend mode
 *       - lighting
 */

// A struct describing a light source
struct LightSource {
    vec3 glow;
    float reach;
    float intensity;
    float x;
    float y;
};

precision mediump float;

// Object Uniforms
uniform vec4 vertex_color;
uniform sampler2D texture_sampler;
uniform int blend_mode; // Enumerated in same order as Java BlendMode class

// Lighting Uniforms/Attributes
const int MAX_LIGHTS = 64;
uniform float ambient_light;
uniform float max_brightness;
uniform LightSource lights[MAX_LIGHTS];

// Variables from vertex shader
varying vec2 tex_coords_f;
varying vec2 frag_world_pos;

// Gets the initial unlit color from the texture, color, and blend mode
vec4 get_unlit_color(sampler2D texture_sampler, vec2 tex_coords_f, int blend_mode, vec4 vertex_color) {
    if (blend_mode == 0) {        // JUST COLOR
        return vertex_color;
    } else if (blend_mode == 1) { // JUST TEXTURE
        return texture2D(texture_sampler, tex_coords_f);
    } else if (blend_mode == 2) { // ADDITIVE
        return vertex_color + texture2D(texture_sampler, tex_coords_f);
    } else if (blend_mode == 3) { // SUBTRACTIVE
        return vertex_color - texture2D(texture_sampler, tex_coords_f);
    } else if (blend_mode == 4) { // MULTIPLICATIVE
        return vertex_color * texture2D(texture_sampler, tex_coords_f);
    } else if (blend_mode == 5) { // AVG
        return (vertex_color + texture2D(texture_sampler, tex_coords_f)) / 2.0f;
    } else { // ERROR
        return vec4(1.0f, 0.0f, 1.0f, 1.0f);
    }
}

// Applies lighting to an unlit color
vec4 apply_lighting(vec4 unlit_color) {

    // Extract just rgb and apply ambient light
    vec3 working_color = unlit_color.xyz * ambient_light;

    // Keep track of cumulative brightness and glow across all lights
    float cum_brightness = 1.0;
    vec3  cum_glow = vec3(1.0, 1.0, 1.0);

    // Loop through all lights and apply each if within distance
    for (int i = 0; i < MAX_LIGHTS; i++) {

        // Use this check to weed out non-important lights, or end-of-array idx
        if (lights[i].reach > 0.0) {
            float light_dist = distance(vec2(lights[i].x, lights[i].y), frag_world_pos);
            if (light_dist <= lights[i].reach) { // If light close enough to affect fragment

                // 0.0: center of light's reach; 1.0: just out of reach of light;
                float farness = light_dist / lights[i].reach;

                // Add brightness from this light to cumulative brightness
                cum_brightness += (1.0 - farness) * lights[i].intensity;

                // Multiply glow from this light to cumulative glow
                cum_glow = cum_glow * (farness * vec3(1.0, 1.0, 1.0) + (1.0 - farness) * (lights[i].glow + 1.0));
            }
        }
    }
    working_color = working_color * min(max_brightness, cum_brightness);
    working_color = working_color * cum_glow;
    return vec4(working_color, unlit_color.w);
}

// Main function
void main() {

    // Determine the unlit color first
    vec4 unlit_color = get_unlit_color(texture_sampler, tex_coords_f, blend_mode, vertex_color);

    // Apply lighting and set final color
    gl_FragColor = apply_lighting(unlit_color);
}