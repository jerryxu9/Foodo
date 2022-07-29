package com.example.foodo;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.idling.CountingIdlingResource;

import com.example.foodo.objects.RestaurantCard;
import com.example.foodo.objects.RestaurantCardAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchResultActivity extends AppCompatActivity {
    private final String TAG = "SearchResultActivity";
    private RecyclerView searchResults;
    private ArrayList<RestaurantCard> restaurantCardArrayList;
    private TextView searchText;
    private CountingIdlingResource searchResultActivityCountingIdlingResource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        searchResultActivityCountingIdlingResource = new CountingIdlingResource("SearchResultActivityLoaded");

        searchResultActivityCountingIdlingResource.increment();

        setContentView(R.layout.activity_search_result);
        searchResults = findViewById(R.id.search_list);
        searchText = findViewById(R.id.search_text);
        restaurantCardArrayList = new ArrayList<>();

        setSearchBarText();
        populateSearchResultList();

        searchResultActivityCountingIdlingResource.decrement();

    }

    private void setSearchBarText() {
        searchText.setText(getIntent().getStringExtra("query"));
    }

    private void populateSearchResultList() {
        try {
            JSONArray restaurantResultsArray = new JSONArray(getIntent().getStringExtra("restaurantResultsArray"));
            for (int i = 0; i < restaurantResultsArray.length(); i++) {
                JSONObject restaurantResult = restaurantResultsArray.getJSONObject(i);
                String businessStatus;
                if (restaurantResult.getString("businessStatus").equals("OPERATIONAL")) {
                    if (restaurantResult.has("openNow")) {
                        if (restaurantResult.getString("openNow").equals("true")) {
                            businessStatus = "Open";
                        } else {
                            businessStatus = "Closed";
                        }
                    } else {
                        businessStatus = "Unknown";
                    }
                } else {
                    businessStatus = restaurantResult.getString("businessStatus");
                }
                boolean isInFoodoList = false;
                RestaurantCard card = new RestaurantCard(restaurantResult.getString("id"),
                        "dummy_id",
                        isInFoodoList,
                        getIntent().hasExtra("userID") ? getIntent().getStringExtra("userID") : "");
                card.setRestaurantName(restaurantResult.getString("name"));
                card.setAddress(restaurantResult.getString("address"));
                card.setRating(restaurantResult.getString("GoogleRating"));
                card.setStatus(businessStatus);
                card.setLat(restaurantResult.getDouble("lat"));
                card.setLng(restaurantResult.getDouble("lng"));
                card.setUsername(getIntent().hasExtra("username") ? getIntent().getStringExtra("username") : "");
                // pass in a dummy_id for the cardID since these restaurantCards aren't being created as part of a Foodo list.
                restaurantCardArrayList.add(card);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // pass in a dummy id for the listID since these restaurantCards aren't being created as part of a Foodo list.
        RestaurantCardAdapter restaurantCardAdapter = new RestaurantCardAdapter(this, restaurantCardArrayList, "dummy_id");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        searchResults.setLayoutManager(linearLayoutManager);
        searchResults.setAdapter(restaurantCardAdapter);
    }

    public CountingIdlingResource getSearchResultActivityCountingIdlingResource() {
        return searchResultActivityCountingIdlingResource;
    }
}