package svenske.spacedust.graphics;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import svenske.spacedust.utils.Node;

/**
 * The GLSurfaceView used to render OpenGL onto. Basically, how OpenGL ES interfaces with Android.
 */
public class GameView extends GLSurfaceView {

    // Renders GL onto this view
    private final GameRenderer game_renderer;

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
    }

    // Responds to touch events by passing the input to the renderer.
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.game_renderer.input(event);
    }

    // Returns any important continuous data to be used again if the OpenGL ES context changes.
    public Node get_continuous_data() {
        return this.game_renderer.get_continuous_data();
    }
}
