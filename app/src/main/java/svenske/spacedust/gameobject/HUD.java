package svenske.spacedust.gameobject;

import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import svenske.spacedust.R;
import svenske.spacedust.graphics.ShaderProgram;
import svenske.spacedust.graphics.Sprite;
import svenske.spacedust.utils.Global;
import svenske.spacedust.utils.Node;

/**
 * Encapsulation for all that is to be considered part of the heads-up display rendered over top of
 * a World.
 *
 * The HUD class contains robust tools for making complex displays based on positioning objects
 * relative to other objects (or the edge of the screen).
 */
public class HUD implements Sprite.ResizeCallback {

    // Represents a placement of an object relative to another
    public enum RelativePlacement { BELOW, RIGHT, LEFT, ABOVE }

    /**
     * Represents an alignment of an object to its parent
     * - LEFT/RIGHT only make sense if RelativePlacement is BELOW/ABOVE
     * - TOP/BOTTOM only make sense if RelativePlacement is LEFT/RIGHT
     */
    public enum Alignment {
        LEFT, CENTER, RIGHT, TOP, BOTTOM
    }

    // Represents a single node in the hierarchy of HUD elements whose positions depend each other.
    private class HUDObject {

        GameObject object;           // The object whose placement is described.
        List<HUDObject> children;    // Child objects whose placements depend on this object.
        RelativePlacement placement; // How this object's placement relates to its parent's
        Alignment alignment;         // How the object aligns with its parent
        float padding;               // Padding between this object and its parent.

        // Constructs the HUDObject by enumerating its properties
        private HUDObject(GameObject object, RelativePlacement placement, Alignment alignment,
                          float padding) {
            this.object = object;
            this.children = new ArrayList<>();
            this.placement = placement;
            this.alignment = alignment;
            this.padding = padding;
        }
    }

    // HUD Attributes
    ShaderProgram sp;
    List<HUDObject> hud_object_roots;    // A list of the root HUDObjects in the placement hierarchy
    Map<GameObject, HUDObject> go_to_ho; // Map from GameObjects to their encapsulating HUDObjects

    // Constructs the HUD
    public HUD() {
        this.sp = new ShaderProgram(R.raw.vertex_hud, R.raw.fragment_hud);
        this.hud_object_roots = new ArrayList<>();
        this.go_to_ho = new HashMap<>();
    }

    // Allows all GameObjects in the HUD to respond to input if they are InputReceivers
    public void input(MotionEvent me, List<Integer> ignore_idx) {
        for (GameObject go : this.go_to_ho.keySet())
            if (go instanceof InputReceiver) ((InputReceiver)go).input(me, ignore_idx);
    }

    // Updates all HUD GameObjects
    public void update(float dt) {
        for (GameObject go : this.go_to_ho.keySet()) go.update(dt);
    }

    // Renders all HUD GameObjects using the HUD shaders.
    public void render() {
        this.sp.bind();
        for (GameObject go : this.go_to_ho.keySet()) go.render(sp);
        ShaderProgram.unbind_any_shader_program();
    }

    // Updates the aspect ratio uniform in the shader programs and re-places the object hierarchy
    public void resized() {
        this.sp.bind();
        this.sp.set_uniform("aspect_ratio",
                ((float) Global.VIEWPORT_WIDTH / (float)Global.VIEWPORT_HEIGHT));
        ShaderProgram.unbind_any_shader_program();
        this.restructure();
    }

    // Adds an object to the HUD, placing it in the object hierarchy
    public void add_object(GameObject object, GameObject parent, RelativePlacement placement,
                           Alignment alignment, float padding) {
        HUDObject ho = new HUDObject(object, placement, alignment, padding);
        this.go_to_ho.put(object, ho);
        if (parent != null) {
            HUDObject parent_ho = this.go_to_ho.get(parent);
            parent_ho.children.add(ho);
        } else this.hud_object_roots.add(ho);

        // Add callback for the object's Sprite gets resized
        Sprite sprite = object.get_sprite();
        if (sprite != null) sprite.set_resize_callback(this);
    }

    // When a sprite is resized in the HUD, this callback will re-place all HUD GameObjects
    @Override
    public void on_resize() {
        this.restructure();
    }

