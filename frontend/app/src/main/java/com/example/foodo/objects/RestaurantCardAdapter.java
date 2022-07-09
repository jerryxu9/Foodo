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

    private final Context context;
    private final ArrayList<RestaurantCard> restaurantCardArrayList;

    private final String TAG = "RestaurantCardAdapter";
    private final String listID;
    private final String BASE_URL = "http://10.0.2.2:3000";
    private final OkHttpClient client = new OkHttpClient();
    private String cardID;


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

        // Enable delete button only if RestaurantCard is rendered from Foodo List
        if (isInFoodoList) {
            holder.deleteRestaurantFromFoodoListButton.setOnClickListener((View v) -> {
                Log.d(TAG, String.format("Deleted %s", model.getName()));
                holder.deleteRestaurantFromList();
            });
        } else {
            holder.deleteRestaurantFromFoodoListButton.setEnabled(false);
            holder.deleteRestaurantFromFoodoListButton.setVisibility(View.INVISIBLE);
        }

        holder.setGooglePlacesID(model.getGooglePlacesID());
        holder.setCardID(model.getCardID());

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
        private final Button deleteRestaurantFromFoodoListButton;
        private String googlePlacesID, cardID;
        private double lat, lng;
        private boolean isInFoodoList;


        public Viewholder(@NonNull View itemView) {
            super(itemView);
            restaurantName = itemView.findViewById(R.id.restaurantName);
            restaurantAddress = itemView.findViewById(R.id.restaurantAddress);
            restaurantRating = itemView.findViewById(R.id.restaurantRating);
            restaurantStatus = itemView.findViewById(R.id.restaurantStatus);

            deleteRestaurantFromFoodoListButton = itemView.findViewById(R.id.delete_restaurant_from_foodo_list_button);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.getContext().startActivity(new Intent(v.getContext(), RestaurantInfoActivity.class)
                            .putExtra("restaurantName", restaurantName.getText())
                            .putExtra("restaurantAddress", restaurantAddress.getText())
                            .putExtra("restaurantRating", restaurantRating.getText())
                            .putExtra("restaurantStatus", restaurantStatus.getText())
                            .putExtra("googlePlacesID", googlePlacesID)
                            .putExtra("lat", lat)
                            .putExtra("lng", lng)
                            .putExtra("isInFoodoList", isInFoodoList));

                }
            });

        }

        public void deleteRestaurantFromList() {
            String url = BASE_URL + "/deleteRestaurantFromList";

            Log.d(TAG, "CArd:" + cardID + " will be deleted" + getRestaurantName());

            HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();

            String json = String.format("{\"listID\": \"%s\", \"restaurantID\": \"%s\"}", listID, cardID);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"), json);

            Request request = new Request.Builder()
                    .url(httpBuilder.build())
                    .patch(body)
                    .build();

            Log.d(TAG, request + "body: " + json);

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
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
    }
}
