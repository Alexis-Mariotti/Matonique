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
        //  bouton Home
        onView(withId(R.id.buttonHome)).perform(click());

        // verif liste et chemin par default
        onView(withId(R.id.recycler_music)).check(matches(isDisplayed()));
        onView(withId(R.id.txt_current_path)).check(matches(withText("/storage/emulated/0/Music")));

        onView(withId(R.id.recycler_music)).perform(actionOnItemAtPosition(3, click()));

        // verif chemin changé
        onView(withId(R.id.txt_current_path))
                .check(matches(withText("/storage/emulated/0/Music/SMILE - Price of Progress")));

        // clique sur musique
        onView(withId(R.id.recycler_music)).perform(actionOnItemAtPosition(0, click()));

        // verif changement navbar
        onView(withId(R.id.bottom_navigation)).check(matches(isDisplayed()));

        // métadonnées de la musique en cours de lecture
        onView(withId(R.id.img_cover)).check(matches(isDisplayed()));
        onView(withId(R.id.txt_title)).check(matches(withText("Dog in the Manger")));
        onView(withId(R.id.txt_artist)).check(matches(withText("SMILE")));
        onView(withId(R.id.txt_album)).check(matches(withText("Price of Progress")));
        onView(withId(R.id.txt_total_time)).check(matches(withText("4:52")));

        // boutons du lecteur
        onView(withId(R.id.btn_play_pause)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_previous)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_next)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_add_playlist)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_repeat)).check(matches(isDisplayed()));

        // synchronisation bouton pause/play
        onView(withId(R.id.btn_play_pause)).perform(click());
        onView(withId(R.id.btn_play_pause)).perform(click());
        onView(withId(R.id.btn_play_pause)).perform(click());
        onView(withId(R.id.btn_play_pause)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_next)).perform(click());
    }
}