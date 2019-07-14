package wangdaye.com.geometricweather.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.View;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.main.ui.MainActivity;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.manager.ShortcutsManager;
import wangdaye.com.geometricweather.ui.adapter.LocationAdapter;
import wangdaye.com.geometricweather.ui.decotarion.ListDecoration;

/**
 * Manage activity.
 * */

public class ManageActivity extends GeoActivity
        implements LocationAdapter.OnLocationItemClickListener {

    private CoordinatorLayout container;
    private CardView cardView;
    private RecyclerView recyclerView;

    private LocationAdapter adapter;

    public static final int SEARCH_ACTIVITY = 1;

    private class LocationSwipeCallback extends ItemTouchHelper.SimpleCallback {

        LocationSwipeCallback(int dragDirs, int swipeDirs) {
            super(dragDirs, swipeDirs);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            adapter.moveData(fromPosition, toPosition);
            DatabaseHelper.getInstance(ManageActivity.this).writeLocationList(adapter.itemList);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                ShortcutsManager.refreshShortcutsInNewThread(ManageActivity.this, adapter.itemList);
            }

            ((LocationAdapter.ViewHolder) viewHolder).drawDrag(ManageActivity.this, false);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            deleteLocation(viewHolder.getAdapterPosition());
        }

        @Override
        public void onChildDraw(@NonNull Canvas c,
                                @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            switch (actionState) {
                case ItemTouchHelper.ACTION_STATE_SWIPE:
                    ((LocationAdapter.ViewHolder) viewHolder)
                            .drawSwipe(dX);
                    break;

                case ItemTouchHelper.ACTION_STATE_DRAG:
                    ((LocationAdapter.ViewHolder) viewHolder)
                            .drawDrag(ManageActivity.this, dY != 0);
                    break;

                case ItemTouchHelper.ACTION_STATE_IDLE:
                    ((LocationAdapter.ViewHolder) viewHolder)
                            .drawSwipe(0)
                            .drawDrag(ManageActivity.this, false);
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);
        initWidget();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SEARCH_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    this.adapter = new LocationAdapter(
                            this,
                            DatabaseHelper.getInstance(this).readLocationList(),
                            true,
                            this
                    );
                    recyclerView.setAdapter(adapter);

                    SnackbarUtils.showSnackbar(this, getString(R.string.feedback_collect_succeed));
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    ShortcutsManager.refreshShortcutsInNewThread(this, adapter.itemList);
                }
                break;
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        // do nothing.
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    private void initWidget() {
        this.container = findViewById(R.id.activity_manage_container);

        this.cardView = findViewById(R.id.activity_manage_searchBar);
        cardView.setOnClickListener(view ->
                IntentHelper.startSearchActivityForResult(ManageActivity.this, cardView));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cardView.setTransitionName(getString(R.string.transition_activity_search_bar));
        }

        this.recyclerView = findViewById(R.id.activity_manage_recyclerView);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        );
        recyclerView.addItemDecoration(new ListDecoration(this));

        this.adapter = new LocationAdapter(
                this,
                DatabaseHelper.getInstance(this).readLocationList(),
                true,
                this
        );
        recyclerView.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new LocationSwipeCallback(
                        ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
                )
        );
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void deleteLocation(int position) {
        if (0 <= position && position < adapter.itemList.size()) {
            Location location = adapter.itemList.get(position);
            if (adapter.itemList.size() <= 1) {
                adapter.removeData(position);
                adapter.insertData(location, position);
                SnackbarUtils.showSnackbar(
                        this, getString(R.string.feedback_location_list_cannot_be_null));
            } else {
                adapter.removeData(position);

                location.weather = DatabaseHelper.getInstance(this).readWeather(location);
                location.history = DatabaseHelper.getInstance(this).readHistory(location.weather);

                DatabaseHelper.getInstance(this).deleteLocation(location);
                DatabaseHelper.getInstance(this).deleteWeather(location);
                SnackbarUtils.showSnackbar(
                        this,
                        getString(R.string.feedback_delete_succeed),
                        getString(R.string.cancel),
                        new CancelDeleteListener(location)
                );
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    ShortcutsManager.refreshShortcutsInNewThread(ManageActivity.this, adapter.itemList);
                }
            }
        }
    }

    // interface.

    // on click listener.

    private class CancelDeleteListener
            implements View.OnClickListener {

        private Location location;

        CancelDeleteListener(Location l) {
            this.location = l;
        }

        @Override
        public void onClick(View view) {
            adapter.insertData(location, adapter.getItemCount());
            DatabaseHelper.getInstance(ManageActivity.this).writeLocation(location);
            DatabaseHelper.getInstance(ManageActivity.this).writeWeather(location, location.weather);
            DatabaseHelper.getInstance(ManageActivity.this).writeTodayHistory(location.weather);
            DatabaseHelper.getInstance(ManageActivity.this).writeYesterdayHistory(location.history);
        }
    }

    // on location item click listener.

    @Override
    public void onClick(View view, int position) {
        String formattedId = adapter.itemList.get(position).getFormattedId();
        Intent intent = new Intent().putExtra(
                MainActivity.KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID, formattedId);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onDelete(View view, int position) {
        deleteLocation(position);
    }
}
