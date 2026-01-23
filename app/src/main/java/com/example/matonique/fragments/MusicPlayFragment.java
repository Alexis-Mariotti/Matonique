package com.example.matonique.fragments;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.Locale;


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
    private ImageButton butonPlayPause, butonPrevious, butonNext;

    // composants pour la bare de progression
    private android.widget.SeekBar seekBarProgress;
    private TextView txtCurrentTime, txtTotalTime;
    private boolean isUserSeeking = false; // pour savoir si l'utilisateur est en train de bouger la bare

    // variables pour garder l'etat precedent et eviter les mises a jour inutiles de l'UI
    private boolean lastPlayingState = false;
    private boolean lastHasPrevious = false;
    private boolean lastHasNext = false;

    // Handler pour mettre a jour l'UI periodiquement
    private final Handler updateHandler = new Handler(Looper.getMainLooper());
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            // verifier que le service existe avant de mettre a jour
            if (!isBound || musicService == null) {
                return;
            }

            // mettre a jour le bouton play/pause
            updatePlayPauseButton();

            // mettre a jour les boutons de navigation (seulement si necessaire)
            updateNavigationButtons();

            // mettre a jour la bare de progression et les temps
            updateProgressBar();

            // relancer la tache dans 1 seconde (1000ms au lieu de 500ms pour reduire la charge)
            updateHandler.postDelayed(this, 1000);
        }
    };

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
                        // on met a jour l'affichage du bouton play/pause en fonction de si une musique est joué ou non
                        updatePlayPauseButton();
                    });
                }
            });

            // Mettre a jour les boutons de navigation
            updateNavigationButtons();
            updatePlayPauseButton();
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
                updatePlayPauseButton();
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
        butonPlayPause = view.findViewById(R.id.btn_play_pause);
        butonPrevious = view.findViewById(R.id.btn_previous);
        butonNext = view.findViewById(R.id.btn_next);

        // initialiser les composants de la bare de progression
        seekBarProgress = view.findViewById(R.id.seek_bar_progress);
        txtCurrentTime = view.findViewById(R.id.txt_current_time);
        txtTotalTime = view.findViewById(R.id.txt_total_time);

        // Initialiser l'UI si music existe déjà
        if (music != null) {
            updateUI();
        } else {
            // pas de musique, on met le placeholder
            imgCover.setImageResource(R.drawable.music_placeholder);
        }

        // configurer la bare de progression pour etre cliquable
        seekBarProgress.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                // si c'est l'utilisateur qui bouge la bare met a jour le label du temps actuel
                if (fromUser && isBound && musicService != null) {
                    int duration = musicService.getDuration();
                    int newPosition = (progress * duration) / 100;
                    txtCurrentTime.setText(formatTime(newPosition));
                }
            }

            @Override
            public void onStartTrackingTouch(android.widget.SeekBar seekBar) {
                // l'utilisateur commence a bouger la bare
                isUserSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(android.widget.SeekBar seekBar) {
                // l'utilisateur a fini de bouger la bare, on va a la position demandé
                if (isBound && musicService != null) {
                    int duration = musicService.getDuration();
                    int newPosition = (seekBar.getProgress() * duration) / 100;
                    musicService.seekTo(newPosition);
                }
                isUserSeeking = false;
            }
        });

        // Bouton play/pause qui change d'icon selon l'etat
        butonPlayPause.setOnClickListener(v -> {
            if (isBound) {
                if (musicService.isPlaying()) {
                    musicService.pause();
                } else {
                    musicService.play();
                }
                // mettre a jour l'icon du bouton
                updatePlayPauseButton();
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

    // Methode qui met a jour l'icon du bouton play/pause selon si le service de musique est en train de jouer une musique
    private void updatePlayPauseButton() {
        if (isBound && musicService != null) {
            boolean isPlaying = musicService.isPlaying();
            // mettre a jour seulement si l'etat a changé
            if (isPlaying != lastPlayingState) {
                if (isPlaying) {
                    // musique en cours de lecture : afficher l'icon pause
                    butonPlayPause.setImageResource(R.drawable.icon_pause);
                } else {
                    // musique en pause :afficher l'icon play
                    butonPlayPause.setImageResource(R.drawable.icon_play);
                }
                lastPlayingState = isPlaying;
            }
        }
    }

    // Methode pour mettre a jour l'etat des boutons de navigation (activer/desactiver selon disponibilité)
    // si y a pas de musique suivante ou precedente, on desactive le bouton
    private void updateNavigationButtons() {
        if (isBound && musicService != null) {
            boolean hasPrevious = musicService.hasPrevious();
            boolean hasNext = musicService.hasNext();

            // mettre a jour seulement si l'etat a changé
            if (hasPrevious != lastHasPrevious) {
                butonPrevious.setEnabled(hasPrevious);
                lastHasPrevious = hasPrevious;
            }
            if (hasNext != lastHasNext) {
                butonNext.setEnabled(hasNext);
                lastHasNext = hasNext;
            }
        }
    }

    // Methode pour mettre a jour la bare de progression et les labels de temps
    private void updateProgressBar() {
        // on met a jour seulement si l'utilisateur ne bouge pas la bare et que le service existe
        if (isUserSeeking || !isBound || musicService == null) {
            return;
        }

        // on met a jour seulement si une musique est en train de jouer
        if (!musicService.isPlaying()) {
            return;
        }

        try {
            int currentPosition = musicService.getCurrentPosition();
            int duration = musicService.getDuration();

            if (duration > 0) {
                // calculer le pourcentage de progression
                int progress = (currentPosition * 100) / duration;
                seekBarProgress.setProgress(progress);

                // mettre a jour les labels de temps (seulement si la valeur a changé)
                String currentTimeStr = formatTime(currentPosition);
                String totalTimeStr = formatTime(duration);

                if (!txtCurrentTime.getText().equals(currentTimeStr)) {
                    txtCurrentTime.setText(currentTimeStr);
                }
                if (!txtTotalTime.getText().equals(totalTimeStr)) {
                    txtTotalTime.setText(totalTimeStr);
                }
            }
        } catch (Exception e) {
            // en cas d'erreur, on ignore silencieusement pour ne pas crasher l'appli
            android.util.Log.e("MusicPlayFragment", "Erreur updateProgressBar: " + e.getMessage());
        }
    }

    // Methode utilitaire pour formater le temps en millisecondes vers le format MM:SS
    private String formatTime(int milliseconds) {
        int seconds = milliseconds / 1000;
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format(Locale.FRANCE, "%d:%02d", minutes, remainingSeconds);
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
    public void onResume() {
        super.onResume();
        // demarrer la mise a jour periodique du bouton play/pause quand le fragment est visible
        updateHandler.post(updateRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        // arreter la mise a jour periodique quand le fragment n'est plus visible pour economiser les ressources
        updateHandler.removeCallbacks(updateRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBound) {
            requireContext().unbindService(connection);
        }
        // nettoyer le handler
        updateHandler.removeCallbacks(updateRunnable);
    }
}
