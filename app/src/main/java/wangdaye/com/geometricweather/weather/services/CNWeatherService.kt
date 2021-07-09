package wangdaye.com.geometricweather.weather.services

import android.content.Context
import kotlinx.coroutines.coroutineScope
import wangdaye.com.geometricweather.common.basic.models.ChineseCity.CNWeatherSource
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.weather.Weather
import wangdaye.com.geometricweather.common.utils.LanguageUtils
import wangdaye.com.geometricweather.db.DatabaseHelper
import wangdaye.com.geometricweather.weather.apis.CNWeatherApi
import wangdaye.com.geometricweather.weather.converters.CNResultConverter
import java.util.*
import javax.inject.Inject

/**
 * CN weather service.
 */
open class CNWeatherService @Inject constructor(private val api: CNWeatherApi) : WeatherService() {

    open val source: CNWeatherSource
        get() = CNWeatherSource.CN

    override suspend fun getWeather(context: Context, location: Location): Weather? = coroutineScope {
        CNResultConverter.convert(
                context,
                location,
                api.getWeather(location.cityId)
        ).result
    }

    override suspend fun getLocation(context: Context, query: String): List<Location> = coroutineScope {
        if (!LanguageUtils.isChinese(query)) {
            return@coroutineScope ArrayList()
        }

        DatabaseHelper.getInstance(context).ensureChineseCityList(context)
        val cityList = DatabaseHelper.getInstance(context).readChineseCityList(query)

        val locationList = ArrayList<Location>()
        for (c in cityList) {
            locationList.add(c.toLocation(source))
        }
        return@coroutineScope locationList
    }

    override suspend fun getLocation(context: Context, location: Location): List<Location> = coroutineScope {
        val hasGeocodeInformation = location.hasGeocodeInformation()

        DatabaseHelper.getInstance(context).ensureChineseCityList(context)

        val locationList = ArrayList<Location>()

        if (hasGeocodeInformation) {
            DatabaseHelper.getInstance(context).readChineseCity(
                    formatLocationString(convertChinese(location.province)),
                    formatLocationString(convertChinese(location.city)),
                    formatLocationString(convertChinese(location.district))
            )?.let {
                locationList.add(it.toLocation(source))
            }
        }
        if (locationList.isNotEmpty()) {
            return@coroutineScope locationList
        }

        DatabaseHelper.getInstance(context).readChineseCity(
                location.latitude, location.longitude
        )?.let {
            locationList.add(it.toLocation(source))
        }
        return@coroutineScope locationList
    }
}