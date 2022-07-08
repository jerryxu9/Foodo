package com.example.foodo.service;

import android.util.Log;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodo.R;
import com.example.foodo.objects.FoodoListCard;
import com.example.foodo.objects.FoodoListCardAdapter;
import com.example.foodo.objects.RestaurantCard;
import com.example.foodo.objects.RestaurantCardAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

    private final String USERID = "test@gmail.com";
    private final String BASE_URL = "http://10.0.2.2:3000";
    private final OkHttpClient client = new OkHttpClient();
    private AppCompatActivity foodoCardActivity;
    private ArrayList<RestaurantCard> restaurantCardArrayList;
    private String listID;

    private RecyclerView restaurantsView;

    public FoodoListCardService(AppCompatActivity foodoCardActivity, String listID) {
        this.foodoCardActivity = foodoCardActivity;
        restaurantCardArrayList = new ArrayList<>();
        this.listID = listID;
    }

    public void setup() {
        restaurantsView = foodoCardActivity.findViewById(R.id.foodo_list_card_restaurants_list);

        populateRestaurantCardsArray();

        RestaurantCardAdapter restaurantCardAdapter = new RestaurantCardAdapter(foodoCardActivity, restaurantCardArrayList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(foodoCardActivity, LinearLayoutManager.VERTICAL, false);

        restaurantsView.setLayoutManager(linearLayoutManager);
        restaurantsView.setAdapter(restaurantCardAdapter);
    }

    private void populateRestaurantCardsArray() {
        String url = BASE_URL + "/getFoodoLists";
        HttpUrl httpUrl = HttpUrl.parse(url);

        if (httpUrl == null) {
            Log.d(TAG, String.format("unable to parse server URL: %s", url));
            return;
        }
        HttpUrl.Builder httpBuilder = httpUrl.newBuilder().addQueryParameter("userID", USERID);

        Request request = new Request.Builder()
                .url(httpBuilder.build())
                .build();

        client.newCall((request)).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful())
                        throw new IOException(String.format("Unexpected code %s", response));
                    else if (responseBody == null) {
                        throw new IOException("null response from /getFoodoLists endpoint");
                    } else {
                        JSONArray foodoListJSONArray = new JSONArray(responseBody.string());
                        for (int i = 0; i < foodoListJSONArray.length(); i++) {
                            JSONObject foodoListJSON = (JSONObject) foodoListJSONArray.get(i);
                            String id = foodoListJSON.getString("_id");
                            String name = foodoListJSON.getString("name");
                            if(listID.equals(id)) {
                                getRestaurantsByFoodoList(foodoListJSON);
                            }
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

    private void getRestaurantsByFoodoList(JSONObject foodoListJSON) throws JSONException {
        JSONArray restaurantsArray = foodoListJSON.getJSONArray("restaurants");
        Log.d(TAG, restaurantsArray.toString());
        // For each restaurant, populate a data array and render it.
    }


//    private final FoodoListCardAdapter foodoListCardAdapter;
//    private final LinearLayoutManager linearLayoutManager;
//    private final ArrayList<FoodoListCard> foodoListCardArrayList;
//    private RecyclerView foodoLists;
//    private FloatingActionButton createFoodoListButton, refreshButton;
//    private PopupWindow createFoodoListPopupWindow;
}
