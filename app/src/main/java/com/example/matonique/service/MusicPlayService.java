package com.example.matonique.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
import android.support.v4.media.session.MediaSesMesionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.example.matonique.R;
import com.example.matonique.activity.MainActivity;
import com.example.matonique.model.Music;
import com.example.matonique.model.MusicQueue;

// Service permetant de gérer la lecture de musique
// On utilise un service pour permettre la lecture en arrière plan
public class MusicPlayService extends Service {
    // identification du canal
    private static final String CHANNEL_ID = "MusicPlaybackChannel";
    private static final int NOTIFICATION_ID = 1;

    // actions pour les boutons de la notification
    private static final String ACTION_PLAY_PAUSE = "com.example.matonique.ACTION_PLAY_PAUSE";
    private static final String ACTION_PREVIOUS = "com.example.matonique.ACTION_PREVIOUS";
    private static final String ACTION_NEXT = "com.example.matonique.ACTION_NEXT";

    private MediaPlayer mediaPlayer; // lecteur de musique android
    private final IBinder binder = new MusicBinder(); // pour synchroniser avec des activités
    private Music currentMusic;
    private MusicQueue musicQueue; // queue des musiques à jouer
    
    // Interface pour notifier les changements de musique
    public interface OnMusicChangeListener {
        void onMusicChanged(Music newMusic);
    }
    
    // Interface pour notifier les changements de progression (position dans la musique)
    public interface OnProgressChangeListener {
        void onProgressChanged(int currentPosition, int duration);
    }

    // Interface pour notifier les changements d'etat de lecture (play/pause)
    public interface OnPlaybackStateChangeListener {
        void onPlaybackStateChanged(boolean isPlaying);
    }

    private OnMusicChangeListener musicChangeListener;
    private OnProgressChangeListener progressChangeListener;
    private OnPlaybackStateChangeListener playbackStateChangeListener;

    // MediaSession pour etre reconnu correctement par android (notifications, lockscreen, etc.)
    private MediaSessionCompat mediaSession;

    // Handler pour mettre a jour la progression de la musique dans un thread separer
    private final android.os.Handler progressHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable progressUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            // si une musique est en cours de lecture et qu'on a un listener
            if (mediaPlayer != null && mediaPlayer.isPlaying() && progressChangeListener != null) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int duration = mediaPlayer.getDuration();

                // debug : verifier que le callback est appelé
                android.util.Log.d("MusicPlayService", "Calling progress callback: " + currentPosition + " / " + duration);

