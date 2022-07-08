package com.example.foodo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.foodo.service.FoodoListCardService;

public class FoodoListActivity extends AppCompatActivity {


    private TextView foodoListCardName;


    private String listID, name;
    private final String TAG = "FoodoListActivity";

    private  FoodoListCardService foodoListCardService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodo_list);

        getIntentExtras();
        initializeComponents();

        populateRestaurantsList();
    }

    private void populateRestaurantsList(){

    }

    private void initializeComponents() {
        foodoListCardName = findViewById(R.id.foodo_list_card_name);
        foodoListCardName.setText(name);

        foodoListCardService = new FoodoListCardService(this, listID);
        foodoListCardService.setup();
    }

    private void getIntentExtras() {
        name = getIntent().getStringExtra("name");
        listID = getIntent().getStringExtra("listID");
    }

}