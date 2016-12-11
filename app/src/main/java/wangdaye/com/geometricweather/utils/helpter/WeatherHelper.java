package wangdaye.com.geometricweather.utils.helpter;

import android.content.Context;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.service.HefengWeather;
import wangdaye.com.geometricweather.data.service.JuheWeather;

/**
 * Weather kind tools.
 * */

public class WeatherHelper {
    // widget
    private JuheWeather juheWeather;
    private HefengWeather hefengWeather;

    // data
    private static final String KIND_CLEAR = "CLEAR";
    private static final String KIND_PARTLY_CLOUDY = "PARTLY_CLOUDY";
    private static final String KIND_CLOUDY = "CLOUDY";
    private static final String KIND_RAIN = "RAIN";
    private static final String KIND_SNOW = "SNOW";
    private static final String KIND_WIND = "WIND";
    private static final String KIND_FOG = "FOG";
    private static final String KIND_HAZE = "HAZE";
    private static final String KIND_SLEET = "SLEET";
    private static final String KIND_HAIL = "HAIL";
    private static final String KIND_THUNDER = "THUNDER";
    private static final String KIND_THUNDERSTORM = "THUNDERSTORM";

    /** <br> life cycle. */

    public WeatherHelper() {
        juheWeather = null;
        hefengWeather = null;
    }

    /** <br> data. */

    public void requestWeather(Context c, Location location, OnRequestWeatherListener l) {
        if (location.isEngLocation()) {
            hefengWeather = HefengWeather.getService()
                    .requestHefengWeather(c, location, l);
        } else {
            juheWeather = JuheWeather.getService()
                    .requestJuheWeather(c, location, l);
        }
    }

    public void cancel() {
        if (juheWeather != null) {
            juheWeather.cancel();
        }
        if (hefengWeather != null) {
            hefengWeather.cancel();
        }
    }

    /** <br> utils. */

    public static String getJuheWeatherKind(String code) {
        int realCode = Integer.parseInt(code);
        if (realCode == 0) {
            return KIND_CLEAR;
        } else if (realCode == 1) {
            return KIND_PARTLY_CLOUDY;
        } else if (realCode == 3 || (7 <= realCode && realCode <= 12) || (21 <= realCode && realCode <= 25)) {
            return KIND_RAIN;
        } else if (realCode == 4) {
            return KIND_THUNDERSTORM;
        } else if (realCode == 5) {
            return KIND_HAIL;
        } else if (realCode == 6 || realCode == 19) {
            return KIND_SLEET;
        } else if ((13 <= realCode && realCode <= 17) || (26 <= realCode && realCode <= 28)) {
            return KIND_SNOW;
        } else if (realCode == 18) {
            return KIND_FOG;
        } else if (realCode == 20 || realCode == 30 || realCode == 31) {
            return KIND_WIND;
        } else if (realCode == 29 || realCode == 53) {
            return KIND_HAZE;
        } else {
            return KIND_CLOUDY;
        }
    }

    public static String getHefengWeatherKind(String code) {
        int realCode = Integer.parseInt(code);
        if (realCode == 100) {
            return KIND_CLEAR;
        } else if (101 <= realCode && realCode <= 103) {
            return KIND_PARTLY_CLOUDY;
        } else if (realCode == 104) {
            return KIND_CLOUDY;
        } else if (realCode == 200) {
            return KIND_WIND;
        } else if (201 <= realCode && realCode <= 204) {
            return KIND_CLOUDY;
        } else if (205 <= realCode && realCode <= 213) {
            return KIND_WIND;
        } else if (300 <= realCode && realCode <= 303) {
            return KIND_RAIN;
        } else if (realCode == 304) {
            return KIND_HAIL;
        } else if (305 <= realCode && realCode <= 312) {
            return KIND_RAIN;
        } else if (realCode == 313) {
            return KIND_SLEET;
        } else if (400 <= realCode && realCode <= 403) {
            return KIND_SNOW;
        } else if (404 <= realCode && realCode <= 406) {
            return KIND_SLEET;
        } else if (realCode == 407) {
            return KIND_SNOW;
        } else if (500 <= realCode && realCode <= 501) {
            return KIND_FOG;
        } else if (realCode == 502 || realCode == 504) {
            return KIND_HAZE;
        } else if (503 <= realCode && realCode <= 508) {
            return KIND_WIND;
        } else {
            return KIND_CLOUDY;
        }
    }

