package com.example.matonique.fragments;

import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.matonique.R;
import com.example.matonique.activity.MainActivity;
import com.example.matonique.service.MusicPlayService;

/**
 * Fragment pour les paramètres de l'application.
 * Exemple : égaliseur basique avec presets uniquement.
 */
public class SettingsFragment extends Fragment {

    private Spinner spinnerEqualizerPreset;
    private Equalizer equalizer;
    private Switch switchCrossfade;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerEqualizerPreset = view.findViewById(R.id.spinner_eq_preset);
        switchCrossfade = view.findViewById(R.id.switch_crossfade);

        // ======== ÉGALISEUR ========
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.eq_presets,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEqualizerPreset.setAdapter(adapter);

        spinnerEqualizerPreset.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                short presetIndex = (short) position;
                if (equalizer != null && presetIndex < equalizer.getNumberOfPresets()) {
                    equalizer.usePreset(presetIndex);
                    Toast.makeText(requireContext(),
                            "Preset appliqué: " + equalizer.getPresetName(presetIndex),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        // récupérer l'état sauvegardé
        boolean crossfadeEnabled = requireContext()
                .getSharedPreferences("settings", 0)
                .getBoolean("crossfade_enabled", false);
        switchCrossfade.setChecked(crossfadeEnabled);

        switchCrossfade.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // sauvegarder l'état
            requireContext()
                    .getSharedPreferences("settings", 0)
                    .edit()
                    .putBoolean("crossfade_enabled", isChecked)
                    .apply();

            // appliquer au service si connecté
            MusicPlayService service = null;
            if (getActivity() instanceof MainActivity) {
                service = ((MainActivity) getActivity()).getMusicService();
            }

            if (service != null) {
                service.setCrossfadeEnabled(isChecked);
                Toast.makeText(requireContext(),
                        "Crossfade " + (isChecked ? "activé" : "désactivé"),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (equalizer != null) {
            equalizer.release();
        }
    }

    public void setupEqualizer(int audioSessionId) {
        if (equalizer != null) {
            equalizer.release();
        }
        try {
            equalizer = new Equalizer(0, audioSessionId);
            equalizer.setEnabled(true);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Impossible d'initialiser l'égaliseur", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
