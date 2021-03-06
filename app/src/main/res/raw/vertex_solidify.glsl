
/**
 * Vertex shader program used specifically for solidifying a TextObject.
 * Uses: - vertex position
 *       - object position (0f, 0f in single-texture case)
 *       - object scale (2f, -2f in signle-texture case)
 */

// attributes
attribute vec2 vertex_position;
attribute vec2 tex_coords;
varying vec2 tex_coords_f;

// These are kept simply to allow TextSprite to use its render() method.
uniform float obj_x;
uniform float obj_y;
uniform float obj_scale_x;
uniform float obj_scale_y;
uniform float obj_rot;

void main() {

    // Pass through texture coordinates
    tex_coords_f = tex_coords;

    // Apply object scale, rotation, position
    // (model pos -> aspect pos)
    vec2 pos = vec2(vertex_position.x * obj_scale_x, vertex_position.y * obj_scale_y); // scale
    if (obj_rot != 0.0) { // rotation
        float cos_rot = cos(obj_rot);
        float sin_rot = sin(obj_rot);
        pos = vec2(pos.x * cos_rot - pos.y * sin_rot, pos.y * cos_rot + pos.x * sin_rot);
    }
    pos += vec2(obj_x, obj_y); // position

    // Set final position
    gl_Position = vec4(pos.x, pos.y, 0, 1);
}