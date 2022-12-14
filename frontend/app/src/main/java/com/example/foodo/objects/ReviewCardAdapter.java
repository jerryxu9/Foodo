package com.example.foodo.objects;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodo.R;
import com.example.foodo.service.OKHttpService;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class ReviewCardAdapter extends RecyclerView.Adapter<ReviewCardAdapter.Viewholder> {
    private final ArrayList<ReviewCard> reviewCardArrayList;
    private final String TAG = "ReviewCardAdapter";
    private String googlePlacesId;
    private String currUserID;

    public ReviewCardAdapter(ArrayList<ReviewCard> reviewCardArrayList, String googlePlacesId, String userID) {
        this.reviewCardArrayList = reviewCardArrayList;
        this.googlePlacesId = googlePlacesId;
        this.currUserID = userID;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_card, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        ReviewCard model = reviewCardArrayList.get(position);
        holder.reviewName.setText(model.getReviewerName());
        holder.reviewText.setText(model.getReviewText());
        holder.reviewRating.setText(model.getReviewRating() + " stars");

        if(model.getUserID().equals(currUserID)){
            holder.deleteReviewButton.setOnClickListener((View v)->{
                try {
                    Log.d(TAG, "Delete button has been pressed for review!");
                    holder.deleteReview(model.getReviewId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }else{
            holder.deleteReviewButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return reviewCardArrayList.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        private final TextView reviewName;
        private final TextView reviewRating;
        private final TextView reviewText;
        private final Button deleteReviewButton;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            reviewName = itemView.findViewById(R.id.reviewName);
            reviewRating = itemView.findViewById(R.id.reviewRating);
            reviewText = itemView.findViewById(R.id.reviewText);
            deleteReviewButton = itemView.findViewById(R.id.deleteReview);
        }

        public void deleteReview(String reviewID) throws JSONException {
            Callback deleteReviewCallback = new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    Log.d(TAG, OKHttpService.getResponseBody(response));
                }
            };

            HashMap<String, String> deleteReviewParams = new HashMap<>();
            deleteReviewParams.put("id", reviewID);
            deleteReviewParams.put("position", String.valueOf(getLayoutPosition()));
            deleteReviewParams.put("google_place_id", googlePlacesId);

            OKHttpService.deleteRequest("deleteReview", deleteReviewCallback, deleteReviewParams, new HashMap<>());
        }
    }
}
