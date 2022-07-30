package com.example.foodo;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollable;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ManageFoodoListTest {

    private static final int PAGE_LOAD_TIMEOUT = 60000;
    private static final int SHORTER_PAGE_LOAD_TIMEOUT = 40000;
    private static final int OBJECT_TIMEOUT = 20000;
    private static final String BASIC_SAMPLE_PACKAGE
            = "com.example.foodo";
    private static final String TAG = "ManageFoodoListTest";
    UiDevice mDevice;

    @Before
    public void startMainActivityFromHomeScreen() {
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

    @Test
    public void manageFoodoListTest() throws UiObjectNotFoundException {

        Log.d(TAG, "Log in Action begins");
        UiObject loginButton = mDevice.findObject(new UiSelector()
                .text("LOGIN")
                .className("android.widget.Button"));
        loginButton.waitForExists(OBJECT_TIMEOUT);
        loginButton.clickAndWaitForNewWindow();

        Log.d(TAG, "Enter Email");
        UiObject2 emailInput = mDevice.wait(Until.findObject(By
                .clazz(EditText.class)), PAGE_LOAD_TIMEOUT);

        emailInput.click();
        emailInput.setText("cpen321espresso@gmail.com");
        emailInput.wait(Until.textEquals("cpen321espresso@gmail.com"), OBJECT_TIMEOUT);
        assertEquals(emailInput.getText(), "cpen321espresso@gmail.com");

        UiObject2 nextButton = mDevice.wait(Until.findObject(By
                .textContains("N")
                .clazz("android.widget.Button")), OBJECT_TIMEOUT);

        nextButton.click();

        mDevice.wait(Until.hasObject(By.text("Hi Test").clazz(TextView.class)), SHORTER_PAGE_LOAD_TIMEOUT);

        Log.d(TAG, "Enter Password");
        UiObject2 passwordInput = mDevice.wait(Until.findObject(By
                .clazz(EditText.class)), OBJECT_TIMEOUT);

        passwordInput.click();
        passwordInput.setText("cpen#@!espresso");// type your password here
        passwordInput.wait(Until.textEquals("cpen#@!espresso"), OBJECT_TIMEOUT);

        //check that password is filled
        //Note: it returns *******, probably bc of security, so
        //just compare the length instead
        assertEquals(passwordInput.getText().length(), 15);

        // Confirm Button Click
        nextButton = mDevice.wait(Until.findObject(By
                .textContains("N")
                .clazz("android.widget.Button")), OBJECT_TIMEOUT);

        nextButton.click();

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