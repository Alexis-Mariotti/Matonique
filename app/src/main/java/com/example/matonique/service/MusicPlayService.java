package com.example.matonique.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.example.matonique.R;
import com.example.matonique.fragments.MusicPlayFragment;
import com.example.matonique.model.Music;

// Service permetant de gérer la lecture de musique
// On utilise un service pour permettre la lecture en arrière plan
public class MusicPlayService extends Service {
    // identification du canal
    private static final String CHANNEL_ID = "MusicPlaybackChannel";

    private static final int NOTIFICATION_ID = 1;

    private MediaPlayer mediaPlayer; // lecteur de musique android
    private final IBinder binder = new MusicBinder(); // pour synchroniser avec des activités
    private Music currentMusic;

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
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // on recupere la music via l'intent grace à l'interface parcelable
        Music music = intent.getParcelableExtra("MUSIC");

        //todo: remove debug
        android.util.Log.d("MusicPlayService", "Starting service with music: " + music.getTitle());

        if (music != null) {
            currentMusic = music;
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
        Intent notificationIntent = new Intent(this, MusicPlayFragment.class);
        notificationIntent.putExtra("MUSIC", music);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // On créer une InconCompat à partir de la cover de la musique pour l'afficher dans la notification
        IconCompat compactCover = music.getCover() != null
                ? IconCompat.createWithBitmap(music.getCover())
                : IconCompat.createWithResource(this, R.drawable.music_placeholder);

        // on crée et retourne la notification
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(music.getTitle())
                .setContentText(music.getArtist())
                .setSmallIcon(compactCover)
                .setContentIntent(pendingIntent)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}

