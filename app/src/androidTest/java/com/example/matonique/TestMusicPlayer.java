package com.example.matonique;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.example.matonique.activity.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class TestMusicPlayer {

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.READ_MEDIA_AUDIO",
                    "android.permission.POST_NOTIFICATIONS");

    @Test
    public void testMusicPlayer() {
        // 1. Clic sur le bouton Home pour réinitialiser la vue
        onView(withId(R.id.buttonHome)).perform(click());

        // 2. Vérification rapide que la liste et le chemin par défaut sont là
        onView(withId(R.id.recycler_music)).check(matches(isDisplayed()));
        onView(withId(R.id.txt_current_path)).check(matches(withText("/storage/emulated/0/Music")));

        // 3. Clic sur le 4ème élément (index 3) de la liste (ex: un sous-dossier d'album)
        onView(withId(R.id.recycler_music)).perform(actionOnItemAtPosition(3, click()));

        // 4. Vérification que le chemin a changé pour le dossier de l'album
        onView(withId(R.id.txt_current_path))
                .check(matches(withText("/storage/emulated/0/Music/SMILE - Price of Progress")));

        // 5. Clic sur la première musique (index 0) du dossier
        onView(withId(R.id.recycler_music)).perform(actionOnItemAtPosition(0, click()));

        // 6. Vérification des onglets de la barre de navigation du bas
        onView(withId(R.id.bottom_navigation)).check(matches(isDisplayed()));

        // 7. Vérification des métadonnées de la musique en cours de lecture
        onView(withId(R.id.img_cover)).check(matches(isDisplayed()));
        onView(withId(R.id.txt_title)).check(matches(withText("Dog in the Manger")));
        onView(withId(R.id.txt_artist)).check(matches(withText("SMILE")));
        onView(withId(R.id.txt_album)).check(matches(withText("Price of Progress")));
        onView(withId(R.id.txt_total_time)).check(matches(withText("4:52")));

        // 8. Vérification de la présence des boutons de contrôle du lecteur
        onView(withId(R.id.btn_play_pause)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_previous)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_next)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_add_playlist)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_repeat)).check(matches(isDisplayed()));

        // 9. Actions utilisateur : On joue avec le bouton Play/Pause (Plusieurs clics)
        onView(withId(R.id.btn_play_pause)).perform(click());
        onView(withId(R.id.btn_play_pause)).perform(click());
        onView(withId(R.id.btn_play_pause)).perform(click());

        // 10. Vérification finale que le bouton play/pause est toujours là et clic sur Suivant
        onView(withId(R.id.btn_play_pause)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_next)).perform(click());
    }
}