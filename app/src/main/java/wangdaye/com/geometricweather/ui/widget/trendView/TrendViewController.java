package wangdaye.com.geometricweather.ui.widget.trendView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.History;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.main.ui.adapter.DailyTrendAdapter;
import wangdaye.com.geometricweather.main.ui.adapter.HourlyTrendAdapter;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;

/**
 * Trend view controller.
 * */

public class TrendViewController {

    public static void setDailyTrend(GeoActivity activity, TextView title, TextView subtitle,
                                     TrendRecyclerView recyclerView,
                                     @NonNull ResourceProvider provider, @NonNull MainColorPicker picker,
                                     @NonNull Weather weather, @Nullable History history,
                                     int[] themeColors) {
        title.setText(activity.getString(R.string.daily_overview));

        if (TextUtils.isEmpty(weather.index.simpleForecast)) {
            subtitle.setVisibility(View.GONE);
        } else {
            subtitle.setVisibility(View.VISIBLE);
            subtitle.setText(weather.index.simpleForecast);
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new DailyTrendAdapter(activity, weather, history, themeColors, provider, picker));

        recyclerView.setLineColor(picker.getLineColor(activity));
        if (history == null) {
            recyclerView.setData(null, 0, 0, true);
        } else {
            int highest = history.maxiTemp;
            int lowest = history.miniTemp;
            for (int i = 0; i < weather.dailyList.size(); i ++) {
                if (weather.dailyList.get(i).temps[0] > highest) {
                    highest = weather.dailyList.get(i).temps[0];
                }
                if (weather.dailyList.get(i).temps[1] < lowest) {
                    lowest = weather.dailyList.get(i).temps[1];
                }
            }
            recyclerView.setData(
                    new int[] {history.maxiTemp, history.miniTemp}, highest, lowest, true);
        }
    }

    public static void setHourlyTrend(GeoActivity activity, TextView title, TextView subtitle,
                                      TrendRecyclerView recyclerView,
                                      @NonNull ResourceProvider provider, @NonNull MainColorPicker picker,
                                      @NonNull Weather weather, @Nullable History history,
                                      int[] themeColors) {
        title.setText(activity.getString(R.string.hourly_overview));

        if (TextUtils.isEmpty(weather.index.briefing)) {
            subtitle.setVisibility(View.GONE);
        } else {
            subtitle.setVisibility(View.VISIBLE);
            subtitle.setText(weather.index.briefing);
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new HourlyTrendAdapter(activity, weather, history, themeColors, provider, picker));

        recyclerView.setLineColor(picker.getLineColor(activity));
        if (history == null) {
            recyclerView.setData(null, 0, 0, false);
        } else {
            int highest = history.maxiTemp;
            int lowest = history.miniTemp;
            for (int i = 0; i < weather.hourlyList.size(); i ++) {
                if (weather.hourlyList.get(i).temp > highest) {
                    highest = weather.hourlyList.get(i).temp;
                }
                if (weather.hourlyList.get(i).temp < lowest) {
                    lowest = weather.hourlyList.get(i).temp;
                }
            }
            recyclerView.setData(
                    new int[] {history.maxiTemp, history.miniTemp}, highest, lowest, false);
        }
    }
}
