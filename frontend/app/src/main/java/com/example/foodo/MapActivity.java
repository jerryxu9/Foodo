package com.example.foodo;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.foodo.databinding.ActivityMapsBinding;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private final String BASE_URL = "http://10.0.2.2:3000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
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

        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    //Will complete in a separate PR
//    private void getFoodoLists(String userID){
//        String url = buildURL("/getFoodoLists");
//        HttpUrl httpUrl = HttpUrl.parse(url);
//        HttpUrl.Builder httpBuilder = httpUrl.newBuilder();
//        httpBuilder.addQueryParameter("userID", userID);
//
//        Request request = new Request.Builder()
//                .url(httpBuilder.build())
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                String searchResults;
//                try (ResponseBody responseBody = response.body()) {
//                    if (!response.isSuccessful())
//                        throw new IOException(String.format("Unexpected code %s", response));
//                    else if (responseBody == null) {
//                        throw new IOException("null response from /searchRestaurantsByQuery endpoint");
//                    } else {
//                        searchResults = responseBody.string();
//                        Log.d(TAG, String.format("response from /getFoodoLists: %s", searchResults));
//
//                        JSONArray foodoListsJSON = new JSONArray(searchResults);
//                        for(int i = 0; i < foodoListsJSON.length(); i++){
//                            foodoListNames.add(foodoListsJSON.getJSONObject(i).getString("name"));
//                            foodoListIDandNames.put(foodoListsJSON.getJSONObject(i).getString("name"), foodoListsJSON.getJSONObject(i).getString("_id"));
//                        }
//
//                        Log.d(TAG, foodoListNames.toString());
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }
//
//    private String buildURL(String path){
//        return BASE_URL + path;
//    }
}