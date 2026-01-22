package com.example.matonique.fragments;

import android.annotation.SuppressLint;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.matonique.R;
import com.example.matonique.activity.MainActivity;
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
    private ImageButton butonPlay, butonPause, butonPrevious, butonNext;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayService.MusicBinder binder = (MusicPlayService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;

            // Synchroniser avec la musique actuellement jouée dans le service
            syncWithService();

            // Enregistrer le listener pour etre notifié des changements de musique
            musicService.setOnMusicChangeListener(newMusic -> {
                // Mettre a jour l'UI sur le thread principal
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        music = newMusic;
                        updateUI();
                        updateNavigationButtons();
                    });
                }
            });

            // Mettre a jour les boutons de navigation
            updateNavigationButtons();
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Vérifier si on a un nouveau fichier dans les arguments
        String filePath = null;
        if (getArguments() != null) {
            filePath = getArguments().getString("FILE_PATH");
        }

        if (filePath != null) {
            // musique trouvé : instancier
            music = new Music(filePath);
        }
    }

    // callback appelé à chaques affichage du fragment
    // met a jour le highlight de la navbars
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // met à jour le highlight de la navbar
        updateNavbarHighlight();

        setupUI(view);

        // Si nouvelle musique, démarrer le service
        if (music != null && getArguments() != null && getArguments().containsKey("FILE_PATH")) {
            startMusicService();
            getArguments().remove("FILE_PATH");
        } else {
            // Se connecter au service existant
            bindToExistingService();
        }
    }

    // methode permetant de mettre en evidence le bouton de la navbar correspondant au fragment Music play
    private void updateNavbarHighlight() {
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            // Appeler une méthode de MainActivity pour mettre à jour la navbar
            // Remplacez R.id.nav_music_list par l'ID de votre bouton navbar
            mainActivity.setSelectedNavItem(R.id.nav_playing);
        }
    }


    // Synchroniser l'UI avec la musique actuellement jouée dans le service
    private void syncWithService() {
        if (isBound && musicService != null) {
            Music currentMusic = musicService.getCurrentMusic();
            if (currentMusic != null) {
                music = currentMusic;
                updateUI();
                updateNavigationButtons();
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
        butonPrevious = view.findViewById(R.id.btn_previous);
        butonNext = view.findViewById(R.id.btn_next);

        // Initialiser l'UI si music existe déjà
        if (music != null) {
            updateUI();
        } else {
            // pas de musique, on met le placeholder
            imgCover.setImageResource(R.drawable.music_placeholder);
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

        butonPrevious.setOnClickListener(v -> {
            if (isBound) {
                musicService.playPrevious();
            }
        });

        butonNext.setOnClickListener(v -> {
            if (isBound) {
                musicService.playNext();
            }
        });
    }

    // Methode qui met à jour l'UI avec les informations de la musique
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

    // Methode pour mettre a jour l'etat des boutons de navigation (activer/desactiver selon disponibilité)
    // si y a pas de musique suivante ou precedente, on desactive le bouton
    private void updateNavigationButtons() {
        if (isBound && musicService != null) {
            // activer ou desactiver le bouton precedent selon si y a une musique avant
            butonPrevious.setEnabled(musicService.hasPrevious());
            // activer ou desactiver le bouton suivant selon si y a une musique apres
            butonNext.setEnabled(musicService.hasNext());
        } else {
            // si pas de service, on desactive tous les boutons
            butonPrevious.setEnabled(false);
            butonNext.setEnabled(false);
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
