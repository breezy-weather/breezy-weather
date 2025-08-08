/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.sources.jma

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.google.maps.android.data.geojson.GeoJsonFeature
import com.google.maps.android.data.geojson.GeoJsonMultiPolygon
import com.google.maps.android.data.geojson.GeoJsonParser
import com.google.maps.android.data.geojson.GeoJsonPolygon
import com.google.maps.android.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import okhttp3.OkHttpClient
import okhttp3.Request
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCalendarMonth
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.jma.json.JmaAlertResult
import org.breezyweather.sources.jma.json.JmaAmedasResult
import org.breezyweather.sources.jma.json.JmaAreasResult
import org.breezyweather.sources.jma.json.JmaBulletinResult
import org.breezyweather.sources.jma.json.JmaCurrentResult
import org.breezyweather.sources.jma.json.JmaDailyResult
import org.breezyweather.sources.jma.json.JmaForecastAreaResult
import org.breezyweather.sources.jma.json.JmaHourlyResult
import org.breezyweather.sources.jma.json.JmaWeekAreaResult
import org.json.JSONObject
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.floor
import kotlin.time.Duration.Companion.hours

class JmaService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
    private val okHttpClient: OkHttpClient,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource {
    override val id = "jma"
    override val name by lazy {
        if (context.currentLocale.code.startsWith("ja")) {
            "気象庁"
        } else {
            "JMA (${context.currentLocale.getCountryName("JP")})"
        }
    }
    override val continent = SourceContinent.ASIA
    override val privacyPolicyUrl by lazy {
        if (context.currentLocale.code.startsWith("ja")) {
            "https://www.jma.go.jp/jma/kishou/info/coment.html"
        } else {
            "https://www.jma.go.jp/jma/en/copyright.html"
        }
    }

    private val mApi by lazy {
        client
            .baseUrl(JMA_BASE_URL)
            .build()
            .create(JmaApi::class.java)
    }

    private val weatherAttribution by lazy {
        if (context.currentLocale.code.startsWith("ja")) {
            "気象庁"
        } else {
            "Japan Meteorological Agency"
        }
    }
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.NORMALS to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )
    override val attributionLinks = mapOf(
        weatherAttribution to "https://www.jma.go.jp/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("JP", ignoreCase = true)
    }

    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            isFeatureSupportedForLocation(location, feature) -> PRIORITY_HIGHEST
            else -> PRIORITY_NONE
        }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val parameters = location.parameters.getOrElse(id) { null }
        val class20s = parameters?.getOrElse("class20s") { null }
        val class10s = parameters?.getOrElse("class10s") { null }
        val prefArea = parameters?.getOrElse("prefArea") { null }
        val weekArea05 = parameters?.getOrElse("weekArea05") { null }
        val weekAreaAmedas = parameters?.getOrElse("weekAreaAmedas") { null }
        val forecastAmedas = parameters?.getOrElse("forecastAmedas") { null }
        val currentAmedas = parameters?.getOrElse("currentAmedas") { null }

        if (class20s.isNullOrEmpty() ||
            class10s.isNullOrEmpty() ||
            prefArea.isNullOrEmpty() ||
            weekArea05.isNullOrEmpty() ||
            weekAreaAmedas.isNullOrEmpty() ||
            forecastAmedas.isNullOrEmpty() ||
            currentAmedas.isNullOrEmpty()
        ) {
            return Observable.error(InvalidLocationException())
        }

        // Special case for Amami, Kagoshima Prefecture
        val forecastPrefArea = if (prefArea == "460040") {
            "460100"
        } else {
            prefArea
        }

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val daily = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getDaily(forecastPrefArea).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }
        val hourly = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getHourly(class10s).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(JmaHourlyResult())
            }
        } else {
            Observable.just(JmaHourlyResult())
        }

        // CURRENT
        // Need to first get the correct timestamp for latest observation data.
        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            val request = Request.Builder().url(JMA_BASE_URL + "bosai/amedas/data/latest_time.txt").build()
            val incomingFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH)
            val outgoingFormatter = SimpleDateFormat("yyyyMMdd_HH", Locale.ENGLISH)
            incomingFormatter.timeZone = TimeZone.getTimeZone("Asia/Tokyo")
            outgoingFormatter.timeZone = TimeZone.getTimeZone("Asia/Tokyo")

            okHttpClient.newCall(request).execute().use { call ->
                if (call.isSuccessful) {
                    val latestTime = incomingFormatter.parse(call.body.string())!!.time

                    // Observation data is recorded in 3-hourly files.
                    val timestamp = (
                        floor(latestTime.toDouble() / 3.hours.inWholeMilliseconds) *
                            3.hours.inWholeMilliseconds
                        ).toLong()
                    mApi.getCurrent(
                        amedas = currentAmedas,
                        timestamp = outgoingFormatter.format(timestamp)
                    ).onErrorResumeNext {
                        failedFeatures[SourceFeature.CURRENT] = it
                        Observable.just(emptyMap())
                    }
                } else {
                    failedFeatures[SourceFeature.CURRENT] = WeatherException()
                    Observable.just(emptyMap())
                }
            }
        } else {
            Observable.just(emptyMap())
        }

        val bulletin = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getBulletin(forecastPrefArea).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(JmaBulletinResult())
            }
        } else {
            Observable.just(JmaBulletinResult())
        }

        // ALERT
        val alert = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getAlert(prefArea).onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(JmaAlertResult())
            }
        } else {
            Observable.just(JmaAlertResult())
        }

        return Observable.zip(current, bulletin, daily, hourly, alert) {
                currentResult: Map<String, JmaCurrentResult>,
                bulletinResult: JmaBulletinResult,
                dailyResult: List<JmaDailyResult>,
                hourlyResult: JmaHourlyResult,
                alertResult: JmaAlertResult,
            ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(context, dailyResult, class10s, weekArea05, weekAreaAmedas, forecastAmedas)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(context, hourlyResult)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(context, currentResult, bulletinResult)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(context, alertResult, class20s)
                } else {
                    null
                },
                normals = if (SourceFeature.NORMALS in requestedFeatures) {
                    getNormals(dailyResult, weekAreaAmedas)?.let { normals ->
                        mapOf(
                            Date().getCalendarMonth(location) to normals
                        )
                    }
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    // Reverse geocoding
    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        val areas = mApi.getAreas()
        val class20s = mApi.getRelm().map { relm ->
            val features = mutableListOf<Any?>()
            relm.forEachIndexed { i, it ->
                if (latitude <= it.ne[0] &&
                    longitude <= it.ne[1] &&
                    latitude >= it.sw[0] &&
                    longitude >= it.sw[1]
                ) {
                    features.addAll(mApi.getClass20s(i).blockingFirst().features)
                }
            }
            features
        }

        return Observable.zip(areas, class20s) {
                areasResult: JmaAreasResult,
                class20sFeatures: List<Any?>,
            ->
            convertLocation(context, latitude, longitude, areasResult, class20sFeatures)
        }
    }

    // Reverse geocoding
    private fun convertLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
        areasResult: JmaAreasResult,
        class20sFeatures: List<Any?>,
    ): List<LocationAddressInfo> {
        val matchingLocations = getMatchingLocations(latitude, longitude, class20sFeatures)
        if (matchingLocations.isEmpty()) {
            throw InvalidLocationException()
        }
        val locationList = mutableListOf<LocationAddressInfo>()
        matchingLocations[0].let {
            val code = it.getProperty("code")
            if (code == null) {
                throw InvalidLocationException()
            }
            var city = if (context.currentLocale.code.startsWith("ja")) {
                areasResult.class20s?.get(code)?.name ?: ""
            } else {
                areasResult.class20s?.get(code)?.enName ?: ""
            }
            var district: String? = null

            // Split the city and district strings if necessary
            if (Regex("""^\d{6}[^0]$""").matches(code)) {
                if (context.currentLocale.code.startsWith("ja")) {
                    val matchResult = Regex("""^(.+[市町村])（?([^（^）]*)）?$""").find(city)!!
                    city = matchResult.groups[1]!!.value
                    district = matchResult.groups[2]!!.value
                    if (Regex("""を除く$""").matches(district)) {
                        district = null
                    }
                } else {
                    if (city.contains(",")) {
                        district = city.substringBefore(",").trim()
                        city = city.substringAfter(",").trim()
                    } else if (Regex("""^(Northern|Southern|Eastern|Western) """).matches(city)) {
                        district = city.substringBefore("ern ").trim() + "ern"
                        city = city.substringAfter("ern ").trim()
                    } else if (city.contains(" (")) {
                        district = city.substringAfter(" (").substringBefore(")").trim()
                        city = city.substringBefore(" (")
                    }
                }
            }
            locationList.add(
                LocationAddressInfo(
                    timeZoneId = "Asia/Tokyo",
                    countryCode = "JP",
                    admin1 = getPrefecture(context, code),
                    admin1Code = code.substring(0, 2),
                    city = city,
                    district = district
                )
            )
        }
        return locationList
    }

    private fun getPrefecture(
        context: Context,
        code: String,
    ): String? {
        if (context.currentLocale.code.startsWith("ja")) {
            return with(code) {
                when {
                    startsWith("01") -> "北海道" // Hokkaido
                    startsWith("02") -> "青森県" // Aomori
                    startsWith("03") -> "岩手県" // Iwate
                    startsWith("04") -> "宮城県" // Miyagi
                    startsWith("05") -> "秋田県" // Akita
                    startsWith("06") -> "山形県" // Yamagata
                    startsWith("07") -> "福島県" // Fukushima
                    startsWith("08") -> "茨城県" // Ibaraki
                    startsWith("09") -> "栃木県" // Tochigi
                    startsWith("10") -> "群馬県" // Gunma
                    startsWith("11") -> "埼玉県" // Saitama
                    startsWith("12") -> "千葉県" // Chiba
                    startsWith("13") -> "東京都" // Tōkyō
                    startsWith("14") -> "神奈川県" // Kanagawa
                    startsWith("15") -> "新潟県" // Niigata
                    startsWith("16") -> "富山県" // Toyama
                    startsWith("17") -> "石川県" // Ishikawa
                    startsWith("18") -> "福井県" // Fukui
                    startsWith("19") -> "山梨県" // Yamanashi
                    startsWith("20") -> "長野県" // Nagano
                    startsWith("21") -> "岐阜県" // Gifu
                    startsWith("22") -> "静岡県" // Shizuoka
                    startsWith("23") -> "愛知県" // Aichi
                    startsWith("24") -> "三重県" // Mie
                    startsWith("25") -> "滋賀県" // Shiga
                    startsWith("26") -> "京都府" // Kyōto
                    startsWith("27") -> "大阪府" // Ōsaka
                    startsWith("28") -> "兵庫県" // Hyōgo
                    startsWith("29") -> "奈良県" // Nara
                    startsWith("30") -> "和歌山県" // Wakayama
                    startsWith("31") -> "鳥取県" // Tottori
                    startsWith("32") -> "島根県" // Shimane
                    startsWith("33") -> "岡山県" // Okayama
                    startsWith("34") -> "広島県" // Hiroshima
                    startsWith("35") -> "山口県" // Yamaguchi
                    startsWith("36") -> "徳島県" // Tokushima
                    startsWith("37") -> "香川県" // Kagawa
                    startsWith("38") -> "愛媛県" // Ehime
                    startsWith("39") -> "高知県" // Kōchi
                    startsWith("40") -> "福岡県" // Fukuoka
                    startsWith("41") -> "佐賀県" // Saga
                    startsWith("42") -> "長崎県" // Nagasaki
                    startsWith("43") -> "熊本県" // Kumamoto
                    startsWith("44") -> "大分県" // Ōita
                    startsWith("45") -> "宮崎県" // Miyazaki
                    startsWith("46") -> "鹿児島県" // Kagoshima
                    startsWith("47") -> "沖縄県" // Okinawa
                    else -> null
                }
            }
        } else {
            return with(code) {
                when {
                    startsWith("01") -> "Hokkaido" // 北海道
                    startsWith("02") -> "Aomori" // 青森県
                    startsWith("03") -> "Iwate" // 岩手県
                    startsWith("04") -> "Miyagi" // 宮城県
                    startsWith("05") -> "Akita" // 秋田県
                    startsWith("06") -> "Yamagata" // 山形県
                    startsWith("07") -> "Fukushima" // 福島県
                    startsWith("08") -> "Ibaraki" // 茨城県
                    startsWith("09") -> "Tochigi" // 栃木県
                    startsWith("10") -> "Gunma" // 群馬県
                    startsWith("11") -> "Saitama" // 埼玉県
                    startsWith("12") -> "Chiba" // 千葉県
                    startsWith("13") -> "Tōkyō" // 東京都
                    startsWith("14") -> "Kanagawa" // 神奈川県
                    startsWith("15") -> "Niigata" // 新潟県
                    startsWith("16") -> "Toyama" // 富山県
                    startsWith("17") -> "Ishikawa" // 石川県
                    startsWith("18") -> "Fukui" // 福井県
                    startsWith("19") -> "Yamanashi" // 山梨県
                    startsWith("20") -> "Nagano" // 長野県
                    startsWith("21") -> "Gifu" // 岐阜県
                    startsWith("22") -> "Shizuoka" // 静岡県
                    startsWith("23") -> "Aichi" // 愛知県
                    startsWith("24") -> "Mie" // 三重県
                    startsWith("25") -> "Shiga" // 滋賀県
                    startsWith("26") -> "Kyōto" // 京都府
                    startsWith("27") -> "Ōsaka" // 大阪府
                    startsWith("28") -> "Hyōgo" // 兵庫県
                    startsWith("29") -> "Nara" // 奈良県
                    startsWith("30") -> "Wakayama" // 和歌山県
                    startsWith("31") -> "Tottori" // 鳥取県
                    startsWith("32") -> "Shimane" // 島根県
                    startsWith("33") -> "Okayama" // 岡山県
                    startsWith("34") -> "Hiroshima" // 広島県
                    startsWith("35") -> "Yamaguchi" // 山口県
                    startsWith("36") -> "Tokushima" // 徳島県
                    startsWith("37") -> "Kagawa" // 香川県
                    startsWith("38") -> "Ehime" // 愛媛県
                    startsWith("39") -> "Kōchi" // 高知県
                    startsWith("40") -> "Fukuoka" // 福岡県
                    startsWith("41") -> "Saga" // 佐賀県
                    startsWith("42") -> "Nagasaki" // 長崎県
                    startsWith("43") -> "Kumamoto" // 熊本県
                    startsWith("44") -> "Ōita" // 大分県
                    startsWith("45") -> "Miyazaki" // 宮崎県
                    startsWith("46") -> "Kagoshima" // 鹿児島県
                    startsWith("47") -> "Okinawa" // 沖縄県
                    else -> null
                }
            }
        }
    }

    private fun getMatchingLocations(
        latitude: Double,
        longitude: Double,
        class20sFeatures: List<Any?>,
    ): List<GeoJsonFeature> {
        val json = """{"type":"FeatureCollection","features":[${class20sFeatures.joinToString(",")}]}"""
        val geoJsonParser = GeoJsonParser(JSONObject(json))
        return geoJsonParser.features.filter { feature ->
            when (feature.geometry) {
                is GeoJsonPolygon -> (feature.geometry as GeoJsonPolygon).coordinates.any { polygon ->
                    PolyUtil.containsLocation(latitude, longitude, polygon, true)
                }
                is GeoJsonMultiPolygon -> (feature.geometry as GeoJsonMultiPolygon).polygons.any {
                    it.coordinates.any { polygon ->
                        PolyUtil.containsLocation(latitude, longitude, polygon, true)
                    }
                }
                else -> false
            }
        }
    }

    // Location parameters
    internal fun convertLocationParameters(
        location: Location,
        areasResult: JmaAreasResult,
        class20sFeatures: List<Any?>,
        weekAreaResult: Map<String, List<JmaWeekAreaResult>>,
        weekArea05Result: Map<String, List<String>>,
        forecastAreaResult: Map<String, List<JmaForecastAreaResult>>,
        amedasResult: Map<String, JmaAmedasResult>,
    ): Map<String, String> {
        val class20s: String
        val class15s: String
        val class10s: String
        val prefArea: String
        var weekArea05 = ""
        var weekAreaAmedas = ""
        var forecastAmedas = ""
        var currentAmedas = ""

        val matchingLocations = getMatchingLocations(location.latitude, location.longitude, class20sFeatures)
        if (matchingLocations.isEmpty()) {
            throw InvalidLocationException()
        }
        matchingLocations[0].let {
            class20s = it.getProperty("code") ?: ""
            class15s = areasResult.class20s?.get(class20s)?.parent ?: ""
            class10s = areasResult.class15s?.get(class15s)?.parent ?: ""
            prefArea = areasResult.class10s?.get(class10s)?.parent ?: ""
        }

        weekArea05Result.getOrElse(class10s) { null }?.forEach { wa5 ->
            weekAreaResult.getOrElse(prefArea) { null }?.forEach { wa ->
                if (wa.week == wa5) {
                    weekArea05 = wa5
                    weekAreaAmedas = wa.amedas
                }
            }
        }

        forecastAreaResult.getOrElse(prefArea) { null }?.forEach { fa ->
            if (fa.class10 == class10s) {
                forecastAmedas = fa.amedas.joinToString(",")
            }
        }

        var nearestDistance = Double.POSITIVE_INFINITY
        var distance: Double
        amedasResult.keys.forEach { key ->
            amedasResult[key]?.let {
                distance = SphericalUtil.computeDistanceBetween(
                    LatLng(location.latitude, location.longitude),
                    LatLng(
                        it.lat.getOrElse(0) { 0.0 } + it.lat.getOrElse(1) { 0.0 } / 60.0,
                        it.lon.getOrElse(0) { 0.0 } + it.lon.getOrElse(1) { 0.0 } / 60.0
                    )
                )
                if (distance < nearestDistance) {
                    if (it.elems.substring(0, 1) == "1") {
                        nearestDistance = distance
                        currentAmedas = key
                    }
                }
            }
        }

        return mapOf(
            "class20s" to class20s,
            "class10s" to class10s,
            "prefArea" to prefArea,
            "weekArea05" to weekArea05,
            "weekAreaAmedas" to weekAreaAmedas,
            "forecastAmedas" to forecastAmedas,
            "currentAmedas" to currentAmedas
        )
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        val parameters = location.parameters.getOrElse(id) { null }

        val class20s = parameters?.getOrElse("class20s") { null }
        val class10s = parameters?.getOrElse("class10s") { null }
        val prefArea = parameters?.getOrElse("prefArea") { null }
        val weekArea05 = parameters?.getOrElse("weekArea05") { null }
        val weekAreaAmedas = parameters?.getOrElse("weekAreaAmedas") { null }
        val forecastAmedas = parameters?.getOrElse("forecastAmedas") { null }
        val currentAmedas = parameters?.getOrElse("currentAmedas") { null }

        return class20s.isNullOrEmpty() ||
            class10s.isNullOrEmpty() ||
            prefArea.isNullOrEmpty() ||
            weekArea05.isNullOrEmpty() ||
            weekAreaAmedas.isNullOrEmpty() ||
            forecastAmedas.isNullOrEmpty() ||
            currentAmedas.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        val areas = mApi.getAreas()
        val class20s = mApi.getRelm().map { relm ->
            val features = mutableListOf<Any?>()
            relm.forEachIndexed { i, it ->
                if (location.latitude <= it.ne[0] &&
                    location.longitude <= it.ne[1] &&
                    location.latitude >= it.sw[0] &&
                    location.longitude >= it.sw[1]
                ) {
                    features.addAll(mApi.getClass20s(i).blockingFirst().features)
                }
            }
            features
        }
        val weekArea = mApi.getWeekArea()
        val weekArea05 = mApi.getWeekArea05()
        val forecastArea = mApi.getForecastArea()
        val amedas = mApi.getAmedas()

        return Observable.zip(areas, class20s, weekArea, weekArea05, forecastArea, amedas) {
                areasResult: JmaAreasResult,
                class20sFeatures: List<Any?>,
                weekAreaResult: Map<String, List<JmaWeekAreaResult>>,
                weekArea05Result: Map<String, List<String>>,
                forecastAreaResult: Map<String, List<JmaForecastAreaResult>>,
                amedasResult: Map<String, JmaAmedasResult>,
            ->
            convertLocationParameters(
                location = location,
                areasResult = areasResult,
                class20sFeatures = class20sFeatures,
                weekAreaResult = weekAreaResult,
                weekArea05Result = weekArea05Result,
                forecastAreaResult = forecastAreaResult,
                amedasResult = amedasResult
            )
        }
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val JMA_BASE_URL = "https://www.jma.go.jp/"
    }
}
