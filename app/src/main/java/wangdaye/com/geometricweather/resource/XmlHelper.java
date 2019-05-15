package wangdaye.com.geometricweather.resource;

import android.content.res.XmlResourceParser;

import androidx.annotation.NonNull;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class XmlHelper {

    @NonNull
    public static Map<String, String> getFilterMap(XmlResourceParser parser)
            throws XmlPullParserException, IOException {
        Map<String, String> map = new HashMap<>();

        for (int type = parser.getEventType(); type != XmlPullParser.END_DOCUMENT; type = parser.next()) {
            if (type == XmlPullParser.START_TAG && Constants.FILTER_TAG_ITEM.equals(parser.getName())) {
                map.put(
                        parser.getAttributeValue(null, Constants.FILTER_TAG_NAME),
                        parser.getAttributeValue(null, Constants.FILTER_TAG_VALUE)
                );
            }
        }

        return map;
    }

    @NonNull
    public static Config getConfig(XmlResourceParser parser)
            throws XmlPullParserException, IOException {
        Config config = new Config();

        for (int type = parser.getEventType(); type != XmlPullParser.END_DOCUMENT; type = parser.next()) {
            if (type == XmlPullParser.START_TAG && Constants.FILTER_TAG_CONFIG.equals(parser.getName())) {
                config.hasWeatherIcons = parser.getAttributeBooleanValue(
                        null,
                        Constants.CONFIG_HAS_WEATHER_ICONS,
                        config.hasWeatherIcons
                );

                config.hasWeatherAnimators = parser.getAttributeBooleanValue(
                        null,
                        Constants.CONFIG_HAS_WEATHER_ANIMATORS,
                        config.hasWeatherIcons
                );

                config.hasMinimalIcons = parser.getAttributeBooleanValue(
                        null,
                        Constants.CONFIG_HAS_MINIMAL_ICONS,
                        config.hasMinimalIcons
                );

                config.hasShortcutIcons = parser.getAttributeBooleanValue(
                        null,
                        Constants.CONFIG_HAS_SHORTCUT_ICONS,
                        config.hasShortcutIcons
                );

                config.hasSunMoonDrawables = parser.getAttributeBooleanValue(
                        null,
                        Constants.CONFIG_HAS_SUN_MOON_DRAWABLES,
                        config.hasSunMoonDrawables
                );
            }
        }

        return config;
    }
}
