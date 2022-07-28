package com.example.foodo.service;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodo.objects.RestaurantCard;
import com.example.foodo.objects.RestaurantCardAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class FoodoListCardService {
    private final String TAG = "FoodoListCardService";
    private final AppCompatActivity foodoCardActivity;
    private final String listID;
    private final RestaurantCardAdapter restaurantCardAdapter;
    private String username;
    private String userID;

    public FoodoListCardService(AppCompatActivity foodoCardActivity, String listID, RestaurantCardAdapter restaurantCardAdapter) {
        this.foodoCardActivity = foodoCardActivity;
        this.restaurantCardAdapter = restaurantCardAdapter;
        this.listID = listID;
    }

    public void setupUserAccount() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(foodoCardActivity);
        if (account != null && userID == null) {
            //no other way to get the token, just go through createUser endpoint
            //and get the id from the existing entry in the database
            createUser(account.getIdToken(), account.getDisplayName(), account.getEmail());
        }
    }

    public void loadRestaurantCards() {
        if (userID == null || username == null) {
            return;
        }
        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("listID", listID);

        Callback loadRestaurantCardsCallback = new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String result = OKHttpService.getResponseBody(response);
                    JSONArray foodoListJSONArray = new JSONArray(result);
                    // For each restaurant, collect its information and render it as a Restaurant Card
                    for (int i = 0; i < foodoListJSONArray.length(); i++) {
                        JSONObject restaurant = foodoListJSONArray.getJSONObject(i);
                        Log.d(TAG, String.format("Create Restaurant Card for %s under Foodo List %s", restaurant.toString(), listID));
                        String placeID = restaurant.getString("place_id");
                        String cardID = restaurant.getString("_id");
                        boolean isVisited = restaurant.getBoolean("isVisited");
                        createRestaurantCards(placeID, cardID, isVisited);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        OKHttpService.getRequest("getRestaurantsByFoodoListID", loadRestaurantCardsCallback, queryParameters);
    }


    private void createRestaurantCards(String googlePlaceID, String cardID, boolean isVisited) {
        if (userID == null || username == null) {
            return;
        }

        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("id", googlePlaceID);

        Callback createRestaurantCardsCallback = new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String result = OKHttpService.getResponseBody(response);
                    JSONObject restaurant = new JSONObject(result);
                    Log.d(TAG, restaurant.toString());
                    Log.d(TAG, "restaurant");
                    String businessStatus = getBusinessStatus(restaurant);
                    foodoCardActivity.runOnUiThread(() -> {
                        try {
                            RestaurantCard card = new RestaurantCard(googlePlaceID, cardID, true, userID);
                            card.setRestaurantName(restaurant.getString("name"));
                            card.setAddress(restaurant.getString("formatted_address"));
                            card.setRating(restaurant.getString("rating"));
                            card.setStatus(businessStatus);
                            card.setLat(getLatitude(restaurant));
                            card.setLng(getLongitude(restaurant));
                            card.setUsername(username);
                            card.setVisited(isVisited);
                            restaurantCardAdapter.addRestaurantCard(card);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }
        };

        OKHttpService.getRequest("searchRestaurantInfoByID", createRestaurantCardsCallback, queryParameters);
    }

    private String getBusinessStatus(JSONObject restaurantObject) throws JSONException {
        String businessStatus = restaurantObject.getString("business_status");
        if (businessStatus.equals("OPERATIONAL")) {
            return restaurantObject.getJSONObject("opening_hours").getBoolean("open_now") ? "Open" : "Closed";
        }
        return businessStatus;
    }

    private Double getLatitude(JSONObject restaurant) throws JSONException {
        return getRestaurantLocationAttribute(restaurant, "lat");
    }

    private Double getLongitude(JSONObject restaurant) throws JSONException {
        return getRestaurantLocationAttribute(restaurant, "lng");
    }

    private Double getRestaurantLocationAttribute(JSONObject restaurant, String key) throws JSONException {
        return restaurant.getJSONObject("geometry").getJSONObject("location").getDouble(key);
    }

    private void createUser(String idToken, String user, String email) {
        Callback createUserCallback = new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBodyString = OKHttpService.getResponseBody(response);
                Log.d(TAG, responseBodyString);
                try {
                    JSONObject resJSON = new JSONObject(responseBodyString);
                    if (!resJSON.has("error")) {
                        Log.d(TAG, resJSON.getString("_id"));
                        Log.d(TAG, resJSON.getString("name"));

                        userID = resJSON.getString("_id");
                        username = resJSON.getString("name");

                        loadRestaurantCards();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        HashMap<String, String> createUserParams = new HashMap<>();
        createUserParams.put("id", idToken);
        createUserParams.put("name", user);
        createUserParams.put("email", email);

        OKHttpService.postRequest("createUser", createUserCallback, createUserParams);
    }
}
