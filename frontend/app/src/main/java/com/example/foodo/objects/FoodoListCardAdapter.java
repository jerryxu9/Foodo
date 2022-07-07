package com.example.foodo.objects;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodo.R;

import java.util.ArrayList;

public class FoodoListCardAdapter extends RecyclerView.Adapter<FoodoListCardAdapter.Viewholder> {

    private final String TAG = "FoodoListCardAdapter";
    private final ArrayList<FoodoListCard> foodoListArrayList;
    Context context;

    public FoodoListCardAdapter(Context context, ArrayList<FoodoListCard> foodoListArrayList) {
        this.foodoListArrayList = foodoListArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public FoodoListCardAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.foodo_list_card, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodoListCardAdapter.Viewholder holder, int position) {
        FoodoListCard model = foodoListArrayList.get(position);
        holder.foodoListName.setText(model.getName());
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return foodoListArrayList.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {

        private final TextView foodoListName;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            foodoListName = itemView.findViewById(R.id.foodo_list_name);

            itemView.setOnClickListener((View v) -> {
                Log.d(TAG, String.format("%s item on Foodo Lists selected", foodoListName.getText()));
                handleOpenFoodoListAction();
            });

            itemView.findViewById(R.id.delete_foodo_list_button).setOnClickListener((View v) -> {
                Log.d(TAG, String.format("%s Foodo List delete button selected", foodoListName.getText()));
                handleDeleteFoodoListAction();
            });
        }
    }

    private void handleOpenFoodoListAction() {

    }

    private void handleDeleteFoodoListAction() {
        Log.d(TAG, "Pressed delete Foodo restaurant button");
    }

}
