package com.example.foodo.objects;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodo.FoodoListActivity;
import com.example.foodo.R;
import com.example.foodo.service.OKHttpService;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FoodoListCardAdapter extends RecyclerView.Adapter<FoodoListCardAdapter.Viewholder> {
    private final String TAG = "FoodoListCardAdapter";
    private final ArrayList<FoodoListCard> foodoListArrayList;
    private final Activity mainActivity;
    Context context;

    public FoodoListCardAdapter(Context context, ArrayList<FoodoListCard> foodoListArrayList) {
        this.context = context;
        this.foodoListArrayList = foodoListArrayList;
        this.mainActivity = ((Activity) context);
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
        holder.username = model.getUsername();
        holder.userID = model.getUserID();
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

    public void clearFoodoLists() {
        int size = getItemCount();
        foodoListArrayList.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void addFoodoList(FoodoListCard card) {
        ((Activity) context).runOnUiThread(() -> {
            foodoListArrayList.add(card);
            notifyItemInserted(foodoListArrayList.size());
        });
    }


    public class Viewholder extends RecyclerView.ViewHolder {
        private final TextView foodoListName;
        String list_id;
        String name;
        String username;
        String userID;

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

            itemView.findViewById(R.id.share_foodo_list_button).setOnClickListener((View v) -> {
                handleShareFoodoListAction();
            });
        }

        private void handleDeleteFoodoListAction() {
            Log.d(TAG, "Pressed delete Foodo button");

            HashMap<String , String> bodyParameters = new HashMap<>();
            bodyParameters.put("listID", list_id);

            HashMap<String , String> queryParameters = new HashMap<>();
            queryParameters.put("userID", userID);

            Callback deleteFoodoListCallback = new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Log.d(TAG, String.format("Delete FoodoList %s failed using id %s", name, list_id));
                    } else {
                        Log.d(TAG, String.format("Foodo list %s deleted using id %s", name, list_id));
                        ((Activity) context).runOnUiThread(() -> {
                            foodoListArrayList.remove(getLayoutPosition());
                            notifyItemRemoved(getLayoutPosition());
                        });
                    }
                }
            };

            OKHttpService.deleteRequest("deleteFoodoList", deleteFoodoListCallback, bodyParameters, queryParameters);

        }

        private void startShareFoodoList(ViewGroup viewGroup) {
            EditText userEmailInput = viewGroup.findViewById(R.id.enter_user_email_edit_text);
            String userEmail = userEmailInput.getText().toString();
            if (userEmail.trim().isEmpty()) {
                Log.d(TAG, "Unable to submit empty userEmail");
                return;
            }
            shareFoodoList(userEmail);
        }

        private void shareFoodoList(String email) {
            Callback callback = new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String result = OKHttpService.getResponseBody(response);
                    Log.d(TAG, result);
                    Log.d(TAG, String.format("Shared FoodoList: %s with %s", list_id, email));
                }
            };

            HashMap<String, String> params = new HashMap<>();
            params.put("listID", list_id);
            params.put("email", email);

            OKHttpService.patchRequest("addNewUserToList", callback, params);
        }

        private void handleShareFoodoListAction() {
            LayoutInflater layoutInflater = (LayoutInflater) mainActivity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.share_foodo_list_popup, null);

            ConstraintLayout createFoodoListConstraintLayout = mainActivity.findViewById(R.id.constraint);

            PopupWindow shareFoodoListPopupWindow = new PopupWindow(container, 800, 800, true);
            shareFoodoListPopupWindow.showAtLocation(createFoodoListConstraintLayout, Gravity.CENTER, 0, 0);
            container.findViewById(R.id.share_foodo_list_confirm_button).setOnClickListener((View v) -> {
                startShareFoodoList(container);
                shareFoodoListPopupWindow.dismiss();
            });

            container.findViewById(R.id.share_foodo_list_cancel_button).setOnClickListener((View v) -> {
                Log.d(TAG, "Cancelled sharing Foodo list");
                shareFoodoListPopupWindow.dismiss();
            });
        }

        private void handleOpenFoodoListAction() {
            Intent foodoIntent = new Intent(mainActivity, FoodoListActivity.class)
                    .putExtra("name", name)
                    .putExtra("listID", list_id)
                    .putExtra("username", username)
                    .putExtra("userID", userID);
            mainActivity.startActivity(foodoIntent);
        }
    }
}


