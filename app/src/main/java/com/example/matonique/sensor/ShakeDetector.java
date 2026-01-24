package com.example.matonique.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
// Event listener pour detecter les secousses de l'appareil
// implemente SensorEventListener pour recevoir les mises à jour des capteurs
// Utilise l'accelerometre pour detecter quand le telephone est secoué
public class ShakeDetector implements SensorEventListener {
    // minimum de detection
    private static final float SHAKE_THRESHOLD = 15.0f;
    // cooldown entre les detections
    private static final int SHAKE_TIME_WINDOW = 500;

    // temps de la derniere detection, init à
    private long lastShakeTime = 0;
    private OnShakeListener listener;

    public interface OnShakeListener {
        void onShake();
    }

    public void setOnShakeListener(OnShakeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // calcul de l'accélération totale
            float acceleration = (float) Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;

            long currentTime = System.currentTimeMillis();

            // vérifier si l'accélération dépasse le seuil
            if (acceleration > SHAKE_THRESHOLD) {
                // vérifier la fenêtre de temps pour éviter les multiples détections
                if (currentTime - lastShakeTime > SHAKE_TIME_WINDOW) {
                    lastShakeTime = currentTime;

                    if (listener != null) {
                        listener.onShake();
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // pas besoin de gérer les changements de précision
    }
}
