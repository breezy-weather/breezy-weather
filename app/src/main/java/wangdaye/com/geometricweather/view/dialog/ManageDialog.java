package wangdaye.com.geometricweather.view.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoDialogFragment;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;
import wangdaye.com.geometricweather.view.adapter.LocationAdapter;

/**
 * Manage dialog.
 * */

public class ManageDialog extends GeoDialogFragment
        implements LocationAdapter.MyItemClickListener, TextView.OnEditorActionListener {
    // widget
    private CoordinatorLayout container;
    private EditText editText;
    private OnLocationChangedListener listener;

    // data
    private LocationAdapter adapter;

    /** <br> life cycle. */

    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_manage, null, false);
        this.initData();
        this.initWidget(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    /** <br> data. */

    private void initData() {
        List<Location> locationList = DatabaseHelper.getInstance(getActivity()).readLocationList();
        this.adapter = new LocationAdapter(locationList);
        adapter.setOnItemClickListener(this);
    }

    /** <br> UI. */

    private void initWidget(View view) {
        this.container = (CoordinatorLayout) view.findViewById(R.id.dialog_manage_container);

        this.editText = (EditText) view.findViewById(R.id.dialog_manage_search_text);
        editText.setOnEditorActionListener(this);

        RecyclerView locationView = (RecyclerView) view.findViewById(R.id.dialog_manage_recycleView);
        locationView.setLayoutManager(new LinearLayoutManager(getActivity()));
        locationView.setAdapter(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new LocationSwipeCallback(
                        ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT));
        itemTouchHelper.attachToRecyclerView(locationView);
    }

    /** <br> interface. */

    // on select name listener.

    public interface OnLocationChangedListener {
        void selectLocation(String result);
        void changeLocationList(List<String> nameList);
    }

    public void setOnLocationChangedListener(OnLocationChangedListener l) {
        this.listener = l;
    }

    // on name item click.

    @Override
    public void onItemClick(View view, int position) {
        if (listener != null) {
            listener.selectLocation(adapter.itemList.get(position).name);
        }
        dismiss();
    }

    // on editor action listener.

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            String location = v.getText().toString();
            editText.setText("");

            if (location.length() > 0 && listener != null) {
                listener.selectLocation(location);
                dismiss();
            }
            return true;
        }
        return false;
    }

    /** <br> callback. */

    public class LocationSwipeCallback extends ItemTouchHelper.SimpleCallback {

        LocationSwipeCallback(int dragDirs, int swipeDirs) {
            super(dragDirs, swipeDirs);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView,
                              RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            adapter.moveData(fromPosition, toPosition);
            if (listener != null) {
                List<String> nameList = new ArrayList<>();
                for (Location i : adapter.itemList) {
                    nameList.add(i.name);
                }
                listener.changeLocationList(nameList);
            }

            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();

            if (adapter.itemList.size() <= 1) {
                Location item = adapter.itemList.get(position);
                adapter.removeData(position);
                adapter.insertData(item, position);
                SnackbarUtils.showSnackbar(getString(R.string.feedback_location_list_cannot_be_null));
            } else {
                adapter.removeData(position);
                SnackbarUtils.showSnackbar(getString(R.string.feedback_delete_succeed));
                if (listener != null) {
                    List<String> nameList = new ArrayList<>();
                    for (Location i : adapter.itemList) {
                        nameList.add(i.name);
                    }
                    listener.changeLocationList(nameList);
                }
            }
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            switch (actionState) {
                case ItemTouchHelper.ACTION_STATE_SWIPE:
                    final float alpha = 1 - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
                    viewHolder.itemView.setAlpha(alpha);
                    viewHolder.itemView.setTranslationX(dX);
                    break;
            }
        }
    }
}
