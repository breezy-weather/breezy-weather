package org.breezyweather.sources.china

import android.content.Context
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.exceptions.LocationSearchException
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherResultWrapper
import org.breezyweather.common.utils.LanguageUtils
import org.breezyweather.db.repositories.ChineseCityEntityRepository
import org.breezyweather.settings.SettingsManager
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.sources.china.json.ChinaForecastResult
import org.breezyweather.sources.china.json.ChinaMinutelyResult
import retrofit2.Retrofit
import javax.inject.Inject

class ChinaService @Inject constructor(
    client: Retrofit.Builder
) : HttpSource(), WeatherSource, LocationSearchSource, ReverseGeocodingSource {

    override val id = "china"
    override val name = "中国"
    override val privacyPolicyUrl = "https://privacy.mi.com/all/zh_CN"

    override val color = -0xa14472
    override val weatherAttribution = "北京天气、彩云天气、中国环境监测总站"
    override val locationSearchAttribution = "Wangdaye"

    private val mApi by lazy {
        client
            .baseUrl(CHINA_WEATHER_BASE_URL)
            .build()
            .create(ChinaApi::class.java)
    }

    private fun isConfigured(context: Context) = true

    override fun requestWeather(
        context: Context,
        location: Location
    ): Observable<WeatherResultWrapper> {
        val mainly = mApi.getForecastWeather(
            location.latitude.toDouble(),
            location.longitude.toDouble(),
            location.isCurrentPosition,
            locationKey = "weathercn%3A" + location.cityId,
            days = 15,
            appKey = "weather20151024",
            sign = "zUFJoAR2ZVrDy1vF3D07",
            isGlobal = false,
            SettingsManager.getInstance(context).language.code
        )
        val forecast = mApi.getMinutelyWeather(
            location.latitude.toDouble(),
            location.longitude.toDouble(),
            SettingsManager.getInstance(context).language.code,
            isGlobal = false,
            appKey = "weather20151024",
            locationKey = "weathercn%3A" + location.cityId,
            sign = "zUFJoAR2ZVrDy1vF3D07"
        )
        return Observable.zip(mainly, forecast) {
                mainlyResult: ChinaForecastResult,
                forecastResult: ChinaMinutelyResult
            ->
            convert(
                context,
                location,
                mainlyResult,
                forecastResult
            )
        }
    }

    override fun requestLocationSearch(
        context: Context, query: String
    ): Observable<List<Location>> {
        if (!LanguageUtils.isChinese(query)) {
            // TODO: We should probably be more precise, but since this provider must be rewritten
            // to use API search, this check won’t be necessary anymore
            return Observable.error(LocationSearchException())
        }
        return Observable.create { emitter ->
            ChineseCityEntityRepository.ensureChineseCityList(context)
            val locationList: MutableList<Location> = ArrayList()
            val cityList = ChineseCityEntityRepository.readChineseCityList(query)
            for (c in cityList) {
                locationList.add(c.toLocation())
            }
            emitter.onNext(locationList)
        }
    }

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location
    ): Observable<List<Location>> {
        val hasGeocodeInformation = location.hasGeocodeInformation()
        return Observable.create(
            ObservableOnSubscribe { emitter ->
                ChineseCityEntityRepository.ensureChineseCityList(context)
                val locationList: MutableList<Location> = ArrayList()
                if (hasGeocodeInformation) {
                    val chineseCity = ChineseCityEntityRepository.readChineseCity(
                        formatLocationString(convertChinese(location.province)),
                        formatLocationString(convertChinese(location.city)),
                        formatLocationString(convertChinese(location.district))
                    )
                    if (chineseCity != null) {
                        locationList.add(chineseCity.toLocation())
                    }
                }
                if (locationList.size > 0) {
                    emitter.onNext(locationList)
                    return@ObservableOnSubscribe
                }
                val chineseCity = ChineseCityEntityRepository.readChineseCity(
                    location.latitude, location.longitude
                )
                if (chineseCity != null) {
                    locationList.add(chineseCity.toLocation())
                }
                emitter.onNext(locationList)
            }
        )
    }

    companion object {
        private const val CHINA_WEATHER_BASE_URL = "https://weatherapi.market.xiaomi.com/wtr-v3/"

        protected fun formatLocationString(str: String?): String {
            if (str.isNullOrEmpty()) return ""
            if (str.endsWith("地区")) {
                return str.substring(0, str.length - 2)
            }
            if (str.endsWith("区")
                && !str.endsWith("新区")
                && !str.endsWith("矿区")
                && !str.endsWith("郊区")
                && !str.endsWith("风景区")
                && !str.endsWith("东区")
                && !str.endsWith("西区")
            ) {
                return str.substring(0, str.length - 1)
            }
            if (str.endsWith("县") && str.length != 2 && !str.endsWith("通化县")
                && !str.endsWith("本溪县")
                && !str.endsWith("辽阳县")
                && !str.endsWith("建平县")
                && !str.endsWith("承德县")
                && !str.endsWith("大同县")
                && !str.endsWith("五台县")
                && !str.endsWith("乌鲁木齐县")
                && !str.endsWith("伊宁县")
                && !str.endsWith("南昌县")
                && !str.endsWith("上饶县")
                && !str.endsWith("吉安县")
                && !str.endsWith("长沙县")
                && !str.endsWith("衡阳县")
                && !str.endsWith("邵阳县")
                && !str.endsWith("宜宾县")
            ) {
                return str.substring(0, str.length - 1)
            }
            if (str.endsWith("市")
                && !str.endsWith("新市")
                && !str.endsWith("沙市")
                && !str.endsWith("津市")
                && !str.endsWith("芒市")
                && !str.endsWith("西市")
                && !str.endsWith("峨眉山市")
            ) {
                return str.substring(0, str.length - 1)
            }
            if (str.endsWith("回族自治州")
                || str.endsWith("藏族自治州")
                || str.endsWith("彝族自治州")
                || str.endsWith("白族自治州")
                || str.endsWith("傣族自治州")
                || str.endsWith("蒙古自治州")
            ) {
                return str.substring(0, str.length - 5)
            }
            if (str.endsWith("朝鲜族自治州")
                || str.endsWith("哈萨克自治州")
                || str.endsWith("傈僳族自治州")
                || str.endsWith("蒙古族自治州")
            ) {
                return str.substring(0, str.length - 6)
            }
            if (str.endsWith("哈萨克族自治州")
                || str.endsWith("苗族侗族自治州")
                || str.endsWith("藏族羌族自治州")
                || str.endsWith("壮族苗族自治州")
                || str.endsWith("柯尔克孜自治州")
            ) {
                return str.substring(0, str.length - 7)
            }
            if (str.endsWith("布依族苗族自治州")
                || str.endsWith("土家族苗族自治州")
                || str.endsWith("蒙古族藏族自治州")
                || str.endsWith("柯尔克孜族自治州")
                || str.endsWith("傣族景颇族自治州")
                || str.endsWith("哈尼族彝族自治州")
            ) {
                return str.substring(0, str.length - 8)
            }
            if (str.endsWith("自治州")) {
                return str.substring(0, str.length - 3)
            }
            if (str.endsWith("省")) {
                return str.substring(0, str.length - 1)
            }
            if (str.endsWith("壮族自治区") || str.endsWith("回族自治区")) {
                return str.substring(0, str.length - 5)
            }
            if (str.endsWith("维吾尔自治区")) {
                return str.substring(0, str.length - 6)
            }
            if (str.endsWith("维吾尔族自治区")) {
                return str.substring(0, str.length - 7)
            }
            if (str.endsWith("自治区")) {
                return str.substring(0, str.length - 3)
            }
            return str
        }

        protected fun convertChinese(text: String?): String? {
            return try {
                LanguageUtils.traditionalToSimplified(text)
            } catch (e: Exception) {
                text
            }
        }
    }
}