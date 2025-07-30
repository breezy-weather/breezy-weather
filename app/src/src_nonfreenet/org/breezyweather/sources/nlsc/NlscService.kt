package org.breezyweather.sources.nlsc

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import com.google.maps.android.model.LatLng
import com.google.maps.android.model.LatLngBounds
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import retrofit2.Retrofit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class NlscService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("XmlClient") xmlClient: Retrofit.Builder,
) : HttpSource(), ReverseGeocodingSource {
    override val id = "nlsc"
    override val name by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("zh") -> "內政部國土測繪中心"
                else -> "NLSC"
            }
        } +
            " (${Locale(context.currentLocale.code, "TW").displayCountry})"
    }
    override val continent = SourceContinent.ASIA
    override val privacyPolicyUrl = "https://www.nlsc.gov.tw/cp.aspx?n=1633"

    val mNlscApi by lazy {
        xmlClient
            .baseUrl(NLSC_BASE_URL)
            .build()
            .create(NlscApi::class.java)
    }

    private val reverseGeocodingAttribution by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("zh") -> "內政部國土測繪中心"
                else -> "National Land Survey and Mapping Center"
            }
        }
    }
    override val supportedFeatures = mapOf(
        SourceFeature.REVERSE_GEOCODING to reverseGeocodingAttribution
    )
    override val attributionLinks = mapOf(
        reverseGeocodingAttribution to "https://www.nlsc.gov.tw/"
    )

    override fun isFeatureSupportedForLocation(location: Location, feature: SourceFeature): Boolean {
        val latLng = LatLng(location.latitude, location.longitude)
        return location.countryCode.equals("TW", ignoreCase = true) ||
            TAIWAN_BBOX.contains(latLng) ||
            PENGHU_BBOX.contains(latLng) ||
            KINMEN_BBOX.contains(latLng) ||
            WUQIU_BBOX.contains(latLng) ||
            MATSU_BBOX.contains(latLng)
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

    override fun requestReverseGeocodingLocation(context: Context, location: Location): Observable<List<Location>> {
        return mNlscApi.getLocationCodes(
            lon = location.longitude,
            lat = location.latitude
        ).map { locationCodes ->
            if (locationCodes.townshipName?.value.isNullOrEmpty()) {
                throw InvalidLocationException()
            }

            listOf(
                location.copy(
                    timeZone = "Asia/Taipei",
                    country = Locale(context.currentLocale.code, "TW").displayCountry,
                    countryCode = "TW",
                    admin1 = locationCodes.countyName?.value,
                    admin1Code = locationCodes.countyCode?.value,
                    admin2 = locationCodes.townshipName?.value,
                    admin2Code = locationCodes.townshipCode?.value,
                    admin3 = locationCodes.villageName?.value,
                    admin3Code = locationCodes.villageCode?.value,
                    city = locationCodes.townshipName!!.value,
                    district = locationCodes.villageName?.value
                )
            )
        }
    }

    companion object {
        private const val NLSC_BASE_URL = "https://api.nlsc.gov.tw/"

        private val TAIWAN_BBOX = LatLngBounds.parse(119.99690416, 21.756143532, 122.10915909, 25.633378776)
        private val PENGHU_BBOX = LatLngBounds.parse(119.314301816, 23.186561404, 119.726986388, 23.810692086)
        private val KINMEN_BBOX = LatLngBounds.parse(118.137979837, 24.160255444, 118.505977425, 24.534228163)
        private val WUQIU_BBOX = LatLngBounds.parse(119.443195363, 24.97760013, 119.479213453, 24.999614154)
        private val MATSU_BBOX = LatLngBounds.parse(119.908905081, 25.940995457, 120.511750672, 26.385275262)
    }
}
