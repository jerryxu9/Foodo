package com.example.foodo;

import android.view.View;
import android.widget.SearchView;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SearchForRestaurantInformationTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);


    @Test
    public void testUserClickSearchBarAction() {

//        Espresso.onView(ViewMatchers.withId(R.id.restaurant_search)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.restaurant_search))
            .perform(ViewActions.typeText("Subway"), ViewActions.click());

      Espresso.onView(ViewMatchers.withId(R.id.restaurant_search)).check(ViewAssertions.matches(ViewMatchers.withText("Subway")));

       /* String searchText = "test";
        Espresso.onView(ViewMatchers.withId(R.id.restaurant_search)).perform(ViewActions.typeText(searchText), ViewActions.pressImeActionButton());*/

    }

}
