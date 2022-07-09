package com.example.foodo.service;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodo.R;
import com.example.foodo.objects.RestaurantCard;
import com.example.foodo.objects.RestaurantCardAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FoodoListCardService {

    private final String TAG = "FoodoListCardService";

    private final String BASE_URL = "http://10.0.2.2:3000";
    private final OkHttpClient client = new OkHttpClient();
    private final AppCompatActivity foodoCardActivity;
    private final ArrayList<RestaurantCard> restaurantCardArrayList;
    private final String listID;
    private RestaurantCardAdapter restaurantCardAdapter;

    public FoodoListCardService(AppCompatActivity foodoCardActivity, String listID) {
        this.foodoCardActivity = foodoCardActivity;
        this.restaurantCardArrayList = new ArrayList<>();
        this.listID = listID;
        initializeComponents();
    }

    public void initializeComponents() {
        RecyclerView restaurantsView = foodoCardActivity.findViewById(R.id.foodo_list_card_restaurants_list);
        populateRestaurantCardsArray();

        restaurantCardAdapter = new RestaurantCardAdapter(foodoCardActivity, restaurantCardArrayList, listID);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(foodoCardActivity, LinearLayoutManager.VERTICAL, false);

        restaurantsView.setLayoutManager(linearLayoutManager);
        restaurantsView.setAdapter(restaurantCardAdapter);
    }


    private void populateRestaurantCardsArray() {

        String url = BASE_URL + "/getRestaurantIDsByFoodoListId";
        HttpUrl httpUrl = HttpUrl.parse(url);

        HttpUrl.Builder httpBuilder = httpUrl.newBuilder().addQueryParameter("listID", listID);

        Request request = new Request.Builder()
                .url(httpBuilder.build())
                .build();

        client.newCall((request)).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful())
                        throw new IOException(String.format("Unexpected code %s", response));
                    else if (responseBody == null) {
                        throw new IOException("null response from /getRestaurantIDsByFoodoListId endpoint");
                    } else {
                        JSONArray foodoListJSONArray = new JSONArray(responseBody.string());
                        // For each restaurant, collect its information and render it as a Restaurant Card
                        for (int i = 0; i < foodoListJSONArray.length(); i++) {
                            JSONObject restaurant = foodoListJSONArray.getJSONObject(i);
                            Log.d(TAG, String.format("Create Restaurant Card for %s under Foodo List %s", restaurant.toString(), listID));
                            String placeID = restaurant.getString("place_id");
                            String cardID = restaurant.getString("_id");
                            searchRestaurantInfo(placeID, cardID);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }
        });

    }

    private void searchRestaurantInfo(String googlePlaceID, String cardID) {

        String url = BASE_URL + "/searchRestaurantInfoByID";
        HttpUrl httpUrl = HttpUrl.parse(url);

        if (httpUrl == null) {
            Log.d(TAG, String.format("unable to parse server URL: %s", url));
            return;
        }

        HttpUrl.Builder httpBuilder = httpUrl.newBuilder().addQueryParameter("id", googlePlaceID);

        Request request = new Request.Builder()
                .url(httpBuilder.build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful())
                        throw new IOException(String.format("Unexpected code %s", response));
                    else if (responseBody == null) {
                        throw new IOException("null response from /searchRestaurantInfoByID endpoint");
                    } else {
                        JSONObject restaurant = new JSONObject(responseBody.string());
                        Log.d(TAG, restaurant.toString());
                        String businessStatus = getBusinessStatus(restaurant);
                        foodoCardActivity.runOnUiThread(() -> {
                            try {
                                restaurantCardArrayList.add(new RestaurantCard(
                                        restaurant.getString("name"),
                                        restaurant.getString("formatted_address"),
                                        restaurant.getString("rating"),
                                        businessStatus,
                                        googlePlaceID,
                                        cardID,
                                        getLatitude(restaurant),
                                        getLongitude(restaurant),
                                        true));
                                restaurantCardAdapter.notifyItemInserted(restaurantCardAdapter.getItemCount());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }
        });
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
}
