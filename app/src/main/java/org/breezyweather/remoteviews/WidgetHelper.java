package org.breezyweather.remoteviews;

import android.content.Context;
import android.text.TextPaint;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.breezyweather.common.basic.models.Location;
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit;
import org.breezyweather.common.basic.models.weather.Weather;
import org.breezyweather.R;
import org.breezyweather.remoteviews.presenters.ClockDayDetailsWidgetIMP;
import org.breezyweather.remoteviews.presenters.ClockDayHorizontalWidgetIMP;
import org.breezyweather.remoteviews.presenters.ClockDayVerticalWidgetIMP;
import org.breezyweather.remoteviews.presenters.ClockDayWeekWidgetIMP;
import org.breezyweather.remoteviews.presenters.DailyTrendWidgetIMP;
import org.breezyweather.remoteviews.presenters.DayWeekWidgetIMP;
import org.breezyweather.remoteviews.presenters.DayWidgetIMP;
import org.breezyweather.remoteviews.presenters.HourlyTrendWidgetIMP;
import org.breezyweather.remoteviews.presenters.MaterialYouCurrentWidgetIMP;
import org.breezyweather.remoteviews.presenters.MaterialYouForecastWidgetIMP;
import org.breezyweather.remoteviews.presenters.MultiCityWidgetIMP;
import org.breezyweather.remoteviews.presenters.TextWidgetIMP;
import org.breezyweather.remoteviews.presenters.WeekWidgetIMP;

public class WidgetHelper {

    public static void updateWidgetIfNecessary(Context context, Location location) {
        if (DayWidgetIMP.isInUse(context)) {
            DayWidgetIMP.updateWidgetView(context, location);
        }
        if (WeekWidgetIMP.isInUse(context)) {
            WeekWidgetIMP.updateWidgetView(context, location);
        }
        if (DayWeekWidgetIMP.isInUse(context)) {
            DayWeekWidgetIMP.updateWidgetView(context, location);
        }
        if (ClockDayHorizontalWidgetIMP.isInUse(context)) {
            ClockDayHorizontalWidgetIMP.updateWidgetView(context, location);
        }
        if (ClockDayVerticalWidgetIMP.isInUse(context)) {
            ClockDayVerticalWidgetIMP.updateWidgetView(context, location);
        }
        if (ClockDayWeekWidgetIMP.isInUse(context)) {
            ClockDayWeekWidgetIMP.updateWidgetView(context, location);
        }
        if (ClockDayDetailsWidgetIMP.isInUse(context)) {
            ClockDayDetailsWidgetIMP.updateWidgetView(context, location);
        }
        if (TextWidgetIMP.isInUse(context)) {
            TextWidgetIMP.updateWidgetView(context, location);
        }
        if (DailyTrendWidgetIMP.isInUse(context)) {
            DailyTrendWidgetIMP.updateWidgetView(context, location);
        }
        if (HourlyTrendWidgetIMP.isInUse(context)) {
            HourlyTrendWidgetIMP.updateWidgetView(context, location);
        }

        // material you.
        if (MaterialYouForecastWidgetIMP.isEnabled(context)) {
            MaterialYouForecastWidgetIMP.updateWidgetView(context, location);
        }
        if (MaterialYouCurrentWidgetIMP.isEnabled(context)) {
            MaterialYouCurrentWidgetIMP.updateWidgetView(context, location);
        }
    }

    public static void updateWidgetIfNecessary(Context context, List<Location> locationList) {
        locationList = Location.excludeInvalidResidentLocation(context, locationList);
        if (MultiCityWidgetIMP.isInUse(context)) {
            MultiCityWidgetIMP.updateWidgetView(context, locationList);
        }
    }

