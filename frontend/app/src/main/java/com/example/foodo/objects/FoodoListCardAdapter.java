package com.example.foodo.objects;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodo.R;

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

public class FoodoListCardAdapter extends RecyclerView.Adapter<FoodoListCardAdapter.Viewholder> {

    private final String BASE_URL = "http://10.0.2.2:3000";
    private final String TAG = "FoodoListCardAdapter";
    private final ArrayList<FoodoListCard> foodoListArrayList;
    private final OkHttpClient client;
    private final String USERID = "test@gmail.com";
    Context context;

    public FoodoListCardAdapter(Context context, ArrayList<FoodoListCard> foodoListArrayList, OkHttpClient client) {
        this.context = context;
        this.foodoListArrayList = foodoListArrayList;
        this.client = client;
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
        holder.name = model.getName();
        holder.list_id = model.getId();
        holder.foodoListName.setText(holder.name);
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

    /**
     * Returns whether the given FoodoListCard ID already exists amongst the rendered FoodoListCards
     *
     * @param id A FoodoList ID obtained from the backend
     * @return True if ID exists, false otherwise
     */

    public boolean checkIfIDExists(String id) {
        for (int i = 0; i < foodoListArrayList.size(); i++) {
            if (foodoListArrayList.get(i).getId() == id) {
                Log.d(TAG, String.format("Id %s already exists at %s", id, foodoListArrayList.get(i).getName()));
                return true;
            }
        }
        Log.d(TAG, String.format("Id %s is unique", id));
        return false;
    }

    public void addFoodoList(FoodoListCard card) {
        ((Activity) context).runOnUiThread(() -> {
            foodoListArrayList.add(card);
            notifyItemInserted(foodoListArrayList.size());
        });
    }

    public ArrayList<FoodoListCard> getFoodoList() {
        return foodoListArrayList;
    }

    private void handleOpenFoodoListAction() {

    }


    public class Viewholder extends RecyclerView.ViewHolder {
        private final TextView foodoListName;
        String list_id, name;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            foodoListName = itemView.findViewById(R.id.foodo_list_name);

            itemView.setOnClickListener((View v) -> {
                Log.d(TAG, String.format("%s item on Foodo Lists selected", foodoListName.getText()));
                handleOpenFoodoListAction();
            });

            itemView.findViewById(R.id.delete_foodo_list_button).setOnClickListener((View v) -> {
                handleDeleteFoodoListAction();
            });
        }

        private void handleDeleteFoodoListAction() {
            Log.d(TAG, "Pressed delete Foodo restaurant button");

            String url = BASE_URL + "/deleteFoodoList";
            HttpUrl httpUrl = HttpUrl.parse(url);

            if (httpUrl == null) {
                Log.d(TAG, String.format("unable to parse server URL: %s", url));
                return;
            }

            String json = String.format("{\"listID\": \"%s\"}", list_id);
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"), json);

            HttpUrl.Builder httpBuilder = httpUrl.newBuilder().addQueryParameter("userID", USERID);

            Request request = new Request.Builder()
                    .url(httpBuilder.build())
                    .delete(body)
                    .build();

            client.newCall((request)).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Log.d(TAG, String.format("Delete FoodoList %s failed using id %s", name, list_id));
                    } else {
                        Log.d(TAG, String.format("Foodo list %s was successfully deleted using id %s.", name, list_id));
                        ((Activity) context).runOnUiThread(() -> {
                            foodoListArrayList.remove(getLayoutPosition());
                            notifyItemRemoved(getLayoutPosition());
                        });
                    }
                }
            });

        }

    }


}


