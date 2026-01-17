package com.example.matonique.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matonique.FileExplorerAdapter;
import com.example.matonique.R;
import com.example.matonique.model.FileItem;
import com.example.matonique.fragments.MusicPlayFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Fragment gerant l'exploration des fichiers musicaux et l'affichage sous forme de liste dans un RecyclerView
public class MusicListFragment extends Fragment
        implements FileExplorerAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private FileExplorerAdapter adapter;
    private List<FileItem> items = new ArrayList<>();
    private TextView txtCurrentPath;
    private TextView txtEmpty; // Texte affiché quand le dossier est vide
    private Button buttonBack;
    private Button buttonHome; // pour retourner au dossier Music

    private File currentDirectory;

    // demande de permission MANAGE_EXTERNAL_STORAGE
    private final ActivityResultLauncher<Intent> manageStorageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        Toast.makeText(requireContext(), "Permission accordée", Toast.LENGTH_LONG).show();
                        File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                        if (!musicDir.exists()) {
                            musicDir = Environment.getExternalStorageDirectory();
                        }
                        loadDirectory(musicDir);
                    } else {
                        Toast.makeText(requireContext(), "Permission refusée", Toast.LENGTH_LONG).show();
                    }
                }
            });

    // constante pour la demande de permission mis en parametre
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(requireContext(),"Permission accordée après retour", Toast.LENGTH_LONG).show();
                    checkPermissionAndLoad();
                } else {
                    Toast.makeText(requireContext(),"Permission non accordée après retour", Toast.LENGTH_LONG).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // init du RecyclerView
        recyclerView = view.findViewById(R.id.recycler_music);
        // init des autres vues
        txtCurrentPath = view.findViewById(R.id.txt_current_path);
        txtEmpty = view.findViewById(R.id.txt_empty);
        buttonBack = view.findViewById(R.id.buttonBack);
        buttonHome = view.findViewById(R.id.buttonHome);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
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
                intent.setData(android.net.Uri.parse("package:" + requireContext().getPackageName()));
                manageStorageLauncher.launch(intent);
                return;
            }
        } else {
            // Android 10 et moins : READ_EXTERNAL_STORAGE suffit
            String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
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
            requireActivity().runOnUiThread(() -> {
                android.widget.Toast.makeText(requireContext(), debugMessage, android.widget.Toast.LENGTH_LONG).show();
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

            requireActivity().runOnUiThread(() -> {
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
// gerer le clic sur un element de la liste
    @Override
    public void onItemClick(FileItem item) {
        if (item.isDirectory()) {
            loadDirectory(new File(item.getPath()));
        } else {
            // On crée et affiche le MusicPlayFragment pour jouer la musique sélectionnée
            MusicPlayFragment fragment = MusicPlayFragment.newInstance(item.getPath());

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

}
