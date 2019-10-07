package wangdaye.com.geometricweather.resource;

import wangdaye.com.geometricweather.basic.model.weather.WeatherCode;

public class Constants {

    public static final String ACTION_ICON_PROVIDER = "com.wangdaye.geometricweather.ICON_PROVIDER";
    public static final String CATEGORY_CHRONUS_ICON_PACK = "com.dvtonder.chronus.ICON_PACK";

    public static final String META_DATA_PROVIDER_CONFIG = "com.wangdaye.geometricweather.PROVIDER_CONFIG";
    public static final String META_DATA_DRAWABLE_FILTER = "com.wangdaye.geometricweather.DRAWABLE_FILTER";
    public static final String META_DATA_ANIMATOR_FILTER = "com.wangdaye.geometricweather.ANIMATOR_FILTER";
    public static final String META_DATA_SHORTCUT_FILTER = "com.wangdaye.geometricweather.SHORTCUT_FILTER";
    public static final String META_DATA_SUN_MOON_FILTER = "com.wangdaye.geometricweather.SUN_MOON_FILTER";

    private static final String RESOURCES_CLEAR = "weather_clear";
    private static final String RESOURCES_PARTLY_CLOUDY = "weather_partly_cloudy";
    private static final String RESOURCES_CLOUDY = "weather_cloudy";
    private static final String RESOURCES_RAIN = "weather_rain";
    private static final String RESOURCES_SNOW = "weather_snow";
    private static final String RESOURCES_WIND = "weather_wind";
    private static final String RESOURCES_FOG = "weather_fog";
    private static final String RESOURCES_HAZE = "weather_haze";
    private static final String RESOURCES_SLEET = "weather_sleet";
    private static final String RESOURCES_HAIL = "weather_hail";
    private static final String RESOURCES_THUNDER = "weather_thunder";
    private static final String RESOURCES_THUNDERSTORM = "weather_thunderstorm";

    public static final String RESOURCES_SUN = "sun";
    public static final String RESOURCES_MOON = "moon";

    private static final String SHORTCUTS_CLEAR = "shortcuts_clear";
    private static final String SHORTCUTS_PARTLY_CLOUDY = "shortcuts_partly_cloudy";
    private static final String SHORTCUTS_CLOUDY = "shortcuts_cloudy";
    private static final String SHORTCUTS_RAIN = "shortcuts_rain";
    private static final String SHORTCUTS_SNOW = "shortcuts_snow";
    private static final String SHORTCUTS_WIND = "shortcuts_wind";
    private static final String SHORTCUTS_FOG = "shortcuts_fog";
    private static final String SHORTCUTS_HAZE = "shortcuts_haze";
    private static final String SHORTCUTS_SLEET = "shortcuts_sleet";
    private static final String SHORTCUTS_HAIL = "shortcuts_hail";
    private static final String SHORTCUTS_THUNDER = "shortcuts_thunder";
    private static final String SHORTCUTS_THUNDERSTORM = "shortcuts_thunderstorm";

    public static final String DAY = "day";
    public static final String NIGHT = "night";
    public static final String MINI = "mini";
    public static final String LIGHT = "light";
    public static final String GREY = "grey";
    public static final String DARK = "dark";
    public static final String XML = "xml";
    public static final String FOREGROUND = "foreground";
    public static final String SEPARATOR = "_";

    static final String FILTER_TAG_ITEM = "item";
    static final String FILTER_TAG_NAME = "name";
    static final String FILTER_TAG_VALUE = "value";
    static final String FILTER_TAG_CONFIG = "config";

    static final String CONFIG_HAS_WEATHER_ICONS = "hasWeatherIcons";
    static final String CONFIG_HAS_WEATHER_ANIMATORS = "hasWeatherAnimators";
    static final String CONFIG_HAS_MINIMAL_ICONS = "hasMinimalIcons";
    static final String CONFIG_HAS_SHORTCUT_ICONS = "hasShortcutIcons";
    static final String CONFIG_HAS_SUN_MOON_DRAWABLES = "hasSunMoonDrawables";

    public static String getResourcesName(WeatherCode code) {
        switch (code) {
            case CLEAR:
                return RESOURCES_CLEAR;

            case PARTLY_CLOUDY:
                return RESOURCES_PARTLY_CLOUDY;

            case CLOUDY:
                return RESOURCES_CLOUDY;

            case RAIN:
                return RESOURCES_RAIN;

            case SNOW:
                return RESOURCES_SNOW;

            case WIND:
                return RESOURCES_WIND;

            case FOG:
                return RESOURCES_FOG;

            case HAZE:
                return RESOURCES_HAZE;

            case SLEET:
                return RESOURCES_SLEET;

            case HAIL:
                return RESOURCES_HAIL;

            case THUNDER:
                return RESOURCES_THUNDER;

            case THUNDERSTORM:
                return RESOURCES_THUNDERSTORM;
        }
        return RESOURCES_CLOUDY;
    }

    public static String getShortcutsName(WeatherCode code) {
        switch (code) {
            case CLEAR:
                return SHORTCUTS_CLEAR;

            case PARTLY_CLOUDY:
                return SHORTCUTS_PARTLY_CLOUDY;

            case CLOUDY:
                return SHORTCUTS_CLOUDY;

            case RAIN:
                return SHORTCUTS_RAIN;

            case SNOW:
                return SHORTCUTS_SNOW;

            case WIND:
                return SHORTCUTS_WIND;

            case FOG:
                return SHORTCUTS_FOG;

            case HAZE:
                return SHORTCUTS_HAZE;

            case SLEET:
                return SHORTCUTS_SLEET;

            case HAIL:
                return SHORTCUTS_HAIL;

            case THUNDER:
                return SHORTCUTS_THUNDER;

            case THUNDERSTORM:
                return SHORTCUTS_THUNDERSTORM;
        }
        return SHORTCUTS_CLOUDY;
    }
}
