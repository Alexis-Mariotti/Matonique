package com.example.matonique.fragments;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Switch;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.matonique.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/**
 * Tests pour le fragment des reglages
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = {33})
public class SettingsFragmentTest {

    @Test
    public void testCrossfadeSwitchSavesPreference() {
        try (FragmentScenario<SettingsFragment> scenario = FragmentScenario.launchInContainer(SettingsFragment.class, null, R.style.Theme_Matonique)) {
            scenario.onFragment(fragment -> {
                View view = fragment.getView();
                assertNotNull(view);
                
                Switch swCrossfade = view.findViewById(R.id.switch_crossfade);
                assertNotNull(swCrossfade);
                
                // on simule le clic sur le switch
                swCrossfade.setChecked(true);
                
                // on verifi si ca a bien ete sauvegarder dans les shared preferences
                SharedPreferences prefs = fragment.requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
                assertTrue("La preference crossfade doit être à true", prefs.getBoolean("crossfade_enabled", false));
            });
        }
    }
}
