package wangdaye.com.geometricweather.weather.converters;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.Date;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.weather.AirQuality;
import wangdaye.com.geometricweather.common.basic.models.weather.UV;
import wangdaye.com.geometricweather.common.basic.models.weather.Wind;

public class CommonConverter {

    public static String getWindLevel(Context c, double speed) {
        if (speed <= Wind.WIND_SPEED_0) {
            return c.getString(R.string.wind_0);
        } else if (speed <= Wind.WIND_SPEED_1) {
            return c.getString(R.string.wind_1);
        } else if (speed <= Wind.WIND_SPEED_2) {
            return c.getString(R.string.wind_2);
        } else if (speed <= Wind.WIND_SPEED_3) {
            return c.getString(R.string.wind_3);
        } else if (speed <= Wind.WIND_SPEED_4) {
            return c.getString(R.string.wind_4);
        } else if (speed <= Wind.WIND_SPEED_5) {
            return c.getString(R.string.wind_5);
        } else if (speed <= Wind.WIND_SPEED_6) {
            return c.getString(R.string.wind_6);
        } else if (speed <= Wind.WIND_SPEED_7) {
            return c.getString(R.string.wind_7);
        } else if (speed <= Wind.WIND_SPEED_8) {
            return c.getString(R.string.wind_8);
        } else if (speed <= Wind.WIND_SPEED_9) {
            return c.getString(R.string.wind_9);
        } else if (speed <= Wind.WIND_SPEED_10) {
            return c.getString(R.string.wind_10);
        } else if (speed <= Wind.WIND_SPEED_11) {
            return c.getString(R.string.wind_11);
        } else {
            return c.getString(R.string.wind_12);
        }
    }

    @Nullable
    public static String getAqiQuality(Context c, @Nullable Integer index) {
        if (index == null || index < 0) {
            return null;
        } if (index <= AirQuality.AQI_INDEX_1) {
            return c.getString(R.string.aqi_1);
        } else if (index <= AirQuality.AQI_INDEX_2) {
            return c.getString(R.string.aqi_2);
        } else if (index <= AirQuality.AQI_INDEX_3) {
            return c.getString(R.string.aqi_3);
        } else if (index <= AirQuality.AQI_INDEX_4) {
            return c.getString(R.string.aqi_4);
        } else if (index <= AirQuality.AQI_INDEX_5) {
            return c.getString(R.string.aqi_5);
        } else {
            return c.getString(R.string.aqi_6);
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

    public static boolean isDaylight(Date sunrise, Date sunset, Date current) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(sunrise);
        int sunriseTime = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);

        calendar.setTime(sunset);
        int sunsetTime = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);

        calendar.setTime(current);
        int currentTime = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);

        return sunriseTime < currentTime && currentTime < sunsetTime;
    }

    public static UV getCurrentUV(int dayMaxUV, Date currentDate, Date sunriseDate, Date sunsetDate) {
        if (currentDate == null || sunriseDate == null || sunsetDate == null || sunriseDate.after(sunsetDate))
            return new UV(null, null, null);

        // You can visualize formula here: https://www.desmos.com/calculator/lna7dco4zi
        Calendar calendar = Calendar.getInstance();
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
