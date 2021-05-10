package svenske.spacedust.gameobject;

import svenske.spacedust.graphics.LightSource;

/**
 * An interface for an object that emits light. That is, an object that has a light source that can
 * be sent to a shader program for lighting calculations.
 */
public interface LightEmitter {

    // Returns the light source
    LightSource get_light();

    // Returns where the source of light is
    float[] get_pos();
}
