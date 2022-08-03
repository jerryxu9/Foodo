package com.example.foodo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodo.objects.RestaurantCard;
import com.example.foodo.objects.RestaurantCardAdapter;
import com.example.foodo.service.FoodoListCardService;

import java.util.ArrayList;

public class FoodoListActivity extends AppCompatActivity {

    private final float SWIPE_THRESHOLD = 0.6f;
    private String listID;
    private String name;
    private RecyclerView restaurantsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodo_list);

        getIntentExtras();
        initializeComponents();
    }

    private void initializeComponents() {
        TextView foodoListCardName = findViewById(R.id.foodo_list_card_name);
        foodoListCardName.setText(name);

        RestaurantCardAdapter restaurantCardAdapter = new RestaurantCardAdapter(this, new ArrayList<RestaurantCard>(), listID);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        FoodoListCardService foodoListCardService = new FoodoListCardService(this, listID, restaurantCardAdapter);

        restaurantsView = findViewById(R.id.foodo_list_card_restaurants_list);

        foodoListCardService.setupUserAccount();

        restaurantsView.setLayoutManager(linearLayoutManager);
        restaurantsView.setAdapter(restaurantCardAdapter);

        foodoListCardService.loadRestaurantCards();

        setupSwipeListener();
    }

    private void setupSwipeListener() {
        ItemTouchHelper.SimpleCallback deleteRestaurantListCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                ((RestaurantCardAdapter.Viewholder) viewHolder).deleteRestaurantFromList();
            }

            /**
             * Determines the amount to swipe before onSwiped() is triggered
             * @param viewHolder the viewHolder of the item currently being interacted with
             * @return a swipe threshold value
             *
             * "1f - means that the action will be performed with a full card swap.
             * 0.1f - the action will be performed when the card moves to 10% of the screen width"
             *
             * Source: https://stackoverflow.com/questions/52726954/how-to-set-the-swipe-threshold-to-half-the-screen
             *
             */
            @Override
            public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
                return SWIPE_THRESHOLD;
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewholder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                setDeleteIcon(c, viewholder, dX, isCurrentlyActive);
                super.onChildDraw(c, recyclerView, viewholder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        new ItemTouchHelper(deleteRestaurantListCallback).attachToRecyclerView(restaurantsView);
    }

    /**
     * Source: https://www.youtube.com/watch?v=l3bkFT-NZHk
     */
    private void setDeleteIcon(Canvas c, RecyclerView.ViewHolder viewHolder,
                               float dX, boolean isCurrentlyActive) {

        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getHeight();

        boolean isCancelled = dX == 0 && !isCurrentlyActive;

        Paint mClearPaint = new Paint();
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        if (isCancelled) {
            c.drawRect(itemView.getRight() + dX, (float) itemView.getTop(),
                    (float) itemView.getRight(), (float) itemView.getBottom(), mClearPaint);
            return;
        }

        // measure
        int wrapSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        itemView.measure(wrapSpec, wrapSpec);

        Log.d("height width", itemView.getMeasuredHeight() + " " + itemView.getMeasuredWidth());

        GradientDrawable deleteActionBackground = new GradientDrawable();
        deleteActionBackground.setCornerRadius(25f);

        Drawable deleteDrawable = ContextCompat.getDrawable(this, R.drawable.delete_button);

        int intrinsicWidth = deleteDrawable.getIntrinsicWidth();
        int intrinsicHeight = deleteDrawable.getIntrinsicHeight();

        int deleteIconMargin = (itemHeight - intrinsicHeight)/ 2;
        int deleteIconTop = itemView.getTop() + deleteIconMargin;
        int deleteIconRight = itemView.getRight();
        int deleteIconLeft = deleteIconRight - intrinsicWidth;
        int deleteIconBottom = deleteIconTop + intrinsicHeight;

        int deleteActionBackgroundColor = getResources().getColor(R.color.delete_button_red);
        deleteActionBackground.setColor(deleteActionBackgroundColor);

        // Use delete Icon position to determine bounds for the red background when sliding
        // left = deleteIconLeft + dX is because we want the left edge of the red background
        // to be even with delete icon plus the amount we swiped left.
        deleteActionBackground.setBounds(
                itemView.getLeft() + (int) dX,
                itemView.getTop(),
                itemView.getRight(),
                itemView.getBottom());
        deleteActionBackground.draw(c);

        deleteDrawable.setBounds(deleteIconLeft,
                deleteIconTop,
                deleteIconRight,
                deleteIconBottom);
        deleteDrawable.draw(c);

    }

    private void getIntentExtras() {
        name = getIntent().getStringExtra("name");
        listID = getIntent().getStringExtra("listID");
    }

}