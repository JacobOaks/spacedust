package svenske.spacedust.gameobject;

import svenske.spacedust.graphics.LightSource;

// An interface for light-emitting objects
public interface LightEmitter {

    // Return the light source
    LightSource get_light();

    // Where the source of light is
    float[] get_pos();
}
