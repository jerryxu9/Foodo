package com.example.foodo;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.foodo.databinding.ActivityMapsBinding;
import com.example.foodo.service.OKHttpService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private final String TAG = "MapActivity";
    private ArrayList<RestaurantMarkerInfo> markers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        markers = new ArrayList<>();
        if (getIntent().hasExtra("userID")) {
            String userID = getIntent().getStringExtra("userID");
            Log.d(TAG, userID + getIntent().getStringExtra("username"));
            getFoodoLists(userID);
        }

        ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //call foodoList API here to get all the foodolists of the user and then get the lat/lng of each restaurant in each list

        GoogleMap mMap = googleMap;

        for (RestaurantMarkerInfo info : markers) {
            mMap.addMarker(new MarkerOptions().position(info.getLatLng()).title(info.getName()));
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(49.3, -123), 10));
    }

    private void getFoodoLists(String userID) {

        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("userID", userID);

        Callback getFoodoListsCallback = new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String searchResults = OKHttpService.getResponseBody(response);
                try {
                    Log.d(TAG, String.format("response from /getFoodoLists: %s", searchResults));
                    getMapPins(new JSONArray(searchResults));
                    Log.d(TAG, markers.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        OKHttpService.getRequest("getFoodoLists", getFoodoListsCallback, queryParameters);
    }

    private void getMapPins(JSONArray foodoListsJSON) throws JSONException {
        for (int i = 0; i < foodoListsJSON.length(); i++) {
            JSONObject foodoListInfo = foodoListsJSON.getJSONObject(i);
            JSONArray restaurants = foodoListInfo.getJSONArray("restaurants");
            Log.d(TAG, restaurants.toString());

            for (int j = 0; j < restaurants.length(); j++) {
                JSONObject restaurantInfo = restaurants.getJSONObject(j);
                markers.add(new RestaurantMarkerInfo(
                        new LatLng(restaurantInfo.getDouble("lat"),
                                restaurantInfo.getDouble("lng")),
                        restaurantInfo.getString("name")));
            }
        }

    }

    public class RestaurantMarkerInfo {
        private final LatLng coordinates;
        private final String name;

        public RestaurantMarkerInfo(LatLng coordinates, String name) {
            this.coordinates = coordinates;
            this.name = name;
        }

        public LatLng getLatLng() {
            return coordinates;
        }

        public String getName() {
            return name;
        }
    }
}