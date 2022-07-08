package com.example.foodo;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodo.objects.RestaurantCard;
import com.example.foodo.objects.RestaurantCardAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchResultActivity extends AppCompatActivity {
    private RecyclerView searchResults;
    private ArrayList<RestaurantCard> restaurantCardArrayList;
    private TextView searchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        searchResults = findViewById(R.id.search_list);
        searchText = findViewById(R.id.search_text);
        restaurantCardArrayList = new ArrayList<>();

        setSearchBarText();
        populateSearchResultList();
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
                    if (restaurantResult.getString("openNow").equals("true")) {
                        businessStatus = "Open";
                    } else {
                        businessStatus = "Closed";
                    }
                } else {
                    businessStatus = restaurantResult.getString("businessStatus");
                }
                boolean isInFoodoList = false;
                restaurantCardArrayList.add(new RestaurantCard(restaurantResult.getString("name"),
                        restaurantResult.getString("address"),
                        restaurantResult.getString("GoogleRating"),
                        businessStatus,
                        restaurantResult.getString("id"),
                        "dummy_id",
                        restaurantResult.getDouble("lat"),
                        restaurantResult.getDouble("lng"),
                        isInFoodoList));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RestaurantCardAdapter restaurantCardAdapter = new RestaurantCardAdapter(this, restaurantCardArrayList, "dummyID");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        searchResults.setLayoutManager(linearLayoutManager);
        searchResults.setAdapter(restaurantCardAdapter);
    }
}