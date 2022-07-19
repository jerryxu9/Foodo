package com.example.foodo;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddFoodoListTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void addFoodoListBlocksUserIfNotLoggedIn() {
        Espresso.onView(ViewMatchers.withId(R.id.create_foodo_list_button)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.login_warning_text)).check(ViewAssertions.matches(ViewMatchers.withText("Please log in to use this feature")));
    }
}