    public static int[] getWeatherIcon(String weatherKind, boolean dayTime) {
        int[] imageId = new int[4];
        switch (weatherKind) {
            case KIND_CLEAR:
                if(dayTime) {
                    imageId[0] = R.drawable.weather_sun_circle;
                    imageId[1] = R.drawable.weather_sun_shine;
                    imageId[2] = 0;
                    imageId[3] = R.drawable.weather_sun_day;
                } else {
                    imageId[0] = R.drawable.weather_sun_night;
                    imageId[1] = 0;
                    imageId[2] = 0;
                    imageId[3] = R.drawable.weather_sun_night;
                }
                break;

            case KIND_PARTLY_CLOUDY:
                if(dayTime) {
                    imageId[0] = R.drawable.weather_cloud_right;
                    imageId[1] = R.drawable.weather_sun_circle;
                    imageId[2] = R.drawable.weather_sun_shine;
                    imageId[3] = R.drawable.weather_cloud_day;
                } else {
                    imageId[0] = R.drawable.weather_cloud_left;
                    imageId[1] = R.drawable.weather_moon;
                    imageId[2] = 0;
                    imageId[3] = R.drawable.weather_cloud_night;
                }
                break;

            case KIND_CLOUDY:
                imageId[0] = R.drawable.weather_cloud_top;
                imageId[1] = R.drawable.weather_cloud_large;
                imageId[2] = 0;
                imageId[3] = R.drawable.weather_cloudy;
                break;

            case KIND_RAIN:
                imageId[0] = R.drawable.weather_cloud_large;
                imageId[1] = R.drawable.weather_rain_left;
                imageId[2] = R.drawable.weather_rain_right;
                imageId[3] = R.drawable.weather_rain;
                break;

            case KIND_WIND:
                imageId[0] = R.drawable.weather_wind;
                imageId[1] = 0;
                imageId[2] = 0;
                imageId[3] = R.drawable.weather_wind;
                break;

            case KIND_SNOW:
                imageId[0] = R.drawable.weather_cloud_large;
                imageId[1] = R.drawable.weather_snow_left;
                imageId[2] = R.drawable.weather_snow_right;
                imageId[3] = R.drawable.weather_snow;
                break;

            case KIND_FOG:
                imageId[0] = R.drawable.weather_fog;
                imageId[1] = R.drawable.weather_fog;
                imageId[2] = R.drawable.weather_fog;
                imageId[3] = R.drawable.weather_fog;
                break;

            case KIND_HAZE:
                imageId[0] = R.drawable.weather_haze_1;
                imageId[1] = R.drawable.weather_haze_2;
                imageId[2] = R.drawable.weather_haze_3;
                imageId[3] = R.drawable.weather_haze;
                break;

            case KIND_SLEET:
                imageId[0] = R.drawable.weather_cloud_large;
                imageId[1] = R.drawable.weather_snow_left;
                imageId[2] = R.drawable.weather_rain_right;
                imageId[3] = R.drawable.weather_sleet;
                break;

            case KIND_THUNDERSTORM:
                imageId[0] = R.drawable.weather_cloud_large;
                imageId[1] = R.drawable.weather_single_thunder;
                imageId[2] = R.drawable.weather_rain_right;
                imageId[3] = R.drawable.weather_thunderstorm;
                break;

            case KIND_THUNDER:
                imageId[0] = R.drawable.weather_cloud_large;
                imageId[1] = R.drawable.weather_single_thunder;
                imageId[2] = 0;
                imageId[3] = R.drawable.weather_thunder;
                break;

            case KIND_HAIL:
                imageId[0] = R.drawable.weather_cloud_large;
                imageId[1] = R.drawable.weather_hail_left;
                imageId[2] = R.drawable.weather_hail_right;
                imageId[3] = R.drawable.weather_hail;
                break;

            default:
                imageId[0] = R.drawable.weather_cloud_top;
                imageId[1] = R.drawable.weather_cloud_large;
                imageId[2] = 0;
                imageId[3] = R.drawable.weather_cloudy;
                break;
        }
        return imageId;
    }

