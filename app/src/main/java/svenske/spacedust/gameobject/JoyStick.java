package svenske.spacedust.gameobject;

import android.util.Log;
import android.view.MotionEvent;

import java.util.List;

import svenske.spacedust.R;
import svenske.spacedust.graphics.BlendMode;
import svenske.spacedust.graphics.Camera;
import svenske.spacedust.graphics.ShaderProgram;
import svenske.spacedust.graphics.Sprite;
import svenske.spacedust.graphics.TextureAtlas;
import svenske.spacedust.utils.Transform;

import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;

/**
 * A render-able and interact-able joy stick class that can be used to receive vector (direction and
 * magnitude) input from the user.
 *
 * NOTE: JoySticks will only world in a HUD as they do not take cameras into account when receiving
 * input
 */
public class JoyStick extends GameObject implements InputReceiver {

    /**
     * A JoystickReceiver can be attached to a JoyStick to receive/react to it as an input medium.
     */
    public interface JoystickReceiver {

        /**
         * Whenever there is an update in the JoySticks inner circle's position, it will call this
         * on its receiver.
         * @param id this JoyStick's id (in case one receiver relies on multiple JoySticks)
         * @param x the x component of the normalized direction vector of the JoyStick
         * @param y the y component of the normalized direction vector of the JoyStick
         * @param magnitude the magnitude of the direction vector of the JoyStick, maxed at 1f. IN
         *                  other words, how far along (from 0 to 1) the outer circle's radius is
         *                  the JoyStick's inner circle?
         */
        void receive_dir_vec(String id, float x, float y, float magnitude);

        // Called whenever the JoyStick's relevant pointer is released and input has ended.
        void input_ended();
    }

    // Attributes
    private String id;                 // An ID assigned when created to differentiate JoySticks
    private Sprite inner_circle;       // Sprite for the inner circle
    private JoystickReceiver receiver; // A receiver to send parsed input to
    private int assigned_pointer = -1; // The ID of a relevant pointer

    // Position attributes
    private float outer_circle_radius = 0.43f; // Radius of outer circle in aspect-norm coordinates
    private float inner_circle_radius = 0.15f; // Radius of inner circle in aspect-norm coordinates
    // Inner circle offset from the center of the outer circle (aspect norm space)
    private float ico_x = 0f;
    private float ico_y = 0f;

    /**
     * Constructs the JoyStick at the given aspect-normalized position and with the given receiver
     * to send updates to.
     */
    public JoyStick(String id, float x, float y, JoystickReceiver receiver) {
        super(null, x, y);
        this.id = id;
        this.receiver = receiver;
        this.sx = this.sy = outer_circle_radius * 2;

        // Inner circle sprite
        this.inner_circle = new Sprite(
                new TextureAtlas(R.drawable.joystick_inner_circle, 1, 1),
                0, 0, new float[] { 1f, 1f, 1f, 0.6f }, BlendMode.MULTIPLICATIVE,
                null, null);

        // Outer circle sprite
        this.sprite = new Sprite(
                new TextureAtlas(R.drawable.joystick_outer_circle, 1, 1),
                0, 0, new float[] { 1f, 1f, 1f, 0.6f }, BlendMode.MULTIPLICATIVE,
                null, null);
    }

    // Determines whether the given position is within the JoyStick's outer circle.
    private boolean point_in_outer_circle(float[] pos) {
        float dx = pos[0] - this.x;
        float dy = pos[1] - this.y;
        double distance = Math.sqrt((dx * dx) + (dy * dy));
        return !(distance > this.outer_circle_radius);
    }

    /**
     * Updates the position of the JoyStick's inner circle with the given relevant finger's position
     * (in aspect normalized co-ordinates)
     */
    private void update_inner_circle_position(float[] pos) {

        // Calculate distances from center of JoyStick
        float dx = pos[0] - this.x;
        float dy = pos[1] - this.y;
        double distance = Math.sqrt((dx * dx) + (dy * dy));

        /*
         * Calculate magnitude as 1 if the finger is outside of the JoyStick, or 0-1 based off of
         * how far along the outer circle's radius the finger is.
         */
        float magnitude = Math.min(1f, (float)(distance / this.outer_circle_radius));

        // Set the correct inner circle's position
        if (distance > this.outer_circle_radius) { // If outside of JoyStick, place on outer edge
            this.ico_x = (float)(dx / distance) * this.outer_circle_radius;
            this.ico_y = (float)(dy / distance) * this.outer_circle_radius;
        } else { // Otherwise place exactly where it is.
            this.ico_x = dx;
            this.ico_y = dy;
        }

        // Notifier the receiver if
        if (this.receiver != null) this.receiver.receive_dir_vec(this.id,
                (float)(dx / distance), (float)(dy / distance), magnitude);
    }

    // Responds to input
    public void input (MotionEvent me, List<Integer> ignore_idx) {

        // Check all pointers if no pointer is already assigned
        for (int i = 0; i < me.getPointerCount() && assigned_pointer == -1; i++) {
            if (!ignore_idx.contains(i)) { // If the pointer is not already in use

                // Check if pointer inside of JoyStick
                float[] aspect_pos = Transform.screen_to_aspect(me.getX(i), me.getY(i));
                if (point_in_outer_circle(aspect_pos)) {

                    // Save the pointer's ID for tracking
                    this.assigned_pointer = me.getPointerId(i);
                    this.update_inner_circle_position(aspect_pos); // move inner circle
                    ignore_idx.add(i);                             // call dibs on this index
                    return;
                }
            }
        }

        // If pointer is already assigned, track it and update inner circle
        if (assigned_pointer != -1) {
            int index = me.findPointerIndex(this.assigned_pointer); // Get its index

            // If the finger was released, no longer track it
            if (me.getAction() == ACTION_UP
                    || (me.getActionMasked() == ACTION_POINTER_UP && me.getActionIndex() == index)
                    /*
                     * If the user decides to spazz out on the JoyStick, sometimes index == -1. Just
                     * log it and assume this means that pointer has been lifted.
                     */
                    || index == -1) {

                if (index == -1)
                    Log.d("spdt/joystick", "assigned pointer no longer valid");
                this.assigned_pointer = -1;
                this.ico_x = this.ico_y = 0f; // Reset inner-circle position
                if (this.receiver != null) this.receiver.input_ended();
            } else {

                // Update inner circle position
                float[] aspect_pos = Transform.screen_to_aspect(me.getX(index), me.getY(index));
                this.update_inner_circle_position(aspect_pos);
            }
            if (index != -1) ignore_idx.add(index); // Claim the relevant pointer index
        }
    }

    // Updates the outer and inner circle sprites
    @Override
    public void update(float dt) {
        super.update(dt);
        this.inner_circle.update(dt);
    }

    // Renders the outer and inner circle sprites
    @Override
    public void render(ShaderProgram sp) {
        // Default square model is 1f, so scale should be set to the diameter.
        super.render(sp);
        this.inner_circle.render(sp, this.x + this.ico_x, this.y + this.ico_y,
                this.inner_circle_radius * 2, this.inner_circle_radius * 2);
    }
}
