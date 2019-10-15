package wangdaye.com.geometricweather.ui.widget.trendView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.basic.model.weather.Temperature;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.main.ui.adapter.DailyTrendAdapter;
import wangdaye.com.geometricweather.main.ui.adapter.HourlyTrendAdapter;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Trend view controller.
 * */

public class TrendViewController {

    public static void setDailyTrend(GeoActivity activity, TextView title, TextView subtitle,
                                     TrendRecyclerView recyclerView,
                                     @NonNull ResourceProvider provider, @NonNull MainColorPicker picker,
                                     @NonNull Weather weather, int[] themeColors) {
        TemperatureUnit unit = SettingsOptionManager.getInstance(activity).getTemperatureUnit();

        title.setText(activity.getString(R.string.daily_overview));

        if (TextUtils.isEmpty(weather.getCurrent().getDailyForecast())) {
            subtitle.setVisibility(View.GONE);
        } else {
            subtitle.setVisibility(View.VISIBLE);
            subtitle.setText(weather.getCurrent().getDailyForecast());
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(
                new DailyTrendAdapter(
                        activity,
                        recyclerView,
                        activity.getResources().getDimensionPixelSize(R.dimen.little_margin),
                        DisplayUtils.isTabletDevice(activity) ? 7 : 5,
                        weather,
                        themeColors,
                        provider,
                        picker,
                        unit
                )
        );

        recyclerView.setLineColor(picker.getLineColor(activity));
        if (weather.getYesterday() == null) {
            recyclerView.setData(null, null, 0, 0, null, null);
        } else {
            int highest = weather.getYesterday().getDaytimeTemperature();
            int lowest = weather.getYesterday().getNighttimeTemperature();
            for (int i = 0; i < weather.getDailyForecast().size(); i ++) {
                if (weather.getDailyForecast().get(i).day().getTemperature().getTemperature() > highest) {
                    highest = weather.getDailyForecast().get(i).day().getTemperature().getTemperature();
                }
                if (weather.getDailyForecast().get(i).night().getTemperature().getTemperature() < lowest) {
                    lowest = weather.getDailyForecast().get(i).night().getTemperature().getTemperature();
                }
            }
            recyclerView.setData(
                    (float) weather.getYesterday().getDaytimeTemperature(),
                    (float) weather.getYesterday().getNighttimeTemperature(),
                    highest,
                    lowest,
                    Temperature.getShortTemperature(weather.getYesterday().getDaytimeTemperature(), unit),
                    Temperature.getShortTemperature(weather.getYesterday().getNighttimeTemperature(), unit)
            );
        }
    }

    public static void setHourlyTrend(GeoActivity activity, TextView title, TextView subtitle,
                                      TrendRecyclerView recyclerView,
                                      @NonNull ResourceProvider provider, @NonNull MainColorPicker picker,
                                      @NonNull Weather weather, int[] themeColors) {
        TemperatureUnit unit = SettingsOptionManager.getInstance(activity).getTemperatureUnit();

        title.setText(activity.getString(R.string.hourly_overview));

        if (TextUtils.isEmpty(weather.getCurrent().getHourlyForecast())) {
            subtitle.setVisibility(View.GONE);
        } else {
            subtitle.setVisibility(View.VISIBLE);
            subtitle.setText(weather.getCurrent().getHourlyForecast());
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(
                new HourlyTrendAdapter(
                        activity,
                        recyclerView,
                        activity.getResources().getDimensionPixelSize(R.dimen.little_margin),
                        DisplayUtils.isTabletDevice(activity) ? 7 : 5,
                        weather,
                        themeColors,
                        provider,
                        picker,
                        unit
                )
        );

        recyclerView.setLineColor(picker.getLineColor(activity));
        if (weather.getYesterday() == null) {
            recyclerView.setData(null, null, 0, 0, null, null);
        } else {
            int highest = weather.getYesterday().getDaytimeTemperature();
            int lowest = weather.getYesterday().getNighttimeTemperature();
            for (int i = 0; i < weather.getHourlyForecast().size(); i ++) {
                if (weather.getHourlyForecast().get(i).getTemperature().getTemperature() > highest) {
                    highest = weather.getHourlyForecast().get(i).getTemperature().getTemperature();
                }
                if (weather.getHourlyForecast().get(i).getTemperature().getTemperature() < lowest) {
                    lowest = weather.getHourlyForecast().get(i).getTemperature().getTemperature();
                }
            }
            recyclerView.setData(
                    (float) weather.getYesterday().getDaytimeTemperature(),
                    (float) weather.getYesterday().getNighttimeTemperature(),
                    highest,
                    lowest,
                    Temperature.getShortTemperature(weather.getYesterday().getDaytimeTemperature(), unit),
                    Temperature.getShortTemperature(weather.getYesterday().getNighttimeTemperature(), unit)
            );
        }
    }
}
