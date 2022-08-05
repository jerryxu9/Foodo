package com.example.foodo.objects;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodo.R;
import com.example.foodo.RestaurantInfoActivity;
import com.example.foodo.service.OKHttpService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RestaurantCardAdapter extends RecyclerView.Adapter<RestaurantCardAdapter.Viewholder> {
    private final Context context;
    private final ArrayList<RestaurantCard> restaurantCardArrayList;
    private final String TAG = "RestaurantCardAdapter";
    private final String listID;

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

        // Enable check button only if RestaurantCard is rendered from Foodo List
        if (isInFoodoList) {
            // render the card based on whether it's been visited or not
            ((Activity) context).runOnUiThread(() -> {
                holder.paintRestaurantCard(model.getVisited());
            });

            holder.checkFoodoListButton.setOnClickListener((View v) -> {
                Log.d(TAG, String.format("Checked %s", model.getName()));
                holder.checkRestaurant();
            });
        } else {
            holder.checkFoodoListButton.setEnabled(false);
            holder.checkFoodoListButton.setVisibility(View.INVISIBLE);
        }

        holder.setGooglePlacesID(model.getGooglePlacesID());
        holder.setCardID(model.getCardID());

        Log.d(TAG, String.valueOf(model.getVisited()));
        holder.setLat(model.getLat());
        holder.setLng(model.getLng());
    }

    public void addRestaurantCard(RestaurantCard card) {
        ((Activity) context).runOnUiThread(() -> {
            restaurantCardArrayList.add(card);
            notifyDataSetChanged();
        });
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
        private final Button checkFoodoListButton;
        private final RelativeLayout relativeLayout;
        private String googlePlacesID;
        private String cardID;
        private String username;
        private String userID;

        private double lat;
        private double lng;
        private boolean isInFoodoList;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            restaurantName = itemView.findViewById(R.id.restaurantName);
            restaurantAddress = itemView.findViewById(R.id.restaurantAddress);
            restaurantRating = itemView.findViewById(R.id.restaurantRating);
            restaurantStatus = itemView.findViewById(R.id.restaurantStatus);
            checkFoodoListButton = itemView.findViewById(R.id.check_status);
            relativeLayout = itemView.findViewById(R.id.restaurant_card_relative_layout);

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
            HashMap<String, String> bodyParameters = new HashMap<>();
            bodyParameters.put("listID", listID);
            bodyParameters.put("restaurantID", cardID);

            Callback deleteRestaurantFromFoodoListCallback = new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (!response.isSuccessful()) {
                        Log.d(TAG, String.format("Delete restaurant %s on foodo list under list id %s failed", getRestaurantName(), listID));
                    } else {
                        Log.d(TAG, String.format("Card with id %s will be deleted %s", cardID, getRestaurantName()));
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
            };

            OKHttpService.patchRequest("deleteRestaurantFromList", deleteRestaurantFromFoodoListCallback, bodyParameters);
        }

        public void checkRestaurant() {
            RestaurantCard card = restaurantCardArrayList.get(getLayoutPosition());
            card.setVisited(!card.getVisited());

            Callback checkRestaurantCallback = new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String result = OKHttpService.getResponseBody(response);
                    Log.d(TAG, result);

                    if (card.getInFoodoList()) {
                        ((Activity) context).runOnUiThread(() -> {
                            Log.d(TAG, String.valueOf(card.getVisited()));
                            paintRestaurantCard(card.getVisited());
                            notifyItemChanged(getLayoutPosition());
                        });
                    } else {
                        checkFoodoListButton.setVisibility(View.INVISIBLE);
                        checkFoodoListButton.setEnabled(false);
                    }
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }
            };

            Log.d(TAG, String.format("Card: Restaurant Card (id: %s) will be checked %s", cardID, getRestaurantName()));

            HashMap<String, String> checkRestaurantParams = new HashMap<>();
            checkRestaurantParams.put("listID", listID);
            checkRestaurantParams.put("restaurantID", cardID);
            checkRestaurantParams.put("isVisited", String.valueOf(card.getVisited()));

            OKHttpService.patchRequest("checkRestaurantOnList", checkRestaurantCallback, checkRestaurantParams);
        }

        public void paintRestaurantCard(boolean isVisited) {
            if (isVisited) {
                // set card color to blue if visited
                relativeLayout.setBackgroundColor(context.getResources().getColor(R.color.visited_blue));
                restaurantName.setTextColor(Color.WHITE);
                restaurantAddress.setTextColor(Color.WHITE);
                restaurantRating.setTextColor(Color.WHITE);
                checkFoodoListButton.setBackgroundResource(R.drawable.visited_image);
            } else {
                relativeLayout.setBackgroundColor(context.getResources().getColor(R.color.inner_boxes));
                restaurantName.setTextColor(Color.BLACK);
                restaurantAddress.setTextColor(Color.BLACK);
                restaurantRating.setTextColor(Color.BLACK);
                checkFoodoListButton.setBackgroundResource(R.drawable.checkmark_button);
            }
        }

        public boolean getVisited() {
            return restaurantCardArrayList.get(getLayoutPosition()).getVisited();
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

        public void setUsername(String username) {
            this.username = username;
        }

        public void setUserID(String userID) {
            this.userID = userID;
        }
    }
}
