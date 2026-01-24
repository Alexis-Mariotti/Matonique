package com.example.matonique.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matonique.FileExplorerAdapter;
import com.example.matonique.R;
import com.example.matonique.activity.MainActivity;
import com.example.matonique.adapter.PlaylistAdapter;
import com.example.matonique.database.PlaylistDao;
import com.example.matonique.database.PlaylistDatabase;
import com.example.matonique.database.PlaylistEntity;
import com.example.matonique.model.FileItem;
import com.example.matonique.model.Playlist;
import com.example.matonique.utils.M3UParser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// Fragment pour gerer les playlists
// permet d'afficher les playlists sauvegardé dans la base de donnée
// et de parcourir les fichiers pour trouver de nouvelles playlists au format m3u
public class PlaylistFragment extends Fragment implements PlaylistAdapter.OnPlaylistClickListener, FileExplorerAdapter.OnItemClickListener {

    // enum pour savoir quel mode d'affichage on est (liste des playlists ou contenu d'une playlist)
    private enum ViewMode {
        PLAYLISTS_LIST,     // afficher la liste des playlists
        PLAYLIST_CONTENT,   // afficher le contenu d'une playlist
        FILE_BROWSER        // afficher le navigateur de fichier pour trouver une playlist
    }

    private RecyclerView recyclerView;
    private TextView txtEmpty;
    private Button btnViewPlaylists;
    private Button btnFindPlaylist;

    private ViewMode currentMode = ViewMode.PLAYLISTS_LIST;
    private Playlist currentPlaylist; // playlist actuellement affichée

    private PlaylistDao playlistDao;

    // launcher pour ouvrir le file picker
    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        handleM3UFileSelected(uri);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialiser la base de donnée
        playlistDao = PlaylistDatabase.getInstance(requireContext()).playlistDao();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // mettre a jour le highlight de la navbar
        updateNavbarHighlight();

        // init des vues
        recyclerView = view.findViewById(R.id.recycler_playlist);
        txtEmpty = view.findViewById(R.id.txt_empty);
        btnViewPlaylists = view.findViewById(R.id.btn_view_playlists);
        btnFindPlaylist = view.findViewById(R.id.btn_find_playlist);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // configurer les boutons
        btnViewPlaylists.setOnClickListener(v -> showPlaylistsList());
        btnFindPlaylist.setOnClickListener(v -> openFileBrowser());

        Button btnCreatePlaylist = view.findViewById(R.id.btn_create_playlist);
        btnCreatePlaylist.setOnClickListener(v -> showCreatePlaylistDialog());

