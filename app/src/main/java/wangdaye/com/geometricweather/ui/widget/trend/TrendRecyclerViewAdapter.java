package wangdaye.com.geometricweather.ui.widget.trend;

import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.basic.model.Location;

public abstract class TrendRecyclerViewAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private Location location;

    public TrendRecyclerViewAdapter(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
        this.notifyDataSetChanged();
    }
}
