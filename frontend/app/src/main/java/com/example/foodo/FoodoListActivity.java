package com.example.foodo;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodo.objects.RestaurantCard;
import com.example.foodo.objects.RestaurantCardAdapter;
import com.example.foodo.service.FoodoListCardService;

import java.util.ArrayList;

public class FoodoListActivity extends AppCompatActivity {

    private String listID;
    private String name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodo_list);

        getIntentExtras();
        initializeComponents();
    }

    private void initializeComponents() {
        TextView foodoListCardName = findViewById(R.id.foodo_list_card_name);
        foodoListCardName.setText(name);

        RestaurantCardAdapter restaurantCardAdapter = new RestaurantCardAdapter(this, new ArrayList<RestaurantCard>(), listID);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        FoodoListCardService foodoListCardService = new FoodoListCardService(this, listID, restaurantCardAdapter);

        RecyclerView restaurantsView = findViewById(R.id.foodo_list_card_restaurants_list);

        foodoListCardService.setupUserAccount();

        restaurantsView.setLayoutManager(linearLayoutManager);
        restaurantsView.setAdapter(restaurantCardAdapter);

        foodoListCardService.loadRestaurantCards();
    }

    private void getIntentExtras() {
        name = getIntent().getStringExtra("name");
        listID = getIntent().getStringExtra("listID");
    }

}