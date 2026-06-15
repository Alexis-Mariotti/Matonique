package com.example.matonique.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MusicQueueTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testEmptyConstructor() {
        MusicQueue queue = new MusicQueue();

        assertEquals("Le type par défaut devrait être FOLDER", MusicQueue.QueueType.FOLDER, queue.getType());
        assertEquals("La taille de la file devrait être 0", 0, queue.getSize());
        assertEquals("L'index devrait être -1", -1, queue.getCurrentIndex());
        assertNull("Le chemin actuel devrait être null", queue.getCurrentPath());
        assertFalse("Il ne devrait pas y avoir de suivant", queue.hasNext());
        assertFalse("Il ne devrait pas y avoir de précédent", queue.hasPrevious());
    }

    @Test
    public void testPlaylistConstructor_WithExistingCurrentFile() {
        List<String> playlist = Arrays.asList("/music/track1.mp3", "/music/track2.flac", "/music/track3.wav");
        String currentFile = "/music/track2.flac";

        MusicQueue queue = new MusicQueue(playlist, currentFile);

        assertEquals(MusicQueue.QueueType.PLAYLIST, queue.getType());
        assertEquals("La taille de la queue devrait être 3", 3, queue.getSize());
        assertEquals("L'index courant devrait correspondre à track2.flac (index 1)", 1, queue.getCurrentIndex());
        assertEquals("Le chemin retourné est incorrect", currentFile, queue.getCurrentPath());
    }

    @Test
    public void testPlaylistConstructor_WithMissingCurrentFile() {
        List<String> playlist = Arrays.asList("/music/track1.mp3", "/music/track2.flac");
        String missingCurrentFile = "/music/unknown.mp3";

        MusicQueue queue = new MusicQueue(playlist, missingCurrentFile);

        assertEquals("La taille de la queue devrait être 3 (2 initiaux + 1 ajouté)", 3, queue.getSize());
        assertEquals("L'index courant devrait être le dernier (index 2)", 2, queue.getCurrentIndex());
        assertEquals("Le fichier ajouté devrait être le courant", missingCurrentFile, queue.getCurrentPath());
    }

    @Test
    public void testNavigationLogic() {
        List<String> playlist = Arrays.asList("A.mp3", "B.mp3", "C.mp3");
        MusicQueue queue = new MusicQueue(playlist, "A.mp3"); // Index initial = 0

        assertFalse("Pas de précédent au début", queue.hasPrevious());
        assertTrue("Il y a un suivant", queue.hasNext());

        assertEquals("On passe à B", "B.mp3", queue.getNext());
        assertEquals("L'index est maintenant 1", 1, queue.getCurrentIndex());
        assertTrue("Il y a un précédent", queue.hasPrevious());
        assertTrue("Il y a un suivant", queue.hasNext());

        assertEquals("On passe à C", "C.mp3", queue.getNext());
        assertFalse("Pas de suivant à la fin", queue.hasNext());

        assertNull("On dépasse la fin, retourne null", queue.getNext());

        assertEquals("On recule à B", "B.mp3", queue.getPrevious());
        assertEquals("On recule à A", "A.mp3", queue.getPrevious());

        assertNull("On dépasse le début, retourne null", queue.getPrevious());
    }

    @Test
    public void testFolderInitializationAndFiltering() throws IOException {
        File folder = tempFolder.newFolder("testMusic");

        File fileC = new File(folder, "c_track.flac");
        File fileA = new File(folder, "a_track.mp3");
        File notMusic = new File(folder, "document.txt"); // Ne devrait pas être pris en compte
        File fileB = new File(folder, "b_track.wav");

        fileC.createNewFile();
        fileA.createNewFile();
        notMusic.createNewFile();
        fileB.createNewFile();

        MusicQueue queue = new MusicQueue(folder, fileB.getAbsolutePath());

        assertEquals(MusicQueue.QueueType.FOLDER, queue.getType());
        assertEquals("Le fichier .txt doit être ignoré, taille attendue = 3", 3, queue.getSize());

        assertEquals("Après tri, fileB devrait être à l'index 1", 1, queue.getCurrentIndex());

        queue.getPrevious();
        assertEquals("Le premier fichier devrait être A", fileA.getAbsolutePath(), queue.getCurrentPath());
    }

    @Test
    public void testSettersResetState() {
        List<String> playlist = Arrays.asList("1.mp3", "2.mp3");
        MusicQueue queue = new MusicQueue(playlist, "2.mp3");

        List<String> newPlaylist = Arrays.asList("A.mp3");
        queue.setFromPlaylist(newPlaylist, "A.mp3");

        assertEquals(MusicQueue.QueueType.PLAYLIST, queue.getType());
        assertEquals("La nouvelle taille devrait être 1", 1, queue.getSize());
        assertEquals("L'index devrait être remis à 0", 0, queue.getCurrentIndex());
        assertEquals("Le chemin actuel devrait être A.mp3", "A.mp3", queue.getCurrentPath());
    }
}