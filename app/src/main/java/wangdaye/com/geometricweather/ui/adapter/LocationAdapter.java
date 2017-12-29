package wangdaye.com.geometricweather.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;

/**
 * Location adapter.
 * */

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private Context context;
    private OnLocationItemClickListener listener = null;

    public List<Location> itemList;
    private boolean manage;

    class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        // widget
        TextView title;
        TextView subtitle;
        ImageButton deleteButton;

        private OnLocationItemClickListener listener;

        ViewHolder(View itemView, OnLocationItemClickListener listener) {
            super(itemView);
            this.title = itemView.findViewById(R.id.item_location_title);
            this.subtitle = itemView.findViewById(R.id.item_location_subtitle);
            this.deleteButton = itemView.findViewById(R.id.item_location_deleteBtn);
            this.listener = listener;

            itemView.findViewById(R.id.item_location).setOnClickListener(this);
            deleteButton.setOnClickListener(this);
            if (!manage) {
                deleteButton.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.item_location:
                    listener.onClick(v,getAdapterPosition());
                    break;

                case R.id.item_location_deleteBtn:
                    listener.onDelete(v,getAdapterPosition());
                    break;
            }
        }
    }

    public LocationAdapter(Context context, List<Location> itemList, boolean manage, OnLocationItemClickListener l) {
        this.context = context;
        this.itemList = itemList;
        this.manage = manage;
        setOnLocationItemClickListener(l);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location, parent, false);
        return new ViewHolder(view, listener);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (itemList.get(position).isLocal()) {
            holder.title.setText(context.getString(R.string.local));
            if (itemList.get(position).isUsable()) {
                holder.subtitle.setText(itemList.get(position).cnty
                        + " " + itemList.get(position).prov
                        + " " + itemList.get(position).city);
            } else {
                holder.subtitle.setText(context.getString(R.string.feedback_not_yet_location));
            }
        } else {
            holder.title.setText(itemList.get(position).city);
            holder.subtitle.setText(itemList.get(position).cnty
                    + " " + itemList.get(position).prov
                    + " " + itemList.get(position).city);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void insertData(Location item, int adapterPosition) {
        this.itemList.add(adapterPosition, item);
        this.notifyItemInserted(adapterPosition);
    }

    public void removeData(int adapterPosition) {
        this.itemList.remove(adapterPosition);
        this.notifyItemRemoved(adapterPosition);
    }

    public void moveData(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(this.itemList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(this.itemList, i, i - 1);
            }
        }
        this.notifyItemMoved(fromPosition, toPosition);
    }

    // interface.

    public interface OnLocationItemClickListener {
        void onClick(View view, int position);
        void onDelete(View view, int position);
    }

    private void setOnLocationItemClickListener(OnLocationItemClickListener l){
        this.listener = l;
    }
}