    public static String[] buildWidgetDayStyleText(Context context, Weather weather, TemperatureUnit unit) {
        String[] texts = new String[] {
                weather.getCurrent() != null
                        && weather.getCurrent().getWeatherText() != null
                        ? weather.getCurrent().getWeatherText()
                        : "",
                weather.getCurrent() != null
                        && weather.getCurrent().getTemperature() != null
                        && weather.getCurrent().getTemperature().getTemperature() != null
                        ? weather.getCurrent().getTemperature().getTemperature(context, unit)
                        : "",
                weather.getDailyForecast().size() > 0
                        && weather.getDailyForecast().get(0).getDay() != null
                        && weather.getDailyForecast().get(0).getDay().getTemperature() != null
                        && weather.getDailyForecast().get(0).getDay().getTemperature().getTemperature() != null
                        ? weather.getDailyForecast().get(0).getDay().getTemperature().getShortTemperature(context, unit)
                        : "",
                weather.getDailyForecast().size() > 0
                        && weather.getDailyForecast().get(0).getNight() != null
                        && weather.getDailyForecast().get(0).getNight().getTemperature() != null
                        && weather.getDailyForecast().get(0).getNight().getTemperature().getTemperature() != null
                        ? weather.getDailyForecast().get(0).getNight().getTemperature().getShortTemperature(context, unit)
                        : ""
        };

        TextPaint paint = new TextPaint();

        float[] widths = new float[4];
        for (int i = 0; i < widths.length; i++) {
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

            for (int i = 0; i < 2; i++) {
                if (widths[i] < maxiWidth) {
                    texts[i] = texts[i] + " ";
                    widths[i] = paint.measureText(texts[i]);
                } else {
                    flags[i] = true;
                }
            }
            for (int i = 2; i < 4; i++) {
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

    public static String getWeek(Context context, TimeZone timeZone) {
        Calendar c = Calendar.getInstance();
        int week = c.get(Calendar.DAY_OF_WEEK);
        switch (week) {
            case Calendar.SUNDAY:
                return context.getString(R.string.short_sunday);

            case Calendar.MONDAY:
                return context.getString(R.string.short_monday);

            case Calendar.TUESDAY:
                return context.getString(R.string.short_tuesday);

            case Calendar.WEDNESDAY:
                return context.getString(R.string.short_wednesday);

            case Calendar.THURSDAY:
                return context.getString(R.string.short_thursday);

            case Calendar.FRIDAY:
                return context.getString(R.string.short_friday);

            case Calendar.SATURDAY:
                return context.getString(R.string.short_saturday);

            default:
                return "";
        }
    }

    // TODO: Remove this function, see ClockDayWeekWidgetIMP, there is a simpler and more reliable way
    // with daily.isToday(location.timeZone)
    public static String getDailyWeek(Context context, Weather weather, int index, TimeZone timeZone) {
        if (index > 1) {
            return weather.getDailyForecast().get(index).getWeek(context, timeZone);
        }

        String firstDay;
        String secondDay;

        Calendar today = Calendar.getInstance(timeZone);
        today.setTime(new Date());

        Calendar publish = Calendar.getInstance(timeZone);
        publish.setTime(weather.getDailyForecast().get(0).getDate());

        if (today.get(Calendar.YEAR) == publish.get(Calendar.YEAR)
                && today.get(Calendar.DAY_OF_YEAR) == publish.get(Calendar.DAY_OF_YEAR)) {
            firstDay = context.getString(R.string.short_today);
            secondDay = weather.getDailyForecast().get(1).getWeek(context, timeZone);
        } else if (today.get(Calendar.YEAR) == publish.get(Calendar.YEAR)
                && today.get(Calendar.DAY_OF_YEAR) == publish.get(Calendar.DAY_OF_YEAR) + 1) {
            firstDay = context.getString(R.string.short_yesterday);
            secondDay = context.getString(R.string.short_today);
        } else {
            firstDay = weather.getDailyForecast().get(0).getWeek(context, timeZone);
            secondDay = weather.getDailyForecast().get(1).getWeek(context, timeZone);
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
}
