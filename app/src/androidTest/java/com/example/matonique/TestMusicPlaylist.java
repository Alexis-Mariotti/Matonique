package com.example.matonique;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
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
public class TestMusicPlaylist {

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.READ_MEDIA_AUDIO",
                    "android.permission.POST_NOTIFICATIONS");

    @Test
    public void testMusicPlaylist() {
        // 1. Clic sur l'onglet Playlist dans la barre du bas
        onView(withId(R.id.nav_playlist)).perform(click());

        // 2. Vérification que le bouton "Trouver une playlist" est là, puis clic
        onView(withId(R.id.btn_find_playlist))
                .check(matches(allOf(withText("Trouver une playlist"), isDisplayed())))
                .perform(click());

        // 3. Vérification des détails de la playlist affichée à l'écran
        onView(withId(R.id.recycler_playlist)).check(matches(isDisplayed()));
        onView(withId(R.id.txt_name)).check(matches(withText("NEUA mixed playlist")));
        onView(withId(R.id.txt_subtitle)).check(matches(withText("5 musiques")));

        // 4. Sélection (clic) de la première playlist (index 0)
        onView(withId(R.id.recycler_playlist)).perform(actionOnItemAtPosition(0, click()));

        // 5. Clic sur la première musique (index 0) de cette playlist pour lancer la lecture
        onView(withId(R.id.recycler_playlist)).perform(actionOnItemAtPosition(0, click()));

        // 6. Vérification du lecteur audio : Titre "Océan"
        onView(withId(R.id.img_cover)).check(matches(isDisplayed()));
        onView(withId(R.id.txt_title)).check(matches(withText("Océan")));
        onView(withId(R.id.txt_artist)).check(matches(withText("NOUS ÉTIONS UNE ARMÉE")));
        onView(withId(R.id.txt_album)).check(matches(withText("Pourtant, ces lambeaux ont été un trésor pour moi.")));
        onView(withId(R.id.btn_play_pause)).check(matches(isDisplayed()));

        // 7. Clic sur le bouton Suivant
        onView(withId(R.id.btn_next)).perform(click());

        // 8. Vérification que le lecteur affiche la chanson suivante : "Messe"
        onView(withId(R.id.txt_title)).check(matches(withText("Messe")));
        onView(withId(R.id.txt_artist)).check(matches(withText("NOUS ÉTIONS UNE ARMÉE")));
        onView(withId(R.id.txt_album)).check(matches(withText("Pourtant, ces lambeaux ont été un trésor pour moi.")));

        // 9. Clic sur le bouton Précédent pour revenir en arrière
        onView(withId(R.id.btn_previous)).perform(click());

        // 10. Vérification qu'on est bien revenu sur la chanson "Océan"
        onView(withId(R.id.txt_title)).check(matches(withText("Océan")));
        onView(withId(R.id.txt_artist)).check(matches(withText("NOUS ÉTIONS UNE ARMÉE")));
    }
}