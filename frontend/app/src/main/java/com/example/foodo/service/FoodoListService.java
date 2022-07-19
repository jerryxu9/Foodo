package com.example.foodo.service;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodo.R;
import com.example.foodo.objects.FoodoListCard;
import com.example.foodo.objects.FoodoListCardAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FoodoListService {

    private final String TAG = "FoodoListService";
    /*private final String BASE_URL = "http://20.51.215.223:3000";*/
    private final String BASE_URL = "http://10.0.2.2:3000";
    private final AppCompatActivity main_activity;
    private final FoodoListCardAdapter foodoListCardAdapter;
    private final LinearLayoutManager linearLayoutManager;
    private final ArrayList<FoodoListCard> foodoListCardArrayList;
    private final OkHttpClient client = new OkHttpClient();
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private String userID, username;
    private RecyclerView foodoLists;
    private FloatingActionButton createFoodoListButton, refreshButton;
    private PopupWindow createFoodoListPopupWindow, loginDecisionPopupWindow;

    public FoodoListService(AppCompatActivity activity) {
        this.main_activity = activity;
        foodoListCardArrayList = new ArrayList<>();
        foodoListCardAdapter = new FoodoListCardAdapter(main_activity, foodoListCardArrayList, client);
        linearLayoutManager = new LinearLayoutManager(main_activity, LinearLayoutManager.VERTICAL, false);
    }

    public void setup() {
        createFoodoListButton = main_activity.findViewById(R.id.create_foodo_list_button);
        createFoodoListButton.setOnClickListener((View v) -> {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(main_activity);
            if (account != null) {
                Log.d(TAG, "User is logged in, allowing them to create FoodoList");
                if (userID == null) {
                    Log.d(TAG, "need to fetch user ID before creating foodo list foodoList");
                    createUser(account.getIdToken(), account.getDisplayName(), account.getEmail());
                }
                handleCreateFoodoListAction();
            } else {
                handleNonLoggedInUser();
            }
        });

        refreshButton = main_activity.findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener((View v) -> {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(main_activity);
            if (account != null) {
                if (userID == null) {
                    createUser(account.getIdToken(), account.getDisplayName(), account.getEmail());
                }
                refreshFoodoLists();
            }
        });

        foodoLists = main_activity.findViewById(R.id.foodo_lists);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(main_activity);
        if (account != null && userID == null) {
            //no other way to get the token, just go through createUser endpoint
            //and get the id from the existing entry in the database

            //Have to duplicate this method from MainActivity too :( DRY who
            createUser(account.getIdToken(), account.getDisplayName(), account.getEmail());
        }

        foodoLists.setLayoutManager(linearLayoutManager);
        foodoLists.setAdapter(foodoListCardAdapter);
    }


    public void refreshFoodoLists() {
        foodoListCardAdapter.clearFoodoLists();
        loadFoodoLists();
    }

    public void loadFoodoLists() {
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
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("userID", userID);
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

    private void handleCreateFoodoListAction() {
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

        container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
    }

    private void createFoodoList(ViewGroup container) throws IOException {
        String url = BASE_URL + "/createFoodoList";
        HttpUrl httpUrl = HttpUrl.parse(url);

        if (httpUrl == null) {
            Log.d(TAG, String.format("unable to parse server URL: %s", url));
            return;
        }

        if (userID == null || username == null) {
            return;
        }

        EditText foodoListNameInput = container.findViewById(R.id.enter_foodo_list_name_edit_text);
        String foodoListName = foodoListNameInput.getText().toString();

        // Remove trailing whitespace on text input before checking if it's empty
        if (foodoListName.trim().isEmpty()) {
            Log.d(TAG, "Unable to submit empty foodoListName");
            return;
        }
        String json = String.format("{\"userID\": \"%s\", \"listName\": \"%s\"}", userID, foodoListName.trim());
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), json);

        HttpUrl.Builder httpBuilder = httpUrl.newBuilder();

        Request request = new Request.Builder()
                .url(httpBuilder.build())
                .post(body)
                .build();

        client.newCall((request)).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful())
                        throw new IOException(String.format("Unexpected code %s", response));
                    else if (responseBody == null) {
                        throw new IOException("null response from /getFoodoLists endpoint");
                    } else {
                        JSONObject createdFoodoList = new JSONObject(responseBody.string());
                        String listName = createdFoodoList.getString("name");
                        String listID = createdFoodoList.getString("_id");
                        main_activity.runOnUiThread(() -> {
                            foodoListCardAdapter.addFoodoList(new FoodoListCard(listName, listID, username, userID));
                            createFoodoListPopupWindow.dismiss();
                        });
                        Log.d(TAG, String.format("Foodo list %s was successfully created. Result: %s", foodoListName, listName));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void createUser(String idToken, String user, String email) {
        String url = buildURL("/createUser");
        HttpUrl httpUrl = HttpUrl.parse(url);

        if (httpUrl == null) {
            Log.d(TAG, String.format("unable to parse server URL: %s", url));
            return;
        }
        Log.d(TAG, user);

        Map<String, String> params = new HashMap<>();
        params.put("id", idToken);
        params.put("name", user);
        params.put("email", email);

        JSONObject paramsJSON = new JSONObject(params);
        RequestBody body = RequestBody.create(paramsJSON.toString(), JSON);
        Request request = new Request.Builder()
                .url(httpUrl)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBodyString;
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        Log.d(TAG, responseBody.string());
                    } else {
                        responseBodyString = responseBody.string();
                        JSONObject resJSON = new JSONObject(responseBodyString);
                        //seems that an invalid token doesn't respond with an error?
                        if (!resJSON.has("error")) {
                            //valid session, snatch that id and username
                            Log.d(TAG, responseBodyString);
                            JSONObject responseBodyJSON = new JSONObject(responseBodyString);

                            Log.d(TAG, responseBodyJSON.getString("_id"));
                            Log.d(TAG, responseBodyJSON.getString("name"));

                            userID = responseBodyJSON.getString("_id");
                            Log.d(TAG, "userID: " + userID);
                            username = responseBodyJSON.getString("name");

                            loadFoodoLists();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String buildURL(String path) {
        return BASE_URL + path;
    }
}

