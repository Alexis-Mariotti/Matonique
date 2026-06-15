package com.example.matonique.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlaylistTest {

    @Test
    public void testPlaylistCreationAndGetters() {
        int expectedId = 1;
        String expectedName = "Rock Classics";
        String expectedPath = "/music/playlists/rock.m3u";

        Playlist iut = new Playlist(expectedId, expectedName, expectedPath);

        assertEquals("L'ID retourné est incorrect", expectedId, iut.getId());
        assertEquals("Le nom retourné est incorrect", expectedName, iut.getName());
        assertEquals("Le chemin retourné est incorrect", expectedPath, iut.getFilePath());

        assertNull("La liste de musiques devrait être null à l'initialisation", iut.getMusicPaths());
    }

    @Test
    public void testSetters() {
        Playlist iut = new Playlist(0, "Temp", "");

        int newId = 42;
        String newName = "Jazz Vibes";
        String newPath = "/music/playlists/jazz.m3u";

        iut.setId(newId);
        iut.setName(newName);
        iut.setFilePath(newPath);

        assertEquals("Le setter de l'ID ne fonctionne pas", newId, iut.getId());
        assertEquals("Le setter du nom ne fonctionne pas", newName, iut.getName());
        assertEquals("Le setter du chemin ne fonctionne pas", newPath, iut.getFilePath());
    }

    @Test
    public void testGetMusicCount_WhenMusicPathsIsNull() {
        Playlist iut = new Playlist(1, "Empty Playlist", "/path.m3u");

        assertEquals("Le compte doit être de 0 si la liste de musiques est null", 0, iut.getMusicCount());
    }

    @Test
    public void testGetMusicCount_WhenMusicPathsIsEmpty() {
        Playlist iut = new Playlist(2, "Empty List", "/empty.m3u");

        iut.setMusicPaths(new ArrayList<>());

        assertEquals("Le compte doit être de 0 si la liste est vide", 0, iut.getMusicCount());
    }

    @Test
    public void testSetMusicPathsAndGetMusicCount() {
        Playlist iut = new Playlist(3, "My Mix", "/mix.m3u");

        List<String> tracks = Arrays.asList(
                "/music/track1.mp3",
                "/music/track2.mp3",
                "/music/track3.mp3"
        );

        iut.setMusicPaths(tracks);

        assertNotNull("La liste de musiques ne devrait pas être null", iut.getMusicPaths());
        assertEquals("La liste devrait contenir exactement les éléments ajoutés", tracks, iut.getMusicPaths());
        assertEquals("La méthode getMusicCount doit retourner la taille exacte de la liste", 3, iut.getMusicCount());
    }
}