package com.mbestavros.geometricweather.main.adapter.main.holder;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.mbestavros.geometricweather.R;
import com.mbestavros.geometricweather.basic.GeoActivity;
import com.mbestavros.geometricweather.basic.model.location.Location;
import com.mbestavros.geometricweather.basic.model.option.provider.WeatherSource;
import com.mbestavros.geometricweather.basic.model.weather.Base;
import com.mbestavros.geometricweather.basic.model.weather.Hourly;
import com.mbestavros.geometricweather.basic.model.weather.Minutely;
import com.mbestavros.geometricweather.basic.model.weather.Weather;
import com.mbestavros.geometricweather.main.adapter.main.MainTag;
import com.mbestavros.geometricweather.main.adapter.trend.HourlyTrendAdapter;
import com.mbestavros.geometricweather.main.layout.TrendHorizontalLinearLayoutManager;
import com.mbestavros.geometricweather.resource.provider.ResourceProvider;
import com.mbestavros.geometricweather.settings.SettingsOptionManager;
import com.mbestavros.geometricweather.ui.adapter.TagAdapter;
import com.mbestavros.geometricweather.ui.decotarion.GridMarginsDecoration;
import com.mbestavros.geometricweather.ui.widget.PrecipitationBar;
import com.mbestavros.geometricweather.ui.widget.trend.TrendRecyclerView;
import com.mbestavros.geometricweather.utils.DisplayUtils;

public class HourlyViewHolder extends AbstractMainCardViewHolder {

    private CardView card;

    private TextView title;
    private TextView subtitle;
    private RecyclerView tagView;

    private TrendRecyclerView trendRecyclerView;
    private HourlyTrendAdapter trendAdapter;

    private LinearLayout minutelyContainer;
    private TextView minutelyTitle;
    private PrecipitationBar precipitationBar;
    private TextView minutelyStartText;
    private TextView minutelyEndText;

    public HourlyViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.container_main_hourly_trend_card, parent, false));

        this.card = itemView.findViewById(R.id.container_main_hourly_trend_card);
        this.title = itemView.findViewById(R.id.container_main_hourly_trend_card_title);
        this.subtitle = itemView.findViewById(R.id.container_main_hourly_trend_card_subtitle);
        this.tagView = itemView.findViewById(R.id.container_main_hourly_trend_card_tagView);
        this.trendRecyclerView = itemView.findViewById(R.id.container_main_hourly_trend_card_trendRecyclerView);
        this.minutelyContainer = itemView.findViewById(R.id.container_main_hourly_trend_card_minutely);
        this.minutelyTitle = itemView.findViewById(R.id.container_main_hourly_trend_card_minutelyTitle);
        this.precipitationBar = itemView.findViewById(R.id.container_main_hourly_trend_card_minutelyBar);
        this.minutelyStartText = itemView.findViewById(R.id.container_main_hourly_trend_card_minutelyStartText);
        this.minutelyEndText = itemView.findViewById(R.id.container_main_hourly_trend_card_minutelyEndText);

        this.trendAdapter = new HourlyTrendAdapter();

        minutelyContainer.setOnClickListener(v -> {

        });
    }

    @Override
    public void onBindView(GeoActivity activity, @NonNull Location location, @NonNull ResourceProvider provider,
                           boolean listAnimationEnabled, boolean itemAnimationEnabled, boolean firstCard) {
        super.onBindView(activity, location, provider, listAnimationEnabled, itemAnimationEnabled, firstCard);

        Weather weather = location.getWeather();
        assert weather != null;

        int weatherColor = themeManager.getWeatherThemeColors()[0];

        card.setCardBackgroundColor(themeManager.getRootColor(context));

        title.setTextColor(weatherColor);

        if (TextUtils.isEmpty(weather.getCurrent().getHourlyForecast())) {
            subtitle.setVisibility(View.GONE);
        } else {
            subtitle.setVisibility(View.VISIBLE);
            subtitle.setText(weather.getCurrent().getHourlyForecast());
        }

        List<TagAdapter.Tag> tagList = getTagList(weather, location.getWeatherSource());
        if (tagList.size() < 2) {
            tagView.setVisibility(View.GONE);
        } else {
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
                        setTrendAdapterByTag(weather, (MainTag) tagList.get(newPosition));
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
        setTrendAdapterByTag(weather, (MainTag) tagList.get(0));

        List<Minutely> minutelyList = weather.getMinutelyForecast();
        if (minutelyList.size() != 0 && needToShowMinutelyForecast(minutelyList)) {
            minutelyContainer.setVisibility(View.VISIBLE);

            minutelyTitle.setTextColor(themeManager.getTextContentColor(context));

            precipitationBar.setBackgroundColor(themeManager.getLineColor(context));
            precipitationBar.setPrecipitationColor(themeManager.getWeatherThemeColors()[0]);
            precipitationBar.setMinutelyList(minutelyList);

            int size = minutelyList.size();
            minutelyStartText.setText(Base.getTime(context, minutelyList.get(0).getDate()));
            minutelyStartText.setTextColor(themeManager.getTextSubtitleColor(context));
            minutelyEndText.setText(Base.getTime(context, minutelyList.get(size - 1).getDate()));
            minutelyEndText.setTextColor(themeManager.getTextSubtitleColor(context));
        } else {
            minutelyContainer.setVisibility(View.GONE);
        }
    }

    private static boolean needToShowMinutelyForecast(List<Minutely> minutelyList) {
        for (Minutely m : minutelyList) {
            if (m.isPrecipitation()) {
                return true;
            }
        }
        return false;
    }

    private void setTrendAdapterByTag(Weather weather, MainTag tag) {
        switch (tag.getType()) {
            case TEMPERATURE:
                trendAdapter.temperature(
                        (GeoActivity) context, trendRecyclerView,
                        weather,
                        provider,
                        SettingsOptionManager.getInstance(context).getTemperatureUnit()
                );
                break;

            case PRECIPITATION:
                trendAdapter.precipitation(
                        (GeoActivity) context, trendRecyclerView,
                        weather,
                        provider,
                        SettingsOptionManager.getInstance(context).getPrecipitationUnit()
                );
                break;
        }
    }

    private List<TagAdapter.Tag> getTagList(Weather weather, WeatherSource source) {
        List<TagAdapter.Tag> tagList = new ArrayList<>();
        tagList.add(new MainTag(context.getString(R.string.tag_temperature), MainTag.Type.TEMPERATURE));
        // tagList.addAll(getPrecipitationTagList(weather));
        return tagList;
    }

    private List<TagAdapter.Tag> getPrecipitationTagList(Weather weather) {
        int precipitationCount = 0;
        for (Hourly h : weather.getHourlyForecast()) {
            if (h.getWeatherCode().isPercipitation() && h.getPrecipitation().isValid()) {
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
