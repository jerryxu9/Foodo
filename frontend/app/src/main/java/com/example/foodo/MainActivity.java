package com.example.foodo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.idling.CountingIdlingResource;

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
    private final float SWIPE_THRESHOLD = 0.6f;
    private FoodoListService foodoListService;
    private GoogleSignInClient mGoogleSignInClient;
    private Intent mapsIntent;
    private Intent searchResultIntent;
    private RecyclerView foodoLists;
    private Button loginButton;
    private Button logoutButton;
    private TextView loginText;
    private LocationManager locationManager;
    private Double lat;
    private Double lng;
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            lat = location.getLatitude();
            lng = location.getLongitude();
        }
    };
    private CountingIdlingResource searchQueryCountingIdlingResource;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mapsIntent = new Intent(MainActivity.this, MapActivity.class);
        searchResultIntent = new Intent(MainActivity.this, SearchResultActivity.class);
        loginButton = findViewById(R.id.login_button);
        loginText = findViewById(R.id.login_text);
        logoutButton = findViewById(R.id.logout_button);

        logoutButton.setOnClickListener(v -> signOut());


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
            hideLogoutButton();
            loginButton.setOnClickListener((View v) -> signIn());
        }

        Log.d(TAG, "starting idling resource");
        searchQueryCountingIdlingResource = new CountingIdlingResource("QueryCountingIdlingResource");

        setupButtonListeners();
        setupFoodoLists();
        setupSwipeListeners();
    }

    private void setupButtonListeners() {

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

        restaurantSearch.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            @SuppressLint("MissingPermission")
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission was not granted, requesting permissions now");
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, locationListener);
                    Log.d(TAG, "Permissions set!");
                }
            }
        });

        restaurantSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Location permissions are needed in order to search!", Toast.LENGTH_LONG).show();
                    return false;
                } else {
                    return searchRestaurant(query);
                }
            }

            @Override
            @SuppressLint("MissingPermission")
            public boolean onQueryTextChange(String newText) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, locationListener);
                }
                return false;
            }

        });
    }

    private void setupSwipeListeners() {
        ItemTouchHelper.SimpleCallback deleteFoodoListCallback = new ItemTouchHelper
                .SimpleCallback(0, ItemTouchHelper.LEFT) {

            /**
             * Called when ItemTouchHelper wants to move the dragged item from its old position to
             * the new position.
             * <p>
             * If this method returns true, ItemTouchHelper assumes {@code viewHolder} has been moved
             * to the adapter position of {@code target} ViewHolder
             * ({@link RecyclerView.ViewHolder#getAdapterPosition()
             * ViewHolder#getAdapterPosition()}).
             * <p>
             * If you don't support drag & drop, this method will never be called.
             *
             * @param recyclerView The RecyclerView to which ItemTouchHelper is attached to.
             * @param viewHolder   The ViewHolder which is being dragged by the user.
             * @param target       The ViewHolder over which the currently active item is being
             *                     dragged.
             * @return True if the {@code viewHolder} has been moved to the adapter position of
             * {@code target}.
             */
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            /**
             * Called when a ViewHolder is swiped by the user.
             * <p>
             * If you are returning relative directions (START , END) from the
             * getMovementFlags(RecyclerView, ViewHolder)} method, this method
             * will also use relative directions. Otherwise, it will use absolute directions.
             * <p>
             * If you don't support swiping, this method will never be called.
             * <p>
             * ItemTouchHelper will keep a reference to the View until it is detached from
             * RecyclerView.
             * As soon as it is detached, ItemTouchHelper will call
             * clearView(RecyclerView, ViewHolder).
             *
             * @param viewHolder The ViewHolder which has been swiped by the user.
             * @param direction  The direction to which the ViewHolder is swiped. It is one of
             *                   UP, DOWN,
             *                   LEFT or RIGHT. If your getMovementFlags(RecyclerView, ViewHolder
             *                   method returned relative flags instead of LEFT / RIGHT;
             *                   `direction` will be relative as well. (START or END).
             */
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                ((FoodoListCardAdapter.Viewholder) viewHolder).handleDeleteFoodoListAction();
            }

            /**
             * Determines the amount to swipe before onSwiped() is triggered
             * @param viewHolder the viewHolder of the item currently being interacted with
             * @return a swipe threshold value
             *
             * "1f - means that the action will be performed with a full card swap.
             * 0.1f - the action will be performed when the card moves to 10% of the screen width"
             *
             * Source: https://stackoverflow.com/questions/52726954/how-to-set-the-swipe-threshold-to-half-the-screen
             *
             */
            @Override
            public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
                return SWIPE_THRESHOLD;
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewholder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                setDeleteIcon(c, viewholder, dX, isCurrentlyActive);
                super.onChildDraw(c, recyclerView, viewholder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(deleteFoodoListCallback).attachToRecyclerView(foodoLists);

    }

    private void setupFoodoLists() {
        FoodoListCardAdapter foodoListCardAdapter = new FoodoListCardAdapter(this, new ArrayList<FoodoListCard>());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        this.foodoListService = new FoodoListService(this, foodoListCardAdapter);

        foodoLists = findViewById(R.id.foodo_lists);

        foodoListService.setupUserAccount();

        foodoLists.setLayoutManager(linearLayoutManager);
        foodoLists.setAdapter(foodoListCardAdapter);

        foodoListService.loadFoodoLists();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, task -> {
                    showLoginPrompts();
                    hideLogoutButton();
                    Toast.makeText(MainActivity.this, "You have successfully logged out", Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void hideLogoutButton() {
        logoutButton.setVisibility(View.INVISIBLE);
    }

    private void showLogoutButton() {
        logoutButton.setVisibility(View.VISIBLE);
    }

    private void hideLoginPrompts() {
        loginButton.setVisibility(View.INVISIBLE);
        loginText.setVisibility(View.INVISIBLE);
    }

    private void showLoginPrompts() {
        loginButton.setVisibility(View.VISIBLE);
        loginText.setVisibility(View.VISIBLE);
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
        showLogoutButton();
    }

    private boolean searchRestaurant(String query) {

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
                Log.d(TAG, searchResults);
                try {
                    JSONArray restaurantResultsArray = new JSONArray(searchResults);
                    Log.d(TAG, String.format("response from /searchRestaurantsByQuery: %s", searchResults));
                    runOnUiThread(() -> {
                        searchResultIntent.putExtra("restaurantResultsArray", restaurantResultsArray.toString());
                        searchResultIntent.putExtra("query", query);
                        startActivity(searchResultIntent);
                        if (searchQueryCountingIdlingResource != null) {
                            Log.d(TAG, "Decrement");
                            searchQueryCountingIdlingResource.decrement();
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        if (searchQueryCountingIdlingResource != null) {
            Log.d(TAG, "Increment. Awiting");
            searchQueryCountingIdlingResource.increment();
        }

        Log.d(TAG, "Hello?");
        OKHttpService.getRequest("searchRestaurantsByQuery", searchRestaurantCallback, queryParameters);

        return true;
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

    public CountingIdlingResource getSearchQueryCountingIdlingResource() {
        return searchQueryCountingIdlingResource;
    }

    /**
     * Source: https://www.youtube.com/watch?v=l3bkFT-NZHk
     */
    private void setDeleteIcon(Canvas c, RecyclerView.ViewHolder viewHolder,
                               float dX, boolean isCurrentlyActive) {

        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getHeight();

        boolean isCancelled = dX == 0 && !isCurrentlyActive;

        Paint mClearPaint = new Paint();
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        if (isCancelled) {
            c.drawRect(itemView.getRight() + dX, (float) itemView.getTop(),
                    (float) itemView.getRight(), (float) itemView.getBottom(), mClearPaint);
            return;
        }

        GradientDrawable deleteActionBackground = new GradientDrawable();
        deleteActionBackground.setCornerRadius(25f);

        Drawable deleteDrawable = ContextCompat.getDrawable(this, R.drawable.delete_button);

        int intrinsicWidth = deleteDrawable.getIntrinsicWidth();
        int intrinsicHeight = deleteDrawable.getIntrinsicHeight();

        int deleteIconMargin = (itemHeight - intrinsicHeight) / 2;
        int deleteIconTop = itemView.getTop() + deleteIconMargin;
        int deleteIconRight = itemView.getRight() - deleteIconMargin;
        int deleteIconLeft = deleteIconRight - intrinsicWidth;
        int deleteIconBottom = deleteIconTop + intrinsicHeight;

        int deleteActionBackgroundColor = getResources().getColor(R.color.delete_button_red);
        deleteActionBackground.setColor(deleteActionBackgroundColor);

        // Use delete Icon position to determine bounds for the red background when sliding
        // left = deleteIconLeft + dX is because we want the left edge of the red background
        // to be even with delete icon plus the amount we swiped left.
        deleteActionBackground.setBounds(
                deleteIconLeft + (int) dX,
                deleteIconTop,
                deleteIconRight,
                deleteIconBottom);
        deleteActionBackground.draw(c);

        deleteDrawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
        deleteDrawable.draw(c);

    }

}
