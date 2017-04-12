package wangdaye.com.geometricweather.utils.helpter;

import android.annotation.SuppressLint;
import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.service.NewWeather;

/**
 * Weather kind tools.
 * */

public class WeatherHelper {
    // widget
    private NewWeather newWeather;

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
        newWeather = null;
    }

    /** <br> data. */

    public void requestWeather(Context c, Location location, OnRequestWeatherListener l) {
        newWeather = NewWeather.getService().requestNewWeather(c, location, l);
    }
/*
    public void requestWeather(Context c, final Location location, OnRequestWeatherListener l) {
        if (DatabaseHelper.getInstance(c).isNeedWriteCityList()
                || DatabaseHelper.getInstance(c).isNeedWriteOverseaCityList()) {
            l.requestWeatherFailed(location);
            return;
        }

        if (location.isEngLocation()) {
            requestHefengWeather(c, location, l);
        } else {
            requestFWeather(location, l);
        }
    }

    private void requestHefengWeather(Context context, Location location, OnRequestWeatherListener l) {
        hefengWeather = HefengWeather.getService().requestHefengWeather(
                DatabaseHelper.getInstance(context).readWeather(location), location, l);
    }

    private void requestFWeather(Location location, OnRequestWeatherListener l) {
        fWeather = FWeather.getService().requestFWeather(location, l);
    }
*/
    public void cancel() {
        if (newWeather != null) {
            newWeather.cancel();
        }
    }

    /** <br> utils. */
