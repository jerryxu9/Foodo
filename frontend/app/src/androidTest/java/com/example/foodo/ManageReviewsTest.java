package com.example.foodo;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;

import androidx.test.uiautomator.UiObjectNotFoundException;

import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import org.junit.Before;
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
public class ManageReviewsTest {

    UiDevice mDevice;
    private static final int OBJECT_TIMEOUT = 20000;
    private final String REVIEW_NAME = "Test Account";
    private final String REVIEW_TEXT = "hello world";
    private final String REVIEW_RATING = "3 stars";
    private final String SEARCH_QUERY = "Tim Hortons";

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION");

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    /**
     * Matcher to pick a single view in an Activity with multiple views
     * with the same resID, text, or content description
     * <p>
     * Source: https://stackoverflow.com/a/39756832
     */
    public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<View>() {
            int currentIndex = 0;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: ");
                description.appendValue(index);
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
            }
        };
    }

    @Before
    public void initializeUiDevice() {
        // Initialize UiDevice instance
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    @Test
    public void userCanAddAndDeleteReviews() throws InterruptedException {
        ViewInteraction appCompatImageView = onView(
                allOf(withClassName(is("androidx.appcompat.widget.AppCompatImageView")), withContentDescription("Search"),
                        childAtPosition(
                                allOf(withClassName(is("android.widget.LinearLayout")),
                                        childAtPosition(
                                                withId(R.id.restaurant_search),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageView.perform(click());

        ViewInteraction searchAutoComplete = onView(
                allOf(withClassName(is("android.widget.SearchView$SearchAutoComplete")),
                        childAtPosition(
                                allOf(withClassName(is("android.widget.LinearLayout")),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                0),
                        isDisplayed()));
        searchAutoComplete.perform(replaceText(SEARCH_QUERY));


        searchAutoComplete.perform(pressImeActionButton());

        Thread.sleep(1000);

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.search_list),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                1)));

        recyclerView.perform(actionOnItemAtPosition(0, click()));

        //Check that all the review related components are there
        onView(withId(R.id.reviewTextBox)).check(matches(isDisplayed()));
        onView(withId(R.id.reviewSendButton)).check(matches(isDisplayed()));
        onView(withId(R.id.choose_rating_spinner)).check(matches(isDisplayed()));

        //Type in body of review and check textbox text has actually been updated
        onView(withId(R.id.reviewTextBox)).perform(typeText(REVIEW_TEXT));
        Thread.sleep(1000);
        onView(withId(R.id.reviewTextBox)).check(matches(withText(REVIEW_TEXT)));

        //Ensure all rating options are displayed in the spinner
        //https://stackoverflow.com/questions/31420839/android-espresso-check-selected-spinner-text
        onView(withId(R.id.choose_rating_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("1"))).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is("2"))).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is("3"))).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is("4"))).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is("5"))).check(matches(isDisplayed()));

        onData(allOf(is(instanceOf(String.class)), is("3"))).perform(click());

        //Submit the review
        onView(withId(R.id.reviewSendButton)).perform(click());

        //Wait until the review shows up in the UI
        mDevice.wait(Until.hasObject(By.text(REVIEW_NAME)), OBJECT_TIMEOUT);

        //Check that review has populated recycler view and the data matches what was put
        onView(withId(R.id.review_list)).check(matches(hasChildCount(1)));
        onView(withId(R.id.reviewName)).check(matches(withText(REVIEW_NAME)));
        onView(withId(R.id.reviewText)).check(matches(withText(REVIEW_TEXT)));
        onView(withId(R.id.reviewRating)).check(matches(withText(REVIEW_RATING)));

        //Now delete the review
        onView(withId(R.id.deleteReview)).perform(click());

        //Wait for review to be deleted from UI
        mDevice.wait(Until.gone(By.text(REVIEW_NAME)), OBJECT_TIMEOUT);

        //Check that the recycler view has no children (review cards)
        onView(withId(R.id.review_list)).check(matches(hasChildCount(0)));
    }
}