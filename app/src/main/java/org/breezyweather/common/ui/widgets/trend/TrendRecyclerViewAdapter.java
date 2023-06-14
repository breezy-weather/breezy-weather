package org.breezyweather.common.ui.widgets.trend;

import androidx.recyclerview.widget.RecyclerView;

import org.breezyweather.common.basic.models.Location;

public abstract class TrendRecyclerViewAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private Location mLocation;

    public TrendRecyclerViewAdapter(Location location) {
        mLocation = location;
    }

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location location) {
        mLocation = location;
        notifyDataSetChanged();
    }
}
