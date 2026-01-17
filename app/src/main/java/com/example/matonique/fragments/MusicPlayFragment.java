package com.example.matonique.fragments;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.matonique.R;
import com.example.matonique.model.Music;
import com.example.matonique.service.MusicPlayService;

import java.util.Objects;

// Fragment utilisé pour la page de jeu d'une musique
// permet aussi de lancer le service MusicPlayService pour jouer la musique en arriere plan
public class MusicPlayFragment extends Fragment {

    // service qui joue la musique
    // on sépare l'UI et le back
    private MusicPlayService musicService;
    private boolean isBound = false; // verifier si le fragment est rattaché à un MusicPlayService
    private Music music;

    private ImageView imgCover;
    private TextView txtTitle, txtArtist, txtAlbum;
    private Button butonPlay, butonPause;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayService.MusicBinder binder = (MusicPlayService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;

            // Synchroniser avec la musique actuellement jouée dans le service
            syncWithService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    // Méthode factory pour créer une nouvelle instance avec un fichier
    public static MusicPlayFragment newInstance(String filePath) {
        MusicPlayFragment fragment = new MusicPlayFragment();
        if (filePath != null) {
            Bundle args = new Bundle();
            args.putString("FILE_PATH", filePath);
            fragment.setArguments(args);
        }
        return fragment;
    }

    // Méthode factory pour créer une instance vide (synchronisation avec service)
    public static MusicPlayFragment newInstance() {
        return new MusicPlayFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music_play, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialiser les vues
        setupUI(view);

        // Vérifier si on a un nouveau fichier dans les arguments
        String filePath = null;
        if (getArguments() != null) {
            filePath = getArguments().getString("FILE_PATH");
            // Important : éviter la réutilisation des arguments lors de la réinstanciation du fragment
            getArguments().remove("FILE_PATH");
        }

        if (filePath != null) {
            // debug toast
            android.util.Log.d("MusicPlayFrag", "OEEEEEEEEENew file path: " + filePath);

            // Nouvelle musique : instancier et démarrer le service
            music = new Music(filePath);
            startMusicService();
        } else {
            // Pas de nouveau fichier : se connecter au service existant
            bindToExistingService();
        }
    }

    // Synchroniser l'UI avec la musique actuellement jouée dans le service
    private void syncWithService() {
        if (isBound && musicService != null) {
            Music currentMusic = musicService.getCurrentMusic();
            if (currentMusic != null) {
                music = currentMusic;
                updateUI();
            }
        }
    }

    // Se connecter à un service existant sans en créer un nouveau
    private void bindToExistingService() {
        Intent serviceIntent = new Intent(requireContext(), MusicPlayService.class);
        requireContext().bindService(serviceIntent, connection, 0);
        //todo: remove debug
        android.util.Log.d("MusicPlayFrag", "Binding to existing service");
    }

    private void setupUI(View view) {
        imgCover = view.findViewById(R.id.img_cover);
        txtTitle = view.findViewById(R.id.txt_title);
        txtArtist = view.findViewById(R.id.txt_artist);
        txtAlbum = view.findViewById(R.id.txt_album);
        butonPlay = view.findViewById(R.id.btn_play);
        butonPause = view.findViewById(R.id.btn_pause);

        // Initialiser l'UI si music existe déjà
        if (music != null) {
            updateUI();
        }

        butonPlay.setOnClickListener(v -> {
            if (isBound) {
                musicService.play();
            }
        });

        butonPause.setOnClickListener(v -> {
            if (isBound) {
                musicService.pause();
            }
        });
    }

    // Mettre à jour l'UI avec les informations de la musique
    private void updateUI() {
        if (music == null) return;

        txtTitle.setText(music.getTitle());
        txtArtist.setText(music.getArtist());
        txtAlbum.setText(music.getAlbum());

        Bitmap cover = music.getCover();
        if (cover != null) {
            imgCover.setImageBitmap(cover);
        } else {
            imgCover.setImageResource(R.drawable.music_placeholder);
        }
    }

    private void startMusicService() {
        Intent serviceIntent = new Intent(requireContext(), MusicPlayService.class);
        serviceIntent.putExtra("MUSIC", music);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            requireContext().startForegroundService(serviceIntent);
        } else {
            requireContext().startService(serviceIntent);
        }

        requireContext().bindService(serviceIntent, connection, android.content.Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBound) {
            requireContext().unbindService(connection);
        }
    }
}
