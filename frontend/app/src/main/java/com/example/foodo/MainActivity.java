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

import com.example.foodo.service.FoodoListService;
import com.example.foodo.service.OKHttpService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    private final String TAG = "MainActivity";
    private final OkHttpClient client = new OkHttpClient();
    private static final String BASE_URL = "http://20.51.215.223:3000";
//    private LocationManager locationManager;
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//    private SearchView restaurantSearch;
//    private Button mapButton;
    private GoogleSignInClient mGoogleSignInClient;
//    private FoodoListService foodoListService;
    private Intent mapsIntent, searchResultIntent;
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
        FoodoListService foodoListService = new FoodoListService(this);
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
            loginButton.setVisibility(View.VISIBLE);
            loginText.setVisibility(View.VISIBLE);
            loginButton.setOnClickListener((View v) -> signIn());
        }

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

        Button mapButton = findViewById(R.id.map_button);
        mapButton.setOnClickListener((View v) -> startActivity(mapsIntent));
        foodoListService.setup();
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
        String url = buildURL("/createUser");
        HttpUrl httpUrl = HttpUrl.parse(url);

        if (httpUrl == null) {
            Log.d(TAG, String.format("unable to parse server URL: %s", url));
            return;
        }
        Log.d(TAG, username);

        Map<String, String> params = new HashMap<>();
        params.put("id", idToken);
        params.put("name", username);
        params.put("email", email);

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
                String responseBodyString;
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        signIn();
                    } else {
                        responseBodyString = responseBody.string();
                        JSONObject resJSON = new JSONObject(responseBodyString);

                        if (resJSON.has("error")) {
                            //validation failed, perhaps the token has expired, login again
                            signIn();
                        } else {
                            Log.d(TAG, responseBodyString);
                            JSONObject responseBodyJSON = new JSONObject(responseBodyString);

                            Log.d(TAG, responseBodyJSON.getString("_id"));

                            mapsIntent.putExtra("username", responseBodyJSON.getString("name"));
                            mapsIntent.putExtra("userID", responseBodyJSON.getString("_id"));

                            searchResultIntent.putExtra("username", responseBodyJSON.getString("name"));
                            searchResultIntent.putExtra("userID", responseBodyJSON.getString("_id"));

                            Log.d(TAG, "intent extras have all been added");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String buildURL(String path) {
        return BASE_URL + path;
    }

//    // Declare the launcher at the top of your Activity/Fragment:
//    private final ActivityResultLauncher<String> requestPermissionLauncher =
//            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
//                if (isGranted) {
//                    // FCM SDK (and your app) can post notifications.
//                } else {
//                    // TODO: Inform user that that your app will not show notifications.
//                }
//            });
//
//    // ...
//    @RequiresApi(api = Build.VERSION_CODES.M)
//    private void askNotificationPermission() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
//                PackageManager.PERMISSION_GRANTED) {
//            // FCM SDK (and your app) can post notifications.
//        } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
//            // TODO: display an educational UI explaining to the user the features that will be enabled
//            //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
//            //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
//            //       If the user selects "No thanks," allow the user to continue without notifications.
//
//        } else {
//            // Directly ask for the permission
//            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
//        }
//    }
}

