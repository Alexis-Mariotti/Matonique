package com.example.matonique.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Utilitaire pour parser les fichiers de playlist M3U
// Format M3U : un fichier texte avec une ligne par fichier audio
public class M3UParser {

    // Lire une playlist M3U et retourner la liste des chemins de fichiers
    // les chemins peuvent etre relatifs ou absolus
    public static List<String> parsePlaylist(String m3uFilePath) {
        List<String> musicPaths = new ArrayList<>();
        File m3uFile = new File(m3uFilePath);

        android.util.Log.d("M3UParser", "=== Début parsing de: " + m3uFilePath);

        if (!m3uFile.exists()) {
            android.util.Log.e("M3UParser", "Fichier M3U introuvable: " + m3uFilePath);
            return musicPaths;
        }

        if (!m3uFile.canRead()) {
            android.util.Log.e("M3UParser", "Fichier M3U illisible (permissions?): " + m3uFilePath);
            return musicPaths;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(m3uFile))) {
            String line;
            File m3uParentDir = m3uFile.getParentFile();
            android.util.Log.d("M3UParser", "Dossier parent du M3U: " + (m3uParentDir != null ? m3uParentDir.getAbsolutePath() : "null"));

            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                // ignorer les lignes vides et les commentaires
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                if (line.startsWith("#")) {
                    android.util.Log.d("M3UParser", "Ligne " + lineNumber + " (commentaire): " + line);
                    continue;
                }

                android.util.Log.d("M3UParser", "Ligne " + lineNumber + " (chemin): " + line);

                // si le chemin est relatif, le resoudre par rapport au dossier du m3u
                File musicFile = new File(line);

                if (!musicFile.isAbsolute() && m3uParentDir != null) {
                    musicFile = new File(m3uParentDir, line);
                    android.util.Log.d("M3UParser", "  Chemin relatif converti en: " + musicFile.getAbsolutePath());
                }

                // verifier que le fichier existe
                if (!musicFile.exists()) {
                    android.util.Log.w("M3UParser", "  ✗ Fichier introuvable: " + musicFile.getAbsolutePath());
                    continue;
                }

                // verifier que c'est un fichier audio
                if (!isMusicFile(musicFile)) {
                    android.util.Log.w("M3UParser", "  ✗ Pas un fichier audio supporté: " + musicFile.getName());
                    continue;
                }

                // fichier valide !
                musicPaths.add(musicFile.getAbsolutePath());
                android.util.Log.d("M3UParser", "  ✓ Fichier ajouté: " + musicFile.getName() + " (" + formatFileSize(musicFile.length()) + ")");
            }
        } catch (IOException e) {
            android.util.Log.e("M3UParser", "Erreur lecture du fichier M3U: " + e.getMessage());
            e.printStackTrace();
        }

        android.util.Log.d("M3UParser", "=== Fin parsing: " + musicPaths.size() + " fichiers valides trouvés sur " + m3uFile.length() + " octets lus");
        return musicPaths;
    }

    // formater la taille d'un fichier en texte lisible
    private static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    // verifier si un fichier est un fichier audio supporté
    private static boolean isMusicFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp3") || name.endsWith(".m4a") ||
                name.endsWith(".flac") || name.endsWith(".wav");
    }

    // extraire le nom de la playlist à partir du nom du fichier m3u
    public static String extractPlaylistName(String m3uFilePath) {
        File file = new File(m3uFilePath);
        String fileName = file.getName();

        // enlever l'extension .m3u ou .m3u8
        if (fileName.toLowerCase().endsWith(".m3u8")) {
            return fileName.substring(0, fileName.length() - 5);
        } else if (fileName.toLowerCase().endsWith(".m3u")) {
            return fileName.substring(0, fileName.length() - 4);
        }

        return fileName;
    }
}

