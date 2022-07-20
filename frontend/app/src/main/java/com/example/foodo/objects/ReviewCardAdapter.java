package com.example.foodo.objects;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodo.R;

import java.util.ArrayList;

public class ReviewCardAdapter extends RecyclerView.Adapter<ReviewCardAdapter.Viewholder> {
    private final Context context;
    private final ArrayList<ReviewCard> reviewCardArrayList;

    public ReviewCardAdapter(Context context, ArrayList<ReviewCard> reviewCardArrayList) {
        this.context = context;
        this.reviewCardArrayList = reviewCardArrayList;
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
    }

    @Override
    public int getItemCount() {
        return reviewCardArrayList.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        private final TextView reviewName;
        private final TextView reviewRating;
        private final TextView reviewText;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            reviewName = itemView.findViewById(R.id.reviewName);
            reviewRating = itemView.findViewById(R.id.reviewRating);
            reviewText = itemView.findViewById(R.id.reviewText);
        }
    }
}
