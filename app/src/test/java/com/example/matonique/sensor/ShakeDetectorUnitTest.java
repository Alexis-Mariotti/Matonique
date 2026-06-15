package com.example.matonique.sensor;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.robolectric.util.ReflectionHelpers;

/**
 * Test pour ShakeDetector.
 * On simule une secousse manuelement.
 */
public class ShakeDetectorUnitTest {

    private boolean shakeCalled = false;

    @Test
    public void testOnShake() {
        ShakeDetector detector = new ShakeDetector();
        
        // on met un listener qui change notre boolean
        detector.setOnShakeListener(() -> shakeCalled = true);

        // on va chercher le listener privé et on l'appel
        ShakeDetector.OnShakeListener listener = ReflectionHelpers.getField(detector, "listener");
        if (listener != null) {
            listener.onShake();
        }

        assertTrue("Le listener doit être appelé", shakeCalled);
    }
}
