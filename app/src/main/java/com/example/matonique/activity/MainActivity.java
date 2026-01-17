package com.example.matonique.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.matonique.R;

import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.matonique.fragments.MusicListFragment;
import com.example.matonique.fragments.MusicPlayFragment;
import com.example.matonique.fragments.PlaylistFragment;
import com.example.matonique.fragments.SettingsFragment;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // pour stocker les fragments et éviter de les instancier plusieurs fois
    private Map<Integer, Fragment> fragmentMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        // Afficher le fragment d'accueil par défaut au démarrage
        if (savedInstanceState == null) {
            // instancie le fragment MusicList
            MusicListFragment musicList = new MusicListFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    musicList).commit();
            // on le stocke dans la map pour ne pas le recréer quand l'utilisateur y retourne via la nav bar
            fragmentMap.put(R.id.nav_home, musicList);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private final BottomNavigationView.OnItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        // Vérifier si le fragment a déja été instancié
        if (fragmentMap.containsKey(itemId)) {
            selectedFragment = fragmentMap.get(itemId);
        } else {
            // Créer le fragment seulement s'il ne l'est pas deja
            if (itemId == R.id.nav_home) {
                // La page par default est la page de parcours des musiques
                selectedFragment = new MusicListFragment();
            } else if (itemId == R.id.nav_playing) {
                // on ne passe pas de paramètre pour synchroniser avec la musique en cours
                selectedFragment = MusicPlayFragment.newInstance();
            } else if (itemId == R.id.nav_playlist) {
                selectedFragment = new PlaylistFragment();
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            }

            // on stocke le fragment qui vient d'être instancié
            if (selectedFragment != null) {
                fragmentMap.put(itemId, selectedFragment);
            }
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
        }
        return true;
    };
}