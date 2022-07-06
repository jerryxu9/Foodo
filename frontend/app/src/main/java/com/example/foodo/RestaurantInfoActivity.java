package com.example.foodo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodo.objects.ReviewCard;
import com.example.foodo.objects.ReviewCardAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RestaurantInfoActivity extends AppCompatActivity {

    private MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private TextView restaurantName_info, restaurantAddress_info, restaurantRating_info, restaurantPhoneNumber, restaurantStatus,
            mondayHours, tuesdayHours, wednesdayHours, thursdayHours, fridayHours, saturdayHours, sundayHours;
    private RecyclerView reviewList;
    private Spinner spinner;
    private ArrayList<ReviewCard> reviewCardArrayList;
    private String restaurantID;
    private Button submitReviewButton;
    private EditText reviewTextBox;
    private final OkHttpClient client = new OkHttpClient();
    private final String TAG = "restaurantInfoActivity", BASE_URL = "http://10.0.2.2:3000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_info);
        initializeComponents();
        getIntentExtras();

        submitReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reviewText = reviewTextBox.getText().toString();
                if(!reviewText.trim().isEmpty()){
                    Log.d(TAG, "Got following review from text box: " + reviewText);
                    Log.d(TAG, "got following rating from spinner: " + spinner.getSelectedItem().toString());
                    addReview(restaurantID, "name", reviewText, spinner.getSelectedItem().toString());
                    reviewTextBox.getText().clear();
                }
            }
        });

        setStatusBackground(restaurantStatus);

        searchRestaurantInfoByID(restaurantID);

        try {
            getReviews(restaurantID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeComponents(){
        reviewCardArrayList = new ArrayList<>();

        submitReviewButton = findViewById(R.id.reviewSendButton);
        reviewTextBox = findViewById(R.id.reviewTextBox);
        reviewList = findViewById(R.id.review_list);
        restaurantAddress_info = findViewById(R.id.restaurantAddress_info);
        restaurantName_info = findViewById(R.id.restaurantName_info);
        restaurantRating_info = findViewById(R.id.restaurantRating_info);
        restaurantPhoneNumber = findViewById(R.id.restaurantNumber_info);
        restaurantStatus = findViewById(R.id.restaurantStatus_info);

        mondayHours = findViewById(R.id.Monday_Hours);
        tuesdayHours = findViewById(R.id.Tuesday_Hours);
        wednesdayHours = findViewById(R.id.Wednesday_Hours);
        thursdayHours = findViewById(R.id.Thursday_Hours);
        fridayHours = findViewById(R.id.Friday_Hours);
        saturdayHours = findViewById(R.id.Saturday_Hours);
        sundayHours = findViewById(R.id.Sunday_Hours);

        initializeSpinner();
    }

    private void initializeSpinner(){
        spinner = findViewById(R.id.choose_rating_spinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(RestaurantInfoActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.rating_options));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
    }

    private void getIntentExtras(){
        restaurantName_info.setText(getIntent().getStringExtra("restaurantName"));
        restaurantAddress_info.setText(getIntent().getStringExtra("restaurantAddress"));
        restaurantRating_info.setText(getIntent().getStringExtra("restaurantRating"));
        restaurantStatus.setText(getIntent().getStringExtra("restaurantStatus"));
        restaurantID = getIntent().getStringExtra("restaurantID");
    }

    private void setStatusBackground(TextView restaurantStatus){
        switch(restaurantStatus.getText().toString()){
            case "Open": restaurantStatus.setBackgroundResource(R.drawable.open_tag);
                break;
            case "Closed": restaurantStatus.setBackgroundResource(R.drawable.closed_tag);
                break;
            default: restaurantStatus.setBackgroundResource(R.drawable.non_operational_tag);
                break;
        }
    }

    private void searchRestaurantInfoByID(String restaurantID){
        String url = buildURL("/searchRestaurantInfoByID");
        HttpUrl httpUrl = HttpUrl.parse(url);
        HttpUrl.Builder httpBuilder = httpUrl.newBuilder();
        httpBuilder.addQueryParameter("id", restaurantID);

        Request request = new Request.Builder()
                .url(httpBuilder.build())
                .build();
        Log.d(TAG, String.format("Search Request invoked by searchRestaurantInfoByID to %s with query %s", httpBuilder.build(), restaurantID));

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try(ResponseBody responseBody = response.body()){
                    if (!response.isSuccessful())
                        throw new IOException(String.format("Unexpected code %s", response));
                    else {

                        String responseBodyString = responseBody.string();
                        Log.d(TAG, responseBodyString);

                        runOnUiThread(()->{
                            try {
                                JSONObject restaurantObj = new JSONObject(responseBodyString);
                                if(restaurantObj.has("formatted_phone_number")){
                                    restaurantPhoneNumber.setText(restaurantObj.getString("formatted_phone_number"));
                                }else{
                                    restaurantPhoneNumber.setText("Phone Number Unavailable");
                                }
                                JSONArray openingHours = restaurantObj.getJSONObject("opening_hours").getJSONArray("weekday_text");
                                Log.d(TAG, openingHours.toString());
                                Log.d(TAG, openingHours.getString(0));
                                Log.d(TAG, openingHours.getString(0).split(" ", 2)[0]);

                                mondayHours.setText(openingHours.getString(0).split(" ", 2)[1]);
                                tuesdayHours.setText(openingHours.getString(1).split(" ", 2)[1]);
                                wednesdayHours.setText(openingHours.getString(2).split(" ", 2)[1]);
                                thursdayHours.setText(openingHours.getString(3).split(" ", 2)[1]);
                                fridayHours.setText(openingHours.getString(4).split(" ", 2)[1]);
                                saturdayHours.setText(openingHours.getString(5).split(" ", 2)[1]);
                                sundayHours.setText(openingHours.getString(6).split(" ", 2)[1]);
                            } catch (JSONException e) {
                                restaurantPhoneNumber.setText("Phone Number Unavailable");
                            }
                        });
                    }
                }
            }
        });
    }



    private void addReview(String restaurantID, String name, String text, String rating){
        String url = buildURL("/addReview");
        HttpUrl httpUrl = HttpUrl.parse(url);

        if (httpUrl == null) {
            Log.d(TAG, String.format("unable to parse server URL: %s", url));
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("google_place_id", restaurantID);
        params.put("user_name", name);
        params.put("review", text);
        params.put("rating", rating);

        JSONObject paramsJSON = new JSONObject(params);
        RequestBody body = RequestBody.create(paramsJSON.toString(), JSON);
        Request request = new Request.Builder()
                .url(httpUrl)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try(ResponseBody responseBody = response.body()){
                    if (!response.isSuccessful())
                        throw new IOException(String.format("Unexpected code %s", response));
                    else {
                        Log.d(TAG, responseBody.string());
                    }
                }
            }
        });
    }

    private void getReviews(String restaurantID) {
        Log.d(TAG, String.format("searching for restaurant with ID: %s", restaurantID));

        String url = buildURL("/getReviews");
        HttpUrl httpUrl = HttpUrl.parse(url);

        if (httpUrl == null) {
            Log.d(TAG, String.format("unable to parse server URL: %s", url));
            return;
        }

        HttpUrl.Builder httpBuilder = httpUrl.newBuilder();
        httpBuilder.addQueryParameter("google_place_id", restaurantID);

        Request request = new Request.Builder()
                .url(httpBuilder.build())
                .build();
        Log.d(TAG, String.format("Search Request invoked by getReview to %s with query %s", httpBuilder.build(), restaurantID));

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
                            try {
                                JSONArray responseBodyJSONArray = new JSONArray(searchResults);

                                for(int i = 0; i < responseBodyJSONArray.length(); i++){
                                    JSONObject reviewCardJSON = responseBodyJSONArray.getJSONObject(i);
                                    Log.d(TAG, reviewCardJSON.toString());
                                    reviewCardArrayList.add(new ReviewCard(reviewCardJSON.getString("user_name"), reviewCardJSON.getString("review"),reviewCardJSON.getString("rating")));
                                }

                                ReviewCardAdapter reviewCardAdapter = new ReviewCardAdapter(RestaurantInfoActivity.this, reviewCardArrayList);
                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(RestaurantInfoActivity.this, LinearLayoutManager.VERTICAL, false);

                                reviewList.setLayoutManager(linearLayoutManager);
                                reviewList.setAdapter(reviewCardAdapter);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        });
                        Log.d(TAG, String.format("response from /searchRestaurantsByQuery: %s", searchResults));
                    }
                }
            }
        });
    }
    private String buildURL(String path){
        return BASE_URL + path;
    }

}