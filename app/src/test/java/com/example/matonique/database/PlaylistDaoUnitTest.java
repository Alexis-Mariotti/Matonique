package com.example.matonique.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.List;

/**
 * Test pour la base de donnée.
 * On verifi toutes les actions du DAO.
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = {33})
public class PlaylistDaoUnitTest {

    private PlaylistDatabase db;
    private PlaylistDao dao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, PlaylistDatabase.class)
                .allowMainThreadQueries()
                .build();
        dao = db.playlistDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void testInsertAndGet() {
        PlaylistEntity p = new PlaylistEntity("/path/to/p.m3u", "Ma Playlist");
        dao.insert(p);
        
        List<PlaylistEntity> all = dao.getAllPlaylists();
        assertEquals(1, all.size());
        assertEquals("Ma Playlist", all.get(0).getName());
    }

    @Test
    public void testDelete() {
        PlaylistEntity p = new PlaylistEntity("/test.m3u", "Supprimé");
        dao.insert(p);
        
        PlaylistEntity fromDb = dao.getAllPlaylists().get(0);
        dao.delete(fromDb);
        
        assertEquals(0, dao.getAllPlaylists().size());
    }

    @Test
    public void testPlaylistExists() {
        String path = "/chemin/unique.m3u";
        dao.insert(new PlaylistEntity(path, "Check"));
        
        assertEquals(1, dao.playlistExists(path));
        assertEquals(0, dao.playlistExists("/autre/truc.m3u"));
    }

    @Test
    public void testGetById() {
        dao.insert(new PlaylistEntity("/p1.m3u", "P1"));
        PlaylistEntity p = dao.getAllPlaylists().get(0);
        
        PlaylistEntity found = dao.getPlaylistById(p.getId());
        assertNotNull(found);
        assertEquals("P1", found.getName());
    }
}
