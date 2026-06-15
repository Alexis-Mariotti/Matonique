package com.example.matonique.fragments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import android.view.View;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.matonique.R;
import com.example.matonique.database.PlaylistDao;
import com.example.matonique.database.PlaylistEntity;
import com.example.matonique.model.Playlist;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Test pour PlaylistFragment
 * On mock la base de donnée pour pas avoir de problemes de thread ou de fichier reel
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = {33})
public class PlaylistFragmentTest {

    @Mock
    private PlaylistDao mockDao;
    
    private AutoCloseable closeable;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testUIInit() {
        try (FragmentScenario<PlaylistFragment> scenario = FragmentScenario.launchInContainer(PlaylistFragment.class, null, R.style.Theme_Matonique)) {
            scenario.onFragment(fragment -> {

                ReflectionHelpers.setField(fragment, "playlistDao", mockDao);
                
                View view = fragment.getView();
                assertNotNull(view);
                assertNotNull(view.findViewById(R.id.recycler_playlist));
                assertNotNull(view.findViewById(R.id.btn_view_playlists));
                assertNotNull(view.findViewById(R.id.btn_find_playlist));
            });
        }
    }

    @Test
    public void testToggleEmptyView() {
        try (FragmentScenario<PlaylistFragment> scenario = FragmentScenario.launchInContainer(PlaylistFragment.class, null, R.style.Theme_Matonique)) {
            scenario.onFragment(fragment -> {
                View txtEmpty = fragment.getView().findViewById(R.id.txt_empty);
                
                // on test si le message s'affiche quand c'est vide
                ReflectionHelpers.callInstanceMethod(fragment, "toggleEmptyView", 
                    ReflectionHelpers.ClassParameter.from(boolean.class, true));
                
                assertNotNull(txtEmpty);
                assertEquals("Le texte vide doit être visible", View.VISIBLE, txtEmpty.getVisibility());
            });
        }
    }

    @Test
    public void testShowPlaylistsList() throws Exception {
        // on test le chargement des playlists depuis la base (sans tricher)
        List<PlaylistEntity> fakeEntities = new ArrayList<>();
        fakeEntities.add(new PlaylistEntity("/tmp/p1.m3u", "Playlist 1"));
        fakeEntities.add(new PlaylistEntity("/tmp/p2.m3u", "Playlist 2"));
        
        when(mockDao.getAllPlaylists()).thenReturn(fakeEntities);

        try (FragmentScenario<PlaylistFragment> scenario = FragmentScenario.launchInContainer(PlaylistFragment.class, null, R.style.Theme_Matonique)) {
            scenario.onFragment(fragment -> {
                ReflectionHelpers.setField(fragment, "playlistDao", mockDao);
                // on lance le chargement
                ReflectionHelpers.callInstanceMethod(fragment, "showPlaylistsList");
            });

            // on attend la fin du thread de la base de donnée
            Thread.sleep(300);
            org.robolectric.shadows.ShadowLooper.idleMainLooper();

            scenario.onFragment(fragment -> {
                RecyclerView rv = fragment.getView().findViewById(R.id.recycler_playlist);
                // on doit avoir nos 2 playlists affichées
                assertEquals(2, rv.getAdapter().getItemCount());
            });
        }
    }

    @Test
    public void testShowPlaylistContent() throws Exception {
        // on crée un vrai fichier m3u et des musiques pour tester l'affichage du contenu sans tricher
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "MatoniquePlaylist_" + System.currentTimeMillis());
        tempDir.mkdirs();
        
        File music1 = new File(tempDir, "music1.mp3");
        music1.createNewFile();
        File playlistFile = new File(tempDir, "test.m3u");
        
        // on ecrit le chemin de la musique dans le fichier m3u
        FileWriter writer = new FileWriter(playlistFile);
        writer.write(music1.getAbsolutePath());
        writer.close();

        try (FragmentScenario<PlaylistFragment> scenario = FragmentScenario.launchInContainer(PlaylistFragment.class, null, R.style.Theme_Matonique)) {
            scenario.onFragment(fragment -> {
                ReflectionHelpers.setField(fragment, "playlistDao", mockDao);
                
                //  cree l'objet playlist manuellement
                List<String> paths = new ArrayList<>();
                paths.add(music1.getAbsolutePath());
                Playlist p = new Playlist(1, "Ma Playlist", playlistFile.getAbsolutePath());
                p.setMusicPaths(paths);

                // on appel la méthode pour afficher le contenu
                ReflectionHelpers.callInstanceMethod(fragment, "showPlaylistContent",
                        ReflectionHelpers.ClassParameter.from(Playlist.class, p));

                RecyclerView rv = fragment.getView().findViewById(R.id.recycler_playlist);
                assertEquals(1, rv.getAdapter().getItemCount());
            });
        } finally {
            music1.delete();
            playlistFile.delete();
            tempDir.delete();
        }
    }
}
