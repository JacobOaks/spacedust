package svenske.spacedust.stages;

import android.view.MotionEvent;

import svenske.spacedust.utils.Node;

/*
 * Stages represent the different states the game can be in (menu, world, etc.)
 */
public interface Stage {

    /**
     * This will be called after any time that the OpenGL ES context is initialized. Sometimes this
     * will get called many times within one session of the stage being the current stage. This is
     * due to the fact that the OpenGL ES context gets destroyed and re-created as a response to
     * various events like re-orientation of the screen, or leaving the app for a long period of
     * time.
     * @param previous_continuous_data if the OpenGL ES context was destroyed, and re-created, this
     *                                 data is what was returned by this Stage's
     *                                 get_continuous_data() method before the context was
     *                                 destroyed. This can be used to restore import data.
     */
    void init(Node previous_continuous_data);

    /**
     * Called whenever there is scaling input from the Activity.
     * @param scale_factor the factor by which the user scaled
     * @param focal_x the middle of the scale x
     * @param focal_y the middle of the scale y
     * @return true if the input should not be responded to further, false otherwise
     */
    boolean scale_input(float scale_factor, float focal_x, float focal_y);

    /**
     * Called whenever there is input that was not previously handled as a gesture
     * @param me the MotionEvent containing the input info
     * @return true if the input should not be responded to further, false otherwise
     */
    boolean other_input(MotionEvent me);

    /**
     * Called every loop before render().
     * @param dt the amount of time since the last loop in ms.
     */
    void update(float dt);

    /**
     * Called every loop after update().
     */
    void render();

    /**
     * Called whenever the renderer has a new viewport size to share
     * @param width the new viewport width
     * @param height the new viewport height
     */
    void resize(float width, float height);

    /**
     * @return data to be passed into a new instantiation of this Stage if the OpenGL ES context
     *         gets destroyed and needs to be rebuilt. Specifically, the return from this method
     *         will get passed into this Stage's init() method. If there is no important data to
     *         be returned, null can be returned.
     */
    Node get_continuous_data();
}
