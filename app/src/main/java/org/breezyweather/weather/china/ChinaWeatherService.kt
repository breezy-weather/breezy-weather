package org.breezyweather.weather.china

import android.content.Context
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.rxjava.ApiObserver
import org.breezyweather.common.rxjava.ObserverContainer
import org.breezyweather.common.rxjava.SchedulerTransformer
import org.breezyweather.common.utils.LanguageUtils
import org.breezyweather.db.repositories.ChineseCityEntityRepository.ensureChineseCityList
import org.breezyweather.db.repositories.ChineseCityEntityRepository.readChineseCity
import org.breezyweather.db.repositories.ChineseCityEntityRepository.readChineseCityList
import org.breezyweather.main.utils.RequestErrorType
import org.breezyweather.settings.SettingsManager
import org.breezyweather.weather.WeatherService
import org.breezyweather.weather.china.json.ChinaForecastResult
import org.breezyweather.weather.china.json.ChinaMinutelyResult
import javax.inject.Inject

class ChinaWeatherService @Inject constructor(
    private val mApi: ChinaApi,
    private val mCompositeDisposable: CompositeDisposable
) : WeatherService() {
    override fun isConfigured(context: Context) = true

    override fun requestWeather(
        context: Context,
        location: Location,
        callback: RequestWeatherCallback
    ) {
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
        Observable.zip(mainly, forecast) {
            mainlyResult: ChinaForecastResult,
            forecastResult: ChinaMinutelyResult
            ->
            convert(
                context,
                location,
                mainlyResult,
                forecastResult
            )
        }.compose(SchedulerTransformer.create())
            .subscribe(ObserverContainer(mCompositeDisposable, object : ApiObserver<WeatherResultWrapper>() {
                override fun onSucceed(t: WeatherResultWrapper) {
                    if (t.result != null) {
                        callback.requestWeatherSuccess(location.copy(weather = t.result))
                    } else {
                        onFailed()
                    }
                }

                override fun onFailed() {
                    callback.requestWeatherFailed(location, RequestErrorType.WEATHER_REQ_FAILED)
                }
            }))
    }

    override fun requestLocationSearch(context: Context, query: String): List<Location> {
        if (!LanguageUtils.isChinese(query)) {
            return ArrayList()
        }
        ensureChineseCityList(context)
        val locationList: MutableList<Location> = ArrayList()
        val cityList = readChineseCityList(query)
        for (c in cityList) {
            locationList.add(c.toLocation())
        }
        return locationList
    }

    override fun requestReverseLocationSearch(
        context: Context,
        location: Location,
        callback: RequestLocationCallback
    ) {
        val hasGeocodeInformation = location.hasGeocodeInformation()
        Observable.create(
            ObservableOnSubscribe<List<Location>> { emitter ->
                ensureChineseCityList(context)
                val locationList: MutableList<Location> = ArrayList()
                if (hasGeocodeInformation) {
                    val chineseCity = readChineseCity(
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
                val chineseCity = readChineseCity(
                    location.latitude, location.longitude
                )
                if (chineseCity != null) {
                    locationList.add(chineseCity.toLocation())
                }
                emitter.onNext(locationList)
            }
        ).compose(SchedulerTransformer.create())
            .subscribe(ObserverContainer(mCompositeDisposable, object : ApiObserver<List<Location>>() {
                override fun onSucceed(t: List<Location>) {
                    if (t.isNotEmpty()) {
                        callback.requestLocationSuccess(location.formattedId, t)
                    } else {
                        onFailed()
                    }
                }

                override fun onFailed() {
                    callback.requestLocationFailed(location.formattedId, RequestErrorType.REVERSE_GEOCODING_FAILED)
                }
            }))
    }

    override fun cancel() {
        mCompositeDisposable.clear()
    }

    companion object {
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