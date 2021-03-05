package svenske.spacedust.gameobject;

import android.view.MotionEvent;

import java.util.List;

/**
 * A generic interface for anything that is interested in use input.
 */
public interface InputReceiver {

    /**
     * Called whenever user input occurs.
     * @param me the MotionEvent describing the user input.
     * @param ignore_idx a list of pointer indices whose input must NOT be responded to. This list
     *                   can be added to to inform other InputReceivers to not respond to a
     *                   particular pointer. I.e., this can be used to "claim" pointers per cycle.
     */
    void input(MotionEvent me, List<Integer> ignore_idx);
}
