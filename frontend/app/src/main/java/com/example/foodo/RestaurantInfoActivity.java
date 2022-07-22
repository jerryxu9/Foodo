package com.example.foodo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodo.objects.ReviewCard;
import com.example.foodo.objects.ReviewCardAdapter;
import com.example.foodo.service.OKHttpService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import com.google.android.gms.auth.api.signin.GoogleSignIn;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RestaurantInfoActivity extends AppCompatActivity {
    private final String TAG = "restaurantInfoActivity";
    private TextView restaurantName_info;
    private TextView restaurantAddress_info;
    private TextView restaurantRating_info;
    private TextView restaurantPhoneNumber;
    private TextView restaurantStatus;
    private TextView mondayHours;
    private TextView tuesdayHours;
    private TextView wednesdayHours;
    private TextView thursdayHours;
    private TextView fridayHours;
    private TextView saturdayHours;
    private TextView sundayHours;

    private RecyclerView reviewList;
    private Spinner spinner;
    private Spinner addResToListSpinner;
    private ArrayList<ReviewCard> reviewCardArrayList;
    private String googlePlacesID;
    private Button submitReviewButton;
    private Button addRestaurantToFoodoListButton;
    private EditText reviewTextBox;
    private PopupWindow createAddRestaurantToListPopupWindow;
    private PopupWindow userNotLoggedInPopupWindow;
    private double lng;
    private double lat;
    private boolean isInFoodoList;
    private ArrayList<String> foodoListNames;
    private HashMap<String, String> foodoListIDandNames;
    private ReviewCardAdapter reviewCardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_info);
        initializeComponents();
        getIntentExtras();

        submitReviewButton.setOnClickListener((View v) -> {
            if (GoogleSignIn.getLastSignedInAccount(RestaurantInfoActivity.this) == null) {
                reviewTextBox.getText().clear();
                handleNonLoggedInUser(v);
            } else {
                String reviewText = reviewTextBox.getText().toString();
                if (!reviewText.trim().isEmpty()) {
                    Log.d(TAG, "Got following review from text box: " + reviewText);
                    Log.d(TAG, "got following rating from spinner: " + spinner.getSelectedItem().toString());
                    addReview(reviewText, spinner.getSelectedItem().toString());
                    reviewTextBox.getText().clear();
                }
            }
        });

        // Hide add button if info page displayed by clicking on restaurant in Foodo list
        if (isInFoodoList) {
            addRestaurantToFoodoListButton.setEnabled(false);
            addRestaurantToFoodoListButton.setVisibility(View.INVISIBLE);
        } else {
            addRestaurantToFoodoListButton.setOnClickListener((View view) -> {
                if (GoogleSignIn.getLastSignedInAccount(RestaurantInfoActivity.this) == null) {
                    handleNonLoggedInUser(view);
                } else {
                    initializePopUp(view);
                }
            });
        }

        setStatusBackground(restaurantStatus);
        searchRestaurantInfoByID(googlePlacesID);
