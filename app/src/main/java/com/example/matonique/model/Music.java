package com.example.matonique.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import java.io.File;
import java.io.IOException;

public class Music {

    // Chemin fichier
    private final String filePath;

    // on extrait toutes les données du fichier

    // Métadonnées
    private final String title;
    private final String artist;
    private final String album;
    private final String genre;
    private final String year;
    private final String trackNumber;
    private final String composer;
    private final String author;

    // Technique
    private final long durationMs;
    private final long fileSize;

    // Cover
    private final Bitmap cover;

    public Music(String filePath) {
        this.filePath = filePath;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        try {
            retriever.setDataSource(filePath);

            title = valueOrUnknown(retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_TITLE));

            artist = valueOrUnknown(retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_ARTIST));

            album = valueOrUnknown(retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_ALBUM));

            genre = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_GENRE);

            year = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_YEAR);

            trackNumber = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);

            composer = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_COMPOSER);

            author = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_AUTHOR);

            String durationStr = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION);
            durationMs = durationStr != null ? Long.parseLong(durationStr) : 0;

            byte[] art = retriever.getEmbeddedPicture();
            cover = art != null
                    ? BitmapFactory.decodeByteArray(art, 0, art.length)
                    : null;

        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture metadata MP3", e);
        } finally {
            try {
                retriever.release();
            } catch (IOException e) {
                throw new RuntimeException("Erreur release", e);
            }
        }

        fileSize = new File(filePath).length();
    }

    // -------- Getters --------

    public String getFilePath() {
        return filePath;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getGenre() {
        return genre;
    }

    public String getYear() {
        return year;
    }

    public String getTrackNumber() {
        return trackNumber;
    }

    public String getComposer() {
        return composer;
    }

    public String getAuthor() {
        return author;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public long getFileSize() {
        return fileSize;
    }

    public Bitmap getCover() {
        return cover;
    }

    // -------- Utils --------

    private String valueOrUnknown(String value) {
        return value != null ? value : "Unknown";
    }
}

