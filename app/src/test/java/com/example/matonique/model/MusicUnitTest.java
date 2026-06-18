package com.example.matonique.model;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

/**
 * Test pour le model Music.
 * On verifi les utilitaires internes.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = {33})
public class MusicUnitTest {

    @Test
    public void testValueOrUnknown() {
        // on mock la classe pour pouvoir appeler la methode privee sans fichier mp3
        Music music = mock(Music.class);
        
        // on verifi si la methode privee marche
        String result = ReflectionHelpers.callInstanceMethod(music, "valueOrUnknown",
                ReflectionHelpers.ClassParameter.from(String.class, "Test"));
        assertEquals("Test", result);
        
        result = ReflectionHelpers.callInstanceMethod(music, "valueOrUnknown",
                ReflectionHelpers.ClassParameter.from(String.class, null));
        assertEquals("Unknown", result);
    }

    @Test
    public void testFormatFileSize() {
        String result = ReflectionHelpers.callStaticMethod(Music.class, "formatFileSize",
                ReflectionHelpers.ClassParameter.from(long.class, 1024L));
        assertEquals("1.0 KB", result);
    }
}
