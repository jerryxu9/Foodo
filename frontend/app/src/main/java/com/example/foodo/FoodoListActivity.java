package com.example.foodo;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodo.service.FoodoListCardService;

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

        FoodoListCardService foodoListCardService = new FoodoListCardService(this, listID);
        foodoListCardService.initializeComponents();
    }

    private void getIntentExtras() {
        name = getIntent().getStringExtra("name");
        listID = getIntent().getStringExtra("listID");
    }

}