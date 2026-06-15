package com.example.matonique.adapter;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.widget.LinearLayout;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.matonique.model.Playlist;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * Test pour PlaylistAdapter.
 * On verifi que la liste affiche bien les bons noms.
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = {33})
public class PlaylistAdapterUnitTest {

    @Test
    public void testItemCount() {
        Context context = ApplicationProvider.getApplicationContext();
        
        List<Playlist> playlists = new ArrayList<>();
        playlists.add(new Playlist(1, "Rap", "/tmp/p1.m3u"));
        playlists.add(new Playlist(2, "Rock", "/tmp/p2.m3u"));

        PlaylistAdapter adapter = new PlaylistAdapter(playlists, null);
        
        // on verifi la taille
        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void testOnBind() {
        Context context = ApplicationProvider.getApplicationContext();
        
        List<Playlist> playlists = new ArrayList<>();
        playlists.add(new Playlist(1, "Ma Liste", "/tmp/p1.m3u"));

        PlaylistAdapter adapter = new PlaylistAdapter(playlists, null);
        
        // creation d'un holder pour tester
        LinearLayout parent = new LinearLayout(context);
        PlaylistAdapter.PlaylistViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        
        adapter.onBindViewHolder(holder, 0);
        
        // on check si le nom est bien "Ma Liste"
        assertEquals("Ma Liste", holder.txtName.getText().toString());
    }
}
