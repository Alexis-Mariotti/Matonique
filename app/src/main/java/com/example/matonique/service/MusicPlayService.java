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

    private MediaPlayer mediaPlayer; // lecteur de musique android
    private final IBinder binder = new MusicBinder(); // pour synchroniser avec des activités
    private Music currentMusic;
    private MusicQueue musicQueue; // queue des musiques à jouer
    
    // Interface pour notifier les changements de musique
    public interface OnMusicChangeListener {
        void onMusicChanged(Music newMusic);
    }
    
    private OnMusicChangeListener musicChangeListener;

    public class MusicBinder extends Binder {
        public MusicPlayService getService() {
            return MusicPlayService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //todo: remove debug
        android.util.Log.d("MusicPlayService", "Starting service");

        mediaPlayer = new MediaPlayer();
        musicQueue = new MusicQueue(); // initialiser la queue vide
        
        // Configurer le listener pour jouer la musique suivante automatiquement
        mediaPlayer.setOnCompletionListener(mp -> {
            android.util.Log.d("MusicPlayService", "Musique terminée, tentative de jouer la suivante");
            playNext();
        });
        
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // on recupere la music via l'intent grace à l'interface parcelable
        Music music = intent.getParcelableExtra("MUSIC");

        //todo: remove debug
        android.util.Log.d("MusicPlayService", "Starting service with music: " + (music != null ? music.getTitle() : "null"));

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
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

    private Notification createNotification(Music music) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("MUSIC", music);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // On construit la notification avec la cover comme large icon et une icone simple comme small icon
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(music.getTitle())
                .setContentText(music.getArtist())
                .setSmallIcon(R.drawable.icon_play) // petite icone en haut de la notification
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true); // la notification ne peut pas etre swipé

        // ajouter la cover comme large icon si elle existe
        if (music.getCover() != null) {
            builder.setLargeIcon(music.getCover());
        }

        return builder.build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}

