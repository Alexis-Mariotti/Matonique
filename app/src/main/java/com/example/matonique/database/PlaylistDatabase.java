package com.example.matonique.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Base de données Room pour sauvegarder les playlists
// singleton pour avoir qu'une seule instance dans toute l'app
@Database(entities = {PlaylistEntity.class}, version = 1, exportSchema = false)
public abstract class PlaylistDatabase extends RoomDatabase {

    private static PlaylistDatabase instance;

    // methode abstraite pour recuperer le DAO
    public abstract PlaylistDao playlistDao();

    // methode singleton pour recuperer l'instance de la base
    public static synchronized PlaylistDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    PlaylistDatabase.class,
                    "playlist_database"
            )
            .fallbackToDestructiveMigration() // recreer la base si changement de version
            .build();
        }
        return instance;
    }
}

