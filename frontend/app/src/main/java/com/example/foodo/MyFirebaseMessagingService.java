package com.example.foodo;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Check if message contains a data payload.
        ArrayMap<String, String> body = (ArrayMap<String, String>) remoteMessage.getData();
        ArrayList<String> reviewBody = new ArrayList<String>(body.values());
        Log.d(TAG, reviewBody.get(0) + reviewBody.get(1) + reviewBody.get(2));
        if (body.size() > 0) {
            Intent myIntent = new Intent("FBR-IMAGE");
            myIntent.putStringArrayListExtra("action", reviewBody);
            this.sendBroadcast(myIntent);
            Log.d(TAG, "Message received");
        }
    }

    // [START on_new_token]
    /**
     * There are two scenarios when onNewToken is called:
     * 1) When a new token is generated on initial app startup
     * 2) Whenever an existing token is changed
     * Under #2, there are three scenarios when the existing token is changed:
     * A) App is restored to a new device
     * B) User uninstalls/reinstalls the app
     * C) User clears app data
     */
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }
}