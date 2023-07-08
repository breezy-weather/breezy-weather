package org.breezyweather.theme.resource.utils

import android.content.res.XmlResourceParser
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

object XmlHelper {
    @Throws(XmlPullParserException::class, IOException::class)
    fun getFilterMap(parser: XmlResourceParser): Map<String, String> {
        val map: MutableMap<String, String> = HashMap()
        var type = parser.eventType
        while (type != XmlPullParser.END_DOCUMENT) {
            if (type == XmlPullParser.START_TAG && Constants.FILTER_TAG_ITEM == parser.name) {
                map[parser.getAttributeValue(null, Constants.FILTER_TAG_NAME)] =
                    parser.getAttributeValue(null, Constants.FILTER_TAG_VALUE)
            }
            type = parser.next()
        }
        return map
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun getConfig(parser: XmlResourceParser): Config {
        val config = Config()
        var type = parser.eventType
        while (type != XmlPullParser.END_DOCUMENT) {
            if (type == XmlPullParser.START_TAG && Constants.FILTER_TAG_CONFIG == parser.name) {
                config.hasWeatherIcons = parser.getAttributeBooleanValue(
                    null,
                    Constants.CONFIG_HAS_WEATHER_ICONS,
                    config.hasWeatherIcons
                )
                config.hasWeatherAnimators = parser.getAttributeBooleanValue(
                    null,
                    Constants.CONFIG_HAS_WEATHER_ANIMATORS,
                    config.hasWeatherIcons
                )
                config.hasMinimalIcons = parser.getAttributeBooleanValue(
                    null,
                    Constants.CONFIG_HAS_MINIMAL_ICONS,
                    config.hasMinimalIcons
                )
                config.hasShortcutIcons = parser.getAttributeBooleanValue(
                    null,
                    Constants.CONFIG_HAS_SHORTCUT_ICONS,
                    config.hasShortcutIcons
                )
                config.hasSunMoonDrawables = parser.getAttributeBooleanValue(
                    null,
                    Constants.CONFIG_HAS_SUN_MOON_DRAWABLES,
                    config.hasSunMoonDrawables
                )
            }
            type = parser.next()
        }
        return config
    }
}
