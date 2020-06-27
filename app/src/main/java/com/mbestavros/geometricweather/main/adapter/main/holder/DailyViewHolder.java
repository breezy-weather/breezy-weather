package com.mbestavros.geometricweather.main.adapter.main.holder;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import com.mbestavros.geometricweather.R;
import com.mbestavros.geometricweather.basic.GeoActivity;
import com.mbestavros.geometricweather.basic.model.location.Location;
import com.mbestavros.geometricweather.basic.model.option.appearance.DailyTrendDisplay;
import com.mbestavros.geometricweather.basic.model.option.provider.WeatherSource;
import com.mbestavros.geometricweather.basic.model.weather.Daily;
import com.mbestavros.geometricweather.basic.model.weather.Weather;
import com.mbestavros.geometricweather.main.adapter.main.MainTag;
import com.mbestavros.geometricweather.main.adapter.trend.DailyTrendAdapter;
import com.mbestavros.geometricweather.main.layout.TrendHorizontalLinearLayoutManager;
import com.mbestavros.geometricweather.resource.provider.ResourceProvider;
import com.mbestavros.geometricweather.settings.SettingsOptionManager;
import com.mbestavros.geometricweather.ui.adapter.TagAdapter;
import com.mbestavros.geometricweather.ui.decotarion.GridMarginsDecoration;
import com.mbestavros.geometricweather.ui.widget.trend.TrendRecyclerView;
import com.mbestavros.geometricweather.utils.DisplayUtils;

public class DailyViewHolder extends AbstractMainCardViewHolder {

    private CardView card;

    private TextView title;
    private TextView subtitle;
    private RecyclerView tagView;

    private TrendRecyclerView trendRecyclerView;
    private DailyTrendAdapter trendAdapter;

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
                        setTrendAdapterByTag(location.getFormattedId(), weather,
                                location.getTimeZone(), (MainTag) tagList.get(newPosition));
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
        setTrendAdapterByTag(location.getFormattedId(), weather, location.getTimeZone(),
                (MainTag) tagList.get(0));
    }

    private void setTrendAdapterByTag(String formattedId, Weather weather, TimeZone timeZone, MainTag tag) {
        switch (tag.getType()) {
            case TEMPERATURE:
                trendAdapter.temperature(
                        (GeoActivity) context, trendRecyclerView,
                        formattedId,
                        weather,
                        timeZone,
                        provider,
                        SettingsOptionManager.getInstance(context).getTemperatureUnit()
                );
                break;

            case WIND:
                trendAdapter.wind(
                        (GeoActivity) context, trendRecyclerView,
                        formattedId,
                        weather,
                        timeZone,
                        SettingsOptionManager.getInstance(context).getSpeedUnit()
                );
                break;

            case PRECIPITATION:
                trendAdapter.precipitation(
                        (GeoActivity) context, trendRecyclerView,
                        formattedId,
                        weather,
                        timeZone,
                        provider,
                        SettingsOptionManager.getInstance(context).getPrecipitationUnit()
                );
                break;

            case AIR_QUALITY:
                trendAdapter.airQuality(
                        (GeoActivity) context, trendRecyclerView,
                        formattedId,
                        weather,
                        timeZone
                );
                break;

            case UV_INDEX:
                trendAdapter.uv(
                        (GeoActivity) context, trendRecyclerView,
                        formattedId,
                        weather,
                        timeZone
                );
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
}