package com.example.matonique.activity;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.matonique.R;
import com.example.matonique.model.Music;

public class MusicPlayActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private Music music;

    private ImageView imgCover;
    private TextView txtTitle, txtArtist;
    private Button btnPlay, btnPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);

        String filePath = getIntent().getStringExtra("FILE_PATH");
        if (filePath == null) {
            finish();
            return;
        }

        music = new Music(filePath);

        setupUI();
        setupPlayer();
    }

    private void setupUI() {
        imgCover = findViewById(R.id.img_cover);
        txtTitle = findViewById(R.id.txt_title);
        txtArtist = findViewById(R.id.txt_artist);
        btnPlay = findViewById(R.id.btn_play);
        btnPause = findViewById(R.id.btn_pause);

        txtTitle.setText(music.getTitle());
        txtArtist.setText(music.getArtist());

        Bitmap cover = music.getCover();
        if (cover != null) {
            imgCover.setImageBitmap(cover);
        } else {
            imgCover.setImageResource(R.drawable.music_placeholder);
        }

        btnPlay.setOnClickListener(v -> {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        });

        btnPause.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        });
    }

    private void setupPlayer() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(music.getFilePath());
            mediaPlayer.prepare();
        } catch (Exception e) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}

