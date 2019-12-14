package wangdaye.com.geometricweather.settings;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.basic.model.option.DarkMode;
import wangdaye.com.geometricweather.basic.model.option.appearance.CardDisplay;
import wangdaye.com.geometricweather.basic.model.option.appearance.Language;
import wangdaye.com.geometricweather.basic.model.option.NotificationStyle;
import wangdaye.com.geometricweather.basic.model.option.NotificationTextColor;
import wangdaye.com.geometricweather.basic.model.option.appearance.UIStyle;
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

            case "k":
                return TemperatureUnit.K;
                
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
            case "in":
                return PrecipitationUnit.IN;

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

    @NonNull
    public static List<CardDisplay> getCardDisplayList(String value) {
        if (TextUtils.isEmpty(value)) {
            return new ArrayList<>();
        }
        try {
            String[] cards = value.split("&");

            List<CardDisplay> list = new ArrayList<>();
            for (String card : cards) {
                switch (card) {
                    case "daily_overview":
                        list.add(CardDisplay.CARD_DAILY_OVERVIEW);
                        break;

                    case "hourly_overview":
                        list.add(CardDisplay.CARD_HOURLY_OVERVIEW);
                        break;

                    case "air_quality":
                        list.add(CardDisplay.CARD_AIR_QUALITY);
                        break;

                    case "allergen":
                        list.add(CardDisplay.CARD_ALLERGEN);
                        break;

                    case "life_details":
                        list.add(CardDisplay.CARD_LIFE_DETAILS);
                        break;

                    case "sunrise_sunset":
                        list.add(CardDisplay.CARD_SUNRISE_SUNSET);
                        break;
                }
            }
            return list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @NonNull
    public static String getCardDisplayValue(@NonNull List<CardDisplay> list) {
        StringBuilder builder = new StringBuilder();
        for (CardDisplay v : list) {
            builder.append("&").append(v.getCardValue());
        }
        if (builder.length() > 0 && builder.charAt(0) == '&') {
            builder.deleteCharAt(0);
        }
        return builder.toString();
    }

    @NonNull
    public static String getCardDisplaySummary(Context context, @NonNull List<CardDisplay> list) {
        StringBuilder builder = new StringBuilder();
        for (CardDisplay v : list) {
            builder.append(",").append(v.getCardName(context));
        }
        if (builder.length() > 0 && builder.charAt(0) == ',') {
            builder.deleteCharAt(0);
        }
        return builder.toString().replace(",", ", ");
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
