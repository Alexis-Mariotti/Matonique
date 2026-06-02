package com.example.matonique.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FileItemTest {

    @Test
    public void testFileCreationAndGetters() {
        String expectedPath = "/home/user/documents/rapport.pdf";
        String expectedName = "rapport.pdf";
        boolean expectedIsDirectory = false;

        FileItem iut = new FileItem(expectedPath, expectedName, expectedIsDirectory);

        assertEquals("Le chemin (path) retourné est incorrect", expectedPath, iut.getPath());
        assertEquals("Le nom (name) retourné est incorrect", expectedName, iut.getName());
        assertFalse("L'élément devrait être reconnu comme un fichier, pas un dossier", iut.isDirectory());
    }

    @Test
    public void testDirectoryCreationAndGetters() {
        String expectedPath = "/home/user/documents";
        String expectedName = "documents";
        boolean expectedIsDirectory = true;

        FileItem iut = new FileItem(expectedPath, expectedName, expectedIsDirectory);

        assertEquals("Le chemin (path) retourné est incorrect", expectedPath, iut.getPath());
        assertEquals("Le nom (name) retourné est incorrect", expectedName, iut.getName());
        assertTrue("L'élément devrait être reconnu comme un dossier", iut.isDirectory());
    }

    @Test
    public void testConstructorWithNullPath() {
        assertThrows(IllegalArgumentException.class, () -> {
            new FileItem(null, "name", false);
        });
    }

    @Test
    public void testConstructorWithNullName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new FileItem("directory/path", null, false);
        });
    }

    @Test
    public void testConstructorWithEmptyPath() {
        assertThrows(IllegalArgumentException.class, () -> {
            new FileItem(" \t \n", "name", false);
        });
    }

    @Test
    public void testConstructorWithEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new FileItem("directory/path", "\n\t ", false);
        });
    }

}