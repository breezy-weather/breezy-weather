package wangdaye.com.geometricweather.remoteviews;

import android.content.Context;
import android.text.TextPaint;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.unit.TemperatureUnit;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.remoteviews.presenters.AndroidSWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenters.ClockDayDetailsWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenters.ClockDayHorizontalWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenters.ClockDayVerticalWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenters.ClockDayWeekWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenters.DailyTrendWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenters.DayWeekWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenters.DayWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenters.HourlyTrendWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenters.MultiCityWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenters.TextWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenters.WeekWidgetIMP;

public class WidgetHelper {

    public static void updateWidgetIfNecessary(Context context, Location location) {
        if (DayWidgetIMP.isEnable(context)) {
            DayWidgetIMP.updateWidgetView(context, location);
        }
        if (WeekWidgetIMP.isEnable(context)) {
            WeekWidgetIMP.updateWidgetView(context, location);
        }
        if (DayWeekWidgetIMP.isEnable(context)) {
            DayWeekWidgetIMP.updateWidgetView(context, location);
        }
        if (ClockDayHorizontalWidgetIMP.isEnable(context)) {
            ClockDayHorizontalWidgetIMP.updateWidgetView(context, location);
        }
        if (ClockDayVerticalWidgetIMP.isEnable(context)) {
            ClockDayVerticalWidgetIMP.updateWidgetView(context, location);
        }
        if (ClockDayWeekWidgetIMP.isEnable(context)) {
            ClockDayWeekWidgetIMP.updateWidgetView(context, location);
        }
        if (ClockDayDetailsWidgetIMP.isEnable(context)) {
            ClockDayDetailsWidgetIMP.updateWidgetView(context, location);
        }
        if (TextWidgetIMP.isEnable(context)) {
            TextWidgetIMP.updateWidgetView(context, location);
        }
        if (DailyTrendWidgetIMP.isEnable(context)) {
            DailyTrendWidgetIMP.updateWidgetView(context, location);
        }
        if (HourlyTrendWidgetIMP.isEnable(context)) {
            HourlyTrendWidgetIMP.updateWidgetView(context, location);
        }

        // android S.
        if (AndroidSWidgetIMP.isEnable(context)) {
            AndroidSWidgetIMP.updateWidgetView(context, location);
        }
    }

    public static void updateWidgetIfNecessary(Context context, List<Location> locationList) {
        locationList = Location.excludeInvalidResidentLocation(context, locationList);
        if (MultiCityWidgetIMP.isEnable(context)) {
            MultiCityWidgetIMP.updateWidgetView(context, locationList);
        }
    }

    public static String[] buildWidgetDayStyleText(Context context, Weather weather, TemperatureUnit unit) {
        String[] texts = new String[] {
                weather.getCurrent().getWeatherText(),
                weather.getCurrent().getTemperature().getTemperature(context, unit),
                weather.getDailyForecast().get(0).day().getTemperature().getShortTemperature(context, unit),
                weather.getDailyForecast().get(0).night().getTemperature().getShortTemperature(context, unit)
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

    public static String getDailyWeek(Context context, Weather weather, int index) {
        if (index > 1) {
            return weather.getDailyForecast().get(index).getWeek(context);
        }

        String firstDay;
        String secondDay;

        Calendar today = Calendar.getInstance();
        today.setTime(new Date());

        Calendar publish = Calendar.getInstance();
        publish.setTime(weather.getDailyForecast().get(0).getDate());

        if (today.get(Calendar.YEAR) == publish.get(Calendar.YEAR)
                && today.get(Calendar.DAY_OF_YEAR) == publish.get(Calendar.DAY_OF_YEAR)) {
            firstDay = context.getString(R.string.today);
            secondDay = weather.getDailyForecast().get(1).getWeek(context);
        } else if (today.get(Calendar.YEAR) == publish.get(Calendar.YEAR)
                && today.get(Calendar.DAY_OF_YEAR) == publish.get(Calendar.DAY_OF_YEAR) + 1) {
            firstDay = context.getString(R.string.yesterday);
            secondDay = context.getString(R.string.today);
        } else {
            firstDay = weather.getDailyForecast().get(0).getWeek(context);
            secondDay = weather.getDailyForecast().get(1).getWeek(context);
        }

        if (index == 0) {
            return firstDay;
        } else {
            return secondDay;
        }
    }

    public static float getNonNullValue(Float value, float defaultValue) {
        return value == null ? defaultValue : value;
    }

    public static int getNonNullValue(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }
}
