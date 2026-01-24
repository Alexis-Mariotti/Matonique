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

        // init du fichier donné
        File file = new File(filePath);

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

            // extraction de la cover avec gestion de la taille
            byte[] art = retriever.getEmbeddedPicture();
            if (art != null) {
                android.util.Log.d("Music", "Taille de la cover: " + formatFileSize(art.length));

                // si la cover est trop grosse (> 5 MB), on la redimensionne
                if (art.length > 5 * 1024 * 1024) {
                    android.util.Log.w("Music", "Cover très grosse, redimensionnement...");
                    // decoder avec des options pour reduire la taille
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4; // reduire par 4
                    cover = BitmapFactory.decodeByteArray(art, 0, art.length, options);
                } else {
                    cover = BitmapFactory.decodeByteArray(art, 0, art.length);
                }

                if (cover != null) {
                    android.util.Log.d("Music", "Cover chargée: " + cover.getWidth() + "x" + cover.getHeight());
                }
            } else {
                cover = null;
                android.util.Log.d("Music", "Pas de cover intégrée");
            }

        } catch (IllegalArgumentException e) {
            android.util.Log.e("Music", "Fichier invalide ou corrompu: " + filePath, e);
            throw new RuntimeException("Fichier audio invalide ou corrompu: " + file.getName(), e);
        } catch (Exception e) {
            android.util.Log.e("Music", "Erreur lecture metadata: " + filePath, e);
            throw new RuntimeException("Erreur lecture metadata MP3: " + e.getMessage(), e);
        } finally {
            try {
                retriever.release();
            } catch (IOException e) {
                android.util.Log.e("Music", "Erreur release du retriever", e);
                throw new RuntimeException("Erreur release", e);
            }
        }

        fileSize = file.length();
        android.util.Log.d("Music", "=== Chargement terminé: " + title + " (" + formatFileSize(fileSize) + ")");
    }

    // formater la taille d'un fichier en texte lisible
    private static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    // constructeur privé pour la factory utilisée par Parcelable
    // NOTE: On ne désérialise PAS le Bitmap ici pour éviter les transactions Binder trop grosses
    // Le service ou l'activité qui reçoit l'objet rechargera la cover depuis le fichier si nécessaire
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

        // NE PAS DESERIALISER LE BITMAP - Il sera rechargé depuis le fichier si nécessaire
        // Cela évite les erreurs "transaction too large" du Binder Android (limite ~1MB)
        cover = null;

        android.util.Log.d("Music", "Objet Music désérialisé depuis Parcel (sans cover): " + title);
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
    // NOTE: On ne sérialise PAS le Bitmap pour éviter les transactions Binder trop grosses
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

        // NE PAS SERIALISER LE BITMAP - Le Binder Android a une limite de ~1MB
        // Une cover de 700x700 peut faire 764KB, ce qui cause une erreur -74
        // Le destinataire rechargera la cover depuis le fichier si nécessaire

        android.util.Log.d("Music", "Objet Music sérialisé vers Parcel (sans cover): " + title);
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

    // Recharger la cover depuis le fichier si elle est null
    // Utile quand l'objet Music a été recréé depuis un Parcel (sans cover pour éviter les transactions trop grosses)
    public Bitmap getCoverOrLoad() {
        if (cover != null) {
            return cover;
        }

        // Charger la cover depuis le fichier
        android.util.Log.d("Music", "Cover null, rechargement depuis le fichier: " + filePath);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            byte[] art = retriever.getEmbeddedPicture();

            if (art != null) {
                android.util.Log.d("Music", "Cover rechargée: " + formatFileSize(art.length));

                // Redimensionner si trop grosse
                if (art.length > 5 * 1024 * 1024) {
                    android.util.Log.w("Music", "Cover très grosse, redimensionnement...");
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4;
                    return BitmapFactory.decodeByteArray(art, 0, art.length, options);
                } else {
                    return BitmapFactory.decodeByteArray(art, 0, art.length);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("Music", "Erreur rechargement cover: " + e.getMessage());
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                android.util.Log.e("Music", "Erreur release retriever", e);
            }
        }

        return null;
    }

    // -------- Utils --------

    private String valueOrUnknown(String value) {
        return value != null ? value : "Unknown";
    }
}

