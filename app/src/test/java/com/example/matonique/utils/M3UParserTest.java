package com.example.matonique.utils;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class M3UParserTest {

    private Path tempDir;
    private File musicDir;
    private File m3uDir;

    @Before
    public void setUp() throws IOException {
        // Créer un dossier temporaire pour les tests
        tempDir = Files.createTempDirectory("m3u_test");
        musicDir = new File(tempDir.toFile(), "music");
        m3uDir = new File(tempDir.toFile(), "playlists");

        musicDir.mkdirs();
        m3uDir.mkdirs();
    }

    @After
    public void tearDown() throws IOException {
        // Nettoyer les fichiers temporaires
        deleteRecursively(tempDir.toFile());
    }

    // Méthodes utilitaires 

    private File createMusicFile(String fileName) throws IOException {
        File file = new File(musicDir, fileName);
        file.createNewFile();
        return file;
    }

    private File createFileWithContent(String fileName, String content) throws IOException {
        File file = new File(musicDir, fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }

    private File createM3UFile(String m3uFileName, String content) throws IOException {
        File m3uFile = new File(m3uDir, m3uFileName);
        try (FileWriter writer = new FileWriter(m3uFile)) {
            writer.write(content);
        }
        return m3uFile;
    }

    private File createM3UFile(String m3uFileName, File musicFile) throws IOException {
        return createM3UFile(m3uFileName, musicFile.getAbsolutePath());
    }

    private File createM3UFile(String m3uFileName, String content1, String content2) throws IOException {
        return createM3UFile(m3uFileName, content1 + "\n" + content2);
    }

    // methode uttilitaire pour supprimer les fichiers et dossiers créés pendant les tests
    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteRecursively(f);
                }
            }
        }
        file.delete();
    }

    //  Tests parsePlaylist 

    @Test
    public void fileDoesNotExist_returnsEmptyList() {
        String nonExistentPath = m3uDir.getAbsolutePath() + "/nonexistent.m3u";
        List<String> result = M3UParser.parsePlaylist(nonExistentPath);

        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
    }

    @Test
    public void emptyFile_returnsEmptyList() throws IOException {
        File m3uFile = new File(m3uDir, "empty.m3u");
        m3uFile.createNewFile();

        List<String> result = M3UParser.parsePlaylist(m3uFile.getAbsolutePath());

        assertEquals(0, result.size());
    }

    @Test
    public void absolutePathToValidMusicFile_returnsPath() throws IOException {
        File musicFile = createMusicFile("test.mp3");
        File m3uFile = createM3UFile("absolute.m3u", musicFile.getAbsolutePath());

        List<String> result = M3UParser.parsePlaylist(m3uFile.getAbsolutePath());

        assertEquals(1, result.size());
        assertEquals(musicFile.getAbsolutePath(), result.get(0));
    }

    @Test
    public void supportedFormats_mp3_added() throws IOException {
        File musicFile = createMusicFile("test.mp3");
        File m3uFile = createM3UFile("mp3.m3u", musicFile.getAbsolutePath());

        List<String> result = M3UParser.parsePlaylist(m3uFile.getAbsolutePath());

        assertEquals(1, result.size());
    }

    @Test
    public void supportedFormats_flac_added() throws IOException {
        File musicFile = createMusicFile("test.flac");
        File m3uFile = createM3UFile("flac.m3u", musicFile.getAbsolutePath());

        List<String> result = M3UParser.parsePlaylist(m3uFile.getAbsolutePath());

        assertEquals(1, result.size());
    }

    @Test
    public void supportedFormats_wav_added() throws IOException {
        File musicFile = createMusicFile("test.wav");
        File m3uFile = createM3UFile("wav.m3u", musicFile.getAbsolutePath());

        List<String> result = M3UParser.parsePlaylist(m3uFile.getAbsolutePath());

        assertEquals(1, result.size());
    }

    @Test
    public void parsedPlaylist_multipleSupportedFormats_allAdded() throws IOException {
        File music1 = createMusicFile("song1.mp3");
        File music2 = createMusicFile("song2.flac");
        File music3 = createMusicFile("song3.wav");

        File m3uFile = createM3UFile("multiple_formats.m3u",
            music1.getAbsolutePath() + "\n" +
            music2.getAbsolutePath() + "\n" +
            music3.getAbsolutePath());

        List<String> result = M3UParser.parsePlaylist(m3uFile.getAbsolutePath());

        assertEquals(3, result.size());
        assertTrue(result.contains(music1.getAbsolutePath()));
        assertTrue(result.contains(music2.getAbsolutePath()));
        assertTrue(result.contains(music3.getAbsolutePath()));
    }

    @Test
    public void unsupportedFormat_notAdded() throws IOException {
        File unsupportedFile = createFileWithContent("test.txt", "");
        File m3uFile = createM3UFile("unsupported.m3u", unsupportedFile.getAbsolutePath());

        List<String> result = M3UParser.parsePlaylist(m3uFile.getAbsolutePath());

        assertEquals(0, result.size());
    }

    @Test
    public void multipleMusicFiles_allValid_returnsAllPaths() throws IOException {
        // on teste avec le meme format de fichiers pour se concentrer sur l'objectif du test
        File music1 = createMusicFile("song1.mp3");
        File music2 = createMusicFile("song2.mp3");
        File music3 = createMusicFile("song3.mp3");

        File m3uFile = createM3UFile("multiple.m3u",
            music1.getAbsolutePath() + "\n" +
            music2.getAbsolutePath() + "\n" +
            music3.getAbsolutePath());

        List<String> result = M3UParser.parsePlaylist(m3uFile.getAbsolutePath());

        assertEquals(3, result.size());
        assertTrue(result.contains(music1.getAbsolutePath()));
        assertTrue(result.contains(music2.getAbsolutePath()));
        assertTrue(result.contains(music3.getAbsolutePath()));
    }

    @Test
    public void musicFileDoesNotExist_skipped() throws IOException {
        File m3uFile = createM3UFile("missing.m3u",
            musicDir.getAbsolutePath() + "/nonexistent.mp3");

        List<String> result = M3UParser.parsePlaylist(m3uFile.getAbsolutePath());

        assertEquals(0, result.size());
    }

    @Test
    public void mixedValidAndInvalidFiles_onlyValidAdded() throws IOException {
        File validMusic = createMusicFile("valid.mp3");
        File invalidType = createFileWithContent("invalid.txt", "");
        File nonExistentMusic = new File(musicDir, "ghost.mp3");

        File m3uFile = createM3UFile("mixed.m3u",
            validMusic.getAbsolutePath() + "\n" +
            invalidType.getAbsolutePath() + "\n" +
            nonExistentMusic.getAbsolutePath());

        List<String> result = M3UParser.parsePlaylist(m3uFile.getAbsolutePath());

        assertEquals(1, result.size());
        assertEquals(validMusic.getAbsolutePath(), result.get(0));
    }

    //  Tests extractPlaylistName 

    @Test
    public void extractPlaylistName_m3uExtension_removesExtension() {
        String result = M3UParser.extractPlaylistName("/path/to/myplaylist.m3u");
        assertEquals("myplaylist", result);
    }

    @Test
    public void extractPlaylistName_m3u8Extension_removesExtension() {
        String result = M3UParser.extractPlaylistName("/path/to/myplaylist.m3u8");
        assertEquals("myplaylist", result);
    }

    @Test
    public void extractPlaylistName_uppercase_removesExtension() {
        String result = M3UParser.extractPlaylistName("/path/to/myplaylist.M3U");
        assertEquals("myplaylist", result);
    }

    @Test
    public void extractPlaylistName_mixedCase_removesExtension() {
        String result = M3UParser.extractPlaylistName("/path/to/myplaylist.M3u");
        assertEquals("myplaylist", result);
    }

    @Test
    public void extractPlaylistName_multipleDotsInName_onlyRemovesExtension() {
        String result = M3UParser.extractPlaylistName("/path/to/my.playlist.m3u");
        assertEquals("my.playlist", result);
    }

    @Test
    public void extractPlaylistName_emptyFileName_returnsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            M3UParser.extractPlaylistName("");
        });

    }

}