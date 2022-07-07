package com.example.foodo.service;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodo.R;
import com.example.foodo.objects.FoodoListCard;
import com.example.foodo.objects.FoodoListCardAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FoodoListService {

    private final String TAG = "FoodoListService";

    private final String USERID = "test@gmail.com";
    private final String BASE_URL = "http://10.0.2.2:3000";

    private AppCompatActivity main_activity;
    private OkHttpClient client;
    private RecyclerView foodoLists;
    private FloatingActionButton createFoodoListButton;
    private PopupWindow createFoodoListPopupWindow;

    public FoodoListService(AppCompatActivity activity, OkHttpClient client) {
        this.main_activity = activity;
        this.client = client;
    }

    public void setup() {
        initializeFoodoLists();
        setupComponents();
    }

    private void initializeFoodoLists() {

        // TODO: Add check to see if user is signed in
        foodoLists = main_activity.findViewById(R.id.foodo_lists);
        ArrayList<FoodoListCard> foodoListCardArrayList = getFoodoLists();

        FoodoListCardAdapter foodoListCardAdapter = new FoodoListCardAdapter(main_activity, foodoListCardArrayList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(main_activity, LinearLayoutManager.VERTICAL, false);

        foodoLists.setLayoutManager(linearLayoutManager);
        foodoLists.setAdapter(foodoListCardAdapter);
    }

    private ArrayList<FoodoListCard> getFoodoLists() {
        String url = BASE_URL + "/getFoodoLists";
        HttpUrl httpUrl = HttpUrl.parse(url);
        ArrayList<FoodoListCard> result = new ArrayList<>();

        if (httpUrl == null) {
            Log.d(TAG, String.format("unable to parse server URL: %s", url));
            return new ArrayList<>();
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
                            result.add(new FoodoListCard(foodoListJSON.getString("name"), "abc"));
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

        return result;
    }

    private void setupComponents() {
        createFoodoListButton = main_activity.findViewById(R.id.create_foodo_list_button);
        createFoodoListButton.setOnClickListener((View v) -> handleCreateFoodoListAction());
    }


    private void handleCreateFoodoListAction() {
        Log.d(TAG, "Pressed add Foodo restaurant button");
        LayoutInflater layoutInflater = (LayoutInflater) main_activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.activity_create_foodo_list, null);

        ConstraintLayout createFoodoListConstraintLayout = main_activity.findViewById(R.id.constraint);

        createFoodoListPopupWindow = new PopupWindow(container, 800, 800, true);
        createFoodoListPopupWindow.showAtLocation(createFoodoListConstraintLayout, Gravity.CENTER, 0, 0);

        container.findViewById(R.id.create_foodo_list_confirm_button).setOnClickListener((View v) -> {
            createFoodoList();
        });

        container.findViewById(R.id.create_foodo_list_cancel_button).setOnClickListener((View v) -> {
            Log.d(TAG, "Cancelled creating Foodo list");
            createFoodoListPopupWindow.dismiss();
        });
    }

    private void createFoodoList() {
        Log.d(TAG, "Confirmed creating Foodo list");

        createFoodoListPopupWindow.dismiss();
    }
}
