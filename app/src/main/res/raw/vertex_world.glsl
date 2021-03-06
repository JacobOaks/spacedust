
/**
 * Vertex shader program used for rendering a game world.
 * Uses: - vertex position
 *       - object scale
 *       - object rotation
 *       - object position
 *       - camera (position and zoom)
 *       - aspect ratio
 */

// Attributes
attribute vec2 vertex_position;
attribute vec2 tex_coords;

// Variables to pass to fragment shader
varying vec2 tex_coords_f;
varying vec2 frag_world_pos;

// Object uniforms
uniform float obj_x;
uniform float obj_y;
uniform float obj_scale_x;
uniform float obj_scale_y;
uniform float obj_rot;

// Camera uniforms
uniform float cam_x;
uniform float cam_y;
uniform float cam_zoom;

// Other uniforms
uniform float aspect_ratio;

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
    frag_world_pos = pos;

    // Apply camera pos and zoom
    // (aspect pos -> world pos)
    pos -= vec2(cam_x, cam_y);
    pos *= cam_zoom;

    // Apply aspect ratio
    // (world pos -> normalized pos)
    if  (aspect_ratio < 1.0f)
        pos.y *= aspect_ratio;
    else
        pos.x /= aspect_ratio;

    // Set final position
    gl_Position = vec4(pos.x, pos.y, 0, 1);
}