    // Re-places all HUD GameObjects starting at the roots and moving to the leaves
    public void restructure() {

        // Calculate screen info
        float ar = (float)Global.VIEWPORT_WIDTH / (float)Global.VIEWPORT_HEIGHT;
        float max_x = 1f, max_y = 1f;
        if (ar > 1f) max_x *= ar;
        else max_y /= ar;

        // Start with root objects
        for (HUDObject ho : this.hud_object_roots) {
            float[] size = ho.object.get_size();
            float x = 0f, y = 0f;

            // Calculate correct placement for this root object
            if (ho.placement != null) {
                if (ho.placement == RelativePlacement.BELOW)      // TOP OF SCREEN
                    y = max_y - (size[1] / 2) - ho.padding;
                else if (ho.placement == RelativePlacement.RIGHT) // LEFT OF SCREEN
                    x = -max_x + (size[0] / 2) + ho.padding;
                else if (ho.placement == RelativePlacement.LEFT)  // RIGHT OF SCREEN
                    x = max_x - (size[0] / 2) + ho.padding;
                else                                              // BOTTOM OF SCREEN
                    y = -max_y + (size[1] / 2) + ho.padding;

                // Calculate alignment if there is one
                if (ho.alignment != null && ho.alignment != Alignment.CENTER) {
                    if (ho.placement == RelativePlacement.BELOW || ho.placement == RelativePlacement.ABOVE) {
                        if (ho.alignment == Alignment.LEFT) // ALIGN LEFT
                            x = -max_x + size[0] / 2 + ho.padding;
                        else                                // ALIGN RIGHT
                            x = max_x - size[0] / 2 - ho.padding;
                    }
                    else {
                        if (ho.alignment == Alignment.TOP) // ALIGN TOP
                            y = max_y - size[1] / 2 - ho.padding;
                        else                               // ALIGN BOTTOM
                            y = -max_y + size[1] / 2 + ho.padding;
                    }
                }
            }

            // Place it and place children
            ho.object.set_pos(x, y);
            for (HUDObject child_ho : ho.children) restructure_recursive(ho.object, child_ho);
        }
    }

    // Recursively re-places a HUDObject and its children
    private void restructure_recursive(GameObject parent, HUDObject ho) {

        // Get dimensions/position of parent and child
        float[] size = ho.object.get_size();
        float[] parent_pos = parent.get_pos();
        float[] parent_size = parent.get_size();
        float x = parent_pos[0], y = parent_pos[1];

        // Calculate correct placement for this object
        if (ho.placement != null) {
            if (ho.placement == RelativePlacement.BELOW)      // BELOW PARENT
                y -= (parent_size[1] / 2 + size[1] / 2 + ho.padding);
            else if (ho.placement == RelativePlacement.RIGHT) // RIGHT OF PARENT
                x += (parent_size[0] / 2 + size[0] / 2 + ho.padding);
            else if (ho.placement == RelativePlacement.LEFT)  // LEFT OF PARENT
                x -= (parent_size[0] / 2 + size[0] / 2 + ho.padding);
            else                                              // ABOVE PARENT
                y += (parent_size[1] / 2 + size[1] / 2 + ho.padding);

            // Calculate alignment if one is given
            if (ho.alignment != null && ho.alignment != Alignment.CENTER) {
                if (ho.placement == RelativePlacement.BELOW || ho.placement == RelativePlacement.ABOVE) {
                    if (ho.alignment == Alignment.LEFT) // ALIGN LEFT
                        x = x - (parent_size[0] / 2) + (size[0] / 2);
                    else                                // ALIGN RIGHT
                        x = x + (parent_size[0] / 2) + (size[0] / 2);
                }
                else {
                    if (ho.alignment == Alignment.TOP) // ALIGN TOP
                        y += (parent_size[1] / 2 - size[1] / 2);
                    else                               // ALIGN BOTTOM
                        y -= (parent_size[1] / 2 - size[1] / 2);
                }
            }
        }

        // Place the object and recurse for each child
        ho.object.set_pos(x, y);
        for (HUDObject child_ho : ho.children) restructure_recursive(ho.object, child_ho);
    }
}
