package wangdaye.com.geometricweather.ui.widget.trendView;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.History;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.ui.adapter.DailyTrendAdapter;
import wangdaye.com.geometricweather.ui.adapter.HourlyTrendAdapter;

/**
 * Trend view controller.
 * */

public class TrendViewController {

    public static void setDailyTrend(Context context, TextView title, TrendRecyclerView recyclerView,
                                     @NonNull Weather weather, @Nullable History history,
                                     int[] themeColors) {
        title.setText(context.getString(R.string.daily_overview));

        recyclerView.setLayoutManager(
                new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new DailyTrendAdapter(context, weather, history, themeColors));

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

    public static void setHourlyTrend(Context context, TextView title, TrendRecyclerView recyclerView,
                                      @NonNull Weather weather, @Nullable History history,
                                      int[] themeColors) {
        title.setText(context.getString(R.string.hourly_overview));

        recyclerView.setLayoutManager(
                new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new HourlyTrendAdapter(context, weather, history, themeColors));

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