//        try {
//            getReviews(googlePlacesID);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        Log.d(TAG, googlePlacesID);

        FirebaseMessaging.getInstance().subscribeToTopic(googlePlacesID)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed";
                        if (!task.isSuccessful()) {
                            msg = "Subscribe failed";
                        }
                        Log.d(TAG, msg);
                    }
                });
    }

    private void handleNonLoggedInUser(View view) {
        Log.d(TAG, "User is not logged in, login now!!! >:(");
        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.activity_login_popup, null);

        userNotLoggedInPopupWindow = new PopupWindow(container, 800, 800, true);
        userNotLoggedInPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        container.findViewById(R.id.login_cancel_button).setOnClickListener((View v) -> {
            Log.d(TAG, "Exit pop up");
            userNotLoggedInPopupWindow.dismiss();
        });
    }

    private void initializePopUp(View view) {
        Log.d(TAG, "Pressed add Foodo restaurant button");
        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.activity_add_restaurant_to_list, null);

        addResToListSpinner = container.findViewById(R.id.choose_foodolist_spinner);

        String[] foodoListNamesPrim = getFoodoListsPrimitiveArray();

        ArrayAdapter<String> addResToListSpinnerAdapter = new ArrayAdapter<>(RestaurantInfoActivity.this,
                android.R.layout.simple_list_item_1, foodoListNamesPrim);
        addResToListSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        addResToListSpinner.setAdapter(addResToListSpinnerAdapter);

        createAddRestaurantToListPopupWindow = new PopupWindow(container, 900, 900, true);
        createAddRestaurantToListPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        container.findViewById(R.id.add_res_to_list_confirm_button).setOnClickListener((View v) -> {
            Log.d(TAG, "adding restaurant to " + addResToListSpinner.getSelectedItem().toString());
            addRestaurantToList(foodoListIDandNames.get(addResToListSpinner.getSelectedItem().toString()));
            createAddRestaurantToListPopupWindow.dismiss();
        });

        container.findViewById(R.id.add_res_to_list_cancel_button).setOnClickListener((View v) -> {
            Log.d(TAG, "Cancelled adding restaurant to list");
            createAddRestaurantToListPopupWindow.dismiss();
        });
    }

    private String[] getFoodoListsPrimitiveArray() {
        String[] foodoListArray = new String[foodoListNames.size()];

        for (int i = 0; i < foodoListNames.size(); i++) {
            foodoListArray[i] = foodoListNames.get(i);
        }
        return foodoListArray;
    }

    private void initializeComponents() {
        reviewCardArrayList = new ArrayList<>();
        foodoListNames = new ArrayList<>();
        foodoListIDandNames = new HashMap<>();

        getFoodoLists();

        submitReviewButton = findViewById(R.id.reviewSendButton);
        addRestaurantToFoodoListButton = findViewById(R.id.add_restaurant_to_list_button);
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

    private void initializeSpinner() {
        spinner = findViewById(R.id.choose_rating_spinner);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(RestaurantInfoActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.rating_options));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
    }

    private void getIntentExtras() {
        restaurantName_info.setText(getIntent().getStringExtra("restaurantName"));
        restaurantAddress_info.setText(getIntent().getStringExtra("restaurantAddress"));
        restaurantRating_info.setText(getIntent().getStringExtra("restaurantRating"));
        restaurantStatus.setText(getIntent().getStringExtra("restaurantStatus"));
        googlePlacesID = getIntent().getStringExtra("googlePlacesID");
        lat = getIntent().getDoubleExtra("lat", 0);
        lng = getIntent().getDoubleExtra("lng", 0);
        isInFoodoList = getIntent().getBooleanExtra("isInFoodoList", isInFoodoList);
    }

    private void setStatusBackground(TextView restaurantStatus) {
        switch (restaurantStatus.getText().toString()) {
            case "Open":
                restaurantStatus.setBackgroundResource(R.drawable.open_tag);
                break;
            case "Closed":
                restaurantStatus.setBackgroundResource(R.drawable.closed_tag);
                break;
            default:
                restaurantStatus.setBackgroundResource(R.drawable.non_operational_tag);
                break;
        }
    }

    private void getFoodoLists() {
        if (!getIntent().hasExtra("userID")) {
            Log.d(TAG, "Error: Intent does not have the user ID");
            return;
        }
        String userID = getIntent().getStringExtra("userID");

        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("userID", userID);

        Callback getFoodoListCallback = new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    JSONArray foodoListsJSON = new JSONArray(OKHttpService.getResponseBody(response));
                    for (int i = 0; i < foodoListsJSON.length(); i++) {
                        Log.d(TAG, foodoListsJSON.getJSONObject(i).getString("name") + " " + i);
                        foodoListNames.add(foodoListsJSON.getJSONObject(i).getString("name"));
                        foodoListIDandNames.put(foodoListsJSON.getJSONObject(i).getString("name"), foodoListsJSON.getJSONObject(i).getString("_id"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        OKHttpService.getRequest("getFoodoLists", getFoodoListCallback, queryParameters);

    }

    private void addRestaurantToList(String listID) {
        Callback addRestaurantToListCallback = new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d(TAG, OKHttpService.getResponseBody(response));
            }
        };

        HashMap<String, String> addRestaurantToListParams = new HashMap<>();
        addRestaurantToListParams.put("listID", listID);
        addRestaurantToListParams.put("restaurantID", googlePlacesID);
        addRestaurantToListParams.put("restaurantName", restaurantName_info.getText().toString());
        addRestaurantToListParams.put("isVisited", "false");
        addRestaurantToListParams.put("lat", Double.toString(lat));
        addRestaurantToListParams.put("lng", Double.toString(lng));

        OKHttpService.patchRequest("addRestaurantToList", addRestaurantToListCallback, addRestaurantToListParams);
    }

    private void searchRestaurantInfoByID(String restaurantID) {
        Callback searchRestaurantInfoByIDCallback = new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBodyString = OKHttpService.getResponseBody(response);
                Log.d(TAG, responseBodyString);
                Log.d(TAG, "restaurantID: " + restaurantID);
                runOnUiThread(() -> {
                    try {
                        JSONObject restaurantObj = new JSONObject(responseBodyString);
                        if (restaurantObj.has("formatted_phone_number")) {
                            restaurantPhoneNumber.setText(restaurantObj.getString("formatted_phone_number"));
                        } else {
                            restaurantPhoneNumber.setText("Phone Number Unavailable");
                        }
                        JSONArray openingHours = restaurantObj.getJSONObject("opening_hours").getJSONArray("weekday_text");
                        setWeekHours(new TextView[]{mondayHours, tuesdayHours, wednesdayHours, thursdayHours, fridayHours, saturdayHours, sundayHours}, openingHours);

                        JSONArray responseBodyJSONArray = restaurantObj.getJSONArray("reviews");

                        for (int i = 0; i < responseBodyJSONArray.length(); i++) {
                            JSONObject reviewCardJSON = responseBodyJSONArray.getJSONObject(i);
                            Log.d(TAG, reviewCardJSON.toString());
                            reviewCardArrayList.add(new ReviewCard(reviewCardJSON.getString("user_name"), reviewCardJSON.getString("review"), reviewCardJSON.getString("rating")));
                        }

                        reviewCardAdapter = new ReviewCardAdapter(reviewCardArrayList);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(RestaurantInfoActivity.this, LinearLayoutManager.VERTICAL, false);

                        reviewList.setLayoutManager(linearLayoutManager);
                        reviewList.setAdapter(reviewCardAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
        };

        HashMap<String, String> searchRestaurantInfoByID = new HashMap<>();
        searchRestaurantInfoByID.put("id", restaurantID);

        OKHttpService.getRequest("searchRestaurantInfoByID", searchRestaurantInfoByIDCallback, searchRestaurantInfoByID);
    }

    private void setWeekHours(TextView[] daysOfWeek, JSONArray openingHours) throws JSONException {
        for (int i = 0; i < daysOfWeek.length; i++) {
            daysOfWeek[i].setText(openingHours.getString(i).split(" ", 2)[1]);
        }
    }

    private void addReview(String text, String rating) {
        if (!getIntent().hasExtra("username")) {
            Log.d(TAG, "Error: intent does not have username");
            return;
        }
        String username = getIntent().getStringExtra("username");

        Callback addReviewCallback = new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d(TAG, OKHttpService.getResponseBody(response));
            }
        };

        HashMap<String, String> addReviewParams = new HashMap<>();
        addReviewParams.put("google_place_id", googlePlacesID);
        addReviewParams.put("user_name", username);
        addReviewParams.put("review", text);
        addReviewParams.put("rating", rating);

        OKHttpService.postRequest("addReview", addReviewCallback, addReviewParams);
    }

//    private void getReviews(String restaurantID) {
//        Callback getReviewsCallback = new Callback() {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                String searchResults = OKHttpService.getResponseBody(response);
//                runOnUiThread(() -> {
//                    try {
//                        JSONArray responseBodyJSONArray = new JSONArray(searchResults);
//
//                        for (int i = 0; i < responseBodyJSONArray.length(); i++) {
//                            JSONObject reviewCardJSON = responseBodyJSONArray.getJSONObject(i);
//                            Log.d(TAG, reviewCardJSON.toString());
//                            reviewCardArrayList.add(new ReviewCard(reviewCardJSON.getString("user_name"), reviewCardJSON.getString("review"), reviewCardJSON.getString("rating")));
//                        }
//
//                        reviewCardAdapter = new ReviewCardAdapter(reviewCardArrayList);
//                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(RestaurantInfoActivity.this, LinearLayoutManager.VERTICAL, false);
//
//                        reviewList.setLayoutManager(linearLayoutManager);
//                        reviewList.setAdapter(reviewCardAdapter);
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                });
//                Log.d(TAG, String.format("response from /searchRestaurantsByQuery: %s", searchResults));
//            }
//        };
//
//        HashMap<String, String> getReviewsParams = new HashMap<>();
//        getReviewsParams.put("google_place_id", restaurantID);
//
//        OKHttpService.getRequest("getReviews", getReviewsCallback, getReviewsParams);
//    }

    public BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<String> action = intent.getStringArrayListExtra("action");
            changeUi(action);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(myReceiver, new IntentFilter("FBR-IMAGE"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(myReceiver);
    }

    private void changeUi(ArrayList<String> action) {
        ReviewCard reviewCard = new ReviewCard(action.get(2),
                action.get(1),
                action.get(0));
        runOnUiThread(() -> {
            reviewCardArrayList.add(reviewCard);
            reviewCardAdapter.notifyItemInserted(reviewCardArrayList.size());
        });
    }
}