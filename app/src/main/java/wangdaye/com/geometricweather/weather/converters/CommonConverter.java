package wangdaye.com.geometricweather.weather.converters;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import us.dustinj.timezonemap.TimeZoneMap;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.weather.Daily;
import wangdaye.com.geometricweather.common.basic.models.weather.HalfDay;
import wangdaye.com.geometricweather.common.basic.models.weather.Hourly;
import wangdaye.com.geometricweather.common.basic.models.weather.UV;
import wangdaye.com.geometricweather.common.basic.models.weather.Wind;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;

public class CommonConverter {

    /**
     * Help complete a daily list with information from hourly list.
     * Mainly used by providers who don’t provide half days but only full days.
     * Currently helps completing:
     * - Weather code (at 12:00 for day, at 00:00 for night)
     * - Weather phase (at 12:00 for day, at 00:00 for night)
     * - Weather text (at 12:00 for day, at 00:00 for night)
     * You can expand it to other fields if you need it.
     *
     * @param dailyList a list of Daily where date is initialized at 00:00 on the TimeZone passed as 3rd parameter
     * @param hourlyListByDay a Map constructed as {"yyyyMMdd" => {"day": List<Hourly>, "night": List<Hourly>}}
     * @param timeZone the timezone of the location
     */
    public static List<Daily> completeDailyListWithHourlyList(
            List<Daily> dailyList,
            Map<String, Map<String, List<Hourly>>> hourlyListByDay,
            TimeZone timeZone
    ) {
        for (Daily daily : dailyList) {
            String dailyDateFormatted = DisplayUtils.getFormattedDate(daily.getDate(), timeZone, "yyyyMMdd");

            if (hourlyListByDay.containsKey(dailyDateFormatted)) {
                // Initialize half days to avoid null pointer exceptions
                if (daily.day() == null) {
                    daily.setDay(new HalfDay());
                }
                if (daily.night() == null) {
                    daily.setNight(new HalfDay());
                }

                for (Map.Entry<String, List<Hourly>> hourlySet : hourlyListByDay.get(dailyDateFormatted).entrySet()) {
                    HalfDay halfDay = hourlySet.getKey().equals("day") ? daily.day() : daily.night();

                    for (Hourly hourly : hourlySet.getValue()) {
                        // Weather code + Weather text
                        if (halfDay.getWeatherCode() == null || halfDay.getWeatherText() == null) {
                            // Update at 12:00 on daytime and 00:00 on nighttime
                            int nbHours = hourlySet.getKey().equals("day") ? 12 : 24;
                            if (hourly.getDate().getTime() == daily.getDate().getTime() + (nbHours * 3600 * 1000)) {
                                if (halfDay.getWeatherCode() == null) {
                                    halfDay.setWeatherCode(hourly.getWeatherCode());
                                }
                                if (halfDay.getWeatherPhase() == null) {
                                    halfDay.setWeatherPhase(hourly.getWeatherText());
                                }
                                if (halfDay.getWeatherText() == null) {
                                    halfDay.setWeatherText(hourly.getWeatherText());
                                }
                            }
                        }
                    }
                }
            }
        }
        return dailyList;
    }

    public static TimeZone getTimeZoneForPosition(TimeZoneMap map, double lat, double lon) {
        try {
            return TimeZone.getTimeZone(map.getOverlappingTimeZone(lat, lon).getZoneId());
        } catch (Exception ignored) {
            return TimeZone.getDefault();
        }
    }

    public static String getWindLevel(Context context, double speed) {
        if (speed <= Wind.WIND_SPEED_0) {
            return context.getString(R.string.wind_0);
        } else if (speed <= Wind.WIND_SPEED_1) {
            return context.getString(R.string.wind_1);
        } else if (speed <= Wind.WIND_SPEED_2) {
            return context.getString(R.string.wind_2);
        } else if (speed <= Wind.WIND_SPEED_3) {
            return context.getString(R.string.wind_3);
        } else if (speed <= Wind.WIND_SPEED_4) {
            return context.getString(R.string.wind_4);
        } else if (speed <= Wind.WIND_SPEED_5) {
            return context.getString(R.string.wind_5);
        } else if (speed <= Wind.WIND_SPEED_6) {
            return context.getString(R.string.wind_6);
        } else if (speed <= Wind.WIND_SPEED_7) {
            return context.getString(R.string.wind_7);
        } else if (speed <= Wind.WIND_SPEED_8) {
            return context.getString(R.string.wind_8);
        } else if (speed <= Wind.WIND_SPEED_9) {
            return context.getString(R.string.wind_9);
        } else if (speed <= Wind.WIND_SPEED_10) {
            return context.getString(R.string.wind_10);
        } else if (speed <= Wind.WIND_SPEED_11) {
            return context.getString(R.string.wind_11);
        } else {
            return context.getString(R.string.wind_12);
        }
    }

