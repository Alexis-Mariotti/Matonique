package com.example.matonique.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Entité pour sauvegarder les playlists dans la base de données
// on sauvegarde seulement le chemin du fichier m3u
@Entity(tableName = "playlists")
public class PlaylistEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    // chemin absolu du fichier m3u de la playlist
    private String filePath;

    // nom de la playlist (extrait du nom du fichier)
    private String name;

    public PlaylistEntity(String filePath, String name) {
        this.filePath = filePath;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