        // afficher la liste des playlists par defaut
        showPlaylistsList();
    }

    // methode permetant de mettre en evidence le bouton de la navbar correspondant au fragment Playlist
    private void updateNavbarHighlight() {
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.setSelectedNavItem(R.id.nav_playlist);
        }
    }

    // afficher la liste des playlists sauvegardé dans la base
    private void showPlaylistsList() {
        currentMode = ViewMode.PLAYLISTS_LIST;

        // charger les playlists depuis la base dans un thread separer
        new Thread(() -> {
            List<PlaylistEntity> entities = playlistDao.getAllPlaylists();
            List<Playlist> playlists = new ArrayList<>();

            // convertir les entités en objets Playlist et parser les fichiers m3u
            for (PlaylistEntity entity : entities) {
                Playlist playlist = new Playlist(entity.getId(), entity.getName(), entity.getFilePath());
                // parser le fichier m3u pour avoir le nombre de musiques
                List<String> musicPaths = M3UParser.parsePlaylist(entity.getFilePath());
                playlist.setMusicPaths(musicPaths);
                playlists.add(playlist);
            }

            // mettre a jour l'UI sur le thread principal
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    PlaylistAdapter adapter = new PlaylistAdapter(playlists, this);
                    recyclerView.setAdapter(adapter);
                    toggleEmptyView(playlists.isEmpty());
                });
            }
        }).start();
    }

    // afficher le contenu d'une playlist (les musiques qu'elle contient)
    private void showPlaylistContent(Playlist playlist) {
        currentMode = ViewMode.PLAYLIST_CONTENT;
        currentPlaylist = playlist;

        // creer une liste d'items pour le FileExplorerAdapter
        List<FileItem> items = new ArrayList<>();
        for (String musicPath : playlist.getMusicPaths()) {
            File file = new File(musicPath);
            if (file.exists()) {
                items.add(new FileItem(musicPath, file.getName(), false));
            }
        }

        // reutiliser le FileExplorerAdapter pour afficher les musiques
        FileExplorerAdapter adapter = new FileExplorerAdapter(items, this);
        recyclerView.setAdapter(adapter);
        toggleEmptyView(items.isEmpty());
    }

    // ouvrir le navigateur de fichier pour trouver une playlist m3u
    private void openFileBrowser() {
        currentMode = ViewMode.FILE_BROWSER;

        // ouvrir le file picker d'android pour selectionner un fichier m3u
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*"); // on filtre les m3u apres
        filePickerLauncher.launch(intent);
    }

    // gerer la selection d'un fichier m3u dans le file picker
    private void handleM3UFileSelected(Uri uri) {
        // convertir l'URI en chemin de fichier
        String filePath = getFilePathFromUri(uri);

        if (filePath == null || !filePath.toLowerCase().endsWith(".m3u")) {
            Toast.makeText(requireContext(), "Veuillez sélectionner un fichier M3U", Toast.LENGTH_SHORT).show();
            return;
        }

        // verifier si la playlist existe deja dans la base
        new Thread(() -> {
            int count = playlistDao.playlistExists(filePath);

            if (count > 0) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Cette playlist existe déjà", Toast.LENGTH_SHORT).show()
                    );
                }
                return;
            }

            // sauvegarder la playlist dans la base
            String name = M3UParser.extractPlaylistName(filePath);
            PlaylistEntity entity = new PlaylistEntity(filePath, name);
            playlistDao.insert(entity);

            // rafraichir la liste des playlists
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Playlist ajoutée: " + name, Toast.LENGTH_SHORT).show();
                    showPlaylistsList();
                });
            }
        }).start();
    }

    // convertir un URI en chemin de fichier absolu
    // methode amelioré qui fonctionne avec les URI content:// d'Android
    private String getFilePathFromUri(Uri uri) {
        android.util.Log.d("PlaylistFragment", "URI reçu: " + uri.toString());

        if ("file".equalsIgnoreCase(uri.getScheme())) {
            String path = uri.getPath();
            android.util.Log.d("PlaylistFragment", "Chemin file:// : " + path);
            return path;
        }

        // pour les URI content://, on essaye plusieurs methodes
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            try {
                // Methode 1 : utiliser un cursor pour recuperer le vrai chemin
                String[] projection = {android.provider.MediaStore.Audio.Media.DATA};
                android.database.Cursor cursor = requireContext().getContentResolver().query(
                    uri, projection, null, null, null);

                if (cursor != null) {
                    try {
                        if (cursor.moveToFirst()) {
                            int columnIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.DATA);
                            String path = cursor.getString(columnIndex);
                            android.util.Log.d("PlaylistFragment", "Chemin via cursor: " + path);
                            if (path != null) {
                                return path;
                            }
                        }
                    } finally {
                        cursor.close();
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("PlaylistFragment", "Erreur cursor: " + e.getMessage());
            }

            // Methode 2 : decoder manuellement l'URI
            String path = uri.getPath();
            android.util.Log.d("PlaylistFragment", "URI path brut: " + path);

            if (path != null && path.contains("/document/")) {
                // extraire le chemin apres /document/
                String[] parts = path.split("/document/");
                if (parts.length > 1) {
                    String documentPath = parts[1];
                    // decoder les : en /
                    documentPath = documentPath.replace(":", "/");

                    // gerer les differents types de stockage
                    if (documentPath.startsWith("primary/")) {
                        documentPath = documentPath.replace("primary/", "emulated/0/");
                    }

                    String fullPath = "/storage/" + documentPath;
                    android.util.Log.d("PlaylistFragment", "Chemin reconstruit: " + fullPath);

                    // verifier que le fichier existe
                    if (new File(fullPath).exists()) {
                        return fullPath;
                    }
                }
            }

            // Methode 3 : copier le fichier temporairement et lire depuis la copie
            // ceci est un dernier recours si on ne peut pas obtenir le chemin direct
            try {
                java.io.InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                if (inputStream != null) {
                    // creer un fichier temporaire
                    File tempFile = new File(requireContext().getCacheDir(), "temp_playlist.m3u");
                    java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile);

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    inputStream.close();
                    outputStream.close();

                    android.util.Log.d("PlaylistFragment", "Fichier copié vers: " + tempFile.getAbsolutePath());
                    return tempFile.getAbsolutePath();
                }
            } catch (Exception e) {
                android.util.Log.e("PlaylistFragment", "Erreur copie fichier: " + e.getMessage());
            }
        }

        android.util.Log.e("PlaylistFragment", "Impossible de convertir l'URI en chemin");
        return null;
    }

    // afficher ou cacher le message de liste vide
    private void toggleEmptyView(boolean isEmpty) {
        txtEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    // --- Callbacks du PlaylistAdapter ---

    @Override
    public void onPlaylistClick(Playlist playlist) {
        // afficher le contenu de la playlist
        showPlaylistContent(playlist);
    }

    @Override
    public void onPlaylistLongClick(Playlist playlist) {
        // demander confirmation avant de suprimer
        new AlertDialog.Builder(requireContext())
                .setTitle("Supprimer la playlist")
                .setMessage("Voulez-vous vraiment supprimer la playlist \"" + playlist.getName() + "\" ?")
                .setPositiveButton("Supprimer", (dialog, which) -> deletePlaylist(playlist))
                .setNegativeButton("Annuler", null)
                .show();
    }

    // suprimer une playlist de la base de donnée
    private void deletePlaylist(Playlist playlist) {
        new Thread(() -> {
            PlaylistEntity entity = playlistDao.getPlaylistById(playlist.getId());
            if (entity != null) {
                playlistDao.delete(entity);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Playlist supprimée", Toast.LENGTH_SHORT).show();
                        showPlaylistsList();
                    });
                }
            }
        }).start();
    }

    // --- Callbacks du FileExplorerAdapter (quand on affiche le contenu d'une playlist) ---

    @Override
    public void onItemClick(FileItem item) {
        // si on est en mode playlist content, lancer la musique avec la playlist comme queue
        if (currentMode == ViewMode.PLAYLIST_CONTENT && currentPlaylist != null) {
            // convertir List<String> en ArrayList<String> pour l'intent
            ArrayList<String> playlistPaths = new ArrayList<>(currentPlaylist.getMusicPaths());

            // creer un nouveau MusicPlayFragment avec la musique et la playlist
            MusicPlayFragment fragment = MusicPlayFragment.newInstance(item.getPath(), playlistPaths);

            // remplacer le fragment actuel par le MusicPlayFragment
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();

                // mettre a jour la navbar
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).setSelectedNavItem(R.id.nav_playing);
                }
            }
        }
    }

    private void createPlaylist(String name) {
        new Thread(() -> {
            try {
                // 1. Dossier playlists interne à l'app
                File playlistDir = new File(requireContext().getFilesDir(), "playlists");

                if (!playlistDir.exists()) {
                    boolean created = playlistDir.mkdirs();
                    android.util.Log.d("PLAYLIST", "Dossier créé: " + created);
                }

                // 2. Nom de fichier propre
                String safeName = name.replaceAll("[^a-zA-Z0-9-_ ]", "_");
                File m3uFile = new File(playlistDir, safeName + ".m3u");

                // 3. Vérifier si elle existe déjà
                if (m3uFile.exists()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(),
                                    "Playlist déjà existante",
                                    Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                // 4. Créer le fichier
                boolean fileCreated = m3uFile.createNewFile();
                android.util.Log.d("PLAYLIST", "Fichier créé: " + fileCreated +
                        " -> " + m3uFile.getAbsolutePath());

                // 5. Sauvegarde DB
                PlaylistEntity entity = new PlaylistEntity(
                        m3uFile.getAbsolutePath(),
                        name
                );
                playlistDao.insert(entity);

                // 6. UI
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(),
                            "Playlist créée",
                            Toast.LENGTH_SHORT).show();
                    showPlaylistsList();
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(),
                                "Erreur création playlist",
                                Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void showCreatePlaylistDialog() {
        EditText input = new EditText(requireContext());
        input.setHint("Nom de la playlist");

        new AlertDialog.Builder(requireContext())
                .setTitle("Créer une playlist")
                .setView(input)
                .setPositiveButton("Créer", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        createPlaylist(name);
                    } else {
                        Toast.makeText(requireContext(),
                                "Nom invalide",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}

