package svenske.spacedust;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import svenske.spacedust.graphics.GameView;
import svenske.spacedust.stages.StageName;
import svenske.spacedust.utils.Node;

/**
 * The sole Activity used for the app.
 */
public class GameActivity extends Activity {

    // This is the stage that will always be loaded first when the app starts up.
    private static final StageName START_STAGE = StageName.WORLD_STAGE;

    // This is the current stage
    public static StageName current_stage = START_STAGE;

    // Any continuous data to be transferred across OpenGL ES context changes
    private static Node continuous_data;

    // Static app resources that can be used app-wide
    public static Resources app_resources;

    // The OpenGLSurfaceView drawn upon by a GameRenderer, and displayed on this Activity.
    private GameView gl_view;

    /**
     * Whenever this activity is created, the GameView is created and made to be the content view of
     * this GameActivity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GameActivity.app_resources = this.getResources();
        this.gl_view = new GameView(this);
        this.setContentView(this.gl_view);
    }

    /**
     * This ~seems~ to get called anytime there is a threat of the OpenGL ES context being
     * destroyed. Thus I am using this to get any important continuous data from the GameView
     * (which eventually gets it from the current Stage), and saving it to be accessed in the case
     * of a new OpenGL ES context.
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        GameActivity.continuous_data = this.gl_view.get_continuous_data();
        Log.d("spdt/gameactivity", "saving continuous data in case of context destroy");
    }

    /**
     * Can be used to retrieve continuous data after a new OpenGL ES context was created, say after
     * an old was one destroyed. This can only be done once.
     * @return null if there was no continuous data from a previous context, or a Node containing
     *         continuous data from a previous context.
     */
    public static Node access_continuous_data() {
        Node cd = GameActivity.continuous_data;
        GameActivity.continuous_data = null;
        return cd;
    }

   // public static Resources get_app_resources() { return GameActivity.resources; }
}