/*
    public static String getFWeatherKind(String weather) {
        if(weather.contains("雨")) {
            if(weather.contains("雪")) {
                return KIND_SLEET;
            } else if(weather.contains("雷")) {
                return KIND_THUNDERSTORM;
            } else {
                return KIND_RAIN;
            }
        }
        if(weather.contains("雷")) {
            if (weather.contains("雨")) {
                return KIND_THUNDERSTORM;
            } else {
                return KIND_THUNDER;
            }
        }
        if (weather.contains("雪")) {
            if(weather.contains("雨")) {
                return KIND_SLEET;
            } else {
                return KIND_SNOW;
            }
        }
        if (weather.contains("雹")) {
            return KIND_HAIL;
        }
        if (weather.contains("冰")) {
            return KIND_HAIL;
        }
        if (weather.contains("冻")) {
            return KIND_HAIL;
        }
        if (weather.contains("云")) {
            return KIND_PARTLY_CLOUDY;
        }
        if (weather.contains("阴")) {
            return KIND_CLOUDY;
        }
        if (weather.contains("风")) {
            return KIND_WIND;
        }
        if(weather.contains("沙")) {
            return KIND_WIND;
        }
        if(weather.contains("尘")) {
            return KIND_HAZE;
        }
        if(weather.contains("雾")) {
            return KIND_FOG;
        }
        if(weather.contains("霾")) {
            return KIND_HAZE;
        }
        if (weather.contains("晴")) {
            return KIND_CLEAR;
        }
        return KIND_CLOUDY;
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
*/
    public static String getNewWeatherKind(int icon) {
        if (icon == 1 || icon == 2 || icon == 3
                || icon == 30 || icon == 33 || icon == 34 || icon == 35) {
            return KIND_CLEAR;
        } else if (icon == 4 || icon == 6 || icon == 36 || icon == 38) {
            return KIND_PARTLY_CLOUDY;
        } else if (icon == 5 || icon == 7 || icon == 8
                || icon == 37) {
            return KIND_CLOUDY;
        } else if (icon == 11) {
            return KIND_FOG;
        } else if (icon == 12 || icon == 13 || icon == 14 || icon == 18
                || icon == 39 || icon == 40) {
            return KIND_RAIN;
        } else if (icon == 15 || icon == 16 || icon == 17 || icon == 41 || icon == 42) {
            return KIND_THUNDERSTORM;
        } else if (icon == 19 || icon == 20 || icon == 21 || icon == 22 || icon == 23 || icon == 24
                || icon == 31 || icon == 43 || icon == 44) {
            return KIND_SNOW;
        } else if (icon == 25) {
            return KIND_HAIL;
        } else if (icon == 26 || icon == 29) {
            return KIND_SLEET;
        } else if (icon == 32) {
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

    public static int getShortcutIcon(String weatherInfo, boolean dayTime) {
        int imageId;
        switch (weatherInfo) {
            case KIND_CLEAR:
                if(dayTime) {
                    imageId = R.drawable.ic_shortcut_sun_day;
                } else {
                    imageId = R.drawable.ic_shortcut_sun_night;
                }
                break;

            case KIND_PARTLY_CLOUDY:
                if(dayTime) {
                    imageId = R.drawable.ic_shortcut_cloud_day;
                } else {
                    imageId = R.drawable.ic_shortcut_cloudy;
                }
                break;

            case KIND_CLOUDY:
                imageId = R.drawable.ic_shortcut_cloudy;
                break;

            case KIND_RAIN:
                imageId = R.drawable.ic_shortcut_rain;
                break;

            case KIND_WIND:
                imageId = R.drawable.ic_shortcut_wind;
                break;

            case KIND_SNOW:
                imageId = R.drawable.ic_shortcut_snow;
                break;

            case KIND_FOG:
                imageId = R.drawable.ic_shortcut_fog;
                break;

            case KIND_HAZE:
                imageId = R.drawable.ic_shortcut_haze;
                break;

            case KIND_SLEET:
                imageId = R.drawable.ic_shortcut_sleet;
                break;

            case KIND_THUNDERSTORM:
                imageId = R.drawable.ic_shortcut_thunder;
                break;

            case KIND_THUNDER:
                imageId = R.drawable.ic_shortcut_thunder;
                break;

            case KIND_HAIL:
                imageId = R.drawable.ic_shortcut_hail;
                break;

            default:
                imageId = R.drawable.ic_shortcut_cloudy;
                break;
        }
        return imageId;
    }

    @SuppressLint("SimpleDateFormat")
    public static String getWeek(Context c, String dateTxt) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(simpleDateFormat.parse(dateTxt));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int day = calendar.get(Calendar.DAY_OF_WEEK);
        if (day == 1){
            return c.getString(R.string.week_7);
        } else if (day == 2) {
            return c.getString(R.string.week_1);
        } else if (day == 3) {
            return c.getString(R.string.week_2);
        } else if (day == 4) {
            return c.getString(R.string.week_3);
        } else if (day == 5) {
            return c.getString(R.string.week_4);
        } else if (day == 6) {
            return c.getString(R.string.week_5);
        } else {
            return c.getString(R.string.week_6);
        }
    }

    public static String getWindLevel(Context c, double speed) {
        if (speed <= 2) {
            return c.getString(R.string.wind_0);
        } else if (speed <= 6) {
            return c.getString(R.string.wind_1);
        } else if (speed <= 12) {
            return c.getString(R.string.wind_2);
        } else if (speed <= 19) {
            return c.getString(R.string.wind_3);
        } else if (speed <= 30) {
            return c.getString(R.string.wind_4);
        } else if (speed <= 40) {
            return c.getString(R.string.wind_5);
        } else if (speed <= 51) {
            return c.getString(R.string.wind_6);
        } else if (speed <= 62) {
            return c.getString(R.string.wind_7);
        } else if (speed <= 75) {
            return c.getString(R.string.wind_8);
        } else if (speed <= 87) {
            return c.getString(R.string.wind_9);
        } else if (speed <= 103) {
            return c.getString(R.string.wind_10);
        } else if (speed <= 117) {
            return c.getString(R.string.wind_11);
        } else {
            return c.getString(R.string.wind_12);
        }
    }

    public static int getWindColorResId(String speed) {
        double s = Double.parseDouble(speed.split("km/h")[0]);
        if (s <= 30) {
            return 0;
        } else if (s <= 51) {
            return 0;
        } else if (s <= 75) {
            return 0;
        } else if (s <= 103) {
            return R.color.colorLevel_4;
        } else if (s <= 117) {
            return R.color.colorLevel_5;
        } else {
            return R.color.colorLevel_6;
        }
    }
/*
    public static int getPrecipitation(int precipitation) {
        if (precipitation < 3) {
            return 10;
        } else if (precipitation < 6) {
            return 30;
        } else if (precipitation < 9) {
            return 60;
        } else {
            return 90;
        }
    }
*/
    public static String getAqiQuality(Context c, int index) {
        if (index <= 50) {
            return c.getString(R.string.aqi_1);
        } else if (index <= 100) {
            return c.getString(R.string.aqi_2);
        } else if (index <= 150) {
            return c.getString(R.string.aqi_3);
        } else if (index <= 200) {
            return c.getString(R.string.aqi_4);
        } else if (index <= 300) {
            return c.getString(R.string.aqi_5);
        } else {
            return c.getString(R.string.aqi_6);
        }
    }

    public static int getAqiColorResId(int index) {
        if (index <= 50) {
            return 0;
        } else if (index <= 100) {
            return 0;
        } else if (index <= 150) {
            return 0;
        } else if (index <= 200) {
            return R.color.colorLevel_4;
        } else if (index <= 300) {
            return R.color.colorLevel_5;
        } else {
            return R.color.colorLevel_6;
        }
    }

    /** <br> listener. */

    public interface OnRequestWeatherListener {
        void requestWeatherSuccess(Weather weather, Location requestLocation);
        void requestWeatherFailed(Location requestLocation);
    }
}
