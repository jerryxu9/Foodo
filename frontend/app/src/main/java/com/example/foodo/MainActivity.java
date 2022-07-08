package com.example.foodo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodo.service.FoodoListService;

import org.json.JSONArray;
import org.json.JSONException;

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
    private final String BASE_URL = "http://10.0.2.2:3000";
    private SearchView restaurantSearch;
    private Button mapButton;
    private FoodoListService foodoListService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        foodoListService = new FoodoListService(this);
        foodoListService.setup();
    }

    private void searchRestaurant(String query) {

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
                            try {
                                JSONArray restaurantResultsArray = new JSONArray(searchResults);
                                Log.d(TAG, restaurantResultsArray.toString());
                                Intent searchResultIntent = new Intent(MainActivity.this, SearchResultActivity.class);
                                searchResultIntent.putExtra("restaurantResultsArray", restaurantResultsArray.toString());
                                searchResultIntent.putExtra("query", query);
                                startActivity(searchResultIntent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

    }

    private void handleMapAction() {
        Log.d(TAG, "Pressed map button");
        Intent mapsIntent = new Intent(MainActivity.this, MapActivity.class);
        startActivity(mapsIntent);
    }


}