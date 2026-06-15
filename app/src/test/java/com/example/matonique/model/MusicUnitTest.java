package com.example.matonique.model;

import static org.junit.Assert.assertEquals;

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
        // on instancie sans constructeur pour tester la methode privee
        Music music = ReflectionHelpers.newInstance(Music.class);
        
        String result = ReflectionHelpers.callInstanceMethod(music, "valueOrUnknown",
                ReflectionHelpers.ClassParameter.from(String.class, "Test"));
        assertEquals("Test", result);
        
        result = ReflectionHelpers.callInstanceMethod(music, "valueOrUnknown",
                ReflectionHelpers.ClassParameter.from(String.class, null));
        assertEquals("Unknown", result);
    }

    @Test
    public void testFormatFileSize() {
        // test de la methode statique de formatage
        String result = ReflectionHelpers.callStaticMethod(Music.class, "formatFileSize",
                ReflectionHelpers.ClassParameter.from(long.class, 1024L));
        assertEquals("1.0 KB", result);

        result = ReflectionHelpers.callStaticMethod(Music.class, "formatFileSize",
                ReflectionHelpers.ClassParameter.from(long.class, 500L));
        assertEquals("500 B", result);
    }
}
