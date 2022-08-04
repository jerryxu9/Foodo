package com.example.foodo.service;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.foodo.R;
import com.example.foodo.objects.FoodoListCard;
import com.example.foodo.objects.FoodoListCardAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FoodoListService {
    private final String TAG = "FoodoListService";
    private final AppCompatActivity main_activity;
    private final FoodoListCardAdapter foodoListCardAdapter;
    private String userID;
    private String username;
    private PopupWindow createFoodoListPopupWindow;
    private PopupWindow loginDecisionPopupWindow;

    public FoodoListService(AppCompatActivity activity, FoodoListCardAdapter foodoListCardAdapter) {
        this.main_activity = activity;
        this.foodoListCardAdapter = foodoListCardAdapter;
    }

    public void createFoodoList() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(main_activity);
        if (account != null) {
            Log.d(TAG, "User is logged in, allowing them to create FoodoList");
            if (userID == null) {
                Log.d(TAG, "need to fetch user ID before creating foodo list foodoList");
                createUser(account.getIdToken(), account.getDisplayName(), account.getEmail());
            }
            renderCreateFoodoListPopup();
        } else {
            Log.d(TAG, "User is not logged in");
            handleNonLoggedInUser();
        }
    }

    public void setupUserAccount() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(main_activity);
        if (account != null && userID == null) {
            //no other way to get the token, just go through createUser endpoint
            //and get the id from the existing entry in the database
            createUser(account.getIdToken(), account.getDisplayName(), account.getEmail());
        }
    }

    public void setUserID(String userID){
        this.userID = userID;
    }

    public void loadFoodoLists() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("userID", userID);
        
        Callback loadFoodoListCallback = new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    String result = OKHttpService.getResponseBody(response);
                    foodoListCardAdapter.clearFoodoLists();
                    JSONArray foodoListJSONArray = new JSONArray(result);

                    for (int i = 0; i < foodoListJSONArray.length(); i++) {
                        JSONObject foodoListJSON = (JSONObject) foodoListJSONArray.get(i);
                        String id = foodoListJSON.getString("_id");
                        String name = foodoListJSON.getString("name");

                        Log.d(TAG, String.format("Loaded Foodo List '%s' with id: %s", name, id));

                        FoodoListCard card = new FoodoListCard(foodoListJSON.getString("name"), id, username, userID);
                        foodoListCardAdapter.addFoodoList(card);
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }
        };

        OKHttpService.getRequest("getFoodoLists", loadFoodoListCallback, queryParameters);

    }

    private void handleNonLoggedInUser() {
        Log.d(TAG, "User is not logged in");
        LayoutInflater layoutInflater = (LayoutInflater) main_activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.activity_login_popup, null);

        ConstraintLayout loginDecisionConstraintLayout = main_activity.findViewById(R.id.constraint);

        loginDecisionPopupWindow = new PopupWindow(container, 800, 800, true);
        loginDecisionPopupWindow.showAtLocation(loginDecisionConstraintLayout, Gravity.CENTER, 0, 0);

        container.findViewById(R.id.login_cancel_button).setOnClickListener((View v) -> {
            Log.d(TAG, "Exit pop up");
            loginDecisionPopupWindow.dismiss();
        });
    }

    private void renderCreateFoodoListPopup() {
        LayoutInflater layoutInflater = (LayoutInflater) main_activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.create_foodo_list_popup, null);

        ConstraintLayout createFoodoListConstraintLayout = main_activity.findViewById(R.id.constraint);

        createFoodoListPopupWindow = new PopupWindow(container, 800, 800, true);
        createFoodoListPopupWindow.showAtLocation(createFoodoListConstraintLayout, Gravity.CENTER, 0, 0);

        container.findViewById(R.id.create_foodo_list_confirm_button).setOnClickListener((View v) -> {
            try {
                createFoodoList(container);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        container.findViewById(R.id.create_foodo_list_cancel_button).setOnClickListener((View v) -> {
            Log.d(TAG, "Cancelled creating Foodo list");
            createFoodoListPopupWindow.dismiss();
        });

        container.setOnTouchListener((v, event) -> false);
    }

    private void createFoodoList(ViewGroup container) throws IOException {
        if (userID == null) {
            return;
        }

        EditText foodoListNameInput = container.findViewById(R.id.enter_foodo_list_name_edit_text);
        String foodoListName = foodoListNameInput.getText().toString();

        // Remove trailing whitespace on text input before checking if it's empty
        if (foodoListName.trim().isEmpty()) {
            Log.d(TAG, "Unable to submit empty foodoListName");
            return;
        }

        Callback createFoodoListCallback = new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    JSONObject createdFoodoList = new JSONObject(OKHttpService.getResponseBody(response));
                    String listName = createdFoodoList.getString("name");
                    String listID = createdFoodoList.getString("_id");
                    main_activity.runOnUiThread(() -> {
                        foodoListCardAdapter.addFoodoList(new FoodoListCard(listName, listID, username, userID));
                        createFoodoListPopupWindow.dismiss();
                    });
                    Log.d(TAG, String.format("Foodo list %s was successfully created. Result: %s", foodoListName, listName));
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
        };

        HashMap<String, String> createFoodoListParams = new HashMap<>();
        createFoodoListParams.put("userID", userID);
        createFoodoListParams.put("listName", foodoListName.trim());

        OKHttpService.postRequest("createFoodoList", createFoodoListCallback, createFoodoListParams);
    }

    private void createUser(String idToken, String user, String email) {
        Callback createUserCallback = new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseBodyString = OKHttpService.getResponseBody(response);
                    Log.d(TAG, responseBodyString);
                    JSONObject resJSON = new JSONObject(responseBodyString);
                    Log.d(TAG, resJSON.toString());
                    if (!resJSON.has("error")) {
                        //valid session
                        Log.d(TAG, resJSON.getString("_id"));
                        Log.d(TAG, resJSON.getString("name"));

                        userID = resJSON.getString("_id");
                        Log.d(TAG, "userID: " + userID);
                        username = resJSON.getString("name");
                        loadFoodoLists();
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }
                
            }
        };
        Log.d(TAG, user);

        HashMap<String, String> createUserParams = new HashMap<>();
        createUserParams.put("id", idToken);
        createUserParams.put("name", user);
        createUserParams.put("email", email);

        OKHttpService.postRequest("createUser", createUserCallback, createUserParams);
    }
}

