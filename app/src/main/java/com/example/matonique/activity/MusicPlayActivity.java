package com.example.matonique.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.matonique.R;
import com.example.matonique.model.Music;
import com.example.matonique.service.MusicPlayService;

// Activité utilisé pour la page de jeu d'une musique
// permet aussi de lancer le service MusicPlayService pour jouer la musique en arriere plan
public class MusicPlayActivity extends AppCompatActivity {

    // service qui joue la musique
    // on sépare l'UI et le back
    private MusicPlayService musicService;
    private boolean isBound = false; // verfiier si l'activité est ratacher à un MusicPlayService
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
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);

        //on instancie l'objet music à partir du path dans l'intent
        String filePath = getIntent().getStringExtra("FILE_PATH");
        if (filePath == null) {
            finish();
            return;
        }
        music = new Music(filePath);

        // on setup l'ui et le service qui joue la musique
        setupUI();
        startMusicService();
    }

    private void setupUI() {
        imgCover = findViewById(R.id.img_cover);
        txtTitle = findViewById(R.id.txt_title);
        txtArtist = findViewById(R.id.txt_artist);
        txtAlbum = findViewById(R.id.txt_album);
        butonPlay = findViewById(R.id.btn_play);
        butonPause = findViewById(R.id.btn_pause);

        txtTitle.setText(music.getTitle());
        txtArtist.setText(music.getArtist());
        txtAlbum.setText(music.getAlbum());

        Bitmap cover = music.getCover();
        if (cover != null) {
            imgCover.setImageBitmap(cover);
        } else {
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
    }

    private void startMusicService() {
        Intent serviceIntent = new Intent(this, MusicPlayService.class);
        serviceIntent.putExtra("MUSIC", music);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
            //todo: remove debug
            android.util.Log.d("MusicPlayAct", "oeee");
        } else {
            startService(serviceIntent);
            //todo: remove debug
            android.util.Log.d("MusicPlayAct", "oeuf");
        }

        bindService(serviceIntent, connection, BIND_AUTO_CREATE);
        //todo: remove debug
        android.util.Log.d("MusicPlayAct", "oeeeuf");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(connection);
        }
    }
}
