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

    private RecyclerView restaurantsView;

    public FoodoListCardService(AppCompatActivity foodoCardActivity, String listID) {
        this.foodoCardActivity = foodoCardActivity;
        restaurantCardArrayList = new ArrayList<>();
        this.listID = listID;
    }

    public void setup() {
        restaurantsView = foodoCardActivity.findViewById(R.id.foodo_list_card_restaurants_list);
        populateRestaurantCardsArray();


        restaurantCardAdapter = new RestaurantCardAdapter(foodoCardActivity, restaurantCardArrayList, listID);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(foodoCardActivity, LinearLayoutManager.VERTICAL, false);

        restaurantsView.setLayoutManager(linearLayoutManager);
        restaurantsView.setAdapter(restaurantCardAdapter);
    }


    private void populateRestaurantCardsArray() {

        String url = BASE_URL + "/getRestaurantIDsByFoodoListId";
        HttpUrl httpUrl = HttpUrl.parse(url);

        if (httpUrl == null) {
            Log.d(TAG, String.format("unable to parse server URL: %s", url));
            return;
        }
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
                        // For each restaurant, collect its information and render it as a card.
                        for (int i = 0; i < foodoListJSONArray.length(); i++) {
                            JSONObject restaurant = foodoListJSONArray.getJSONObject(i);
                            Log.d(TAG, String.format("Create restaurantcard %s under Foodo List %s", restaurant.toString(), listID));
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
                        String responseBodyString = responseBody.string();
                        JSONObject restaurantObj = new JSONObject(responseBodyString);
                        Log.d(TAG, restaurantObj.toString());
                        String businessStatus = restaurantObj.getString("business_status");
                        if (businessStatus.equals("OPERATIONAL")) {
                            if (restaurantObj.getJSONObject("opening_hours").getBoolean("open_now")) {
                                businessStatus = "Open";
                            } else {
                                businessStatus = "Closed";
                            }
                        } else {
                            businessStatus = restaurantObj.getString("businessStatus");
                        }

                        String finalBusinessStatus = businessStatus;
                        foodoCardActivity.runOnUiThread(() -> {
                            try {
                                boolean isInFoodoList = true;

                                RestaurantCard restaurantCard = new RestaurantCard(restaurantObj.getString("name"),
                                        restaurantObj.getString("formatted_address"),
                                        restaurantObj.getString("rating"),
                                        finalBusinessStatus,
                                        googlePlaceID,
                                        cardID,
                                        restaurantObj.getJSONObject("geometry").getJSONObject("location").getDouble("lat"),
                                        restaurantObj.getJSONObject("geometry").getJSONObject("location").getDouble("lng"),
                                        isInFoodoList);

                                restaurantCardArrayList.add(restaurantCard);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            restaurantCardAdapter.notifyItemInserted(restaurantCardAdapter.getItemCount());
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

}
