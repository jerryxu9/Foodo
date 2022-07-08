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
import android.widget.Toast;

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

    private final String USERID = "test@gmail.com";
    private final String BASE_URL = "http://10.0.2.2:3000";
    private final OkHttpClient client;
    private final AppCompatActivity main_activity;
    private final FoodoListCardAdapter foodoListCardAdapter;
    private final LinearLayoutManager linearLayoutManager;
    private final ArrayList<FoodoListCard> foodoListCardArrayList;
    private RecyclerView foodoLists;
    private FloatingActionButton createFoodoListButton;
    private PopupWindow createFoodoListPopupWindow;

    public FoodoListService(AppCompatActivity activity, OkHttpClient client) {
        this.main_activity = activity;
        this.client = client;
        foodoListCardArrayList = new ArrayList<>();
        foodoListCardAdapter = new FoodoListCardAdapter(main_activity, foodoListCardArrayList, client);
        linearLayoutManager = new LinearLayoutManager(main_activity, LinearLayoutManager.VERTICAL, false);
    }

    public void setup() {
        createFoodoListButton = main_activity.findViewById(R.id.create_foodo_list_button);
        createFoodoListButton.setOnClickListener((View v) -> handleCreateFoodoListAction());

        foodoLists = main_activity.findViewById(R.id.foodo_lists);
        initializeFoodoLists();
        foodoLists.setLayoutManager(linearLayoutManager);
        foodoLists.setAdapter(foodoListCardAdapter);
    }

    private void initializeFoodoLists() {
        String url = BASE_URL + "/getFoodoLists";
        HttpUrl httpUrl = HttpUrl.parse(url);

        if (httpUrl == null) {
            Log.d(TAG, String.format("unable to parse server URL: %s", url));
            return;
        }

        HttpUrl.Builder httpBuilder = httpUrl.newBuilder().addQueryParameter("userID", USERID);

        Request request = new Request.Builder()
                .url(httpBuilder.build())
                .build();

        client.newCall((request)).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful())
                        throw new IOException(String.format("Unexpected code %s", response));
                    else if (responseBody == null) {
                        throw new IOException("null response from /getFoodoLists endpoint");
                    } else {
                        JSONArray foodoListJSONArray = new JSONArray(responseBody.string());
                        for (int i = 0; i < foodoListJSONArray.length(); i++) {
                            JSONObject foodoListJSON = (JSONObject) foodoListJSONArray.get(i);
                            String id = foodoListJSON.getString("_id");
                            // Render only those FoodoListCards only if they did not already exist
                            if (!foodoListCardAdapter.checkIfIDExists(id)) {
                                Log.d(TAG, String.format("item %s", foodoListCardAdapter.getItemId(i)));
                                Log.d(TAG, String.format("add %s", foodoListJSON.getString("name")));
//                                foodoListCardArrayList.add(new FoodoListCard(foodoListJSON.getString("name"), id));
                                foodoListCardAdapter.addFoodoList(new FoodoListCard(foodoListJSON.getString("name"), id));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }
        });

    }

    private void handleCreateFoodoListAction() {
        Log.d(TAG, "Pressed add Foodo restaurant button");
        LayoutInflater layoutInflater = (LayoutInflater) main_activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.activity_create_foodo_list, null);

        ConstraintLayout createFoodoListConstraintLayout = main_activity.findViewById(R.id.constraint);

        createFoodoListPopupWindow = new PopupWindow(container, 800, 800, true);
        createFoodoListPopupWindow.showAtLocation(createFoodoListConstraintLayout, Gravity.CENTER, 0, 0);

        container.findViewById(R.id.create_foodo_list_confirm_button).setOnClickListener((View v) -> {
            try {
                authenticateAccount(GoogleSignIn.getLastSignedInAccount(main_activity));
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

    private void authenticateAccount(GoogleSignInAccount account){
        //user has never logged in
        if(account == null){

        }else{

        }

    }

    private void createFoodoList(ViewGroup container) throws IOException {
        String url = BASE_URL + "/createFoodoList";
        HttpUrl httpUrl = HttpUrl.parse(url);

        if (httpUrl == null) {
            Log.d(TAG, String.format("unable to parse server URL: %s", url));
            return;
        }

        EditText foodoListNameInput = container.findViewById(R.id.enter_foodo_list_name_edit_text);
        String foodoListName = foodoListNameInput.getText().toString();

        // Remove trailing whitespace on text input before checking if it's empty
        if (foodoListName == null || foodoListName.replaceAll("\\s", "") == "") {
            Log.d(TAG, "Unable to submit empty foodoListName");
            return;
        }
        String json = String.format("{\"userID\": \"%s\", \"listName\": \"%s\"}", USERID, foodoListName.replaceAll("\\s", ""));
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), json);

        HttpUrl.Builder httpBuilder = httpUrl.newBuilder().addQueryParameter("userID", USERID);

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
                        String listID =  createdFoodoList.getString("_id");
                        main_activity.runOnUiThread(() -> {
                            foodoListCardAdapter.addFoodoList(new FoodoListCard(listName, listID));
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

}

