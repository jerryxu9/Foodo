package com.example.foodo.objects;

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

import java.util.ArrayList;

public class RestaurantCardAdapter extends RecyclerView.Adapter<RestaurantCardAdapter.Viewholder> {

    private final Context context;
    private final ArrayList<RestaurantCard> restaurantCardArrayList;
    private final String TAG = "RestaurantCardAdapter";

    public RestaurantCardAdapter(Context context, ArrayList<RestaurantCard> restaurantCardArrayList) {
        this.context = context;
        this.restaurantCardArrayList = restaurantCardArrayList;
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
                Log.d(TAG, String.format("Delete %s", model.getName()));
            });
        } else {
            holder.deleteRestaurantFromFoodoListButton.setEnabled(false);
            holder.deleteRestaurantFromFoodoListButton.setVisibility(View.INVISIBLE);
        }

        holder.setRestaurantID(model.getId());
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
        private String restaurantID;
        private double lat, lng;
        private boolean isInFoodoList;
        private final Button deleteRestaurantFromFoodoListButton;

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
                            .putExtra("restaurantID", restaurantID)
                            .putExtra("lat", lat)
                            .putExtra("lng", lng)
                            .putExtra("isInFoodoList", isInFoodoList));
                }
            });

        }

        public void setRestaurantID(String id) {
            this.restaurantID = id;
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

        public void setFoodoListID(String listID) {

        }
    }
}
