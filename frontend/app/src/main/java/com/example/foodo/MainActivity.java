package com.example.foodo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodo.objects.FoodoListCard;
import com.example.foodo.objects.FoodoListCardAdapter;
import com.example.foodo.service.FoodoListService;
import com.example.foodo.service.OKHttpService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    private final String TAG = "MainActivity";
    private FoodoListService foodoListService;
    private GoogleSignInClient mGoogleSignInClient;
    private Intent mapsIntent;
    private Intent searchResultIntent;
    private Button loginButton;
    private TextView loginText;
    private Double lat;
    private Double lng;

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            lat = location.getLatitude();
            lng = location.getLongitude();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapsIntent = new Intent(MainActivity.this, MapActivity.class);
        searchResultIntent = new Intent(MainActivity.this, SearchResultActivity.class);
        loginButton = findViewById(R.id.login_button);
        loginText = findViewById(R.id.login_text);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (account != null) {
            handleSuccessfulSignIn(account);
        } else {
            loginButton.setOnClickListener((View v) -> signIn());
        }
        setupComponentListeners();
        setupFoodoLists();
    }

    private void setupComponentListeners() {

        Button mapButton = findViewById(R.id.map_button);
        mapButton.setOnClickListener((View v) -> startActivity(mapsIntent));

        FloatingActionButton createFoodoListButton = findViewById(R.id.create_foodo_list_button);
        createFoodoListButton.setOnClickListener((View v) -> {
            foodoListService.createFoodoList();
        });

        FloatingActionButton refreshButton = findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener((View v) -> {
            foodoListService.refreshFoodoLists();
        });

        SearchView restaurantSearch = findViewById(R.id.restaurant_search);
        restaurantSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchRestaurant(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void setupFoodoLists() {
        FoodoListCardAdapter foodoListCardAdapter = new FoodoListCardAdapter(this, new ArrayList<FoodoListCard>());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        this.foodoListService = new FoodoListService(this, foodoListCardAdapter);

        RecyclerView foodoLists = findViewById(R.id.foodo_lists);

        foodoListService.setupUserAccount();

        foodoLists.setLayoutManager(linearLayoutManager);
        foodoLists.setAdapter(foodoListCardAdapter);

        foodoListService.loadFoodoLists();
    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void hideLoginPrompts() {
        loginButton.setVisibility(View.INVISIBLE);
        loginText.setVisibility(View.INVISIBLE);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            handleSuccessfulSignIn(account);
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void handleSuccessfulSignIn(GoogleSignInAccount account) {
        String username = account.getDisplayName();
        Log.d(TAG, account.getIdToken() + " and " + username);
        createUser(account.getIdToken(), username, account.getEmail());
        hideLoginPrompts();
    }

    @SuppressLint("MissingPermission")
    private void searchRestaurant(String query) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission was not granted, requesting permissions now");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, locationListener);

        if (lat == null || lng == null) {
            Toast.makeText(this, "Unable to get location, please try again", Toast.LENGTH_LONG);
            return;
        }

        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("query", query);
        queryParameters.put("lat", String.valueOf(lat));
        queryParameters.put("lng", String.valueOf(lng));

        Callback searchRestaurantCallback = new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String searchResults = OKHttpService.getResponseBody(response);
                try {
                    JSONArray restaurantResultsArray = new JSONArray(searchResults);
                    Log.d(TAG, String.format("response from /searchRestaurantsByQuery: %s", searchResults));
                    runOnUiThread(() -> {
                        searchResultIntent.putExtra("restaurantResultsArray", restaurantResultsArray.toString());
                        searchResultIntent.putExtra("query", query);
                        startActivity(searchResultIntent);
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        OKHttpService.getRequest("searchRestaurantsByQuery", searchRestaurantCallback, queryParameters);
    }

    private void createUser(String idToken, String username, String email) {
        Callback createUserCallback = new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseBodyString = OKHttpService.getResponseBody(response);
                    JSONObject resJSON = new JSONObject(responseBodyString);

                    if (resJSON.has("error")) {
                        //validation failed, perhaps the token has expired, login again
                        signIn();
                    } else {
                        Log.d(TAG, responseBodyString);
                        Log.d(TAG, resJSON.getString("_id"));

                        mapsIntent.putExtra("username", resJSON.getString("name"));
                        mapsIntent.putExtra("userID", resJSON.getString("_id"));

                        searchResultIntent.putExtra("username", resJSON.getString("name"));
                        searchResultIntent.putExtra("userID", resJSON.getString("_id"));

                        Log.d(TAG, "intent extras have all been added");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        Log.d(TAG, username);

        HashMap<String, String> createUserParams = new HashMap<>();
        createUserParams.put("id", idToken);
        createUserParams.put("name", username);
        createUserParams.put("email", email);

        OKHttpService.postRequest("createUser", createUserCallback, createUserParams);
    }
}
