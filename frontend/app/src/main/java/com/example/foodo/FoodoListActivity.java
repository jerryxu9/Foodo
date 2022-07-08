package com.example.foodo;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodo.service.FoodoListCardService;

import org.json.JSONException;

public class FoodoListActivity extends AppCompatActivity {


    private final String TAG = "FoodoListActivity";
    private TextView foodoListCardName;
    private String listID, name;
    private FoodoListCardService foodoListCardService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodo_list);

        getIntentExtras();
        initializeComponents();
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