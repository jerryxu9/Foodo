package com.example.foodo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.assertThat;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollable;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;
import static org.hamcrest.CoreMatchers.notNullValue;

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
    private static final int PAGE_LOAD_TIMEOUT =30000;
    private static final int SHORTER_PAGE_LOAD_TIMEOUT = 10000;
    private static final int OBJECT_TIMEOUT = 5000;
    private static final int WAIT_FOR_CHANGE = 3000;
    private static final String BASIC_SAMPLE_PACKAGE
            = "com.example.foodo";

//    @Rule
//    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);
//
//    @Before
//    public void setUp() {
////        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
//
//    }

    @Before
    public void startMainActivityFromHomeScreen() {
        // Initialize UiDevice instance
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Start from the home screen
        mDevice.pressHome();

        // Wait for launcher
        final String launcherPackage = getLauncherPackageName();
        assertThat(launcherPackage, notNullValue());
        mDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), SHORTER_PAGE_LOAD_TIMEOUT);

        // Launch the blueprint app
        Context context = getApplicationContext();
        final Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(BASIC_SAMPLE_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);    // Clear out any previous instances
        context.startActivity(intent);

        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(BASIC_SAMPLE_PACKAGE).depth(0)), SHORTER_PAGE_LOAD_TIMEOUT);
    }

    @Test
    public void userCanAddAndDeleteReviews() throws UiObjectNotFoundException, InterruptedException {
        UiObject loginButton = mDevice.findObject(new UiSelector()
                .text("LOGIN")
                .className("android.widget.Button"));
        loginButton.waitForExists(OBJECT_TIMEOUT);
        loginButton.click();

        mDevice.wait(Until.hasObject(By.text("Sign in")), PAGE_LOAD_TIMEOUT);

        UiObject emailInput = mDevice.findObject(new UiSelector()
                .instance(0)
                .className(EditText.class));

        emailInput.waitForExists(OBJECT_TIMEOUT);
//        emailInput.click();
//        UiObject nextButton = mDevice.findObject(new UiSelector()
//                .text("Next"));
//        nextButton.waitForExists(SHORTER_PAGE_LOAD_TIMEOUT);
        emailInput.click();

        emailInput.setText("cpen321espresso@gmail.com");

        UiObject nextButton = mDevice.findObject(new UiSelector()
                .className("android.widget.Button")
                .textContains("N"));
        nextButton.waitForExists(OBJECT_TIMEOUT);
        nextButton.click();

//        //SOmetimes Google gives a pop up that we have to close
//        //We then have to click next again ugh
//        UiObject closeButton = mDevice.findObject(new UiSelector()
//                .className("android.widget.Button")
//                .textContains("Close"));
//
//        if(closeButton.exists()){
//            closeButton.click();
//            nextButton.click();
//        }

        mDevice.wait(Until.hasObject(By.text("Hi Test").clazz(TextView.class)), SHORTER_PAGE_LOAD_TIMEOUT);

        // Set Password
        UiObject passwordInput = mDevice.findObject(new UiSelector()
                .className(EditText.class));

        passwordInput.waitForExists(OBJECT_TIMEOUT);
        passwordInput.setText("cpen#@!espresso");// type your password here

        // Confirm Button Click
        nextButton = mDevice.findObject(new UiSelector()
                .className("android.widget.Button")
                .textContains("N"));

        nextButton.waitForExists(OBJECT_TIMEOUT);
        nextButton.click();

        //This doesn't always show up, if it does, we don't want to turn it on
        UiObject syncContacts = mDevice.findObject(new UiSelector()
                .text("Don't turn on"));
        if(syncContacts.exists()){
            syncContacts.longClick();
        }

        UiObject agreeTermsOfService = mDevice.findObject(new UiSelector()
                .text("I agree"));
        agreeTermsOfService.waitForExists(OBJECT_TIMEOUT);
        agreeTermsOfService.clickAndWaitForNewWindow();

        mDevice.wait(Until.hasObject(By.textContains("Tap to learn more about each service")), SHORTER_PAGE_LOAD_TIMEOUT);

        UiScrollable scrollToAccept = new UiScrollable(
                new UiSelector().scrollable(true));
        scrollToAccept.waitForExists(OBJECT_TIMEOUT);
        scrollToAccept.scrollToEnd(10);

        UiObject acceptButton = mDevice.findObject(new UiSelector().text("ACCEPT"));
        acceptButton.clickAndWaitForNewWindow(SHORTER_PAGE_LOAD_TIMEOUT);

        startMainActivityFromHomeScreen();

        loginButton = mDevice.findObject(new UiSelector()
                .text("LOGIN")
                .className("android.widget.Button"));
        loginButton.waitForExists(OBJECT_TIMEOUT);
        loginButton.click();

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
}