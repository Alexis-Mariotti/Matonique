package com.example.matonique.model;

// model permettant de représenter un élément de fichier (fichier ou dossier)
public class FileItem {
    private final String path; // chemin absolu
    private final String name;
    private final boolean isDirectory;

    public FileItem(String path, String name, boolean isDirectory) {
        if (path == null || path.trim().isEmpty() || name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le chemin (path) et le nom (name) ne peuvent pas être null ou vides");
        }
        this.path = path;
        this.name = name;
        this.isDirectory = isDirectory;
    }

    public String getPath() { return path; }
    public String getName() { return name; }
    public boolean isDirectory() { return isDirectory; }
}
