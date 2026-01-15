package com.example.matonique.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;


// Classe helper pour les methodes utilitaires de Bitmap
public class BitmapUtils {

    // Convertit un Bitmap en tableau de bytes
    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    // Convertit un tableau de bytes en Bitmap
    public static Bitmap byteArrayToBitmap(byte[] byteArray) {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    // redimensionne un Bitmap pour que sa plus grande dimension soit de 500 pixels
    private Bitmap scaleBitmap500(Bitmap bitmap) {
        int maxSize = 500; // pixels
        float ratio = Math.min(
                (float) maxSize / bitmap.getWidth(),
                (float) maxSize / bitmap.getHeight()
        );
        int width = Math.round(ratio * bitmap.getWidth());
        int height = Math.round(ratio * bitmap.getHeight());

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    // redimensionne un Bitmap pour que sa plus grande dimension soit de 700 pixels
    private Bitmap scaleBitmap700(Bitmap bitmap) {
        int maxSize = 700; // pixels
        float ratio = Math.min(
                (float) maxSize / bitmap.getWidth(),
                (float) maxSize / bitmap.getHeight()
        );
        int width = Math.round(ratio * bitmap.getWidth());
        int height = Math.round(ratio * bitmap.getHeight());

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }
}
