package com.example.matonique.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

    // Launcher pour demander plusieurs permissions en une fois
    private final ActivityResultLauncher<String[]> multiplePermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                boolean allGranted = true;
                for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                    android.util.Log.d("MainActivity", "Permission " + entry.getKey() + " = " + entry.getValue());
                    if (!entry.getValue()) {
                        allGranted = false;
                    }
                }

                if (allGranted) {
                    android.util.Log.d("MainActivity", "Toutes les permissions ont été accordées");
                } else {
                    android.util.Log.d("MainActivity", "Certaines permissions ont été refusées");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // demander toutes les permissions nécessaires au démarrage
        checkAllPermissions();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        // Afficher le fragment d'accueil par défaut au démarrage
        if (savedInstanceState == null) {
            // verifier si on doit ouvrir le fragment MusicPlay (venant de la notification)
            if (getIntent().getBooleanExtra("OPEN_MUSIC_PLAY", false)) {
                // ouvrir le fragment MusicPlay qui se synchronisera avec le service
                MusicPlayFragment musicPlayFragment = MusicPlayFragment.newInstance();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, musicPlayFragment)
                        .commit();
                fragmentMap.put(R.id.nav_playing, musicPlayFragment);
                // mettre a jour la navbar pour highlighter le bon bouton
                bottomNav.setSelectedItemId(R.id.nav_playing);
            } else {
                // comportement normal : ouvrir le fragment MusicList
                MusicListFragment musicList = new MusicListFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, musicList)
                        .commit();
                fragmentMap.put(R.id.nav_home, musicList);
            }
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

    // methode pour definir le bouton selectionné dans la navbar
    // on fourni l'id du bouton à selectionner
    public void setSelectedNavItem(int itemId) {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(itemId);
    }

    // appelé quand l'activité reçoit un nouvel intent alors qu'elle est déjà lancée
    // par exemple quand on clique sur la notification pendant que l'app est ouverte
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // important pour que getIntent() retourne le nouveau

        // verifier si on doit ouvrir le fragment MusicPlay
        if (intent.getBooleanExtra("OPEN_MUSIC_PLAY", false)) {
            android.util.Log.d("MainActivity", "Ouverture du fragment MusicPlay depuis la notification");

            // verifier si le fragment existe déja dans la map
            Fragment musicPlayFragment = fragmentMap.get(R.id.nav_playing);
            if (musicPlayFragment == null) {
                // creer un nouveau fragment qui se synchronisera avec le service
                musicPlayFragment = MusicPlayFragment.newInstance();
                fragmentMap.put(R.id.nav_playing, musicPlayFragment);
            }

            // afficher le fragment et mettre a jour la navbar
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, musicPlayFragment)
                    .commit();

            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(R.id.nav_playing);
        }
    }

    // verifier et demander toutes les permissions nécessaires pour Android 13+
    // demande POST_NOTIFICATIONS et READ_MEDIA_AUDIO en une seule fois pour éviter les conflits
    private void checkAllPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // liste des permissions à demander
            java.util.List<String> permissionsToRequest = new java.util.ArrayList<>();

            // permission pour les notifications
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
            }

            // permission pour lire les fichiers audio
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO);
            }

            // demander toutes les permissions manquantes en une seule fois
            if (!permissionsToRequest.isEmpty()) {
                android.util.Log.d("MainActivity", "Demande de " + permissionsToRequest.size() + " permission(s)");
                multiplePermissionsLauncher.launch(permissionsToRequest.toArray(new String[0]));
            } else {
                android.util.Log.d("MainActivity", "Toutes les permissions sont déjà accordées");
            }
        }
    }
}