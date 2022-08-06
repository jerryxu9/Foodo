package com.example.foodo;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollable;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SearchForRestaurantInformationTest {

    private static final String TAG = "SearchForRestaurantInformationTest";
    private final String SEARCH_QUERY = "Tim Hortons";
    private final String INVALID_QUERY = "**&@(#$&";
    UiDevice mDevice;
    private static final int OBJECT_TIMEOUT = 20000;
    private static final int SHORTER_PAGE_LOAD_TIMEOUT = 40000;
    private static final int PAGE_LOAD_TIMEOUT = 60000;

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);
    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION");
    private IdlingResource searchQueryIdlingResource;

    static Matcher<View> childAtPosition(
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

    @Before
    public void registerIdlingResource() {
        ActivityScenario<MainActivity> activityScenario = ActivityScenario.launch(MainActivity.class);
        activityScenario.onActivity(new ActivityScenario.ActivityAction<MainActivity>() {
            /**
             * This method is invoked on the main thread with the reference to the Activity.
             *
             * @param activity an Activity instrumented by the {@link ActivityScenario}. It never be null.
             */
            @Override
            public void perform(MainActivity activity) {
                searchQueryIdlingResource = activity.getSearchQueryCountingIdlingResource();
                IdlingRegistry.getInstance().register(searchQueryIdlingResource);
            }
        });
    }


    @Test
    public void searchForRestaurantInformationTest() throws InterruptedException, UiObjectNotFoundException {
        login();

        Log.d(TAG, "Click Search View");
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

        Log.d(TAG, "Type Tim Hortons in Search Bar");
        ViewInteraction searchAutoComplete = onView(
                allOf(withClassName(is("android.widget.SearchView$SearchAutoComplete")),
                        childAtPosition(
                                allOf(withClassName(is("android.widget.LinearLayout")),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                0),
                        isDisplayed()));
        searchAutoComplete.perform(replaceText(SEARCH_QUERY), closeSoftKeyboard());

        ViewInteraction editText = onView(
                allOf(IsInstanceOf.instanceOf(android.widget.EditText.class), withText(SEARCH_QUERY),
                        withParent(allOf(IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                withParent(IsInstanceOf.instanceOf(android.widget.LinearLayout.class)))),
                        isDisplayed()));
        editText.check(matches(withText(SEARCH_QUERY)));

        Log.d(TAG, "Click Search Button");

        searchAutoComplete.perform(pressImeActionButton());

        mDevice.wait(Until.findObject(By.res("com.example.foodo", "com.example.foodo:id/search_list")), OBJECT_TIMEOUT);

        Log.d(TAG, "Check search results page displays Tim Hortons in top bar");
        ViewInteraction textView = onView(
                allOf(withIndex(withId(R.id.search_text), 0),
                        withParent(withParent(withId(android.R.id.content))),
                        isDisplayed()));
        textView.check(matches(withText(SEARCH_QUERY)));

        Log.d(TAG, "Assert search results list exists");
        ViewInteraction relativeLayout = onView(
                allOf(withIndex(withId(R.id.restaurant_card_relative_layout), 0),
                        withParent(withParent(withId(R.id.search_list))),
                        isDisplayed()));
        relativeLayout.check(matches(isDisplayed()));

        Log.d(TAG, "Check Tim Hortons is displayed in the search result");
        ViewInteraction textView2 = onView(
                allOf(withId(R.id.restaurantName), withText(SEARCH_QUERY),
                        withParent(allOf(withIndex(withId(R.id.restaurant_card_relative_layout), 0),
                                withParent(IsInstanceOf.instanceOf(android.widget.FrameLayout.class)))),
                        isDisplayed()));
        textView2.check(matches(withText(SEARCH_QUERY)));

        Log.d(TAG, "Click first search result for Tim Hortons query");
        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.search_list),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                1)));
        recyclerView.perform(actionOnItemAtPosition(0, click()));

        Log.d(TAG, "Check Tim Hortons is in the restaurant name field");
        ViewInteraction textView3 = onView(
                allOf(withId(R.id.restaurantName_info), withText(SEARCH_QUERY),
                        withParent(withParent(withId(R.id.linearLayout))),
                        isDisplayed()));
        textView3.check(matches(withText(SEARCH_QUERY)));

        Log.d(TAG, "Check Reviews text is displayed in the restaurant info page");
        ViewInteraction textView4 = onView(
                allOf(withId(R.id.Review_title), withText("Reviews"),
                        withParent(withParent(IsInstanceOf.instanceOf(ViewGroup.class))),
                        isDisplayed()));
        textView4.check(matches(withText("Reviews")));

        Log.d(TAG, "Check Hours text is displayed in the restaurant info page");
        ViewInteraction textView5 = onView(
                allOf(withText("Hours"),
                        withParent(allOf(withId(R.id.linearLayout),
                                withParent(IsInstanceOf.instanceOf(ViewGroup.class)))),
                        isDisplayed()));
        textView5.check(matches(withText("Hours")));

        Log.d(TAG, "Check Address text is displayed in the restaurant info page");
        ViewInteraction textView6 = onView(
                allOf(withId(R.id.restaurantAddress_info),
                        withParent(allOf(withId(R.id.linearLayout),
                                withParent(IsInstanceOf.instanceOf(ViewGroup.class)))),
                        isDisplayed()));
        textView6.check(matches(isDisplayed()));

        Log.d(TAG, "Check Phone Number text is displayed in the restaurant info page");
        ViewInteraction textView7 = onView(
                allOf(withId(R.id.restaurantNumber_info),
                        withParent(allOf(withId(R.id.linearLayout),
                                withParent(IsInstanceOf.instanceOf(ViewGroup.class)))),
                        isDisplayed()));
        textView7.check(matches(isDisplayed()));

        Log.d(TAG, "Check Rating text is displayed in the restaurant info page");
        ViewInteraction textView8 = onView(
                allOf(withId(R.id.restaurantRating_info),
                        withParent(withParent(withId(R.id.linearLayout))),
                        isDisplayed()));
        textView8.check(matches(isDisplayed()));

        Log.d(TAG, "Navigate back to the Search Results Page");
        pressBack();

        Log.d(TAG, "Navigate back to the Main Page");
        pressBack();

        Log.d(TAG, "Enter in an invalid string to the search bar");
        searchAutoComplete.perform(replaceText(INVALID_QUERY), closeSoftKeyboard());

        Log.d(TAG, "Click search using the invalid query");
        searchAutoComplete.perform(pressImeActionButton());

        Thread.sleep(2000);

        Log.d(TAG, "Check that no search results appear");
        recyclerView.check(new RecyclerViewItemCountAssertion(0));

        logout();
    }

    private void logout() {
        pressBack();
        pressBack();

        onView(withId(R.id.logout_button)).perform(click());
    }

    private void login() throws UiObjectNotFoundException, InterruptedException {
        UiObject loginButton = mDevice.findObject(new UiSelector()
                .text("LOGIN")
                .className("android.widget.Button"));
        loginButton.waitForExists(OBJECT_TIMEOUT);
        loginButton.clickAndWaitForNewWindow();

        UiObject2 emailInput = mDevice.wait(Until.findObject(By
                .clazz(EditText.class)), PAGE_LOAD_TIMEOUT);

        emailInput.setText("cpen321espresso@gmail.com");
        emailInput.wait(Until.textEquals("cpen321espresso@gmail.com"), OBJECT_TIMEOUT);
        Thread.sleep(2000);

        mDevice.click(896, 1930);
        mDevice.click(896, 1930);
        mDevice.wait(Until.hasObject(By.text("Hi Test").clazz(TextView.class)), SHORTER_PAGE_LOAD_TIMEOUT);

        UiObject2 passwordInput = mDevice.wait(Until.findObject(By
                .clazz(EditText.class)), OBJECT_TIMEOUT);

        passwordInput.setText("cpen#@!espresso");// type your password here
        passwordInput.wait(Until.textEquals("cpen#@!espresso"), OBJECT_TIMEOUT);

        //check that password is filled
        //Note: it returns *******, probably bc of security, so
        //just compare the length instead
        assertEquals(passwordInput.getText().length(), 15);

        Thread.sleep(2000);

        // Confirm Button Click
        UiObject2 nextButton = mDevice.wait(Until.findObject(By
                .textContains("N")
                .clazz("android.widget.Button")), OBJECT_TIMEOUT);

        nextButton.click();

        mDevice.waitForWindowUpdate("com.google.android.gms", PAGE_LOAD_TIMEOUT);

        UiObject2 agreeTermsOfService = mDevice.wait(Until.findObject(By
                .text("I agree")
                .clazz("android.widget.Button")), OBJECT_TIMEOUT);

        agreeTermsOfService.click();

        mDevice.wait(Until.hasObject(By.textContains("Tap to learn more about each service")), SHORTER_PAGE_LOAD_TIMEOUT);

        UiScrollable scrollToAccept = new UiScrollable(
                new UiSelector().scrollable(true));
        scrollToAccept.waitForExists(OBJECT_TIMEOUT);
        scrollToAccept.scrollToEnd(10);

        UiObject acceptButton = mDevice.findObject(new UiSelector().text("ACCEPT"));
        acceptButton.clickAndWaitForNewWindow(SHORTER_PAGE_LOAD_TIMEOUT);

        mDevice.wait(Until.hasObject(By.text("Foodo")), PAGE_LOAD_TIMEOUT);
        mDevice.click(133, 496);
    }


    @After
    public void unregisterIdlingResource() {
        if (searchQueryIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(searchQueryIdlingResource);
        }
    }


    /**
     * Custom ViewAssertion to check Recycler View
     * <p>
     * Source: https://stackoverflow.com/a/37339656
     */
    public static class RecyclerViewItemCountAssertion implements ViewAssertion {
        private final int expectedCount;

        public RecyclerViewItemCountAssertion(int expectedCount) {
            this.expectedCount = expectedCount;
        }

    @Override
    public void check(View view, NoMatchingViewException noViewFoundException) {
        if (noViewFoundException != null) {
            throw noViewFoundException;
        }

        RecyclerView recyclerView = (RecyclerView) view;
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        ViewMatchers.assertThat(adapter.getItemCount(), is(expectedCount));
        }
    }
}
