package com.example.foodo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

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
    private Button placeholderSearchButton;

    private final String BASE_URL = "http://10.0.2.2:3000";

    SearchView restaurantSearch;
    ImageButton mapButton;

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
                Log.d(TAG, String.format("Submit button pressed with query %s", query));
                searchRestaurant(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mapButton = findViewById(R.id.map_button);
        mapButton.setOnClickListener((View v) -> {
            Log.d(TAG, "Pressed mapButton");
            handleMapAction();
        });

    }

    private void searchRestaurant(String query) {
        Log.d(TAG, String.format("called doMySearch with query: %s", query));

        String url = BASE_URL + "/searchRestaurantsByQuery";
        HttpUrl httpUrl = HttpUrl.parse(url);

        if (httpUrl == null) {
            Log.d(TAG, String.format("unable to parse server URL: %s", url));
            return;
        }

        HttpUrl.Builder httpBuilder = httpUrl.newBuilder();
        httpBuilder.addQueryParameter("query", query);

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
                    else {
                        if (responseBody == null) {
                            Log.d(TAG, "response from /searchRestaurantsByQuery is null");
                            throw new IOException("null response from /searchRestaurantsByQuery endpoint");
                        }
                        searchResults = responseBody.string();
                        runOnUiThread(() -> {
                            // Async method to update UI
                        });
                        Log.d(TAG, String.format("response from /searchRestaurantsByQuery: %s", searchResults));
                    }
                }
            }

        });

    }

    private void handleMapAction() {
        Intent mapsIntent = new Intent(MainActivity.this, MapActivity.class);
        startActivity(mapsIntent);
    }
}