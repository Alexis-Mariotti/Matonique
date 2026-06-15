package com.example.matonique.fragments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.view.View;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.matonique.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.util.ReflectionHelpers;

import java.io.File;

/**
 * Tests pour MusicListFragment.
 * On verifi que le chargement des dossiers et la navigation marchent bien.
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = {33})
public class MusicListFragmentTest {

    @Test
    public void testUIInitialization() {
        // on lance le fragment pour voir si les vues s'affiche
        try (FragmentScenario<MusicListFragment> scenario = FragmentScenario.launchInContainer(MusicListFragment.class, null, R.style.Theme_Matonique)) {
            scenario.onFragment(fragment -> {
                View view = fragment.getView();
                assertNotNull("La vue du fragment ne doit pas être nulle", view);
                
                assertNotNull(view.findViewById(R.id.recycler_music));
                assertNotNull(view.findViewById(R.id.txt_current_path));
                assertNotNull(view.findViewById(R.id.buttonBack));
            });
        }
    }

    @Test
    public void testNavigateToMusicDir() {
        try (FragmentScenario<MusicListFragment> scenario = FragmentScenario.launchInContainer(MusicListFragment.class, null, R.style.Theme_Matonique)) {
            scenario.onFragment(fragment -> {
                // on appel le retour au dossier musique
                ReflectionHelpers.callInstanceMethod(fragment, "navigateToMusicDir");
                
                File currentDir = ReflectionHelpers.getField(fragment, "currentDirectory");
                assertNotNull(currentDir);
            });
        }
    }

    @Test
    public void testNavigateUp() {
        try (FragmentScenario<MusicListFragment> scenario = FragmentScenario.launchInContainer(MusicListFragment.class, null, R.style.Theme_Matonique)) {
            scenario.onFragment(fragment -> {
                File currentDir = new File("/storage/emulated/0/Music/SubDir");
                ReflectionHelpers.setField(fragment, "currentDirectory", currentDir);
                
                // on remonte d'un cran
                ReflectionHelpers.callInstanceMethod(fragment, "navigateUp");
                
                File newDir = ReflectionHelpers.getField(fragment, "currentDirectory");
                assertEquals("/storage/emulated/0/Music", newDir.getAbsolutePath());
            });
        }
    }

    @Test
    public void testDisplayAllChild() throws Exception {
        // dossier temporaire
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "MatoniqueTest_" + System.currentTimeMillis());
        tempDir.mkdirs();
        new File(tempDir, "song1.mp3").createNewFile();
        new File(tempDir, "song2.wav").createNewFile();
        new File(tempDir, "SubFolder").mkdir();
        new File(tempDir, "image.png").createNewFile(); // ne doit pas être compté car pas de la musique

        try (FragmentScenario<MusicListFragment> scenario = FragmentScenario.launchInContainer(MusicListFragment.class, null, R.style.Theme_Matonique)) {
            scenario.onFragment(fragment -> {
                ReflectionHelpers.callInstanceMethod(fragment, "loadDirectory",
                        ReflectionHelpers.ClassParameter.from(File.class, tempDir));
            });
            // attend la methode asynchrone
            Thread.sleep(1000);
            // maj de l'ui
            ShadowLooper.idleMainLooper();

            scenario.onFragment(fragment -> {
                RecyclerView recyclerView = fragment.getView().findViewById(R.id.recycler_music);
                assertEquals(3, recyclerView.getAdapter().getItemCount());
            });
        } finally {
            tempDir.delete();
        }
    }
}
