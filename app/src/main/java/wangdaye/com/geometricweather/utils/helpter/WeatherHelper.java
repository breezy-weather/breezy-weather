package wangdaye.com.geometricweather.utils.helpter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.service.weather.WeatherService;

/**
 * Weather kind tools.
 * */

public class WeatherHelper {

    private WeatherService weatherService;

    public static final String KIND_CLEAR = "CLEAR";
    public static final String KIND_PARTLY_CLOUDY = "PARTLY_CLOUDY";
    public static final String KIND_CLOUDY = "CLOUDY";
    public static final String KIND_RAIN = "RAIN";
    public static final String KIND_SNOW = "SNOW";
    public static final String KIND_WIND = "WIND";
    public static final String KIND_FOG = "FOG";
    public static final String KIND_HAZE = "HAZE";
    public static final String KIND_SLEET = "SLEET";
    public static final String KIND_HAIL = "HAIL";
    public static final String KIND_THUNDER = "THUNDER";
    public static final String KIND_THUNDERSTORM = "THUNDERSTORM";

    public WeatherHelper() {
        weatherService = new WeatherService();
    }

    public void requestWeather(Context c, Location location, OnRequestWeatherListener l) {
        weatherService = WeatherService.getService().requestWeather(c, location, l);
    }

    public void cancel() {
        if (weatherService != null) {
            weatherService.cancel();
        }
    }

