package wangdaye.com.geometricweather.ui.widget.trend;

import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.basic.model.Location;

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
