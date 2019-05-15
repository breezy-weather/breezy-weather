package wangdaye.com.geometricweather.ui.adapter;

import android.animation.Animator;
import android.content.Context;
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

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.weather.WeatherHelper;

public class WeatherIconAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Item> itemList;
    private ResourceProvider helper;
    private boolean darkMode;

    // item.

    public interface Item {}

    public static class Title implements Item {
        String content;

        public Title(String content) {
            this.content = content;
        }
    }

    public static class WeatherIcon implements Item {
        String weatherKind;
        boolean daytime;

        public WeatherIcon(String weatherKind, boolean daytime) {
            this.weatherKind = weatherKind;
            this.daytime = daytime;
        }
    }

    public static class MinimalIcon extends WeatherIcon {

        public MinimalIcon(String weatherKind, boolean daytime) {
            super(weatherKind, daytime);
        }
    }

    public static class SunIcon implements Item {}
    public static class MoonIcon implements Item {}

    public static class Shortcut extends WeatherIcon {

        public Shortcut(String weatherKind, boolean daytime) {
            super(weatherKind, daytime);
        }
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

        private AppCompatImageView[] icons;
        private Drawable[] drawables;
        private Animator[] animators;

        IconHolder(@NonNull View itemView) {
            super(itemView);
            icons = new AppCompatImageView[] {
                    itemView.findViewById(R.id.item_weather_icon_icon_1),
                    itemView.findViewById(R.id.item_weather_icon_icon_2),
                    itemView.findViewById(R.id.item_weather_icon_icon_3)
            };
        }

        void onBindView() {
            WeatherIcon icon = (WeatherIcon) itemList.get(getAdapterPosition());

            drawables = WeatherHelper.getWeatherIcons(helper, icon.weatherKind, icon.daytime);
            for (int i = 0; i < icons.length; i ++) {
                if (drawables[i] == null) {
                    icons[i].setVisibility(View.GONE);
                } else {
                    icons[i].setAlpha(1f);
                    icons[i].setRotation(0f);
                    icons[i].setTranslationX(0f);
                    icons[i].setTranslationY(0f);
                    icons[i].setScaleX(1f);
                    icons[i].setScaleY(1f);
                    icons[i].setImageDrawable(drawables[i]);
                    icons[i].setVisibility(View.VISIBLE);
                }
            }

            animators = WeatherHelper.getWeatherAnimators(helper, icon.weatherKind, icon.daytime);
            for (int i = 0; i < animators.length; i ++) {
                if (animators[i] != null) {
                    animators[i].setTarget(icons[i]);
                }
            }

            itemView.setOnClickListener(v -> {
                for (Animator a : animators) {
                    if (a != null) {
                        a.start();
                    }
                }
            });
        }
    }

    class SingleIconHolder extends RecyclerView.ViewHolder {

        private AppCompatImageView icon;

        SingleIconHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.item_weather_icon_minimal_icon);
        }

        void onBindView(Drawable drawable) {
            icon.setImageDrawable(drawable);
        }
    }

    class LineHolder extends RecyclerView.ViewHolder {

        LineHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    // adapter.

    public WeatherIconAdapter(Context context, List<Item> itemList, ResourceProvider helper) {
        this.itemList = itemList;
        this.helper = helper;
        this.darkMode = DisplayUtils.isDarkMode(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == -1) {
            return new IconHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_weather_icon, parent, false)
            );
        }
        if (viewType == 0) {
            return new TitleHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_weather_icon_title, parent, false)
            );
        }
        if (viewType == 2) {
            return new LineHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_line, parent, false)
            );
        }
        return new SingleIconHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_weather_icon_single, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LineHolder) {
            return;
        }
        if (holder instanceof IconHolder) {
            ((IconHolder) holder).onBindView();
            return;
        }
        if (holder instanceof TitleHolder) {
            ((TitleHolder) holder).onBindView();
            return;
        }

        SingleIconHolder singleIconHolder = (SingleIconHolder) holder;

        if (itemList.get(position) instanceof MinimalIcon) {
            MinimalIcon minimal = (MinimalIcon) itemList.get(position);
            singleIconHolder.onBindView(
                    WeatherHelper.getWidgetNotificationIcon(
                            helper,
                            minimal.weatherKind, minimal.daytime,
                            true, darkMode ? "light" : "dark"
                    )
            );
            return;
        }

        if (itemList.get(position) instanceof SunIcon) {
            Drawable drawable = WeatherHelper.getSunDrawable(helper);
            int iconSize = GeometricWeather.getInstance()
                    .getResources()
                    .getDimensionPixelSize(R.dimen.little_weather_icon_size);
            drawable.setBounds(0, 0, iconSize, iconSize);
            singleIconHolder.onBindView(drawable);
            return;
        }

        if (itemList.get(position) instanceof MoonIcon) {
            Drawable drawable = WeatherHelper.getMoonDrawable(helper);
            int iconSize = GeometricWeather.getInstance()
                    .getResources()
                    .getDimensionPixelSize(R.dimen.little_weather_icon_size);
            drawable.setBounds(0, 0, iconSize, iconSize);
            singleIconHolder.onBindView(drawable);
            return;
        }

        if (itemList.get(position) instanceof Shortcut) {
            Shortcut shortcut = (Shortcut) itemList.get(position);
            singleIconHolder.onBindView(
                    WeatherHelper.getShortcutsIcon(helper, shortcut.weatherKind, shortcut.daytime)
            );
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (itemList.get(position) instanceof Title) {
            return 0;
        }
        if (itemList.get(position) instanceof MinimalIcon) {
            return 1;
        }
        if (itemList.get(position) instanceof SunIcon) {
            return 1;
        }
        if (itemList.get(position) instanceof MoonIcon) {
            return 1;
        }
        if (itemList.get(position) instanceof Shortcut) {
            return 1;
        }
        if (itemList.get(position) instanceof Line) {
            return 2;
        }
        return -1;
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