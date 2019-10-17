package wangdaye.com.geometricweather.main.ui.controller;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.basic.model.weather.Temperature;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.main.ui.adapter.HourlyTrendAdapter;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widget.trendView.TrendHelper;
import wangdaye.com.geometricweather.ui.widget.trendView.TrendRecyclerView;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.utils.DisplayUtils;

public class HourlyTrendCardController extends AbstractMainItemController {

    private CardView card;

    private TextView title;
    private TextView subtitle;
    private TrendRecyclerView trendRecyclerView;

    @NonNull private WeatherView weatherView;
    private int marginsHorizontal;

    public HourlyTrendCardController(@NonNull Activity activity, @NonNull WeatherView weatherView,
                                     @NonNull ResourceProvider provider, @NonNull MainColorPicker picker,
                                     int marginsHorizontal) {
        super(activity, activity.findViewById(R.id.container_main_second_trend_card), provider, picker);

        this.card = view.findViewById(R.id.container_main_second_trend_card);
        this.title = view.findViewById(R.id.container_main_second_trend_card_title);
        this.subtitle = view.findViewById(R.id.container_main_second_trend_card_subtitle);
        this.trendRecyclerView = view.findViewById(R.id.container_main_second_trend_card_trendRecyclerView);

        this.weatherView = weatherView;
        this.marginsHorizontal = marginsHorizontal;
    }

    @Override
    public void onBindView(@NonNull Location location) {
        if (location.getWeather() != null) {
            Weather weather = location.getWeather();
            TemperatureUnit unit = SettingsOptionManager.getInstance(context).getTemperatureUnit();

            card.setCardBackgroundColor(picker.getRootColor(context));

            title.setText(context.getString(R.string.hourly_overview));
            title.setTextColor(weatherView.getThemeColors(picker.isLightTheme())[0]);

            if (TextUtils.isEmpty(weather.getCurrent().getHourlyForecast())) {
                subtitle.setVisibility(View.GONE);
            } else {
                subtitle.setVisibility(View.VISIBLE);
                subtitle.setText(weather.getCurrent().getHourlyForecast());
            }

            trendRecyclerView.setHasFixedSize(true);
            trendRecyclerView.setLayoutManager(
                    new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            trendRecyclerView.setAdapter(
                    new HourlyTrendAdapter(
                            (GeoActivity) context,
                            trendRecyclerView,
                            marginsHorizontal,
                            DisplayUtils.isTabletDevice(context) ? 7 : 5,
                            context.getResources().getDimensionPixelSize(R.dimen.hourly_trend_item_height),
                            weather,
                            weatherView.getThemeColors(picker.isLightTheme()),
                            provider,
                            picker,
                            unit
                    )
            );

            trendRecyclerView.setLineColor(picker.getLineColor(context));
            if (weather.getYesterday() == null) {
                trendRecyclerView.setData(
                        null, null, 0, 0, null, null);
            } else {
                int[] highestAndLowest = TrendHelper.getHighestAndLowestHourlyTemperature(weather);
                trendRecyclerView.setData(
                        (float) weather.getYesterday().getDaytimeTemperature(),
                        (float) weather.getYesterday().getNighttimeTemperature(),
                        highestAndLowest[0],
                        highestAndLowest[1],
                        Temperature.getShortTemperature(weather.getYesterday().getDaytimeTemperature(), unit),
                        Temperature.getShortTemperature(weather.getYesterday().getNighttimeTemperature(), unit)
                );
            }
        }
    }

    @Override
    public void onDestroy() {
        trendRecyclerView.setAdapter(null);
    }
}
