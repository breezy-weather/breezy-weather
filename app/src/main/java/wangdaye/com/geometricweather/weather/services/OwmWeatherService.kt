package wangdaye.com.geometricweather.weather.services

import android.content.Context
import android.text.TextUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.weather.Weather
import wangdaye.com.geometricweather.settings.ConfigStore
import wangdaye.com.geometricweather.settings.SettingsManager
import wangdaye.com.geometricweather.weather.apis.OwmApi
import wangdaye.com.geometricweather.weather.converters.OwmResultConverter
import java.util.*
import javax.inject.Inject

/**
 * Owm weather service.
 */
class OwmWeatherService @Inject constructor(@ApplicationContext context: Context,
                                            private val api: OwmApi) : WeatherService() {

    companion object {
        private const val CONFIG_NAME_LOCAL = "LOCAL_PREFERENCE_OWM"
        private const val KEY_OLD_DISTRICT = "OLD_DISTRICT"
        private const val KEY_OLD_CITY = "OLD_CITY"
        private const val KEY_OLD_PROVINCE = "OLD_PROVINCE"
        private const val KEY_OLD_KEY = "OLD_KEY"
    }

    private val config = ConfigStore.getInstance(context, CONFIG_NAME_LOCAL)

    override suspend fun getWeather(context: Context, location: Location): Weather? = coroutineScope {
        val languageCode = SettingsManager.getInstance(context).getLanguage().code
        val oneCall = async {
            api.getOneCall(
                    SettingsManager.getInstance(context).getProviderOwmKey(true),
                    location.latitude.toDouble(),
                    location.longitude.toDouble(),
                    "metric",
                    languageCode
            )
        }
        val airPollutionCurrent = async {
            try {
                api.getAirPollutionCurrent(
                        SettingsManager.getInstance(context).getProviderOwmKey(true),
                        location.latitude.toDouble(),
                        location.longitude.toDouble()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        val airPollutionForecast = async {
            try {
                api.getAirPollutionForecast(
                        SettingsManager.getInstance(context).getProviderOwmKey(true),
                        location.latitude.toDouble(),
                        location.longitude.toDouble()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        return@coroutineScope OwmResultConverter.convert(
                context,
                location,
                oneCall.await(),
                airPollutionCurrent.await(),
                airPollutionForecast.await()
        ).result
    }

    override suspend fun getLocation(context: Context, query: String): List<Location> = coroutineScope {
        val resultList = api.callWeatherLocation(SettingsManager.getInstance(context).getProviderOwmKey(true), query)

        val zipCode = if (query.matches(Regex("[a-zA-Z0-9]*"))) {
            query
        } else {
            null
        }

        val locationList: MutableList<Location> = ArrayList()
        for (r in resultList) {
            locationList.add(OwmResultConverter.convert(null, r, zipCode))
        }
        return@coroutineScope locationList
    }

    override suspend fun getLocation(context: Context, location: Location): List<Location> = coroutineScope {
        val oldDistrict = config.getString(KEY_OLD_DISTRICT, "")
        val oldCity = config.getString(KEY_OLD_CITY, "")
        val oldProvince = config.getString(KEY_OLD_PROVINCE, "")
        val oldKey = config.getString(KEY_OLD_KEY, "")

        if (location.hasGeocodeInformation()
                && queryEqualsIgnoreEmpty(location.district, oldDistrict)
                && queryEquals(location.city, oldCity)
                && queryEquals(location.province, oldProvince)
                && queryEquals(location.cityId, oldKey)) {
            val list = ArrayList<Location>()
            list.add(location)
            return@coroutineScope list
        }

        config.edit()
                .putString(KEY_OLD_DISTRICT, location.district)
                .putString(KEY_OLD_CITY, location.city)
                .putString(KEY_OLD_PROVINCE, location.province)
                .apply()


        val result = api.getWeatherLocationByGeoPosition(
                SettingsManager.getInstance(context).getProviderOwmKey(true),
                location.latitude.toDouble(),
                location.longitude.toDouble()
        )

        val locationList = ArrayList<Location>()
        locationList.add(OwmResultConverter.convert(location, result[0], null))

        if (locationList.isEmpty()) {
            config.edit()
                    .putString(KEY_OLD_DISTRICT, "")
                    .putString(KEY_OLD_CITY, "")
                    .putString(KEY_OLD_PROVINCE, "")
                    .putString(KEY_OLD_KEY, "")
                    .apply()
        } else if (!TextUtils.isEmpty(locationList[0].cityId)) {
            config.edit()
                    .putString(KEY_OLD_KEY, locationList[0].cityId)
                    .apply()
        }

        return@coroutineScope locationList
    }

    private fun queryEquals(a: String?, b: String?): Boolean {
        return if (!TextUtils.isEmpty(a) && !TextUtils.isEmpty(b)) {
            a == b
        } else {
            false
        }
    }

    private fun queryEqualsIgnoreEmpty(a: String?, b: String?): Boolean {
        if (TextUtils.isEmpty(a) && TextUtils.isEmpty(b)) {
            return true
        }
        return queryEquals(a, b)
    }
}