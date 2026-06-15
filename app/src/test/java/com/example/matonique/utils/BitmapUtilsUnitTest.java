package com.example.matonique.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

/**
 * Test pour BitmapUtils.
 * On verifi les conversion et le redimentionnement.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = {33})
public class BitmapUtilsUnitTest {

    @Test
    public void testBitmapConversion() {
        Bitmap bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
        
        byte[] bytes = BitmapUtils.bitmapToByteArray(bitmap);
        assertNotNull(bytes);
        
        Bitmap result = BitmapUtils.byteArrayToBitmap(bytes);
        assertNotNull(result);
        assertEquals(10, result.getWidth());
    }

    @Test
    public void testScaling() {
        // on cree une grande image
        Bitmap big = Bitmap.createBitmap(1000, 800, Bitmap.Config.ARGB_8888);
        BitmapUtils utils = new BitmapUtils();
        
        // on appel la methode privee scaleBitmap500
        Bitmap scaled = ReflectionHelpers.callInstanceMethod(utils, "scaleBitmap500",
                ReflectionHelpers.ClassParameter.from(Bitmap.class, big));
        
        assertNotNull(scaled);
        assertTrue("La largeur doit être <= 500", scaled.getWidth() <= 500);
        assertTrue("La hauteur doit être <= 500", scaled.getHeight() <= 500);
    }
}
