package com.example.matonique.adapter;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.widget.LinearLayout;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.matonique.R;
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
    public void testOnBind() {
        // IMPORTANT : on ajoute le theme pour pas avoir l'erreur d'inflation
        Context context = new ContextThemeWrapper(ApplicationProvider.getApplicationContext(), R.style.Theme_Matonique);
        
        List<Playlist> playlists = new ArrayList<>();
        playlists.add(new Playlist(1, "Ma Liste", "/tmp/p1.m3u"));

        PlaylistAdapter adapter = new PlaylistAdapter(playlists, null);
        
        LinearLayout parent = new LinearLayout(context);
        PlaylistAdapter.PlaylistViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        
        adapter.onBindViewHolder(holder, 0);
        
        assertEquals("Ma Liste", holder.txtName.getText().toString());
    }
}
