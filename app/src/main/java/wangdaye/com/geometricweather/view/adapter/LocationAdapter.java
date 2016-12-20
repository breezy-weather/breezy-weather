package wangdaye.com.geometricweather.view.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Location adapter.
 * */

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {
    // widget
    private Context context;
    private OnLocationItemClickListener listener = null;

    // data
    public List<Location> itemList;

    /** <br> life cycle. */

    public LocationAdapter(Context context, List<Location> itemList, OnLocationItemClickListener l) {
        this.context = context;
        this.itemList = itemList;
        setOnLocationItemClickListener(l);
    }

    /** <br> UI. */

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (itemList.get(position).isLocal()) {
            holder.title.setText(context.getString(R.string.local));
            if (itemList.get(position).isUsable()) {
                holder.subtitle.setText(itemList.get(position).cnty
                        + " " + itemList.get(position).prov
                        + " " + itemList.get(position).city
                        + " " + itemList.get(position).cityId);
            } else {
                holder.subtitle.setText(context.getString(R.string.feedback_not_yet_location));
            }
        } else {
            if (itemList.get(position).isEngLocation()) {
                holder.title.setText(itemList.get(position).city);
                holder.subtitle.setText(itemList.get(position).cnty
                        + " " + itemList.get(position).city
                        + " " + itemList.get(position).cityId);
            } else {
                holder.title.setText(itemList.get(position).city);
                holder.subtitle.setText(itemList.get(position).cnty
                        + " " + itemList.get(position).prov
                        + " " + itemList.get(position).city
                        + " " + itemList.get(position).cityId);
            }
        }
    }

    /** <br> data. */

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

    /** <br> listener. */

    public interface OnLocationItemClickListener {
        void onItemClick(View view, int position);
    }

    private void setOnLocationItemClickListener(OnLocationItemClickListener l){
        this.listener = l;
    }

    /** <br> inner class. */

    class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        // widget
        TextView title;
        TextView subtitle;

        private OnLocationItemClickListener listener;

        ViewHolder(View itemView, OnLocationItemClickListener listener) {
            super(itemView);
            this.title = (TextView) itemView.findViewById(R.id.item_location_title);
            this.subtitle = (TextView) itemView.findViewById(R.id.item_location_subtitle);
            this.listener = listener;
            itemView.findViewById(R.id.item_location).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClick(v,getAdapterPosition());
        }
    }

    public static class ListDecoration extends RecyclerView.ItemDecoration {
        // widget
        private Paint paint;

        // data
        private int decorationHeight;

        public ListDecoration(Context context) {
            this.decorationHeight = (int) DisplayUtils.dpToPx(context, 2);

            this.paint = new Paint();
            paint.setColor(ContextCompat.getColor(context, R.color.colorLine));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(decorationHeight);
        }

        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            for (int i = 0; i < parent.getChildCount(); i++){
                View child = parent.getChildAt(i);
                c.drawLine(child.getLeft(), child.getBottom(), child.getRight(), child.getBottom(), paint);
            }
        }

        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(0, 0, 0, decorationHeight);
        }
    }
}
