package com.example.matonique.activity;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.matonique.R;
import com.example.matonique.fragments.MusicListFragment;
import com.example.matonique.fragments.MusicPlayFragment;
import com.example.matonique.fragments.PlaylistFragment;
import com.example.matonique.fragments.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

/**
 * Test pour MainActivity.
 * On verifi la navigation et le lancement par intent.
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = {33})
public class MainActivityUnitTest {

    @Test
    public void testNavigation() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                BottomNavigationView bottomNav = activity.findViewById(R.id.bottom_navigation);
                
                // par defaut
                assertTrue(activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof MusicListFragment);

                // on clic sur reglages
                bottomNav.setSelectedItemId(R.id.nav_settings);
                ShadowLooper.idleMainLooper(); // on attend la transaction
                assertTrue(activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof SettingsFragment);
                
                // on clic sur playlist
                bottomNav.setSelectedItemId(R.id.nav_playlist);
                ShadowLooper.idleMainLooper();
                assertTrue(activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof PlaylistFragment);
            });
        }
    }

    @Test
    public void testOpenFromNotification() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("OPEN_MUSIC_PLAY", true);
        
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                ShadowLooper.idleMainLooper();
                Fragment f = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                assertTrue("Doit ouvrir le fragment de lecture", f instanceof MusicPlayFragment);
            });
        }
    }
}
