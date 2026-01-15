package com.example.matonique.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matonique.MusicAdapter;
import com.example.matonique.R;
import com.example.matonique.model.Music;

import java.util.ArrayList;
import java.util.List;

public class MusicListActivity extends AppCompatActivity
        implements MusicAdapter.OnMusicClickListener {

    //permissions
    private static final int PERMISSION_REQUEST_READ_AUDIO = 1;
    private RecyclerView recyclerMusic;
    private MusicAdapter adapter;
    private List<Music> musicList = new ArrayList<>();
    private TextView txtEmpty; // pour afficher aucunes musiques

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        // initialisation du RecyclerView pour la liste de musiques
        recyclerMusic = findViewById(R.id.recycler_music);
        txtEmpty = findViewById(R.id.txt_empty);

        adapter = new MusicAdapter(musicList, this);
        recyclerMusic.setAdapter(adapter);

        // toggle initial
        toggleEmptyView();

        // observer les changements pour mettre à jour l'état vide
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override public void onChanged() { toggleEmptyView(); }
            @Override public void onItemRangeInserted(int positionStart, int itemCount) { toggleEmptyView(); }
            @Override public void onItemRangeRemoved(int positionStart, int itemCount) { toggleEmptyView(); }
        });
    }

    private void toggleEmptyView() {
        boolean isEmpty = adapter == null || adapter.getItemCount() == 0;
        txtEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerMusic.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onMusicClick(Music music) {
        Intent intent = new Intent(this, MusicPlayActivity.class);
        intent.putExtra("FILE_PATH", music.getFilePath());
        startActivity(intent);
    }

    private void checkPermissionAndLoadMusic() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                    PERMISSION_REQUEST_READ_AUDIO
            );
        } else {
            loadMusicFromDevice();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_READ_AUDIO &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            loadMusicFromDevice();
        }
    }

    private void loadMusicFromDevice() {

        new Thread(() -> {
            List<Music> loadedMusic = new ArrayList<>();

            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
            String[] projection = { MediaStore.Audio.Media.DATA };

            Cursor cursor = getContentResolver().query(
                    uri, projection, selection, null,
                    MediaStore.Audio.Media.TITLE + " ASC"
            );

            if (cursor != null) {
                int pathColumn = cursor.getColumnIndexOrThrow(
                        MediaStore.Audio.Media.DATA);

                while (cursor.moveToNext()) {
                    String path = cursor.getString(pathColumn);
                    try {
                        loadedMusic.add(new Music(path));
                    } catch (Exception ignored) {}
                }
                cursor.close();
            }

            // Mise à jour UI
            runOnUiThread(() -> {
                musicList.clear();
                musicList.addAll(loadedMusic);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }


    private void setupRecyclerView(List<Music> musicList) {
        RecyclerView recyclerView = findViewById(R.id.recycler_music);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        MusicAdapter adapter = new MusicAdapter(musicList, this);
        recyclerView.setAdapter(adapter);
    }

}
