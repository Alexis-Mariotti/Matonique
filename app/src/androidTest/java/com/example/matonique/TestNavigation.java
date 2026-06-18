package com.example.matonique;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
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
public class TestNavigation {

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.READ_MEDIA_AUDIO",
                    "android.permission.POST_NOTIFICATIONS");
/*
    @Before
    public void setUp() {
        // on copie les musiques pour le test
        EspressoUtils.copyAllTestsAssets();
    }
*/
    @Test
    public void testNavigation() {
// 1. Vérifie que le chemin du texte affiche bien le bon dossier et est visible
        onView(withId(R.id.txt_current_path))
                .check(matches(allOf(withText("/storage/emulated/0/Music"), isDisplayed())));

        // 2. Vérifie que la liste des musiques (RecyclerView) est bien affichée
        onView(withId(R.id.recycler_music))
                .check(matches(isDisplayed()));

        // 3. Vérifie que le conteneur principal de fragments est bien présent
        onView(withId(R.id.fragment_container))
                .check(matches(isDisplayed()));

        // 4. Vérifie que le bouton Retour est visible et contient le bon texte
        onView(withId(R.id.buttonBack))
                .check(matches(allOf(withText("⬆ Retour"), isDisplayed())));

        // 5. Vérifie que le bouton Home est affiché
        onView(withId(R.id.buttonHome))
                .check(matches(isDisplayed()));

        // 6. Vérifie que la barre de navigation du bas est affichée
        onView(withId(R.id.bottom_navigation))
                .check(matches(isDisplayed()));
    }
}

