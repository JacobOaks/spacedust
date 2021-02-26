package svenske.spacedust.graphics;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import svenske.spacedust.utils.Node;

/**
 * The GLSurfaceView used to render OpenGL onto. Basically, how OpenGL ES interfaces with Android.
 */
public class GameView extends GLSurfaceView {

    private final GameRenderer game_renderer;
    private final ScaleGestureDetector sgd;

    /**
     * Creates the OpenGL ES context and the renderer.
     * @param context the activity this GameView is on.
     */
    public GameView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context
        this.setEGLContextClientVersion(2);

        // Create and set the game renderer for drawing on this view
        this.game_renderer = new GameRenderer();
        this.setRenderer(this.game_renderer);

        // Create scale detector
        this.sgd = new ScaleGestureDetector(context, new ScaleListener());
    }

    private static boolean respond_no_further = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.sgd.onTouchEvent(event);
        if (!this.sgd.isInProgress()) respond_no_further = false;
        if (!respond_no_further) this.game_renderer.other_input(event);
        return true; // generally the android API just wants to know if the input was processed.
    }

    /**
     * @return any important continuous data to be used again if the OpenGL ES context changes.
     */
    public Node get_continuous_data() {
        return this.game_renderer.get_continuous_data();
    }

    public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            respond_no_further = game_renderer.scale_input(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
            return true;
        }
    }
}
