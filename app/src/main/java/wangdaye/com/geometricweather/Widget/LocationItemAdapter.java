package wangdaye.com.geometricweather.Widget;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wangdaye.com.geometricweather.Data.Location;
import wangdaye.com.geometricweather.R;

/**
 * Created by WangDaYe on 2016/2/8.
 */

public class LocationItemAdapter extends RecyclerView.Adapter<LocationItemAdapter.ViewHolder> {
    // widget
    private MyItemClickListener mItemClickListener = null;

    // data
    private List<LocationItem> list;

    public LocationItemAdapter(List<LocationItem> list) {
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.location_item, null);
        return new ViewHolder(view, mItemClickListener);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.locationName.setText(list.get(position).locationName);
        holder.locationNum.setText(Integer.toString(position + 1));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setOnItemClickListener(MyItemClickListener listener){
        this.mItemClickListener = listener;
    }

    public void insertData(LocationItem item, int adapterPosition) {
        this.list.add(adapterPosition, item);
        this.notifyItemInserted(adapterPosition);
    }

    public void removeData(int adapterPosition) {
        this.list.remove(adapterPosition);
        this.notifyItemRemoved(adapterPosition);
    }

    public List<Location> moveData(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(this.list, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(this.list, i, i - 1);
            }
        }
        this.notifyItemMoved(fromPosition, toPosition);

        List<Location> list = new ArrayList<>();
        for (int i = 0; i < this.list.size(); i ++) {
            list.add(new Location(this.list.get(i).locationName));
        }
        return list;
    }

    // inner class
    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        // widget
        public TextView locationName;
        public TextView locationNum;

        private MyItemClickListener mListener;

        public ViewHolder(View itemView, MyItemClickListener listener) {
            super(itemView);
            locationName = (TextView) itemView.findViewById(R.id.location_item_name);
            locationNum = (TextView) itemView.findViewById(R.id.location_item_num);
            mListener = listener;
            itemView.findViewById(R.id.location_item_card).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onItemClick(v,getAdapterPosition());
        }
    }
}
