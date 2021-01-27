package wangdaye.com.geometricweather.main.adapter.main.holder;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.option.appearance.DailyTrendDisplay;
import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.basic.model.weather.Daily;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.adapter.main.MainTag;
import wangdaye.com.geometricweather.main.adapter.trend.DailyTrendAdapter;
import wangdaye.com.geometricweather.ui.widget.trend.TrendRecyclerViewScrollBar;
import wangdaye.com.geometricweather.main.layout.TrendHorizontalLinearLayoutManager;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.adapter.TagAdapter;
import wangdaye.com.geometricweather.ui.decotarion.GridMarginsDecoration;
import wangdaye.com.geometricweather.ui.widget.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.utils.DisplayUtils;

public class DailyViewHolder extends AbstractMainCardViewHolder {

    private final CardView card;

    private final TextView title;
    private final TextView subtitle;
    private final RecyclerView tagView;

    private final TrendRecyclerView trendRecyclerView;
    private final DailyTrendAdapter trendAdapter;
    private @Nullable TrendRecyclerViewScrollBar trendScrollBar;

    public DailyViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.container_main_daily_trend_card, parent, false));

        this.card = itemView.findViewById(R.id.container_main_daily_trend_card);
        this.title = itemView.findViewById(R.id.container_main_daily_trend_card_title);
        this.subtitle = itemView.findViewById(R.id.container_main_daily_trend_card_subtitle);
        this.tagView = itemView.findViewById(R.id.container_main_daily_trend_card_tagView);

        this.trendRecyclerView = itemView.findViewById(R.id.container_main_daily_trend_card_trendRecyclerView);
        this.trendAdapter = new DailyTrendAdapter();
    }

    @Override
    public void onBindView(GeoActivity activity, @NonNull Location location,
                           @NonNull ResourceProvider provider,
                           boolean listAnimationEnabled, boolean itemAnimationEnabled, boolean firstCard) {
        super.onBindView(activity, location, provider, listAnimationEnabled, itemAnimationEnabled, firstCard);

        Weather weather = location.getWeather();
        assert weather != null;

        int weatherColor = themeManager.getWeatherThemeColors()[0];

        card.setCardBackgroundColor(themeManager.getRootColor(context));

        title.setTextColor(weatherColor);

        if (TextUtils.isEmpty(weather.getCurrent().getDailyForecast())) {
            subtitle.setVisibility(View.GONE);
        } else {
            subtitle.setVisibility(View.VISIBLE);
            subtitle.setText(weather.getCurrent().getDailyForecast());
        }

        List<TagAdapter.Tag> tagList = getTagList(weather, location.getWeatherSource());
        if (tagList.size() < 2) {
            tagView.setVisibility(View.GONE);
        } else {
            tagView.setVisibility(View.VISIBLE);
            int decorCount = tagView.getItemDecorationCount();
            for (int i = 0; i < decorCount; i++) {
                tagView.removeItemDecorationAt(0);
            }
            tagView.addItemDecoration(
                    new GridMarginsDecoration(
                            context.getResources().getDimension(R.dimen.little_margin),
                            context.getResources().getDimension(R.dimen.normal_margin),
                            tagView
                    )
            );

            tagView.setLayoutManager(new TrendHorizontalLinearLayoutManager(context));
            tagView.setAdapter(
                    new TagAdapter(context, tagList, weatherColor, (checked, oldPosition, newPosition) -> {
                        setTrendAdapterByTag(location, (MainTag) tagList.get(newPosition));
                        return false;
                    }, 0)
            );
        }

        trendRecyclerView.setHasFixedSize(true);
        trendRecyclerView.setLayoutManager(
                new TrendHorizontalLinearLayoutManager(
                        context,
                        DisplayUtils.isLandscape(context) ? 7 : 5
                )
        );
        trendRecyclerView.setAdapter(trendAdapter);
        trendRecyclerView.setKeyLineVisibility(
                SettingsOptionManager.getInstance(context).isTrendHorizontalLinesEnabled());
        setTrendAdapterByTag(location, (MainTag) tagList.get(0));

        this.trendScrollBar = new TrendRecyclerViewScrollBar(context);
        trendRecyclerView.addItemDecoration(trendScrollBar);
    }

    private void setTrendAdapterByTag(Location location, MainTag tag) {
        switch (tag.getType()) {
            case TEMPERATURE:
                trendAdapter.temperature(
                        (GeoActivity) context,
                        trendRecyclerView,
                        location,
                        provider,
                        SettingsOptionManager.getInstance(context).getTemperatureUnit()
                );
                break;

            case WIND:
                trendAdapter.wind(
                        (GeoActivity) context,
                        trendRecyclerView,
                        location,
                        SettingsOptionManager.getInstance(context).getSpeedUnit()
                );
                break;

            case PRECIPITATION:
                trendAdapter.precipitation(
                        (GeoActivity) context,
                        trendRecyclerView,
                        location,
                        provider,
                        SettingsOptionManager.getInstance(context).getPrecipitationUnit()
                );
                break;

            case AIR_QUALITY:
                trendAdapter.airQuality((GeoActivity) context, trendRecyclerView, location);
                break;

            case UV_INDEX:
                trendAdapter.uv((GeoActivity) context, trendRecyclerView, location);
                break;
        }
        trendAdapter.notifyDataSetChanged();
    }

    private List<TagAdapter.Tag> getTagList(Weather weather, WeatherSource source) {
        List<TagAdapter.Tag> tagList = new ArrayList<>();
        List<DailyTrendDisplay> displayList
                = SettingsOptionManager.getInstance(context).getDailyTrendDisplayList();
        for (DailyTrendDisplay d : displayList) {
            switch (d) {
                case TAG_TEMPERATURE:
                    tagList.add(new MainTag(context.getString(R.string.tag_temperature), MainTag.Type.TEMPERATURE));
                    break;

                case TAG_AIR_QUALITY:
                    tagList.add(new MainTag(context.getString(R.string.tag_aqi), MainTag.Type.AIR_QUALITY));
                    break;

                case TAG_WIND:
                    if (source == WeatherSource.ACCU) {
                        tagList.add(new MainTag(context.getString(R.string.tag_wind), MainTag.Type.WIND));
                    }
                    break;

                case TAG_UV_INDEX:
                    if (source == WeatherSource.ACCU) {
                        tagList.add(new MainTag(context.getString(R.string.tag_uv), MainTag.Type.UV_INDEX));
                    }
                    break;

                case TAG_PRECIPITATION:
                    if (source == WeatherSource.ACCU) {
                        tagList.addAll(getPrecipitationTagList(weather));
                    }
                    break;
            }
        }
        if (tagList.size() == 0) {
            tagList.add(new MainTag(context.getString(R.string.tag_temperature), MainTag.Type.TEMPERATURE));
        }

        return tagList;
    }

    private List<TagAdapter.Tag> getPrecipitationTagList(Weather weather) {
        int precipitationCount = 0;
        for (Daily d : weather.getDailyForecast()) {
            if ((d.day().getWeatherCode().isPercipitation() || d.night().getWeatherCode().isPercipitation())
                    && (d.day().getPrecipitation().isValid() || d.night().getPrecipitation().isValid())) {
                precipitationCount ++;
            }
        }
        if (precipitationCount < 3) {
            return new ArrayList<>();
        } else {
            List<TagAdapter.Tag> list = new ArrayList<>();
            list.add(new MainTag(context.getString(R.string.tag_precipitation), MainTag.Type.PRECIPITATION));
            return list;
        }
    }

    @Override
    public void onRecycleView() {
        super.onRecycleView();
        if (trendScrollBar != null) {
            trendRecyclerView.removeItemDecoration(trendScrollBar);
            trendScrollBar = null;
        }
    }
}