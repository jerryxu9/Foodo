package com.example.foodo;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.example.foodo.SearchForRestaurantInformationTest.childAtPosition;
import static com.example.foodo.SearchForRestaurantInformationTest.withIndex;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.ViewInteraction;
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

import com.example.foodo.SearchForRestaurantInformationTest.RecyclerViewItemCountAssertion;

import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ManageFoodoListTest {

    private static final int SHORTER_PAGE_LOAD_TIMEOUT = 40000;
    private static final int OBJECT_TIMEOUT = 20000;
    private static final String BASIC_SAMPLE_PACKAGE
            = "com.example.foodo";
    private static final String TAG = "ManageFoodoListTest";
    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION");
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);
    UiDevice mDevice;
    private IdlingResource searchQueryIdlingResource;

    private void startMainActivityFromHomeScreen() {

        Log.d(TAG, "Initialize UiDevice instance");
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Start from the home screen
        mDevice.pressHome();

        Log.d(TAG, "Wait for launcher");
        final String launcherPackage = getLauncherPackageName();
        assertThat(launcherPackage, notNullValue());
        mDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), SHORTER_PAGE_LOAD_TIMEOUT);

        Log.d(TAG, "Launch Blueprint app");
        Context context = getApplicationContext();
        final Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(BASIC_SAMPLE_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);    // Clear out any previous instances
        context.startActivity(intent);

        Log.d(TAG, "Wait for the app to appear");
        mDevice.wait(Until.hasObject(By.pkg(BASIC_SAMPLE_PACKAGE).depth(0)), SHORTER_PAGE_LOAD_TIMEOUT);

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

    /**
     * Test implemented for M6
     */
    @Test
    public void addFoodoListBlocksUserIfNotLoggedInTest() {
        onView(withId(R.id.create_foodo_list_button)).perform(click());
        onView(withId(R.id.login_warning_text)).check(matches(withText("Please log in to use this feature")));
    }

    @Test
    public void addRestaurantToFoodoListBlocksUserIfNotLoggedInTest() throws InterruptedException {

        final String SEARCH_QUERY = "Tim Hortons";

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

        Thread.sleep(3000);

        Log.d(TAG, "Click on the first search entry");

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.search_list),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                1)));
        recyclerView.perform(actionOnItemAtPosition(0, click()));

        Log.d(TAG, "Click Button to add Tim Hortons entry to Foodo List");
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.add_restaurant_to_list_button),
                        isDisplayed()));

        appCompatButton.perform(click());

        onView(withId(R.id.login_warning_text)).check(matches(withText("Please log in to use this feature")));
    }

    @Test
    public void manageFoodoListTest() throws UiObjectNotFoundException, InterruptedException {

        startMainActivityFromHomeScreen();

        UiObject loginButton = mDevice.findObject(new UiSelector()
                .text("LOGIN")
                .className("android.widget.Button"));
        loginButton.waitForExists(OBJECT_TIMEOUT);
        loginButton.click();

        Log.d(TAG, "Select existing account");
        mDevice.wait(Until.hasObject(By.text("Choose an account")), OBJECT_TIMEOUT);
        mDevice.click(538, 1099);

        Log.d(TAG, "Select Create Foodo List Button");
        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.create_foodo_list_button), withContentDescription("Create Foodo List"),
                        isDisplayed()));
        floatingActionButton.perform(click());

        Log.d(TAG, "Enter name of Foodo List to be 'Yummy Food'");
        ViewInteraction editText = onView(
                allOf(withId(R.id.enter_foodo_list_name_edit_text),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                0),
                        isDisplayed()));
        editText.perform(replaceText("Yummy Food"), closeSoftKeyboard());

        Log.d(TAG, "Create Foodo List called Yummy Food by clicking confirmation button");
        ViewInteraction button = onView(
                allOf(withId(R.id.create_foodo_list_confirm_button), withText("Create Foodo List"),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                1),
                        isDisplayed()));
        button.perform(click());

        Log.d(TAG, "Check that Recycler View has one item");
        ViewInteraction foodoListCardRecyclerView = onView(
                allOf(withId(R.id.foodo_lists),
                        childAtPosition(
                                withId(R.id.constraint),
                                6)));

        foodoListCardRecyclerView.check(new RecyclerViewItemCountAssertion(1));

        Log.d(TAG, "Check that Recycler View item has its item labeled Yummy Food");
        ViewInteraction textView = onView(
                allOf(withId(R.id.foodo_list_name), withText("Yummy Food"),
                        withParent(withParent(withId(R.id.foodo_list_card_view))),
                        isDisplayed()));
        textView.check(matches(withText("Yummy Food")));

        Log.d(TAG, "Click on Create Foodo List Button again");

        ViewInteraction floatingActionButton2 = onView(
                allOf(withId(R.id.create_foodo_list_button), withContentDescription("Create Foodo List"),
                        childAtPosition(
                                allOf(withId(R.id.constraint),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                5),
                        isDisplayed()));
        floatingActionButton2.perform(click());

        Log.d(TAG, "Enter an empty string as the Foodo List name");
        ViewInteraction editText2 = onView(
                allOf(withId(R.id.enter_foodo_list_name_edit_text),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                0),
                        isDisplayed()));
        editText2.perform(replaceText("    "), closeSoftKeyboard());

        Log.d(TAG, "Click the Create Foodo List button");
        ViewInteraction button2 = onView(
                allOf(withId(R.id.create_foodo_list_confirm_button), withText("Create Foodo List"),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                1),
                        isDisplayed()));
        button2.perform(click());

        Log.d(TAG, "Check that the view does not change and that we remain on the Create Foodo List popup");
        ViewInteraction button3 = onView(
                allOf(withId(R.id.create_foodo_list_confirm_button), withText("Create Foodo List"),
                        withParent(withParent(IsInstanceOf.instanceOf(android.widget.FrameLayout.class))),
                        isDisplayed()));
        button3.check(matches(isDisplayed()));

        Log.d(TAG, "Exit the Create Foodo List popup via the cancel button");
        ViewInteraction button4 = onView(
                allOf(withId(R.id.create_foodo_list_cancel_button),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                2),
                        isDisplayed()));
        button4.perform(click());

        Log.d(TAG, "Search for restaurant");
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
        searchAutoComplete.perform(replaceText("Tim Hortons"), closeSoftKeyboard());

        searchAutoComplete.perform(pressImeActionButton());

        Thread.sleep(3000);

        Log.d(TAG, "Click first search result for Tim Hortons query");

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.search_list),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                1)));
        recyclerView.perform(actionOnItemAtPosition(0, click()));

        Log.d(TAG, "Add restaurant to Foodo List");
        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.add_restaurant_to_list_button),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton2.perform(click());

        Log.d(TAG, "Click button to add restaurant to list");
        ViewInteraction button5 = onView(
                allOf(withId(R.id.add_res_to_list_confirm_button),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                3),
                        isDisplayed()));
        button5.perform(click());

        Log.d(TAG, "Navigate back to MainActivity");
        pressBack();

        pressBack();

        Log.d(TAG, "Navigate into Yummy Food Foodo List");
        ViewInteraction recyclerView2 = onView(
                allOf(withId(R.id.foodo_lists),
                        childAtPosition(
                                withId(R.id.constraint),
                                6)));
        recyclerView2.perform(actionOnItemAtPosition(0, click()));

        Log.d(TAG, "Check Tim Hortons is listed in Yummy Food Foodo List");
        ViewInteraction textView2 = onView(
                allOf(withId(R.id.restaurantName), withText("Tim Hortons"),
                        withParent(allOf(withId(R.id.restaurant_card_relative_layout),
                                withParent(IsInstanceOf.instanceOf(android.widget.FrameLayout.class)))),
                        isDisplayed()));
        textView2.check(matches(withText("Tim Hortons")));

        Log.d(TAG, "Check Restaurant");
        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.check_status),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.restaurant_card_relative_layout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatButton3.perform(click());

        Log.d(TAG, "UnCheck restaurant on Foodo List via swipe action");
        onView(withIndex(allOf(withId(R.id.restaurant_card_relative_layout), isDisplayed()), 0)).perform(swipeRight());

        Log.d(TAG, "Delete restaurant from Foodo List via swipe action");
        onView(withIndex(allOf(withId(R.id.restaurant_card_relative_layout), isDisplayed()), 0)).perform(swipeLeft());

        Log.d(TAG, "Navigate back to main activity");
        pressBack();

        Log.d(TAG, "Delete Yummy Food Foodo List using swiping action");
        ViewInteraction recyclerView5 = onView(
                allOf(withId(R.id.foodo_lists),
                        withParent(allOf(withId(R.id.constraint),
                                withParent(withId(android.R.id.content)))),
                        isDisplayed()));
        recyclerView5.perform(swipeLeft());


        onView(withIndex(allOf(withId(R.id.foodo_list_relative_view), isDisplayed()), 0)).perform(swipeLeft());

        Log.d(TAG, "Check Foodo List is no longer rendered");

        recyclerView5.check(new RecyclerViewItemCountAssertion(0));

        Log.d(TAG, "Close keyboard when returning from Search Results Activity");

        searchAutoComplete.perform(closeSoftKeyboard());

        Log.d(TAG, "Logout to reset app state");

        onView(allOf(withId(R.id.logout_button), isDisplayed())).perform(click());
    }


    private String getLauncherPackageName() {
        // Create launcher Intent
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        // Use PackageManager to get the launcher package name
        PackageManager pm = getApplicationContext().getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo.activityInfo.packageName;
    }

    @After
    public void unregisterIdlingResource() {
        if (searchQueryIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(searchQueryIdlingResource);
        }
    }


}