    public static String getNewWeatherKind(int icon) {
        if (icon == 1 || icon == 2 || icon == 30
                || icon == 33 || icon == 34) {
            return KIND_CLEAR;
        } else if (icon == 3 || icon == 4 || icon == 6 || icon == 7
                || icon == 35 || icon == 36 || icon == 38) {
            return KIND_PARTLY_CLOUDY;
        } else if (icon == 5 || icon == 8 || icon == 37) {
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

    public static String getNewWeatherKind(String icon) {
        if (TextUtils.isEmpty(icon)) {
            return KIND_CLOUDY;
        }

        switch (icon) {
            case "0":
            case "00":
                return KIND_CLEAR;

            case "1":
            case "01":
                return KIND_PARTLY_CLOUDY;

            case "2":
            case "02":
                return KIND_CLOUDY;

            case "3":
            case "7":
            case "8":
            case "9":
            case "03":
            case "07":
            case "08":
            case "09":
            case "10":
            case "11":
            case "12":
            case "21":
            case "22":
            case "23":
            case "24":
            case "25":
                return KIND_RAIN;

            case "4":
            case "04":
                return KIND_THUNDERSTORM;

            case "5":
            case "05":
                return KIND_HAIL;

            case "6":
            case "06":
                return KIND_SLEET;

            case "13":
            case "14":
            case "15":
            case "16":
            case "17":
            case "26":
            case "27":
            case "28":
                return KIND_SNOW;

            case "18":
            case "32":
            case "49":
            case "57":
                return KIND_FOG;

            case "19":
                return KIND_SLEET;

            case "20":
            case "29":
            case "30":
                return KIND_WIND;

            case "53":
            case "54":
            case "55":
            case "56":
                return KIND_HAZE;

            default:
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

    private static int getMiniWeatherIcon(String weatherInfo, boolean dayTime, String textColor) {
        int imageId = R.drawable.weather_cloudy_mini_light;
        switch (weatherInfo) {
            case KIND_CLEAR:
                if(dayTime) {
                    switch (textColor) {
                        case "light":
                            imageId = R.drawable.weather_sun_day_mini_light;
                            break;

                        case "grey":
                            imageId = R.drawable.weather_sun_day_mini_grey;
                            break;

                        case "dark":
                            imageId = R.drawable.weather_sun_day_mini_dark;
                            break;
                    }
                } else {
                    switch (textColor) {
                        case "light":
                            imageId = R.drawable.weather_sun_night_mini_light;
                            break;

                        case "grey":
                            imageId = R.drawable.weather_sun_night_mini_grey;
                            break;

                        case "dark":
                            imageId = R.drawable.weather_sun_night_mini_dark;
                            break;
                    }
                }
                break;

            case KIND_PARTLY_CLOUDY:
                if(dayTime) {
                    switch (textColor) {
                        case "light":
                            imageId = R.drawable.weather_cloud_day_mini_light;
                            break;

                        case "grey":
                            imageId = R.drawable.weather_cloud_day_mini_grey;
                            break;

                        case "dark":
                            imageId = R.drawable.weather_cloud_day_mini_dark;
                            break;
                    }
                } else {
                    switch (textColor) {
                        case "light":
                            imageId = R.drawable.weather_cloud_night_mini_light;
                            break;

                        case "grey":
                            imageId = R.drawable.weather_cloud_night_mini_grey;
                            break;

                        case "dark":
                            imageId = R.drawable.weather_cloud_night_mini_dark;
                            break;
                    }
                }
                break;

            case KIND_CLOUDY:
                switch (textColor) {
                    case "light":
                        imageId = R.drawable.weather_cloudy_mini_light;
                        break;

                    case "grey":
                        imageId = R.drawable.weather_cloudy_mini_grey;
                        break;

                    case "dark":
                        imageId = R.drawable.weather_cloudy_mini_dark;
                        break;
                }
                break;

            case KIND_RAIN:
                switch (textColor) {
                    case "light":
                        imageId = R.drawable.weather_rain_mini_light;
                        break;

                    case "grey":
                        imageId = R.drawable.weather_rain_mini_grey;
                        break;

                    case "dark":
                        imageId = R.drawable.weather_rain_mini_dark;
                        break;
                }
                break;

            case KIND_WIND:
                switch (textColor) {
                    case "light":
                        imageId = R.drawable.weather_wind_mini_light;
                        break;

                    case "grey":
                        imageId = R.drawable.weather_wind_mini_grey;
                        break;

                    case "dark":
                        imageId = R.drawable.weather_wind_mini_dark;
                        break;
                }
                break;

            case KIND_SNOW:
                switch (textColor) {
                    case "light":
                        imageId = R.drawable.weather_snow_mini_light;
                        break;

                    case "grey":
                        imageId = R.drawable.weather_snow_mini_grey;
                        break;

                    case "dark":
                        imageId = R.drawable.weather_snow_mini_dark;
                        break;
                }
                break;

            case KIND_FOG:
                switch (textColor) {
                    case "light":
                        imageId = R.drawable.weather_fog_mini_light;
                        break;

                    case "grey":
                        imageId = R.drawable.weather_fog_mini_grey;
                        break;

                    case "dark":
                        imageId = R.drawable.weather_fog_mini_dark;
                        break;
                }
                break;

            case KIND_HAZE:
                switch (textColor) {
                    case "light":
                        imageId = R.drawable.weather_haze_mini_light;
                        break;

                    case "grey":
                        imageId = R.drawable.weather_haze_mini_grey;
                        break;

                    case "dark":
                        imageId = R.drawable.weather_haze_mini_dark;
                        break;
                }
                break;

            case KIND_SLEET:
                switch (textColor) {
                    case "light":
                        imageId = R.drawable.weather_sleet_mini_light;
                        break;

                    case "grey":
                        imageId = R.drawable.weather_sleet_mini_grey;
                        break;

                    case "dark":
                        imageId = R.drawable.weather_sleet_mini_dark;
                        break;
                }
                break;

            case KIND_THUNDERSTORM:
                switch (textColor) {
                    case "light":
                        imageId = R.drawable.weather_thunderstorm_mini_light;
                        break;

                    case "grey":
                        imageId = R.drawable.weather_thunderstorm_mini_grey;
                        break;

                    case "dark":
                        imageId = R.drawable.weather_thunderstorm_mini_dark;
                        break;
                }
                break;

            case KIND_THUNDER:
                switch (textColor) {
                    case "light":
                        imageId = R.drawable.weather_thunder_mini_light;
                        break;

                    case "grey":
                        imageId = R.drawable.weather_thunder_mini_grey;
                        break;

                    case "dark":
                        imageId = R.drawable.weather_thunder_mini_dark;
                        break;
                }
                break;

            case KIND_HAIL:
                switch (textColor) {
                    case "light":
                        imageId = R.drawable.weather_hail_mini_light;
                        break;

                    case "grey":
                        imageId = R.drawable.weather_hail_mini_grey;
                        break;

                    case "dark":
                        imageId = R.drawable.weather_hail_mini_dark;
                        break;
                }
                break;

            default:
                switch (textColor) {
                    case "light":
                        imageId = R.drawable.weather_cloudy_mini_light;
                        break;

                    case "grey":
                        imageId = R.drawable.weather_cloudy_mini_grey;
                        break;

                    case "dark":
                        imageId = R.drawable.weather_cloudy_mini_dark;
                        break;
                }
                break;
        }
        return imageId;
    }

    public static int getNotificationWeatherIcon(String weatherInfo, boolean dayTime) {
        switch (weatherInfo) {
            case KIND_CLEAR:
                if(dayTime) {
                    return R.drawable.weather_sun_day_xml;
                } else {
                    return R.drawable.weather_sun_night_xml;
                }

            case KIND_PARTLY_CLOUDY:
                if(dayTime) {
                    return R.drawable.weather_cloud_day_xml;
                } else {
                    return R.drawable.weather_cloud_night_xml;
                }

            case KIND_CLOUDY:
                return R.drawable.weather_cloudy_xml;

            case KIND_RAIN:
                return R.drawable.weather_rain_xml;

            case KIND_WIND:
                return R.drawable.weather_wind_xml;

            case KIND_SNOW:
                return R.drawable.weather_snow_xml;

            case KIND_FOG:
                return R.drawable.weather_fog_xml;

            case KIND_HAZE:
                return R.drawable.weather_haze_xml;

            case KIND_SLEET:
                return R.drawable.weather_sleet_xml;

            case KIND_THUNDERSTORM:
                return R.drawable.weather_thunderstorm_xml;

            case KIND_THUNDER:
                return R.drawable.weather_thunder_xml;

            case KIND_HAIL:
                return R.drawable.weather_hail_xml;

            default:
                return R.drawable.weather_cloudy_xml;
        }
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

    public static int getWidgetNotificationIcon(String weatherInfo, boolean dayTime,
                                                String iconStyle, String textColor) {
        if (iconStyle.equals("material")) {
            return getWeatherIcon(weatherInfo, dayTime)[3];
        } else {
            return getMiniWeatherIcon(weatherInfo, dayTime, textColor);
        }
    }

    public static int getWidgetNotificationIcon(String weatherInfo, boolean dayTime,
                                                String iconStyle, boolean darkText) {
        return getWidgetNotificationIcon(weatherInfo, dayTime, iconStyle, darkText ? "dark" : "light");
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

    public static int getShortcutForeground(String weatherInfo, boolean dayTime) {
        int imageId;
        switch (weatherInfo) {
            case KIND_CLEAR:
                if(dayTime) {
                    imageId = R.drawable.ic_shortcut_sun_day_foreground;
                } else {
                    imageId = R.drawable.ic_shortcut_sun_night_foreground;
                }
                break;

            case KIND_PARTLY_CLOUDY:
                if(dayTime) {
                    imageId = R.drawable.ic_shortcut_cloud_day_foreground;
                } else {
                    imageId = R.drawable.ic_shortcut_cloudy_foreground;
                }
                break;

            case KIND_CLOUDY:
                imageId = R.drawable.ic_shortcut_cloudy_foreground;
                break;

            case KIND_RAIN:
                imageId = R.drawable.ic_shortcut_rain_foreground;
                break;

            case KIND_WIND:
                imageId = R.drawable.ic_shortcut_wind_foreground;
                break;

            case KIND_SNOW:
                imageId = R.drawable.ic_shortcut_snow_foreground;
                break;

            case KIND_FOG:
                imageId = R.drawable.ic_shortcut_fog_foreground;
                break;

            case KIND_HAZE:
                imageId = R.drawable.ic_shortcut_haze_foreground;
                break;

            case KIND_SLEET:
                imageId = R.drawable.ic_shortcut_sleet_foreground;
                break;

            case KIND_THUNDERSTORM:
                imageId = R.drawable.ic_shortcut_thunder_foreground;
                break;

            case KIND_THUNDER:
                imageId = R.drawable.ic_shortcut_thunder_foreground;
                break;

            case KIND_HAIL:
                imageId = R.drawable.ic_shortcut_hail_foreground;
                break;

            default:
                imageId = R.drawable.ic_shortcut_cloudy_foreground;
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

    public static String getWindArrows(int degree) {
        if (degree < 0) {
            return "";
        }if (22.5 < degree && degree <= 67.5) {
            return "↙";
        } else if (67.5 < degree && degree <= 112.5) {
            return "←";
        } else if (112.5 < degree && degree <= 157.5) {
            return "↖";
        } else if (157.5 < degree && degree <= 202.5) {
            return "↑";
        } else if (202.5 < degree && degree <= 247.5) {
            return "↗";
        } else if (247.5 < degree && degree <= 292.5) {
            return "→";
        } else if (292. < degree && degree <= 337.5) {
            return "↘";
        } else {
            return "↓";
        }
    }

    @SuppressLint("DefaultLocale")
    public static String getWindSpeed(double speed) {
        return GeometricWeather.getInstance().isImperial()
                ? (String.format("%.1f", speed * 0.621F) + "mi/h") : (speed + "km/h");
    }

    public static String getWindSpeed(String speed) {
        if (speed.endsWith("km/h")) {
            return getWindSpeed(Double.parseDouble(speed.replace("km/h", "")));
        } else if (speed.endsWith("mi/h")) {
            return getWindSpeed(Double.parseDouble(speed.replace("mi/h", "")) * 1.6093);
        } else {
            try {
                return getWindSpeed(Double.parseDouble(speed));
            } catch (Exception e) {
                return speed;
            }
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

    // interface.

    public interface OnRequestWeatherListener {
        void requestWeatherSuccess(Weather weather, Location requestLocation);
        void requestWeatherFailed(Location requestLocation);
    }
}