    public static String getWindDirection(Float degree, boolean isChina) {
        if (degree == null || degree < 0) {
            return isChina ? "无风向" : "Variable";
        }
        if (22.5 < degree && degree <= 67.5) {
            return isChina ? "东北风" : "NE";
        } else if (67.5 < degree && degree <= 112.5) {
            return isChina ? "东风" : "E";
        } else if (112.5 < degree && degree <= 157.5) {
            return isChina ? "东南风" : "SE";
        } else if (157.5 < degree && degree <= 202.5) {
            return isChina ? "南风" : "S";
        } else if (202.5 < degree && degree <= 247.5) {
            return isChina ? "西南风" : "SO";
        } else if (247.5 < degree && degree <= 292.5) {
            return isChina ? "西风" : "O";
        } else if (292. < degree && degree <= 337.5) {
            return isChina ? "西北风" : "NO";
        } else {
            return isChina ? "北风" : "N";
        }
    }

    @Nullable
    public static Integer getMoonPhaseAngle(@Nullable String phase) {
        if (TextUtils.isEmpty(phase)) {
            return null;
        }
        switch (phase.toLowerCase()) {
            case "waxingcrescent":
            case "waxing crescent":
                return 45;

            case "first":
            case "firstquarter":
            case "first quarter":
                return 90;

            case "waxinggibbous":
            case "waxing gibbous":
                return 135;

            case "full":
            case "fullmoon":
            case "full moon":
                return 180;

            case "waninggibbous":
            case "waning gibbous":
                return 225;

            case "third":
            case "thirdquarter":
            case "third quarter":
            case "last":
            case "lastquarter":
            case "last quarter":
                return 270;

            case "waningcrescent":
            case "waning crescent":
                return 315;

            default:
                return 360;
        }
    }

    public static String getUVLevel(Context context, Integer uvIndex) {
        if (uvIndex == null) {
            return null;
        }

        if (uvIndex >= 11) {
            return context.getString(R.string.uv_level_11);
        } else if (uvIndex >= 8) {
            return context.getString(R.string.uv_level_8_10);
        } else if (uvIndex >= 6) {
            return context.getString(R.string.uv_level_6_7);
        } else if (uvIndex >= 3) {
            return context.getString(R.string.uv_level_3_5);
        } else {
            return context.getString(R.string.uv_level_0_2);
        }
    }

    public static boolean isDaylight(Date sunrise, Date sunset, Date current, TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);

        calendar.setTime(sunrise);
        int sunriseTime = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);

        calendar.setTime(sunset);
        int sunsetTime = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);

        calendar.setTime(current);
        int currentTime = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);

        return sunriseTime < currentTime && currentTime < sunsetTime;
    }

    public static UV getCurrentUV(int dayMaxUV, Date currentDate, Date sunriseDate, Date sunsetDate, TimeZone timeZone) {
        if (currentDate == null || sunriseDate == null || sunsetDate == null || sunriseDate.after(sunsetDate))
            return new UV(null, null, null);

        // You can visualize formula here: https://www.desmos.com/calculator/lna7dco4zi
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTime(currentDate);
        float currentTime = calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) / 60f; // Approximating to the minute is enough

        calendar.setTime(sunriseDate);
        float sunRiseTime = calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) / 60f; // b in desmos graph

        calendar.setTime(sunsetDate);
        float sunSetTime = calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) / 60f; // c in desmos graph

        float sunlightDuration = sunSetTime - sunRiseTime; // d in desmos graph

        float sunRiseOffset = (float) -Math.PI * sunRiseTime / sunlightDuration; // o in desmos graph
        double currentUV = dayMaxUV * Math.sin(Math.PI / sunlightDuration * currentTime + sunRiseOffset); // dayMaxUV = a in desmos graph

        return new UV(Math.toIntExact(Math.round(currentUV)), null, null);
    }

    public static float getHoursOfDay(Date sunrise, Date sunset) {
        return (float) (
                (sunset.getTime() - sunrise.getTime()) // get delta millisecond.
                        / 1000 // second.
                        / 60 // minutes.
                        / 60.0 // hours.
        );
    }
}
