package com.example.foodo;

import android.content.Intent;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RestaurantInfoActivity extends AppCompatActivity {

    private TextView restaurantName_info, restaurantAddress_info, restaurantRating_info, restaurantPhoneNumber, restaurantStatus;
    private RecyclerView reviewList;
    private Spinner spinner;
    private ArrayList<ReviewCard> reviewCardArrayList;
    private String restaurantID;
    private Button submitReviewButton;
    private EditText reviewTextBox;
    private final OkHttpClient client = new OkHttpClient();
    private final String TAG = "restaurantInfoActivity";
    private final String BASE_URL = "http://10.0.2.2:3000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_info);

        spinner = findViewById(R.id.choose_rating);
        submitReviewButton = findViewById(R.id.reviewSendButton);
        reviewTextBox = findViewById(R.id.reviewTextBox);
        reviewList = findViewById(R.id.review_list);

//        submitReviewButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String reviewText = reviewTextBox.getText().toString();
//                Log.d(TAG, "Got following review from textbox: " + reviewText);
//
//                uploadReview(reviewText, "name", "rating");
//            }
//        });


        reviewCardArrayList = new ArrayList<>();

        restaurantAddress_info = findViewById(R.id.restaurantAddress_info);
        restaurantName_info = findViewById(R.id.restaurantName_info);
        restaurantRating_info = findViewById(R.id.restaurantRating_info);
        restaurantPhoneNumber = findViewById(R.id.restaurantNumber_info);
        restaurantStatus = findViewById(R.id.restaurantStatus_info);

        restaurantName_info.setText(getIntent().getStringExtra("restaurantName"));
        restaurantAddress_info.setText(getIntent().getStringExtra("restaurantAddress"));
        restaurantRating_info.setText(getIntent().getStringExtra("restaurantRating"));
        restaurantPhoneNumber.setText(getIntent().getStringExtra("restaurantPhoneNumber"));
        restaurantStatus.setText(getIntent().getStringExtra("restaurantStatus"));
        restaurantID = getIntent().getStringExtra("restaurantID");

        Log.d(TAG,getIntent().getStringExtra("restaurantStatus"));

        switch(restaurantStatus.getText().toString()){
            case "Open": restaurantStatus.setBackgroundResource(R.drawable.open_tag);
                break;
            case "Closed": restaurantStatus.setBackgroundResource(R.drawable.closed_tag);
                break;
            default: restaurantStatus.setBackgroundResource(R.drawable.non_operational_tag);
                break;
        }

        try {
            getReviews(restaurantID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//
//    private void uploadReview(String text, String name, String rating){
//        String url = buildURL("/addReview");
//        HttpUrl httpUrl = HttpUrl.parse(url);
//
//        if (httpUrl == null) {
//            Log.d(TAG, String.format("unable to parse server URL: %s", url));
//            return;
//        }
//
//
//    }

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