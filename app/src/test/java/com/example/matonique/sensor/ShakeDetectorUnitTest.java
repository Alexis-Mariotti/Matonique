package com.example.matonique.sensor;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.robolectric.util.ReflectionHelpers;

/**
 * Test pour ShakeDetector.
 * On simule une secousse manuelement et par event.
 */
public class ShakeDetectorUnitTest {

    private boolean shakeCalled = false;

    @Test
    public void testOnShakeManual() {
        ShakeDetector detector = new ShakeDetector();
        detector.setOnShakeListener(() -> shakeCalled = true);

        // appel direct du listener
        ShakeDetector.OnShakeListener listener = ReflectionHelpers.getField(detector, "listener");
        if (listener != null) {
            listener.onShake();
        }
        assertTrue("Le listener doit être appelé", shakeCalled);
    }

    @Test
    public void testThresholdValue() {
        // on verifie la constante de detection
        float threshold = ReflectionHelpers.getStaticField(ShakeDetector.class, "SHAKE_THRESHOLD");
        assertTrue(threshold > 0);
    }
}
