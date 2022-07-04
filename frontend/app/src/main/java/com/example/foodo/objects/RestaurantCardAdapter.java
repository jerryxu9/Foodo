package com.example.foodo.objects;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodo.R;
import com.example.foodo.RestaurantInfoActivity;

import java.util.ArrayList;

public class RestaurantCardAdapter extends RecyclerView.Adapter<RestaurantCardAdapter.Viewholder> {

    private Context context;
    private ArrayList<RestaurantCard> restaurantCardArrayList;

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
        holder.restaurantRating.setText(model.getRating());
        holder.restaurantStatus.setText(model.getStatus());
        switch(model.getStatus()){
            case "Open": holder.restaurantStatus.setBackgroundResource(R.drawable.open_tag); break;
            case "Closed": holder.restaurantStatus.setBackgroundResource(R.drawable.closed_tag); break;
            default: holder.restaurantStatus.setBackgroundResource(R.drawable.non_operational_tag); break;
        }
        holder.setRestaurantPhoneNumber(model.getPhoneNumber());
        holder.setRestaurantID(model.getId());
    }

    @Override
    public int getItemCount() {
        return restaurantCardArrayList.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        private TextView restaurantName, restaurantAddress, restaurantRating, restaurantStatus;
        private String restaurantPhoneNumber, restaurantID;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            restaurantName = itemView.findViewById(R.id.restaurantName);
            restaurantAddress = itemView.findViewById(R.id.restaurantAddress);
            restaurantRating = itemView.findViewById(R.id.restaurantRating);
            restaurantStatus = itemView.findViewById(R.id.restaurantStatus);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override

                //Also need to get restaurant ID here so that we can get the list of reviews in the
                //restaurant info view
                public void onClick(View v) {
                    v.getContext().startActivity(new Intent(v.getContext(), RestaurantInfoActivity.class)
                            .putExtra("restaurantPhoneNumber", restaurantPhoneNumber)
                            .putExtra("restaurantName", restaurantName.getText())
                            .putExtra("restaurantAddress", restaurantAddress.getText())
                            .putExtra("restaurantRating", restaurantRating.getText())
                            .putExtra("restaurantStatus", restaurantStatus.getText())
                            .putExtra("restaurantID", restaurantID));
                }
            });
        }

        public void setRestaurantPhoneNumber(String phoneNumber){
            this.restaurantPhoneNumber = phoneNumber;
        }

        public void setRestaurantID(String id){
            this.restaurantID = id;
        }
    }
}
