package wangdaye.com.geometricweather.settings.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;

public class WeatherIconAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final GeoActivity mActivity;
    private final List<Item> mItemList;

    // item.

    public interface Item {}

    public static class Title implements Item {

        String content;

        public Title(String content) {
            this.content = content;
        }
    }

    public static abstract class WeatherIcon implements Item {

        public abstract Drawable getDrawable();
        public abstract String getContentDescription();

        public abstract void onItemClicked(GeoActivity activity);
    }

    public static class Line implements Item {}

    // holder.

    class TitleHolder extends RecyclerView.ViewHolder {

        private final TextView mTitle;

        TitleHolder(@NonNull View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.item_weather_icon_title);
        }

        void onBindView() {
            Title t = (Title) mItemList.get(getAdapterPosition());
            mTitle.setText(t.content);
        }
    }

    class IconHolder extends RecyclerView.ViewHolder {

        private final AppCompatImageView mImageView;

        IconHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.item_weather_icon_image);
        }

        void onBindView() {
            if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                return;
            }
            WeatherIcon icon = (WeatherIcon) mItemList.get(getAdapterPosition());
            mImageView.setImageDrawable(icon.getDrawable());

            itemView.setContentDescription(icon.getContentDescription());
            itemView.setOnClickListener(v -> icon.onItemClicked(mActivity));
        }
    }

    class LineHolder extends RecyclerView.ViewHolder {

        LineHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    // adapter.

    public WeatherIconAdapter(GeoActivity activity, List<Item> itemList) {
        mActivity = activity;
        mItemList = itemList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            return new TitleHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_weather_icon_title, parent, false)
            );
        }
        if (viewType == -1) {
            return new LineHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_line, parent, false)
            );
        }
        return new IconHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_weather_icon, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LineHolder) {
            return;
        }
        if (holder instanceof TitleHolder) {
            ((TitleHolder) holder).onBindView();
        } else {
            ((IconHolder) holder).onBindView();
        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mItemList.get(position) instanceof Title) {
            return 1;
        }
        if (mItemList.get(position) instanceof Line) {
            return -1;
        }
        return 0;
    }

    public static GridLayoutManager.SpanSizeLookup getSpanSizeLookup(int columnCount,
                                                                     List<WeatherIconAdapter.Item> itemList) {
        return new SpanSizeLookup(columnCount, itemList);
    }
}

class SpanSizeLookup extends GridLayoutManager.SpanSizeLookup {

    private final int mColumnCount;
    private final List<WeatherIconAdapter.Item> mItemList;

    SpanSizeLookup(int columnCount, List<WeatherIconAdapter.Item> itemList) {
        mColumnCount = columnCount;
        mItemList = itemList;
    }

    @Override
    public int getSpanSize(int position) {
        if (mItemList.get(position) instanceof WeatherIconAdapter.Title
                || mItemList.get(position) instanceof WeatherIconAdapter.Line) {
            return mColumnCount;
        }
        return 1;
    }
}