package wangdaye.com.geometricweather.main.ui.adapter.main.holder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.basic.model.weather.Daily;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.main.ui.adapter.trend.DailyAirQualityAdapter;
import wangdaye.com.geometricweather.main.ui.adapter.trend.DailyPrecipitationAdapter;
import wangdaye.com.geometricweather.main.ui.adapter.trend.DailyTemperatureAdapter;
import wangdaye.com.geometricweather.main.ui.adapter.trend.DailyUVAdapter;
import wangdaye.com.geometricweather.main.ui.adapter.trend.DailyWindAdapter;
import wangdaye.com.geometricweather.main.ui.adapter.main.MainTag;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.adapter.TagAdapter;
import wangdaye.com.geometricweather.ui.decotarion.GridMarginsDecoration;
import wangdaye.com.geometricweather.ui.widget.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.utils.DisplayUtils;

public class DailyViewHolder extends AbstractMainCardViewHolder {

    private CardView card;

    private TextView title;
    private TextView subtitle;
    private RecyclerView tagView;
    private TrendRecyclerView trendRecyclerView;
    
    @NonNull private WeatherView weatherView;
    @Px private float cardMarginsVertical;
    @Px private float cardMarginsHorizontal;

    public DailyViewHolder(@NonNull Activity activity, ViewGroup parent, @NonNull WeatherView weatherView,
                           @NonNull ResourceProvider provider, @NonNull MainColorPicker picker,
                           @Px float cardMarginsVertical, @Px float cardMarginsHorizontal,
                           @Px float cardRadius, @Px float cardElevation) {
        super(activity,
                LayoutInflater.from(activity).inflate(R.layout.container_main_daily_trend_card, parent, false),
                provider, picker, cardMarginsVertical, cardMarginsHorizontal, cardRadius, cardElevation, false);

        this.card = itemView.findViewById(R.id.container_main_daily_trend_card);
        this.title = itemView.findViewById(R.id.container_main_daily_trend_card_title);
        this.subtitle = itemView.findViewById(R.id.container_main_daily_trend_card_subtitle);
        this.tagView = itemView.findViewById(R.id.container_main_daily_trend_card_tagView);
        this.trendRecyclerView = itemView.findViewById(R.id.container_main_daily_trend_card_trendRecyclerView);
        
        this.weatherView = weatherView;
        this.cardMarginsVertical = cardMarginsVertical;
        this.cardMarginsHorizontal = cardMarginsHorizontal;
    }

    @SuppressLint({"RestrictedApi", "SetTextI18n"})
    @Override
    public void onBindView(@NonNull Location location) {
        Weather weather = location.getWeather();
        assert weather != null;

        int weatherColor = weatherView.getThemeColors(picker.isLightTheme())[0];

        card.setCardBackgroundColor(picker.getRootColor(context));

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
            tagView.setLayoutManager(
                    new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            tagView.addItemDecoration(
                    new GridMarginsDecoration(
                            context.getResources().getDimension(R.dimen.little_margin),
                            context.getResources().getDimension(R.dimen.normal_margin),
                            tagView
                    )
            );
            tagView.setAdapter(
                    new TagAdapter(tagList, weatherColor, (checked, oldPosition, newPosition) -> {
                        setTrendAdapterByTag(
                                location.getFormattedId(), weather, location.getTimeZone(), (MainTag) tagList.get(newPosition));
                        return false;
                    }, picker, 0)
            );
        }

        trendRecyclerView.setHasFixedSize(true);
        trendRecyclerView.setLayoutManager(
                new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        setTrendAdapterByTag(location.getFormattedId(), weather, location.getTimeZone(), (MainTag) tagList.get(0));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        trendRecyclerView.setAdapter(null);
    }

    private void setTrendAdapterByTag(String formattedId, Weather weather, TimeZone timeZone, MainTag tag) {
        switch (tag.getType()) {
            case TEMPERATURE:
                trendRecyclerView.setAdapter(
                        new DailyTemperatureAdapter(
                                (GeoActivity) context, trendRecyclerView,
                                cardMarginsVertical, cardMarginsHorizontal,
                                DisplayUtils.isTabletDevice(context) ? 7 : 5,
                                context.getResources().getDimensionPixelSize(R.dimen.daily_trend_item_height),
                                formattedId,
                                weather,
                                timeZone,
                                weatherView.getThemeColors(picker.isLightTheme()),
                                provider,
                                picker,
                                SettingsOptionManager.getInstance(context).getTemperatureUnit()
                        )
                );
                break;

            case WIND:
                trendRecyclerView.setAdapter(
                        new DailyWindAdapter(
                                (GeoActivity) context, trendRecyclerView,
                                cardMarginsVertical, cardMarginsHorizontal,
                                DisplayUtils.isTabletDevice(context) ? 7 : 5,
                                context.getResources().getDimensionPixelSize(R.dimen.daily_trend_item_height),
                                formattedId,
                                weather,
                                timeZone,
                                weatherView.getThemeColors(picker.isLightTheme()),
                                picker,
                                SettingsOptionManager.getInstance(context).getSpeedUnit()
                        )
                );
                break;

            case PRECIPITATION:
                trendRecyclerView.setAdapter(
                        new DailyPrecipitationAdapter(
                                (GeoActivity) context, trendRecyclerView,
                                cardMarginsVertical, cardMarginsHorizontal,
                                DisplayUtils.isTabletDevice(context) ? 7 : 5,
                                context.getResources().getDimensionPixelSize(R.dimen.daily_trend_item_height),
                                formattedId,
                                weather,
                                timeZone,
                                provider,
                                picker,
                                SettingsOptionManager.getInstance(context).getPrecipitationUnit()
                        )
                );
                break;

            case AIR_QUALITY:
                trendRecyclerView.setAdapter(
                        new DailyAirQualityAdapter(
                                (GeoActivity) context, trendRecyclerView,
                                cardMarginsVertical, cardMarginsHorizontal,
                                DisplayUtils.isTabletDevice(context) ? 7 : 5,
                                context.getResources().getDimensionPixelSize(R.dimen.daily_trend_item_height),
                                formattedId,
                                weather,
                                timeZone,
                                weatherView.getThemeColors(picker.isLightTheme()),
                                picker
                        )
                );
                break;

            case UV_INDEX:
                trendRecyclerView.setAdapter(
                        new DailyUVAdapter(
                                (GeoActivity) context, trendRecyclerView,
                                cardMarginsVertical, cardMarginsHorizontal,
                                DisplayUtils.isTabletDevice(context) ? 7 : 5,
                                context.getResources().getDimensionPixelSize(R.dimen.daily_trend_item_height),
                                formattedId,
                                weather,
                                timeZone,
                                weatherView.getThemeColors(picker.isLightTheme()),
                                picker
                        )
                );
                break;
        }
    }

    private List<TagAdapter.Tag> getTagList(Weather weather, WeatherSource source) {
        List<TagAdapter.Tag> tagList = new ArrayList<>();
        tagList.add(new MainTag(context.getString(R.string.tag_temperature), MainTag.Type.TEMPERATURE));
        tagList.add(new MainTag(context.getString(R.string.tag_aqi), MainTag.Type.AIR_QUALITY));
        if (source == WeatherSource.ACCU) {
            tagList.add(new MainTag(context.getString(R.string.tag_wind), MainTag.Type.WIND));
            tagList.add(new MainTag(context.getString(R.string.tag_uv), MainTag.Type.UV_INDEX));
            tagList.addAll(getPrecipitationTagList(weather));
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