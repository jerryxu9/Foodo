package com.example.foodo.objects;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodo.BuildConfig;
import com.example.foodo.R;
import com.example.foodo.RestaurantInfoActivity;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RestaurantCardAdapter extends RecyclerView.Adapter<RestaurantCardAdapter.Viewholder> {

    private static final String BASE_URL = BuildConfig.BASE_URL;
    private final Context context;
    private final ArrayList<RestaurantCard> restaurantCardArrayList;
    private final String TAG = "RestaurantCardAdapter";
    private final String listID;
    private final OkHttpClient client = new OkHttpClient();

    public RestaurantCardAdapter(Context context, ArrayList<RestaurantCard> restaurantCardArrayList, String listID) {
        this.context = context;
        this.restaurantCardArrayList = restaurantCardArrayList;
        this.listID = listID;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_card, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        RestaurantCard model = restaurantCardArrayList.get(position);
        holder.restaurantName.setText(model.getName());
        holder.restaurantAddress.setText("" + model.getAddress());
        holder.restaurantRating.setText(model.getRating() + " stars");
        holder.restaurantStatus.setText(model.getStatus());
        switch (model.getStatus()) {
            case "Open":
                holder.restaurantStatus.setBackgroundResource(R.drawable.open_tag);
                break;
            case "Closed":
                holder.restaurantStatus.setBackgroundResource(R.drawable.closed_tag);
                break;
            default:
                holder.restaurantStatus.setBackgroundResource(R.drawable.non_operational_tag);
                break;
        }

        boolean isInFoodoList = model.getInFoodoList();
        holder.setIsInFoodoList(isInFoodoList);
        holder.setUsername(model.getUsername());
        holder.setUserID(model.getUserID());

        // Enable delete and check button only if RestaurantCard is rendered from Foodo List
        if (isInFoodoList) {
            holder.deleteRestaurantFromFoodoListButton.setOnClickListener((View v) -> {
                Log.d(TAG, String.format("Deleted %s", model.getName()));
                holder.deleteRestaurantFromList();
            });
            ((Activity) context).runOnUiThread(() -> {
                if (model.getVisited()) {
                    holder.checkFoodoListButton.setBackgroundResource(R.drawable.visited_image);
                } else {
                    holder.checkFoodoListButton.setBackgroundResource(R.drawable.checkmark_button);
                }
            });

            holder.checkFoodoListButton.setOnClickListener((View v) -> {
                Log.d(TAG, String.format("Checked %s", model.getName()));
                holder.checkRestaurant();
            });
        } else {
            holder.checkFoodoListButton.setEnabled(false);
            holder.checkFoodoListButton.setVisibility(View.INVISIBLE);
            holder.deleteRestaurantFromFoodoListButton.setEnabled(false);
            holder.deleteRestaurantFromFoodoListButton.setVisibility(View.INVISIBLE);
        }

        holder.setGooglePlacesID(model.getGooglePlacesID());
        holder.setCardID(model.getCardID());

        Log.d(TAG, String.valueOf(model.getVisited()));
        holder.setVisited(model.getVisited());
        holder.setLat(model.getLat());
        holder.setLng(model.getLng());
    }

    @Override
    public int getItemCount() {
        return restaurantCardArrayList.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        private final TextView restaurantName;
        private final TextView restaurantAddress;
        private final TextView restaurantRating;
        private final TextView restaurantStatus;
        private final Button deleteRestaurantFromFoodoListButton, checkFoodoListButton;
        private String googlePlacesID, cardID, username, userID;

        private double lat, lng;
        private boolean isInFoodoList, isVisited;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            restaurantName = itemView.findViewById(R.id.restaurantName);
            restaurantAddress = itemView.findViewById(R.id.restaurantAddress);
            restaurantRating = itemView.findViewById(R.id.restaurantRating);
            restaurantStatus = itemView.findViewById(R.id.restaurantStatus);
            deleteRestaurantFromFoodoListButton = itemView.findViewById(R.id.delete_restaurant_from_foodo_list_button);
            checkFoodoListButton = itemView.findViewById(R.id.check_button);

            itemView.setOnClickListener((View v) -> {

                        v.getContext().startActivity(new Intent(v.getContext(), RestaurantInfoActivity.class)
                                .putExtra("restaurantName", restaurantName.getText())
                                .putExtra("restaurantAddress", restaurantAddress.getText())
                                .putExtra("restaurantRating", restaurantRating.getText())
                                .putExtra("restaurantStatus", restaurantStatus.getText())
                                .putExtra("googlePlacesID", googlePlacesID)
                                .putExtra("lat", lat)
                                .putExtra("lng", lng)
                                .putExtra("isInFoodoList", isInFoodoList)
                                .putExtra("username", username)
                                .putExtra("userID", userID));
                    }
            );
        }

        public void deleteRestaurantFromList() {
            String url = BASE_URL + "/deleteRestaurantFromList";
            Log.d(TAG, "Card:" + cardID + " will be deleted " + getRestaurantName());

            HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();

            String json = String.format("{\"listID\": \"%s\", \"restaurantID\": \"%s\"}", listID, cardID);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"), json);

            Request request = new Request.Builder()
                    .url(httpBuilder.build())
                    .patch(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (!response.isSuccessful()) {
                        Log.d(TAG, String.format("Delete restaurant %s on foodo list under list id %s failed", getRestaurantName(), listID));
                    } else {
                        ((Activity) context).runOnUiThread(() -> {
                            restaurantCardArrayList.remove(getLayoutPosition());
                            notifyItemRemoved(getLayoutPosition());
                        });
                    }
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }
            });
        }

        public void checkRestaurant() {
            String url = BASE_URL + "/checkRestaurantOnList";
            Log.d(TAG, String.format("Card: Restaurant Card (id: %s) will be checked %s", cardID, getRestaurantName()));

            RestaurantCard card = restaurantCardArrayList.get(getLayoutPosition());
            card.setVisited(!card.getVisited());

            HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();
            String json = String.format("{\"listID\": \"%s\", \"restaurantID\": \"%s\", \"isVisited\": %b }", listID, cardID, card.getVisited());

            Log.d(TAG, json);
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"), json);

            Request request = new Request.Builder()
                    .url(httpBuilder.build())
                    .patch(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (!response.isSuccessful()) {
                        Log.d(TAG, String.format("Check restaurant %s on foodo list under list id %s failed", getRestaurantName(), listID));
                    } else {
                        if (card.getInFoodoList()) {
                            ((Activity) context).runOnUiThread(() -> {
                                Log.d(TAG, String.valueOf(card.getVisited()));
                                if (card.getVisited()) {
                                    checkFoodoListButton.setBackgroundResource(R.drawable.visited_image);
                                } else {
                                    checkFoodoListButton.setBackgroundResource(R.drawable.checkmark_button);
                                }
                                notifyItemChanged(getLayoutPosition());
                            });
                        } else {
                            checkFoodoListButton.setVisibility(View.INVISIBLE);
                            checkFoodoListButton.setEnabled(false);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }
            });
        }

        public String getRestaurantName() {
            return (String) restaurantName.getText();
        }

        public void setGooglePlacesID(String googlePlacesID) {
            this.googlePlacesID = googlePlacesID;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }

        public void setIsInFoodoList(boolean isInFoodoList) {
            this.isInFoodoList = isInFoodoList;
        }

        public void setCardID(String id) {
            this.cardID = id;
        }

        public void setVisited(boolean visited) {
            this.isVisited = visited;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setUserID(String userID) {
            this.userID = userID;
        }
    }
}
