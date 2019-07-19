package wangdaye.com.geometricweather.remoteviews;

import android.content.Context;
import android.text.TextPaint;

import java.util.Calendar;

import androidx.annotation.Nullable;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.History;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.remoteviews.presenter.ClockDayDetailsWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenter.DailyTrendWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenter.HourlyTrendWidgetIMP;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.remoteviews.presenter.ClockDayHorizontalWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenter.ClockDayVerticalWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenter.ClockDayWeekWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenter.DayWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenter.DayWeekWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenter.TextWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenter.WeekWidgetIMP;

/**
 * Widget utils.
 * */

public class WidgetUtils {

    public static void updateWidgetIfNecessary(Context context, Location location,
                                               @Nullable Weather weather, @Nullable History history) {
        if (DayWidgetIMP.isEnable(context)) {
            DayWidgetIMP.updateWidgetView(context, location, weather);
        }
        if (WeekWidgetIMP.isEnable(context)) {
            WeekWidgetIMP.updateWidgetView(context, location, weather);
        }
        if (DayWeekWidgetIMP.isEnable(context)) {
            DayWeekWidgetIMP.updateWidgetView(context, location, weather);
        }
        if (ClockDayHorizontalWidgetIMP.isEnable(context)) {
            ClockDayHorizontalWidgetIMP.updateWidgetView(context, location, weather);
        }
        if (ClockDayVerticalWidgetIMP.isEnable(context)) {
            ClockDayVerticalWidgetIMP.updateWidgetView(context, location, weather);
        }
        if (ClockDayWeekWidgetIMP.isEnable(context)) {
            ClockDayWeekWidgetIMP.updateWidgetView(context, location, weather);
        }
        if (ClockDayDetailsWidgetIMP.isEnable(context)) {
            ClockDayDetailsWidgetIMP.updateWidgetView(context, location, weather);
        }
        if (TextWidgetIMP.isEnable(context)) {
            TextWidgetIMP.updateWidgetView(context, location, weather);
        }
        if (DailyTrendWidgetIMP.isEnable(context)) {
            DailyTrendWidgetIMP.updateWidgetView(context, location, weather, history);
        }
        if (HourlyTrendWidgetIMP.isEnable(context)) {
            HourlyTrendWidgetIMP.updateWidgetView(context, location, weather, history);
        }
    }

    public static String[] buildWidgetDayStyleText(Weather weather, boolean fahrenheit) {
        String[] texts = new String[] {
                weather.realTime.weather,
                ValueUtils.buildCurrentTemp(weather.realTime.temp, false, fahrenheit),
                ValueUtils.buildAbbreviatedCurrentTemp(weather.dailyList.get(0).temps[0], fahrenheit),
                ValueUtils.buildAbbreviatedCurrentTemp(weather.dailyList.get(0).temps[1], fahrenheit)
        };

        TextPaint paint = new TextPaint();

        float[] widths = new float[4];
        for (int i = 0; i < widths.length; i ++) {
            widths[i] = paint.measureText(texts[i]);
        }

        float maxiWidth = widths[0];
        for (float w : widths) {
            if (w > maxiWidth) {
                maxiWidth = w;
            }
        }

        while (true) {
            boolean[] flags = new boolean[] {false, false, false, false};

            for (int i = 0; i < 2; i ++) {
                if (widths[i] < maxiWidth) {
                    texts[i] = texts[i] + " ";
                    widths[i] = paint.measureText(texts[i]);
                } else {
                    flags[i] = true;
                }
            }
            for (int i = 2; i < 4; i ++) {
                if (widths[i] < maxiWidth) {
                    texts[i] = " " + texts[i];
                    widths[i] = paint.measureText(texts[i]);
                } else {
                    flags[i] = true;
                }
            }

            int n = 0;
            for (boolean flag : flags) {
                if (flag) {
                    n ++;
                }
            }
            if (n == 4) {
                break;
            }
        }

        return new String[] {
                texts[0] + "\n" + texts[1],
                texts[2] + "\n" + texts[3]
        };
    }

    public static String getWeek(Context context) {
        Calendar c = Calendar.getInstance();
        int week = c.get(Calendar.DAY_OF_WEEK);
        switch (week) {
            case Calendar.SUNDAY:
                return context.getString(R.string.week_7);

            case Calendar.MONDAY:
                return context.getString(R.string.week_1);

            case Calendar.TUESDAY:
                return context.getString(R.string.week_2);

            case Calendar.WEDNESDAY:
                return context.getString(R.string.week_3);

            case Calendar.THURSDAY:
                return context.getString(R.string.week_4);

            case Calendar.FRIDAY:
                return context.getString(R.string.week_5);

            case Calendar.SATURDAY:
                return context.getString(R.string.week_6);

            default:
                return "";
        }
    }
}
