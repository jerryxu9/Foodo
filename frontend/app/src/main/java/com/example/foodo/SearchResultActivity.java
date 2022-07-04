package com.example.foodo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodo.objects.RestaurantCard;
import com.example.foodo.objects.RestaurantCardAdapter;

import java.util.ArrayList;

public class SearchResultActivity extends AppCompatActivity {
    private RecyclerView searchResults;
    private ArrayList<RestaurantCard> restaurantCardArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        searchResults = findViewById(R.id.search_list);

        restaurantCardArrayList = new ArrayList<>();

        //when restaurantLookUp is done, replace these with API call

        restaurantCardArrayList.add(new RestaurantCard("Tim Hortons", "6525 Oak St, Vancouver, BC V6P 3Z3", "3.6 Stars", "(604) 261-1062", "Open", "abc"));
        restaurantCardArrayList.add(new RestaurantCard("Tim Hortons", "100 W 49th Ave 1st Floor, Vancouver, BC V5Y 2Z6", "2.9 Stars", "(604) 323-5295", "Open", "def"));
        restaurantCardArrayList.add(new RestaurantCard("Tim Hortons", "4500 Oak St, Vancouver, BC V6H 3V4", "3.8 Stars", "(604) 875-2638", "Closed", "ghi"));
        restaurantCardArrayList.add(new RestaurantCard("Tim Hortons", "6501 Main St, Vancouver, BC V5X 3H1", "3.9 Stars", "(604) 423-4477", "Closed", "jkl"));
        restaurantCardArrayList.add(new RestaurantCard("Tim Hortons", "5702 Granville St, Vancouver, BC V6M 3C7", "3.5 Stars", " +1 888-601-1616", "Non-operational", "mno"));
        restaurantCardArrayList.add(new RestaurantCard("Tim Hortons", "4065 Cambie St, Vancouver, BC V5Z 0G9", "3.0 Stars", "(604) 428-2768","Non-operational", "pqr"));
        restaurantCardArrayList.add(new RestaurantCard("Tim Hortons", "5896 Fraser St, Vancouver, BC V5W 2Z5", "3.9 Stars", "(778) 996-5896", "Non-operational", "stu"));

        RestaurantCardAdapter restaurantCardAdapter = new RestaurantCardAdapter(this, restaurantCardArrayList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        searchResults.setLayoutManager(linearLayoutManager);
        searchResults.setAdapter(restaurantCardAdapter);
    }
}