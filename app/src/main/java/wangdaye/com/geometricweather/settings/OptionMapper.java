package wangdaye.com.geometricweather.settings;

import wangdaye.com.geometricweather.basic.model.option.CardOrder;
import wangdaye.com.geometricweather.basic.model.option.DarkMode;
import wangdaye.com.geometricweather.basic.model.option.Language;
import wangdaye.com.geometricweather.basic.model.option.NotificationStyle;
import wangdaye.com.geometricweather.basic.model.option.NotificationTextColor;
import wangdaye.com.geometricweather.basic.model.option.UIStyle;
import wangdaye.com.geometricweather.basic.model.option.UpdateInterval;
import wangdaye.com.geometricweather.basic.model.option.provider.LocationProvider;
import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.basic.model.option.unit.DistanceUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.PrecipitationUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.PressureUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.SpeedUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;

/**
 * Option mapper.
 * */
public class OptionMapper {

    public static UpdateInterval getUpdateInterval(String value) {
        switch (value) {
            case "0:30":
                return UpdateInterval.INTERVAL_0_30;

            case "1:00":
                return UpdateInterval.INTERVAL_1_00;

            case "2:00":
                return UpdateInterval.INTERVAL_2_00;

            case "2:30":
                return UpdateInterval.INTERVAL_2_30;

            case "3:00":
                return UpdateInterval.INTERVAL_3_00;

            case "3:30":
                return UpdateInterval.INTERVAL_3_30;

            case "4:00":
                return UpdateInterval.INTERVAL_4_00;

            default:
                return UpdateInterval.INTERVAL_1_30;

        }
    }

    public static WeatherSource getWeatherSource(String value) {
        switch (value) {
            case "cn":
                return WeatherSource.CN;

            case "caiyun":
                return WeatherSource.CAIYUN;

            default:
                return WeatherSource.ACCU;
        }
    }

    public static LocationProvider getLocationProvider(String value) {
        switch (value) {
            case "baidu_ip":
                return LocationProvider.BAIDU_IP;

            case "baidu":
                return LocationProvider.BAIDU;

            case "amap":
                return LocationProvider.AMAP;

            default:
                return LocationProvider.NATIVE;
        }
    }

    public static DarkMode getDarkMode(String value) {
        switch (value) {
            case "light":
                return DarkMode.LIGHT;

            case "dark":
                return DarkMode.DARK;

            default:
                return DarkMode.AUTO;
        }
    }
    
    public static TemperatureUnit getTemperatureUnit(String value) {
        switch (value) {
            case "f":
                return TemperatureUnit.F;
                
            default:
                return TemperatureUnit.C;
        }
    }

    public static DistanceUnit getDistanceUnit(String value) {
        switch (value) {
            case "m":
                return DistanceUnit.M;

            case "mi":
                return DistanceUnit.MI;

            case "nmi":
                return DistanceUnit.NMI;

            case "ft":
                return DistanceUnit.FT;

            default:
                return DistanceUnit.KM;
        }
    }

    public static PrecipitationUnit getPrecipitationUnit(String value) {
        switch (value) {
            case "lpsqm":
                return PrecipitationUnit.LPSQM;

            default:
                return PrecipitationUnit.MM;
        }
    }

    public static PressureUnit getPressureUnit(String value) {
        switch (value) {
            case "kpa":
                return PressureUnit.KPA;

            case "hpa":
                return PressureUnit.HPA;

            case "atm":
                return PressureUnit.ATM;

            case "mmhg":
                return PressureUnit.MMHG;

            case "inhg":
                return PressureUnit.INHG;

            case "kgfpsqcm":
                return PressureUnit.KGFPSQCM;

            default:
                return PressureUnit.MB;
        }
    }

    public static SpeedUnit getSpeedUnit(String value) {
        switch (value) {
            case "mps":
                return SpeedUnit.MPS;

            case "kn":
                return SpeedUnit.KN;

            case "mph":
                return SpeedUnit.MPH;

            case "ftps":
                return SpeedUnit.FTPS;

            default:
                return SpeedUnit.KPH;
        }
    }
    
    public static UIStyle getUIStyle(String value) {
        switch (value) {
            case  "circular":
                return UIStyle.CIRCULAR;
                
            default:
                return UIStyle.MATERIAL;
        }
    }

    public static CardOrder getCardOrder(String value) {
        switch (value) {
            case "hourly_first":
                return CardOrder.HOURLY_FIRST;

            default:
                return CardOrder.DAILY_FIRST;
        }
    }
    
    public static Language getLanguage(String value) {
        switch (value) {
            case "chinese":
                return Language.CHINESE;

            case "unsimplified_chinese":
                return Language.UNSIMPLIFIED_CHINESE;

            case "english_america":
                return Language.ENGLISH_US;

            case "english_britain":
                return Language.ENGLISH_UK;

            case "english_australia":
                return Language.ENGLISH_AU;

            case "turkish":
                return Language.TURKISH;

            case "french":
                return Language.FRENCH;

            case "russian":
                return Language.RUSSIAN;

            case "german":
                return Language.GERMAN;

            case "serbian":
                return Language.SERBIAN;

            case "spanish":
                return Language.SPANISH;

            case "italian":
                return Language.ITALIAN;

            case "dutch":
                return Language.DUTCH;

            case "hungarian":
                return Language.HUNGARIAN;

            case "portuguese":
                return Language.PORTUGUESE;

            case "portuguese_brazilian":
                return Language.PORTUGUESE_BR;

            case "slovenian":
                return Language.SLOVENIAN;

            default:
                return Language.FOLLOW_SYSTEM;
        }
    }

    public static NotificationStyle getNotificationStyle(String value) {
        switch (value) {
            case "native":
                return NotificationStyle.NATIVE;

            default:
                return NotificationStyle.CUSTOM;
        }
    }

    public static NotificationTextColor getNotificationTextColor(String value) {
        switch (value) {
            case "light":
                return NotificationTextColor.LIGHT;

            case "grey":
                return NotificationTextColor.GREY;

            default:
                return NotificationTextColor.DARK;

        }
    }
}
