package svenske.spacedust.graphics;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import svenske.spacedust.GameActivity;
import svenske.spacedust.stages.Stage;
import svenske.spacedust.stages.WorldStage;
import svenske.spacedust.utils.Node;

public class GameRenderer implements GLSurfaceView.Renderer {

    private Stage stage;

    /**
     * Whenever the surface is created, initialize GL and the current Stage.
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d("spdt/gamerenderer", "surface (re-)created");
        init_gl();
        init_stage();
    }

    /**
     * Initializes GL after the context was created in GameView.
     */
    private void init_gl() {

        // Set background frame color
        GLES20.glClearColor(0.2f, 0.3f, 0.8f, 1.0f);

        // Enable gl transparencies
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);

    }

    /**
     * Based off of GameActivity's current_stage signifier, creates and initializes a new Stage with
     * any available continuous data from a previous OpenGL ES context.
     */
    private void init_stage() {
        switch (GameActivity.current_stage) {
            case WORLD_STAGE:
                this.stage = new WorldStage();
        }
        this.stage.init(GameActivity.access_continuous_data());
    }

    /**
     * Called every redraw. Performs updates and renders.
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        this.update();
        this.render();
    }

    /**
     * Called whenever a new screen geometry has presented itself.
     * @param width width of the new screen geometry
     * @param height height of the new screen geometry
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        this.stage.resize((float)width, (float)height);
    }

    /**
     * Called whenever scaling input occurs
     * @return true if the input should not be handled further, false otherwise
     */
    public boolean scale_input(float scale_factor, float focal_x, float focal_y) {
        return this.stage.scale_input(scale_factor, focal_x, focal_y);
    }

    /**
     * Called whenever any random input occurs that wasn't previously handled as a gesture
     * @return true if the input should be not be responded to further, false otherwise
     */
    public boolean other_input(MotionEvent me) {
        return this.stage.other_input(me);
    }

    /**
     * Performs timekeeping calculations and updates the current Stage
     */
    private void update() {

        /**
         * TODO: Delta calculations.
         */

        this.stage.update(0f);
    }

    /**
     * Renders the current Stage
     */
    private void render() {
        this.stage.render();
    }

    /**
     * @return continuous data from the Stage to be passed up to be saved across OpenGL ES context
     *         changes.
     */
    public Node get_continuous_data() {
        return this.stage.get_continuous_data();
    }
}
