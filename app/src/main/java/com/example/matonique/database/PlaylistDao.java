package com.example.matonique.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

// DAO (Data Access Object) pour acceder à la base de donnée des playlists
@Dao
public interface PlaylistDao {

    // inserer une nouvelle playlist dans la base
    @Insert
    void insert(PlaylistEntity playlist);

    // recuperer toutes les playlists enregistré
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    List<PlaylistEntity> getAllPlaylists();

    // recuperer une playlist par son ID
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    PlaylistEntity getPlaylistById(int playlistId);

    // recuperer une playlist par son chemin de fichier
    @Query("SELECT * FROM playlists WHERE filePath = :filePath")
    PlaylistEntity getPlaylistByPath(String filePath);

    // suprimer une playlist
    @Delete
    void delete(PlaylistEntity playlist);

    // verifier si une playlist existe deja (par son chemin)
    @Query("SELECT COUNT(*) FROM playlists WHERE filePath = :filePath")
    int playlistExists(String filePath);
}

