package com.example.matonique.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Classe pour gérer une queue de musiques à jouer
 * Permet de naviguer entre les musiques (suivant/précédent)
 * Supporte deux modes : FOLDER (fichiers d'un dossier) et PLAYLIST (liste personnalisée)
 */
public class MusicQueue {

    public enum QueueType {
        FOLDER,    // Musiques d'un dossier
        PLAYLIST   // Playlist personnalisée
    }

    private List<String> musicPaths;  // Liste des chemins de fichiers
    private int currentIndex;          // Index de la musique actuelle
    private QueueType type;            // Type de queue

    // Constructeur pour créer une queue vide
    public MusicQueue() {
        this.musicPaths = new ArrayList<>();
        this.currentIndex = -1;
        this.type = QueueType.FOLDER;
    }

    // Constructeur pour créer une queue à partir d'un dossier
    public MusicQueue(File directory, String currentFilePath) {
        this.type = QueueType.FOLDER;
        this.musicPaths = new ArrayList<>();
        this.currentIndex = -1;

        if (directory != null && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                // Trier les fichiers
                Arrays.sort(files, (f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));

                // Ajouter uniquement les fichiers musicaux
                for (File file : files) {
                    if (!file.isDirectory() && isMusicFile(file)) {
                        musicPaths.add(file.getAbsolutePath());

                        // Trouver l'index du fichier actuel
                        if (file.getAbsolutePath().equals(currentFilePath)) {
                            currentIndex = musicPaths.size() - 1;
                        }
                    }
                }
            }
        }

        // Si le fichier actuel n'a pas été trouvé mais qu'il existe, l'ajouter
        if (currentIndex == -1 && currentFilePath != null) {
            musicPaths.add(currentFilePath);
            currentIndex = musicPaths.size() - 1;
        }
    }

    // Constructeur pour créer une queue à partir d'une playlist
    public MusicQueue(List<String> playlist, String currentFilePath) {
        this.type = QueueType.PLAYLIST;
        this.musicPaths = new ArrayList<>(playlist);
        this.currentIndex = playlist.indexOf(currentFilePath);

        // Si le fichier actuel n'est pas dans la playlist, l'ajouter
        if (currentIndex == -1 && currentFilePath != null) {
            musicPaths.add(currentFilePath);
            currentIndex = musicPaths.size() - 1;
        }
    }

    // Vérifier si le fichier est un fichier musical supporté
    private boolean isMusicFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp3") || name.endsWith(".m4a") ||
                name.endsWith(".flac") || name.endsWith(".wav");
    }

    // Obtenir la musique suivante (retourne null si on est à la fin)
    public String getNext() {
        if (hasNext()) {
            currentIndex++;
            return musicPaths.get(currentIndex);
        }
        return null;
    }

    // Obtenir la musique précédente (retourne null si on est au début)
    public String getPrevious() {
        if (hasPrevious()) {
            currentIndex--;
            return musicPaths.get(currentIndex);
        }
        return null;
    }

    // Vérifier s'il y a une musique suivante
    public boolean hasNext() {
        return currentIndex < musicPaths.size() - 1;
    }

    // Vérifier s'il y a une musique précédente
    public boolean hasPrevious() {
        return currentIndex > 0;
    }

    // Obtenir le chemin de la musique actuelle
    public String getCurrentPath() {
        if (currentIndex >= 0 && currentIndex < musicPaths.size()) {
            return musicPaths.get(currentIndex);
        }
        return null;
    }

    // Obtenir l'index actuel
    public int getCurrentIndex() {
        return currentIndex;
    }

    // Obtenir le nombre total de musiques
    public int getSize() {
        return musicPaths.size();
    }

    // Obtenir le type de queue
    public QueueType getType() {
        return type;
    }

    // Définir une nouvelle queue à partir d'un dossier
    public void setFromFolder(File directory, String currentFilePath) {
        this.type = QueueType.FOLDER;
        this.musicPaths.clear();
        this.currentIndex = -1;

        if (directory != null && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                Arrays.sort(files, (f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));

                for (File file : files) {
                    if (!file.isDirectory() && isMusicFile(file)) {
                        musicPaths.add(file.getAbsolutePath());

                        if (file.getAbsolutePath().equals(currentFilePath)) {
                            currentIndex = musicPaths.size() - 1;
                        }
                    }
                }
            }
        }

        if (currentIndex == -1 && currentFilePath != null) {
            musicPaths.add(currentFilePath);
            currentIndex = musicPaths.size() - 1;
        }
    }

    // Définir une nouvelle queue à partir d'une playlist
    public void setFromPlaylist(List<String> playlist, String currentFilePath) {
        this.type = QueueType.PLAYLIST;
        this.musicPaths = new ArrayList<>(playlist);
        this.currentIndex = playlist.indexOf(currentFilePath);

        if (currentIndex == -1 && currentFilePath != null) {
            musicPaths.add(currentFilePath);
            currentIndex = musicPaths.size() - 1;
        }
    }
}

