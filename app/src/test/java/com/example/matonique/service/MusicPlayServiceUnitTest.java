package com.example.matonique.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.matonique.model.Music;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.annotation.Config;

/**
 * Test pour le service de musique.
 * On verifi l'etat du lecteur.
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = {33})
public class MusicPlayServiceUnitTest {

    @Test
    public void testServiceState() {
        ServiceController<MusicPlayService> controller = Robolectric.buildService(MusicPlayService.class);
        MusicPlayService service = controller.create().get();
        
        assertNotNull(service);
        assertFalse("Ne doit pas jouer au debut", service.isPlaying());
        assertNull("Pas de musique chargee", service.getCurrentMusic());
        
        controller.destroy();
    }

    @Test
    public void testLoopingState() {
        ServiceController<MusicPlayService> controller = Robolectric.buildService(MusicPlayService.class);
        MusicPlayService service = controller.create().get();
        
        service.setLooping(true);
        assertNotNull(service); // evite warning
        // on verifi si le flag est bien mis
        org.junit.Assert.assertTrue(service.isLooping());
        
        controller.destroy();
    }
}
