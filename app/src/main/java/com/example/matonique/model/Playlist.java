package com.example.matonique.model;

import java.util.List;

// Modele représentant une playlist avec son nom et la liste des musiques
public class Playlist {
    private int id;
    private String name;
    private String filePath; // chemin du fichier m3u
    private List<String> musicPaths; // liste des chemins des musiques dans la playlist

    public Playlist(int id, String name, String filePath) {
        this.id = id;
        this.name = name;
        this.filePath = filePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public List<String> getMusicPaths() {
        return musicPaths;
    }

    public void setMusicPaths(List<String> musicPaths) {
        this.musicPaths = musicPaths;
    }

    // retourner le nombre de musiques dans la playlist
    public int getMusicCount() {
        return musicPaths != null ? musicPaths.size() : 0;
    }
}