    public static int[] getAnimatorId(String weatherKind, boolean dayTime) {
        int[] animatorId = new int[3];

        switch (weatherKind) {
            case KIND_CLEAR:
                if(dayTime) {
                    animatorId[0] = R.animator.weather_sun_day_1;
                    animatorId[1] = R.animator.weather_sun_day_2;
                    animatorId[2] = 0;
                } else {
                    animatorId[0] = R.animator.weather_sun_night;
                    animatorId[1] = 0;
                    animatorId[2] = 0;
                }
                break;

            case KIND_PARTLY_CLOUDY:
                if(dayTime) {
                    animatorId[0] = R.animator.weather_cloud_day_1;
                    animatorId[1] = R.animator.weather_cloud_day_2;
                    animatorId[2] = R.animator.weather_cloud_day_3;
                } else {
                    animatorId[0] = R.animator.weather_cloud_night_1;
                    animatorId[1] = R.animator.weather_cloud_night_2;
                    animatorId[2] = 0;
                }
                break;

            case KIND_CLOUDY:
                animatorId[0] = R.animator.weather_cloudy_1;
                animatorId[1] = R.animator.weather_cloudy_2;
                animatorId[2] = 0;
                break;

            case KIND_RAIN:
                animatorId[0] = R.animator.weather_rain_1;
                animatorId[1] = R.animator.weather_rain_2;
                animatorId[2] = R.animator.weather_rain_3;
                break;

            case KIND_WIND:
                animatorId[0] = R.animator.weather_wind;
                animatorId[1] = 0;
                animatorId[2] = 0;
                break;

            case KIND_SNOW:
                animatorId[0] = R.animator.weather_snow_1;
                animatorId[1] = R.animator.weather_snow_2;
                animatorId[2] = R.animator.weather_snow_3;
                break;

            case KIND_FOG:
                animatorId[0] = R.animator.weather_fog_1;
                animatorId[1] = R.animator.weather_fog_2;
                animatorId[2] = R.animator.weather_fog_3;
                break;

            case KIND_HAZE:
                animatorId[0] = R.animator.weather_haze_1;
                animatorId[1] = R.animator.weather_haze_2;
                animatorId[2] = R.animator.weather_haze_3;
                break;

            case KIND_SLEET:
                animatorId[0] = R.animator.weather_sleet_1;
                animatorId[1] = R.animator.weather_sleet_2;
                animatorId[2] = R.animator.weather_sleet_3;
                break;

            case KIND_THUNDERSTORM:
                animatorId[0] = R.animator.weather_thunderstorm_1;
                animatorId[1] = R.animator.weather_thunderstorm_2;
                animatorId[2] = R.animator.weather_thunderstorm_3;
                break;

            case KIND_THUNDER:
                animatorId[0] = R.animator.weather_thunder_1;
                animatorId[1] = R.animator.weather_thunder_2;
                animatorId[2] = R.animator.weather_thunder_2;
                break;

            case KIND_HAIL:
                animatorId[0] = R.animator.weather_hail_1;
                animatorId[1] = R.animator.weather_hail_2;
                animatorId[2] = R.animator.weather_hail_3;
                break;

            default:
                animatorId[0] = R.animator.weather_cloudy_1;
                animatorId[1] = R.animator.weather_cloudy_2;
                animatorId[2] = 0;
                break;
        }
        return animatorId;
    }

    public static int getMiniWeatherIcon(String weatherInfo, boolean dayTime) {
        int imageId;
        switch (weatherInfo) {
            case KIND_CLEAR:
                if(dayTime) {
                    imageId = R.drawable.weather_sun_day_mini;
                } else {
                    imageId = R.drawable.weather_sun_night_mini;
                }
                break;

            case KIND_PARTLY_CLOUDY:
                if(dayTime) {
                    imageId = R.drawable.weather_cloud_day_mini;
                } else {
                    imageId = R.drawable.weather_cloud_mini;
                }
                break;

            case KIND_CLOUDY:
                imageId = R.drawable.weather_cloud_mini;
                break;

            case KIND_RAIN:
                imageId = R.drawable.weather_rain_mini;
                break;

            case KIND_WIND:
                imageId = R.drawable.weather_wind_mini;
                break;

            case KIND_SNOW:
                imageId = R.drawable.weather_snow_mini;
                break;

            case KIND_FOG:
                imageId = R.drawable.weather_fog_mini;
                break;

            case KIND_HAZE:
                imageId = R.drawable.weather_haze_mini;
                break;

            case KIND_SLEET:
                imageId = R.drawable.weather_sleet_mini;
                break;

            case KIND_THUNDERSTORM:
                imageId = R.drawable.weather_thunder_mini;
                break;

            case KIND_THUNDER:
                imageId = R.drawable.weather_thunder_mini;
                break;

            case KIND_HAIL:
                imageId = R.drawable.weather_hail_mini;
                break;

            default:
                imageId = R.drawable.weather_cloud_mini;
                break;
        }
        return imageId;
    }

    /** <br> listener. */

    public interface OnRequestWeatherListener {
        void requestWeatherSuccess(Weather weather, String name);
        void requestWeatherFailed(String name);
    }
}