                // notifier le listener (le fragment) du changement de progression
                progressChangeListener.onProgressChanged(currentPosition, duration);
            }

            // relancer la mise a jour dans 1 seconde
            progressHandler.postDelayed(this, 1000);
        }
    };

    public class MusicBinder extends Binder {
        public MusicPlayService getService() {
            return MusicPlayService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mediaPlayer = new MediaPlayer();
        musicQueue = new MusicQueue(); // initialiser la queue vide
        
        // Initialiser la MediaSession pour les metadonnées
        mediaSession = new MediaSessionCompat(this, "MusicPlayService");
        mediaSession.setActive(true);

        // Lier les controles de la MediaSession avec le MediaPlayer
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                // appelé quand on appuie sur play (bluetooth, lockscreen, etc.)
                play();
            }

            @Override
            public void onPause() {
                // appelé quand on appuie sur pause (bluetooth, lockscreen, etc.)
                pause();
            }

            @Override
            public void onSkipToNext() {
                // appelé quand on appuie sur suivant (bluetooth, lockscreen, etc.)
                playNext();
            }

            @Override
            public void onSkipToPrevious() {
                // appelé quand on appuie sur precedent (bluetooth, lockscreen, etc.)
                playPrevious();
            }

            @Override
            public void onSeekTo(long pos) {
                // appelé quand on cherche une position dans la musique
                seekTo((int) pos);
            }

            @Override
            public void onStop() {
                // appelé quand on arrete la lecture
                pause();
                stopSelf();
            }
        });

        // Configurer le listener pour jouer la musique suivante automatiquement
        mediaPlayer.setOnCompletionListener(mp -> {
            android.util.Log.d("MusicPlayService", "Musique terminée, tentative de jouer la suivante");
            playNext();
        });
        
        createNotificationChannel();

        // demarrer la mise a jour de la progression
        progressHandler.post(progressUpdateRunnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // gerer les actions venant des boutons de la notification
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_PLAY_PAUSE:
                    if (isPlaying()) {
                        pause();
                    } else {
                        play();
                    }
                    // mettre a jour la notification avec le bon icon
                    if (hasNotificationPermission() && currentMusic != null) {
                        NotificationManager manager = getSystemService(NotificationManager.class);
                        if (manager != null) {
                            manager.notify(NOTIFICATION_ID, createNotification(currentMusic));
                        }
                    }
                    return START_STICKY;

                case ACTION_PREVIOUS:
                    playPrevious();
                    return START_STICKY;

                case ACTION_NEXT:
                    playNext();
                    return START_STICKY;
            }
        }

        // on recupere la music via l'intent grace à l'interface parcelable
        Music music = intent.getParcelableExtra("MUSIC");

        if (music != null) {
            currentMusic = music;
            
            // Créer la queue à partir du dossier parent du fichier
            java.io.File musicFile = new java.io.File(music.getFilePath());
            java.io.File parentDir = musicFile.getParentFile();
            
            if (parentDir != null && parentDir.isDirectory()) {
                musicQueue.setFromFolder(parentDir, music.getFilePath());
                android.util.Log.d("MusicPlayService", "Queue créée avec " + musicQueue.getSize() + " musiques");
            }
            
            playMusic(music.getFilePath());
            startForeground(NOTIFICATION_ID, createNotification(music));
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void playMusic(String filePath) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();

            // mettre a jour l'etat de la MediaSession
            updatePlaybackState();

            // notifier le changement d'etat vers "en lecture"
            if (playbackStateChangeListener != null) {
                playbackStateChangeListener.onPlaybackStateChanged(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();

            // mettre a jour l'etat de la MediaSession
            updatePlaybackState();

            // notifier le changement d'etat vers "en lecture"
            if (playbackStateChangeListener != null) {
                playbackStateChangeListener.onPlaybackStateChanged(true);
            }
        }
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();

            // mettre a jour l'etat de la MediaSession
            updatePlaybackState();

            // notifier le changement d'etat vers "en pause"
            if (playbackStateChangeListener != null) {
                playbackStateChangeListener.onPlaybackStateChanged(false);
            }
        }
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    // recuperer la position actuelle de la musique en millisecondes
    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    // recuperer la durée totale de la musique en millisecondes
    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    // se deplacer à une position specifique dans la musique (en millisecondes)
    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    public Music getCurrentMusic() {
        return currentMusic;
    }
    
    public MusicQueue getMusicQueue() {
        return musicQueue;
    }
    
    // Définir le listener pour les changements de musique
    public void setOnMusicChangeListener(OnMusicChangeListener listener) {
        this.musicChangeListener = listener;
    }

    // Definir le listener pour les changements de progression pour la barre de progression dans le layout
    public void setOnProgressChangeListener(OnProgressChangeListener listener) {
        this.progressChangeListener = listener;
    }

    // Definir le listener pour les changements d'etat de lecture (play/pause)
    public void setOnPlaybackStateChangeListener(OnPlaybackStateChangeListener listener) {
        this.playbackStateChangeListener = listener;
    }

    // Jouer la musique suivante dans la queue
    public void playNext() {
        if (musicQueue != null && musicQueue.hasNext()) {
            String nextPath = musicQueue.getNext();
            if (nextPath != null) {
                android.util.Log.d("MusicPlayService", "Lecture de la musique suivante: " + nextPath);
                currentMusic = new Music(nextPath);
                playMusic(nextPath);

                // Mettre à jour la notification si on a la permission
                if (hasNotificationPermission()) {
                    NotificationManager manager = getSystemService(NotificationManager.class);
                    if (manager != null) {
                        manager.notify(NOTIFICATION_ID, createNotification(currentMusic));
                    }
                }

                // Notifier le listener
                if (musicChangeListener != null) {
                    musicChangeListener.onMusicChanged(currentMusic);
                }

                return;
            }
        }
        android.util.Log.d("MusicPlayService", "Pas de musique suivante, fin de la queue");
    }

    // Jouer la musique précédente dans la queue
    public void playPrevious() {
        if (musicQueue != null && musicQueue.hasPrevious()) {
            String previousPath = musicQueue.getPrevious();
            if (previousPath != null) {
                android.util.Log.d("MusicPlayService", "Lecture de la musique précédente: " + previousPath);
                currentMusic = new Music(previousPath);
                playMusic(previousPath);

                // Mettre à jour la notification si on a la permission
                if (hasNotificationPermission()) {
                    NotificationManager manager = getSystemService(NotificationManager.class);
                    if (manager != null) {
                        manager.notify(NOTIFICATION_ID, createNotification(currentMusic));
                    }
                }

                // Notifier le listener
                if (musicChangeListener != null) {
                    musicChangeListener.onMusicChanged(currentMusic);
                }
            }
        }
    }

    // Vérifier s'il y a une musique suivante
    public boolean hasNext() {
        return musicQueue != null && musicQueue.hasNext();
    }

    // Vérifier s'il y a une musique précédente
    public boolean hasPrevious() {
        return musicQueue != null && musicQueue.hasPrevious();
    }

    // verifier si on a la permission de poster des notifications
    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        // avant android 13, pas besoin de permission explicite
        return true;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Lecture de musique",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    // Methode qui met a jour les metadonnées de la MediaSession avec la musique courante
    private void updateMediaSessionMetadata(Music music) {
        // recuperer et redimensionner la cover
        android.graphics.Bitmap coverToDisplay;
        if (music.getCover() != null) {
            coverToDisplay = resizeCoverForNotification(music.getCover());
        } else {
            android.graphics.Bitmap placeholder = android.graphics.BitmapFactory.decodeResource(
                    getResources(),
                    R.drawable.music_placeholder
            );
            coverToDisplay = resizeCoverForNotification(placeholder);
        }

        // construire les metadonnées avec la cover en grand
        MediaMetadataCompat metadata =
                new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, music.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, music.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, music.getAlbum())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, coverToDisplay) // cover en grand
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, getDuration())
                .build();

        mediaSession.setMetadata(metadata);
    }

    // Methode qui met a jour l'etat de lecture de la MediaSession (play/pause, position, etc.)
    private void updatePlaybackState() {
        PlaybackStateCompat.Builder stateBuilder =
                new PlaybackStateCompat.Builder();

        // definir les actions supportées (play, pause, skip, seek, etc.)
        long actions = PlaybackStateCompat.ACTION_PLAY_PAUSE
                | PlaybackStateCompat.ACTION_PLAY
                | PlaybackStateCompat.ACTION_PAUSE
                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                | PlaybackStateCompat.ACTION_SEEK_TO;

        stateBuilder.setActions(actions);

        // definir l'etat actuel (playing ou paused)
        int state = isPlaying()
                ? PlaybackStateCompat.STATE_PLAYING
                : PlaybackStateCompat.STATE_PAUSED;

        // definir la position actuelle et la vitesse de lecture
        stateBuilder.setState(
                state,
                getCurrentPosition(),
                1.0f // vitesse de lecture normale (1.0 = vitesse normale)
        );

        mediaSession.setPlaybackState(stateBuilder.build());
    }

    // notification affichée pendant la lecture de musique
    private Notification createNotification(Music music) {
        // intent qui ouvre MainActivity et lui dit de naviguer vers le fragment MusicPlay
        Intent notificationIntent = new Intent(this, MainActivity.class);
        // flag pour dire d'ouvrir MusicPlay
        notificationIntent.putExtra("OPEN_MUSIC_PLAY", true);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // creer les pending intent pour les boutons
        // bouton precedent
        Intent previousIntent = new Intent(this, MusicPlayService.class);
        previousIntent.setAction(ACTION_PREVIOUS);
        PendingIntent previousPendingIntent = PendingIntent.getService(
                this, 1, previousIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // bouton play/pause
        Intent playPauseIntent = new Intent(this, MusicPlayService.class);
        playPauseIntent.setAction(ACTION_PLAY_PAUSE);
        PendingIntent playPausePendingIntent = PendingIntent.getService(
                this, 2, playPauseIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // bouton suivant
        Intent nextIntent = new Intent(this, MusicPlayService.class);
        nextIntent.setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getService(
                this, 3, nextIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // mettre a jour les metadonnées de la MediaSession (cover, titre, artiste, etc.)
        updateMediaSessionMetadata(music);

        // On construit la notification avec MediaStyle lié à la MediaSession
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(music.getTitle())
                .setContentText(music.getArtist())
                .setSubText(music.getAlbum()) // nom de l'album en sous-texte
                .setSmallIcon(R.drawable.notif_icon_play) // petite icone en haut de la notification (monochrome)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true) // la notification ne peut pas etre swipé
                // ajouter les boutons d'action avec des icones vectorielles monochromes
                .addAction(R.drawable.notif_icon_previous, "Précédent", previousPendingIntent)
                .addAction(
                        // icon change selon l'état de la lecture
                        isPlaying() ? R.drawable.notif_icon_pause : R.drawable.notif_icon_play,
                        isPlaying() ? "Pause" : "Play",
                        playPausePendingIntent
                )
                .addAction(R.drawable.notif_icon_next, "Suivant", nextPendingIntent)
                // MediaStyle lié à la MediaSession
                .setStyle(new MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken()) // IMPORTANT : lie la session pour afficher la cover
                        .setShowActionsInCompactView(0, 1, 2)); // affiche les 3 boutons meme en vue compacte

        return builder.build();
    }

    // Methode qui redimensionne la cover pour qu'elle prenne toute la place dans la notification
    // taille optimale : 512x512 pixels pour les grandes notifications
    private android.graphics.Bitmap resizeCoverForNotification(android.graphics.Bitmap originalCover) {
        if (originalCover == null) {
            return null;
        }

        // taille cible pour la notification (512x512 est la taille recommandé par google)
        int targetSize = 512;

        // si l'image est deja à la bonne taille ou plus grande, on la redimensionne
        if (originalCover.getWidth() != targetSize || originalCover.getHeight() != targetSize) {
            return android.graphics.Bitmap.createScaledBitmap(
                    originalCover,
                    targetSize,
                    targetSize,
                    true // filtre pour un redimensionnement de qualité
                    // ce filtre est geré par la méthode createScaledBitmap
            );
        }

        return originalCover;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // arreter le handler de progression
        progressHandler.removeCallbacks(progressUpdateRunnable);

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        // liberer la MediaSession
        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
        }
    }
}

