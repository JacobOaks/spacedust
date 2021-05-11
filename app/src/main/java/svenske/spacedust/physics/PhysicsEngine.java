package svenske.spacedust.physics;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import svenske.spacedust.graphics.Camera;
import svenske.spacedust.utils.Global;

/**
 * Detects and handles physics between PhysicsObjects. Important notes/assumptions:
 * - The engine only checks for collisions between objects it considers relevant. Objects are
 *   relevant if they are within some scope of relevance, defined by a camera's view multiplied by
 *   some scalar. Both the camera and the scalar are supplied in the constructor.
 * - The scope of relevance relies on the assumption that the scope multiplier will be large enough
 *   and the sizes of all PhysicsObjects small enough such that there will never be an image in
 *   view of the camera but not in scale of the engine (only the center point of objects is
 *   considered when determining if they are relevant)
 * - Rigid/stop collisions are not reliable or directly handled/supported by this engine yet.
 * - In order for the scope of relevance to stay updated correctly, resized() must always be called
 *   on the PhysicsEngine when the viewport size changes.
 */
public class PhysicsEngine {

    // Camera/scope attributes
    private Camera cam;          // The camera
    private float scope_w_2;     // The half-width of the scope of relevance
    private float scope_h_2;     // The half-height of the scope of relevant
    private float last_cam_zoom; // The last recorded zoom of the camera
    private float cam_scope_mul; // The multiplier applied to the camera's view to create the scope

    // Constructs the engine using the given cam and scope multiplier to define a scope of relevance
    public PhysicsEngine(Camera cam, float cam_scope_mul) {
        this.cam = cam;
        this.cam_scope_mul = cam_scope_mul;
        this.update_relevance_scope();
    }

    // Updates the half-width and half-height of the scope of relevance based on the camera's view.
    private void update_relevance_scope() {
        this.last_cam_zoom = this.cam.get_zoom();
        float[] scope_size = this.cam.get_view_size(this.cam_scope_mul);
        this.scope_w_2 = scope_size[0] / 2f;
        this.scope_h_2 = scope_size[1] / 2f;
    }

    // Main entry-point for outside sources to check for collisions.
    public void check_collisions(List<PhysicsObject> objects) {

        // Get only relevant objects
        List<PhysicsObject> relevant_objects = this.get_scoped_objects(objects);

        // Loop through each combination of objects
        for (int i = 0; i < relevant_objects.size(); i++) {
            float[] bounds_a = relevant_objects.get(i).get_bounds();
            for (int j = i + 1; j < relevant_objects.size(); j++) {

                // If they are colliding, call their on_collide methods
                float[] bounds_b = relevant_objects.get(j).get_bounds();
                if (are_colliding(bounds_a, bounds_b)) {
                    relevant_objects.get(i).on_collide(relevant_objects.get(j));
                    relevant_objects.get(j).on_collide(relevant_objects.get(i));
                }
            }
        }
    }

    // Narrows down the given list of objects to a list of only objects that are relevant
    private List<PhysicsObject> get_scoped_objects(List<PhysicsObject> objects) {

        /*
         * Determine min annd max position based on camera's position for an object to be considered
         * relevant. If the zoom has changed, a little more calculation is involved.
         */
        if (this.last_cam_zoom != this.cam.get_zoom()) this.update_relevance_scope();
        float min_x = this.cam.get_x() - this.scope_w_2;
        float max_x = min_x + (2 * this.scope_w_2);
        float min_y = this.cam.get_y() - this.scope_h_2;
        float max_y = min_y + (2 * this.scope_h_2);

        // Only add objects within the bounds calculated above to the list of relevant objects.
        List<PhysicsObject> relevant = new ArrayList<>();
        for (PhysicsObject po : objects) {
            float[] bounds = po.get_bounds();

            /*
             * IMPORTANT ASSUMPTION:
             * All of our objects will have size smaller than the size of the padding included in
             * the scope. In other words we will ONLY have objects small enough such that they will
             * never be in the camera's view but not in the PhysicsEngine's scope.
             */

            if (bounds[0] >= min_x && bounds[0] <= max_x &&   // Check x
                    bounds[1] >= min_y && bounds[1] <= max_y) // Check y
                relevant.add(po);
        }
        return relevant;
    }

    // Respond to a resize by re-calculating the relevance scope
    public void resized() {
        this.update_relevance_scope();
    }

    // Check if the two given bounds are colliding
    public static boolean are_colliding(float[] bounds_a, float[] bounds_b) {
        if (bounds_a.length == 4 && bounds_b.length == 4)      // Rectangle-rectangle collision
            return are_colliding_recs(bounds_a, bounds_b);
        else if (bounds_a.length == 3 && bounds_b.length == 3) // Circle-circle collision
            return are_colliding_circles(bounds_a, bounds_b);
        else if (bounds_a.length == 4 && bounds_b.length == 3) // Rectangle-circle collision
            return are_colliding_rec_circle(bounds_a, bounds_b);
        else if (bounds_a.length == 3 && bounds_b.length == 4) // Circle-rectangle collision
            return are_colliding_rec_circle(bounds_b, bounds_a);
        else
            throw new RuntimeException("[spdt/physicsengine] " +
                    "Unsupported bounds. Float arrays must be length 4 or 3:\n" +
                    " - bounds_a: " + Arrays.toString(bounds_a) + "\n" +
                    " - bounds_b: " + Arrays.toString(bounds_b));
    }

    // Check for collision between two rectangular bounds
    public static boolean are_colliding_recs(float[] bounds_a, float[] bounds_b) {

        // Standard AABB collision check
        return  bounds_a[0] < bounds_b[0] + bounds_b[2] &&
                bounds_a[0] + bounds_a[2] > bounds_b[0] &&
                bounds_a[1] < bounds_b[1] + bounds_b[3] &&
                bounds_a[1] + bounds_a[3] > bounds_b[1];
    }

    // Check for collision between two circle bounds
    public static boolean are_colliding_circles(float[] bounds_a, float[] bounds_b) {

        // Calculate distance between center of circles
        float dx = bounds_a[0] - bounds_b[0];
        float dy = bounds_a[1] - bounds_b[1];
        float d = (float)Math.sqrt((dx * dx) + (dy * dy));

        // If the distance is less than the sum of their radii, collision is occurring
        if (d <= (bounds_a[2] + bounds_b[2]))  return true;
        return false;
    }

    // Check for collision between rectangle bounds and circle bounds
    public static boolean are_colliding_rec_circle(float[] bounds_rec, float[] bounds_circle) {

        // Calculate half-width and half-height of rectangle
        float rb_hw = bounds_rec[2] / 2;
        float rb_hh = bounds_rec[3] / 2;

        // Calculate distance between their centers
        float dx = bounds_circle[0] - bounds_rec[0];
        float dy = bounds_circle[1] - bounds_rec[1];

        // Clamp rectangle
        float clamp_x = Math.max(Math.min(rb_hw, dx), -rb_hw);
        float clamp_y = Math.max(Math.min(rb_hh, dy), -rb_hh);

        // Find closest point on rectangle to circle
        float closest_x = bounds_rec[0] + clamp_x;
        float closest_y = bounds_rec[1] + clamp_y;

        // Calculate distance between closest point and center of circle
        float diff_x = closest_x - bounds_circle[0];
        float diff_y = closest_y - bounds_circle[1];
        float d = (float)Math.sqrt((diff_x * diff_x) + (diff_y * diff_y));

        // If the distance is less than the circle's radius, collision is occurring
        return d < bounds_circle[2];
    }
}
