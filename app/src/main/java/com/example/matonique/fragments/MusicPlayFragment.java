package com.example.matonique.fragments;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.matonique.R;
import com.example.matonique.activity.MainActivity;
import com.example.matonique.model.Music;
import com.example.matonique.sensor.ShakeDetector;
import com.example.matonique.service.MusicPlayService;

import java.util.ArrayList;
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
    private ImageButton btnLoop, btnAddPlaylist;


    // uttile pour la détection de quand on secoue le téléphone
    private SensorManager sensorManager;
    private Sensor accelerometer;
    // listener pour quand on secoue le telephone
    private ShakeDetector shakeDetector;


    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayService.MusicBinder binder = (MusicPlayService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;

            // on synchronise l'UI avec la musique en cours dans le service
            syncWithService();

            // enregistre le listener pour etre notifié des changements de musique
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

            // s'enregistrer au listener pour etre notifié des changements de progression
            musicService.setOnProgressChangeListener((currentPosition, duration) ->
                updateProgressBarFromService(currentPosition, duration)
            );

            // s'enregistre au listener pour etre notifié des changements d'etat de lecture
            musicService.setOnPlaybackStateChangeListener(isPlaying -> {
                // forcer la mise a jour meme si l'etat est le meme pour etre sur
                if (isPlaying) {
                    butonPlayPause.setImageResource(R.drawable.icon_pause);
                } else {
                    butonPlayPause.setImageResource(R.drawable.icon_play);
                }
                lastPlayingState = isPlaying;
            });

            // NOUVEAU : forcer une mise à jour initiale de l'UI
            // cela permet d'afficher les bonnes infos même si on se connecte alors que le service tournait déjà
            musicService.notifyInitialState();

            // Mettre a jour les boutons de navigation
            updateNavigationButtons();
            updatePlayPauseButton();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // cette methode est appelée quand le service est deconecté
            // alors on met a jour l'etat
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

    // Methode factory pour créer une nouvelle instance avec un fichier et une playlist
    public static MusicPlayFragment newInstance(String filePath, ArrayList<String> playlistPaths) {
        MusicPlayFragment fragment = new MusicPlayFragment();
        Bundle args = new Bundle();
        args.putString("FILE_PATH", filePath);
        args.putStringArrayList("PLAYLIST_PATHS", playlistPaths);
        fragment.setArguments(args);
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

        // --- on declare les listeners ---

        // Listener de secousse du telephone
        sensorManager = (SensorManager) (requireActivity()).getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            shakeDetector = new ShakeDetector();
            shakeDetector.setOnShakeListener(() -> {
                // appeler playNext() seulement si le service est connecté
                if (musicService != null) {
                    musicService.playNext();
                }
            });
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
                updateLoopButton(musicService.isLooping());
            }
        }
    }

    // Se connecter à un service existant sans en créer un nouveau
    private void bindToExistingService() {
        Intent serviceIntent = new Intent(requireContext(), MusicPlayService.class);
        requireContext().bindService(serviceIntent, connection, 0);
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
                    int newPosition = (progress * duration) / 1000;
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
                    // cohérent avec le max de 1000 de la seekbar
                    int newPosition = (seekBar.getProgress() * duration) / 1000;
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

        btnLoop = view.findViewById(R.id.btn_repeat);
        btnAddPlaylist = view.findViewById(R.id.btn_add_playlist);

        btnLoop.setOnClickListener(v -> {
            if (isBound && musicService != null) {
                boolean newLoopState = !musicService.isLooping(); // inverse l'état
                musicService.setLooping(newLoopState); // applique l'état
                updateLoopButton(newLoopState); // met à jour l'UI
            }
        });

        btnAddPlaylist.setOnClickListener(v -> {
            if (!isBound || musicService == null || music == null) return;

            String[] playlists = musicService.getAvailablePlaylists();

            if (playlists.length == 0) {
                Toast.makeText(requireContext(), "Aucune playlist disponible", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(requireContext())
                    .setTitle("Ajouter à une playlist")
                    .setItems(playlists, (dialog, which) -> {
                        String selectedPlaylist = playlists[which];
                        musicService.addToPlaylist(music, selectedPlaylist);
                        Toast.makeText(
                                requireContext(),
                                "Ajouté à \"" + selectedPlaylist + "\"",
                                Toast.LENGTH_SHORT
                        ).show();
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        });
    }

    // Methode qui met à jour l'UI avec les informations de la musique
    private void updateUI() {
        if (music == null) return;

        txtTitle.setText(music.getTitle());
        txtArtist.setText(music.getArtist());
        txtAlbum.setText(music.getAlbum());

        // Utiliser getCoverOrLoad() pour recharger la cover si elle est null (cas du Parcelable)
        Bitmap cover = music.getCoverOrLoad();
        if (cover != null) {
            imgCover.setImageBitmap(cover);
        } else {
            imgCover.setImageResource(R.drawable.music_placeholder);
        }

        // seulement si le service est connecté
        if (musicService == null) return;
        // on met l'icon pause/play correspondant à l'etat du service
        if (musicService.isPlaying()){
            butonPlayPause.setImageResource(R.drawable.icon_pause);
        } else {
            butonPlayPause.setImageResource(R.drawable.icon_play);
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
   // uttilisé par le callback du service
    private void updateProgressBarFromService(int currentPosition, int duration) {
        // verifier que les composants UI existent (pas null)
        if (seekBarProgress == null || txtCurrentTime == null || txtTotalTime == null) {
            android.util.Log.e("MusicPlayFragment", "UI components are null! Cannot update progress bar");
            return;
        }

        // on met a jour seulement si l'utilisateur ne bouge pas la bare
        if (isUserSeeking) {
            android.util.Log.d("MusicPlayFragment", "User is seeking, skipping update");
            return;
        }

        if (duration > 0) {
            // calculer le pourcentage de progression (sur 1000 pour etre precis)
            int progress = (currentPosition * 1000) / duration;

            try {
                seekBarProgress.setProgress(progress);

                // mettre a jour les labels de temps
                String currentTimeStr = formatTime(currentPosition);
                String totalTimeStr = formatTime(duration);
                txtCurrentTime.setText(currentTimeStr);
                txtTotalTime.setText(totalTimeStr);

                // debug : verifier que la methode est bien appelé
                android.util.Log.d("MusicPlayFragment", "Progress updated: " + currentTimeStr + " / " + totalTimeStr + " (" + progress + "/1000)");
            } catch (Exception e) {
                android.util.Log.e("MusicPlayFragment", "Error updating progress: " + e.getMessage());
            }
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

        // verifier si on a une playlist dans les arguments
        if (getArguments() != null && getArguments().containsKey("PLAYLIST_PATHS")) {
            ArrayList<String> playlistPaths = getArguments().getStringArrayList("PLAYLIST_PATHS");
            serviceIntent.putStringArrayListExtra("PLAYLIST_PATHS", playlistPaths);
        }

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
        // on enregistre le listener de secousse quand le fragment est visible
        if (sensorManager != null && accelerometer != null && shakeDetector != null) {
            sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // on desinscrit le listener de secousse quand le fragment n'est plus visible
        if (sensorManager != null && shakeDetector != null) {
            sensorManager.unregisterListener(shakeDetector);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBound) {
            requireContext().unbindService(connection);
        }
    }

    private void updateLoopButton(boolean isLooping) {
        if (btnLoop == null) return;

        btnLoop.setAlpha(isLooping ? 1f : 0.4f); // simple et clair
    }
}
