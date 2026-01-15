package com.example.matonique.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matonique.FileExplorerAdapter;
import com.example.matonique.R;
import com.example.matonique.model.FileItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Activity gerant l'exploration des fichiers musicaux et l'affichage sous forme de liste dans un RecyclerView
public class MusicListActivity extends AppCompatActivity
        implements FileExplorerAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private FileExplorerAdapter adapter;
    private List<FileItem> items = new ArrayList<>();
    private TextView txtCurrentPath;
    private TextView txtEmpty; // Texte affiché quand le dossier est vide
    private Button buttonBack;
    private Button buttonHome; // pour retourner au dossier Music

    private File currentDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        // init du RecyclerView
        recyclerView = findViewById(R.id.recycler_music);
        // init des autres vues
        txtCurrentPath = findViewById(R.id.txt_current_path);
        txtEmpty = findViewById(R.id.txt_empty);
        buttonBack = findViewById(R.id.buttonBack);
        buttonHome = findViewById(R.id.buttonHome);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FileExplorerAdapter(items, this);
        recyclerView.setAdapter(adapter);

        buttonBack.setOnClickListener(v -> navigateUp());
        buttonHome.setOnClickListener(v -> navigateToMusicDir());

        checkPermissionAndLoad();
    }

    // verifier les permissions et charger le repertoire
    // ouvre par defaut le dossier Music
    private void checkPermissionAndLoad() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Android 11+ : besoin de MANAGE_EXTERNAL_STORAGE pour explorer les fichiers
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                manageStorageLauncher.launch(intent);
                return;
            }
        } else {
            // Android 10 et moins : READ_EXTERNAL_STORAGE suffit
            String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(permission);
                return;
            }
        }

        File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        if (!musicDir.exists()) {
            musicDir = Environment.getExternalStorageDirectory();
        }
        loadDirectory(musicDir);
    }

    // demande de permission MANAGE_EXTERNAL_STORAGE
    private final ActivityResultLauncher<Intent> manageStorageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        Toast.makeText(this, "Permission accordée", Toast.LENGTH_LONG).show();
                        File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                        if (!musicDir.exists()) {
                            musicDir = Environment.getExternalStorageDirectory();
                        }
                        loadDirectory(musicDir);
                    } else {
                        Toast.makeText(this, "Permission refusée", Toast.LENGTH_LONG).show();
                    }
                }
            });



    // constante pour la demande de permission mis en parametre
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this,"Permission accordée après retour", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this,"Permission non accordée après retour", Toast.LENGTH_LONG).show();
                }
            });



    // charger le contenu d'un repertoire
    private void loadDirectory(File directory) {
        currentDirectory = directory;
        txtCurrentPath.setText(directory.getAbsolutePath());

        new Thread(() -> {
            List<FileItem> newItems = new ArrayList<>();
            File[] files = directory.listFiles();
/*
            final String debugMessage;
            if (files != null) {
                debugMessage = "Fichiers trouvés: " + files.length + " dans " + directory.getAbsolutePath();
                android.util.Log.d("MusicList", debugMessage);
            } else {
                debugMessage = "Aucun fichier (null) dans " + directory.getAbsolutePath();
                android.util.Log.d("MusicList", debugMessage);
            }

            // Afficher un Toast pour debug
            runOnUiThread(() -> {
                android.widget.Toast.makeText(this, debugMessage, android.widget.Toast.LENGTH_LONG).show();
            });
*/
            if (files != null) {
                Arrays.sort(files, (f1, f2) -> {
                    if (f1.isDirectory() && !f2.isDirectory()) return -1;
                    if (!f1.isDirectory() && f2.isDirectory()) return 1;
                    return f1.getName().compareToIgnoreCase(f2.getName());
                });

                for (File file : files) {
                    if (file.isDirectory()) {
                        newItems.add(new FileItem(
                                file.getAbsolutePath(),
                                file.getName(),
                                true
                        ));
                    } else if (isMusicFile(file)) {
                        newItems.add(new FileItem(
                                file.getAbsolutePath(),
                                file.getName(),
                                false
                        ));
                    }
                }
            }

            runOnUiThread(() -> {
                items.clear();
                items.addAll(newItems);
                adapter.notifyDataSetChanged();
                toggleEmptyView();
            });
        }).start();
    }


    // Renvoie true si le fichier est un fichier musical supporté
    // fichiers supportés : .mp3, .m4a, .flac, .wav
    private boolean isMusicFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp3") || name.endsWith(".m4a") ||
                name.endsWith(".flac") || name.endsWith(".wav");
    }

    // naviguer vers le repertoire parent
    private void navigateUp() {
        if (currentDirectory != null && currentDirectory.getParent() != null) {
            loadDirectory(currentDirectory.getParentFile());
        }
    }

    // naviguer vers le dossier Music
    // listener du bouton Home 🏠
    private void navigateToMusicDir() {
        File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        if (!musicDir.exists()) {
            musicDir = Environment.getExternalStorageDirectory();
        }
        loadDirectory(musicDir);
    }

    // afficher ou cacher le message de dossier vide
    private void toggleEmptyView() {
        boolean isEmpty = items.isEmpty();
        txtEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    // gerer le clic sur un element de la liste
    @Override
    public void onItemClick(FileItem item) {
        if (item.isDirectory()) {
            loadDirectory(new File(item.getPath()));
        } else {
            Intent intent = new Intent(this, MusicPlayActivity.class);
            intent.putExtra("FILE_PATH", item.getPath());
            startActivity(intent);
        }
    }
}
