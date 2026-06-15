package com.example.matonique.fragments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.matonique.R;
import com.example.matonique.model.Music;
import com.example.matonique.sensor.ShakeDetector;
import com.example.matonique.service.MusicPlayService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

/**
 * Tests unitaires de MusicPlayFragment avec Robolectric et mockito
 * On vérifie que l'interface réagi correctement aux actions de l'utilisateur et aux changements du service
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = {33})
public class MusicPlayFragmentTest {

    @Mock
    private MusicPlayService mockMusicService;

    @Mock
    private Music mockMusic;

    private AutoCloseable closeable;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        
        // musique mocké
        when(mockMusic.getTitle()).thenReturn("Musique de Test");
        when(mockMusic.getArtist()).thenReturn("Artiste Test");
        when(mockMusic.getAlbum()).thenReturn("Album Test");
    }

    @After
    public void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    @Test
    public void testNewInstance_Arguments() {
        String filePath = "/music/test.mp3";
        MusicPlayFragment fragment = MusicPlayFragment.newInstance(filePath);
        
        Bundle args = fragment.getArguments();
        assertNotNull(args);
        assertEquals(filePath, args.getString("FILE_PATH"));
    }

    @Test
    public void testUpdateUI_DisplaysMusicInfo() {
        try (FragmentScenario<MusicPlayFragment> scenario = FragmentScenario.launchInContainer(MusicPlayFragment.class, null, R.style.Theme_Matonique)) {
            scenario.onFragment(fragment -> {
                View fragmentView = fragment.getView();
                assertNotNull("La vue du fragment ne doit pas être nulle", fragmentView);
                
                // injecte la musique et on force la maj
                ReflectionHelpers.setField(fragment, "music", mockMusic);
                ReflectionHelpers.callInstanceMethod(fragment, "updateUI");

                TextView txtTitle = fragmentView.findViewById(R.id.txt_title);
                TextView txtArtist = fragmentView.findViewById(R.id.txt_artist);
                
                assertEquals("Musique de Test", txtTitle.getText().toString());
                assertEquals("Artiste Test", txtArtist.getText().toString());
            });
        }
    }

    @Test
    public void testPlayPauseButton_InteractionWithService() {
        try (FragmentScenario<MusicPlayFragment> scenario = FragmentScenario.launchInContainer(MusicPlayFragment.class, null, R.style.Theme_Matonique)) {
            scenario.onFragment(fragment -> {
                ReflectionHelpers.setField(fragment, "musicService", mockMusicService);
                ReflectionHelpers.setField(fragment, "isBound", true);
                
                View view = fragment.getView();
                assertNotNull(view);
                ImageButton btnPlayPause = view.findViewById(R.id.btn_play_pause);

                when(mockMusicService.isPlaying()).thenReturn(false);
                btnPlayPause.performClick();
                verify(mockMusicService).play();

                when(mockMusicService.isPlaying()).thenReturn(true);
                btnPlayPause.performClick();
                verify(mockMusicService).pause();
            });
        }
    }

    @Test
    public void testUIButtonState_WhenServiceChanges() {
        try (FragmentScenario<MusicPlayFragment> scenario = FragmentScenario.launchInContainer(MusicPlayFragment.class, null, R.style.Theme_Matonique)) {
            scenario.onFragment(fragment -> {
                ReflectionHelpers.setField(fragment, "musicService", mockMusicService);
                ReflectionHelpers.setField(fragment, "isBound", true);
                
                View view = fragment.getView();
                assertNotNull(view);
                ImageButton btnPlayPause = view.findViewById(R.id.btn_play_pause);

                // etat initial
                ReflectionHelpers.setField(fragment, "lastPlayingState", false);

                // modifie l'etat du service pour simuler l'usage d'une notification ou autre et verifier que le fragment se met biens a jour

                when(mockMusicService.isPlaying()).thenReturn(true);
                ReflectionHelpers.callInstanceMethod(fragment, "updatePlayPauseButton");

                int resId = ReflectionHelpers.getField(btnPlayPause, "mResource");
                assertEquals(R.drawable.icon_pause, resId);

                when(mockMusicService.isPlaying()).thenReturn(false);
                ReflectionHelpers.callInstanceMethod(fragment, "updatePlayPauseButton");
                
                resId = ReflectionHelpers.getField(btnPlayPause, "mResource");
                assertEquals(R.drawable.icon_play, resId);
            });
        }
    }

    @Test
    public void testNavigationButtons_CallsService() {
        try (FragmentScenario<MusicPlayFragment> scenario = FragmentScenario.launchInContainer(MusicPlayFragment.class, null, R.style.Theme_Matonique)) {
            scenario.onFragment(fragment -> {
                ReflectionHelpers.setField(fragment, "musicService", mockMusicService);
                ReflectionHelpers.setField(fragment, "isBound", true);

                View view = fragment.getView();
                assertNotNull(view);
                
                // bouton Suivant
                view.findViewById(R.id.btn_next).performClick();
                verify(mockMusicService).playNext();

                // bouton Précédent
                view.findViewById(R.id.btn_previous).performClick();
                verify(mockMusicService).playPrevious();
            });
        }
    }

    @Test
    public void testShakeDevice_TriggersNextMusic() {
        // on test la fonctionalité qui passe la musique suivante quand on secoue le telephone
        try (FragmentScenario<MusicPlayFragment> scenario = FragmentScenario.launchInContainer(MusicPlayFragment.class, null, R.style.Theme_Matonique)) {
            scenario.onFragment(fragment -> {
                ReflectionHelpers.setField(fragment, "musicService", mockMusicService);
                
                // On recupere le ShakeDetector
                ShakeDetector detector = ReflectionHelpers.getField(fragment, "shakeDetector");
                assertNotNull("Le ShakeDetector doit être initialisé", detector);
                
                ShakeDetector.OnShakeListener listener = ReflectionHelpers.getField(detector, "listener");
                assertNotNull("Le listener de secousse doit être configuré", listener);
                
                // simule une secousse pour passer à la pmusique suivante
                listener.onShake();

                verify(mockMusicService).playNext();
            });
        }
    }
}
