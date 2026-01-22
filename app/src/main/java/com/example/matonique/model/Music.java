package com.example.matonique.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
// Import de Parcelable et utilisation pour passer des Music en intent
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.matonique.utils.BitmapUtils;

import java.io.File;
import java.io.IOException;

// Model permettant de représenter une musique
public class Music implements Parcelable {

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

    // constructeur privé pour la factory utilisée par Parcelable
    protected Music(Parcel in) {
        filePath = in.readString();
        title = in.readString();
        artist = in.readString();
        album = in.readString();
        genre = in.readString();
        year = in.readString();
        trackNumber = in.readString();
        composer = in.readString();
        author = in.readString();
        durationMs = in.readLong();
        fileSize = in.readLong();


        int coverLength = in.readInt();
        if (coverLength > 0) {
            byte[] coverBytes = new byte[coverLength];
            in.readByteArray(coverBytes);
            cover = BitmapUtils.byteArrayToBitmap(coverBytes);
        } else {
            cover = null;
        }

        /*
        // Désérialiser le Bitmap
        if (in.readByte() == 1) {
            cover = Bitmap.CREATOR.createFromParcel(in);
        } else {
            cover = null;
        }
        */
    }

    // Factory pour construire des objets Music
    // Obligatoire pour reconstruire un objet musique depuis un Parcel
    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel in) {
            return new Music(in);
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };

    // -------- Parcelable implementation --------
    // Cette classe est Parcelable pour pouvoir passer des Music en extra d'intent

    @Override
    public int describeContents() {
        return 0;
    }

    // On deporte les données dans le Parcel pour les faire passer dans un intent
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(filePath);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeString(genre);
        dest.writeString(year);
        dest.writeString(trackNumber);
        dest.writeString(composer);
        dest.writeString(author);
        dest.writeLong(durationMs);
        dest.writeLong(fileSize);

        //  on convertit le Bitmap en byte array pour l'écrire dans le Parcel car les Bitmaps ne sont pas directement parcelables
        if (cover != null) {
            byte[] byteArray = BitmapUtils.bitmapToByteArray(cover);
            dest.writeInt(byteArray.length);
            dest.writeByteArray(byteArray);
        } else {
            dest.writeInt(0);
        }

        /*
        // Sérialiser le Bitmap
        if (cover != null) {
            dest.writeByte((byte) 1); // Indicateur que le Bitmap existe
            cover.writeToParcel(dest, flags);
        } else {
            dest.writeByte((byte) 0); // Pas de Bitmap
        }
         */
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

