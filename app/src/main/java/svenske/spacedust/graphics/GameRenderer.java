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
import svenske.spacedust.utils.Global;
import svenske.spacedust.utils.Node;

// Renders on a GLSurfaceView. Is the connecting piece between Android and the current Stage.
public class GameRenderer implements GLSurfaceView.Renderer {

    // The current stage
    private Stage stage;

    // Timekeeping attributes
    private long last_cycle = -1;  // This time since the last cycle (ms)
    private int acc_frame = 0;     // Count frames
    private float acc_time = 0;    // Count seconds
    private final float FPS_report_interval = 0.5f; // Interval in s between FPS logs

    // Whenever the surface is created, initialize GL and the current Stage.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d("spdt/gamerenderer", "surface (re-)created");
        init_gl();
        init_stage();
    }

    // Initializes GL after the context was created in GameView.
    private void init_gl() {

        // Set background frame color
        GLES20.glClearColor(Global.CLEAR_COLOR[0], Global.CLEAR_COLOR[1], Global.CLEAR_COLOR[2],
                Global.CLEAR_COLOR[3]);

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

    // Called every redraw. Performs updates and renders.
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
        Global.VIEWPORT_WIDTH = width;
        Global.VIEWPORT_HEIGHT = height;
        GLES20.glViewport(0, 0, width, height);
        this.stage.resized();
    }

    /**
     * Called whenever any input occurs.
     * @return if the input was handled
     */
    public boolean input(MotionEvent me) { return this.stage.input(me); }

    // Performs timekeeping calculations and updates the current Stage.
    private void update() {

        // Timekeeping
        long now = System.currentTimeMillis();
        float dt = 0;
        if (last_cycle != -1) dt = (float) (now - last_cycle) / 1000f; // seconds
        last_cycle = now;
        this.fps(dt);

        // Update stage
        this.stage.update(dt);
    }

    /**
     * Performs FPS calculations. Every time a specified interval of time passes, FPS is logged
     * in the verbose channel, and sent to the Stage.
     */
    private void fps(float dt) {
        if (this.FPS_report_interval < 0) return;
        this.acc_frame++;
        this.acc_time += dt;
        if (this.acc_time > this.FPS_report_interval) {
            float fps = ((float)this.acc_frame / this.acc_time);
            Log.v("spdt/gamerenderer", "FPS: " + fps);
            this.stage.fps_update(fps);
            this.acc_frame = 0;
            this.acc_time = 0f;
        }
    }

    // Renders the current Stage
    private void render() { this.stage.render(); }

    // Returns continuous data from the Stage to be saved across OpenGL ES context changes.
    public Node get_continuous_data() {
        return this.stage == null ? null : this.stage.get_continuous_data();
    }
}
