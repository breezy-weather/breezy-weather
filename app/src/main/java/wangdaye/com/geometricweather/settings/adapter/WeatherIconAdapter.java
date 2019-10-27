package wangdaye.com.geometricweather.settings.adapter;

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

    private GeoActivity activity;
    private List<Item> itemList;

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

        public abstract void onItemClicked(GeoActivity activity);
    }

    public static class Line implements Item {}

    // holder.

    class TitleHolder extends RecyclerView.ViewHolder {

        private TextView title;

        TitleHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_weather_icon_title);
        }

        void onBindView() {
            Title t = (Title) itemList.get(getAdapterPosition());
            title.setText(t.content);
        }
    }

    class IconHolder extends RecyclerView.ViewHolder {

        private AppCompatImageView imageView;

        IconHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_weather_icon_image);
        }

        void onBindView() {
            if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                return;
            }
            WeatherIcon icon = (WeatherIcon) itemList.get(getAdapterPosition());
            imageView.setImageDrawable(icon.getDrawable());
            itemView.setOnClickListener(v -> icon.onItemClicked(activity));
        }
    }

    class LineHolder extends RecyclerView.ViewHolder {

        LineHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    // adapter.

    public WeatherIconAdapter(GeoActivity activity, List<Item> itemList) {
        this.activity = activity;
        this.itemList = itemList;
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
        return itemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (itemList.get(position) instanceof Title) {
            return 1;
        }
        if (itemList.get(position) instanceof Line) {
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

    private int columnCount;
    private List<WeatherIconAdapter.Item> itemList;

    SpanSizeLookup(int columnCount, List<WeatherIconAdapter.Item> itemList) {
        this.columnCount = columnCount;
        this.itemList = itemList;
    }

    @Override
    public int getSpanSize(int position) {
        if (itemList.get(position) instanceof WeatherIconAdapter.Title
                || itemList.get(position) instanceof WeatherIconAdapter.Line) {
            return columnCount;
        }
        return 1;
    }
}