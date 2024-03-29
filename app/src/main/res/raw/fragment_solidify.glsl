
/**
 * Fragment shader program used specifically for solidifying a TextObject.
 * Uses: - color
 *       - texture
 *       - blend mode
 */

precision mediump float;

// Uniforms
uniform vec4 vertex_color;
uniform sampler2D texture_sampler;
uniform int blend_mode; // enumerated in same order as Java BlendMode class

// Variables from vertex shader
varying vec2 tex_coords_f;

// Main function
void main() {
    if (blend_mode == 0) {        // JUST COLOR
        gl_FragColor = vertex_color;
    } else if (blend_mode == 1) { // JUST TEXTURE
        gl_FragColor = texture2D(texture_sampler, tex_coords_f);
    } else if (blend_mode == 2) { // ADDITIVE
        gl_FragColor = vertex_color + texture2D(texture_sampler, tex_coords_f);
    } else if (blend_mode == 3) { // SUBTRACTIVE
        gl_FragColor = vertex_color - texture2D(texture_sampler, tex_coords_f);
    } else if (blend_mode == 4) { // MULTIPLICATIVE
        gl_FragColor = vertex_color * texture2D(texture_sampler, tex_coords_f);
    } else if (blend_mode == 5) { // AVG
        gl_FragColor = (vertex_color + texture2D(texture_sampler, tex_coords_f)) / 2.0f;
    } else { // ERROR
        gl_FragColor = vec4(1.0f, 0.0f, 1.0f, 1.0f);
    }
}