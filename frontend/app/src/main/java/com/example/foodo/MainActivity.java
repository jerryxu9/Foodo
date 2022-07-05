package com.example.foodo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodo.objects.FoodoListCard;
import com.example.foodo.objects.FoodoListCardAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private final OkHttpClient client = new OkHttpClient();
    private final String BASE_URL = "http://10.0.2.2:3000";
    private SearchView restaurantSearch;
    private Button mapButton;
    private FloatingActionButton createFoodoListButton;
    private Button placeholderSearchButton;
    private RecyclerView foodoLists;
    private PopupWindow createFoodoListPopupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        placeholderSearchButton = findViewById(R.id.search_button_placeholder);

        placeholderSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Going to search results");

                Intent searchResultsIntent = new Intent(MainActivity.this, SearchResultActivity.class);
                startActivity(searchResultsIntent);
            }
        });

        restaurantSearch = findViewById(R.id.restaurant_search);
        restaurantSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchRestaurant(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mapButton = findViewById(R.id.map_button);
        mapButton.setOnClickListener((View v) -> handleMapAction());

        createFoodoListButton = findViewById(R.id.create_foodo_list_button);
        createFoodoListButton.setOnClickListener((View v) -> handleCreateFoodoListAction());

        initializeFoodoLists();
    }

    private void initializeFoodoLists() {

        // TODO: Add check to see if user is signed in
        foodoLists = findViewById(R.id.foodo_lists);
        ArrayList<FoodoListCard> placeHolderFoodoListArrayList = new ArrayList<>();

        // TODO: replace this with an api call and render the output instead
        placeHolderFoodoListArrayList.add(new FoodoListCard("Bubble tea!", "abc"));
        placeHolderFoodoListArrayList.add(new FoodoListCard("Sushi", "def"));
        placeHolderFoodoListArrayList.add(new FoodoListCard("Pasta", "ghi"));
        placeHolderFoodoListArrayList.add(new FoodoListCard("Soup", "jkl"));
        placeHolderFoodoListArrayList.add(new FoodoListCard("Brunch", "mno"));
        placeHolderFoodoListArrayList.add(new FoodoListCard("Chinese", "pqr"));
        placeHolderFoodoListArrayList.add(new FoodoListCard("Hello", "jkl"));
        placeHolderFoodoListArrayList.add(new FoodoListCard("Test", "mno"));
        placeHolderFoodoListArrayList.add(new FoodoListCard("filler", "pqr"));

        FoodoListCardAdapter foodoListCardAdapter = new FoodoListCardAdapter(this, placeHolderFoodoListArrayList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        foodoLists.setLayoutManager(linearLayoutManager);
        foodoLists.setAdapter(foodoListCardAdapter);
    }

    private void searchRestaurant(String query) {
        Log.d(TAG, String.format("Search submit button pressed with query %s", query));

        String url = BASE_URL + "/searchRestaurantsByQuery";
        HttpUrl httpUrl = HttpUrl.parse(url);

        if (httpUrl == null) {
            Log.d(TAG, String.format("unable to parse server URL: %s", url));
            return;
        }

        HttpUrl.Builder httpBuilder = httpUrl.newBuilder().addQueryParameter("query", query);

        Request request = new Request.Builder()
                .url(httpBuilder.build())
                .build();
        Log.d(TAG, String.format("Search Request invoked by searchRestaurant to %s with query %s", httpBuilder.build(), query));

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String searchResults;
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful())
                        throw new IOException(String.format("Unexpected code %s", response));
                    else if (responseBody == null) {
                        throw new IOException("null response from /searchRestaurantsByQuery endpoint");
                    } else {
                        searchResults = responseBody.string();
                        Log.d(TAG, String.format("response from /searchRestaurantsByQuery: %s", searchResults));
                        runOnUiThread(() -> {
                            // Async method to update UI
                        });

                    }
                }
            }

        });

    }

    private void handleMapAction() {
        Log.d(TAG, "Pressed map button");
        Intent mapsIntent = new Intent(MainActivity.this, MapActivity.class);
        startActivity(mapsIntent);
    }

    private void handleCreateFoodoListAction() {
        Log.d(TAG, "Pressed add Foodo restaurant button");
        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.activity_create_foodo_list, null);

        ConstraintLayout createFoodoListConstraintLayout = findViewById(R.id.constraint);

        createFoodoListPopupWindow = new PopupWindow(container, 900, 900, true);
        createFoodoListPopupWindow.showAtLocation(createFoodoListConstraintLayout, Gravity.CENTER, 0, 0);

        container.findViewById(R.id.create_foodo_list_confirm_button).setOnClickListener((View v) -> {
            createFoodoList();
            createFoodoListPopupWindow.dismiss();
        });

        container.findViewById(R.id.create_foodo_list_cancel_button).setOnClickListener((View v) -> {
            Log.d(TAG, "Cancelled creating Foodo list");
            createFoodoListPopupWindow.dismiss();
        });

        container.setOnTouchListener((View v, MotionEvent event) -> {
            Log.d(TAG, "Pressed outside Create Foodo List popup window");
            createFoodoListPopupWindow.dismiss();
            return false;
        });
    }

    private void createFoodoList() {
        Log.d(TAG, "Confirmed creating Foodo list");
    }


}