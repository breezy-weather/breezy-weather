package org.breezyweather.sources.nlsc

import android.content.Context
import breezyweather.domain.location.model.LocationAddressInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.InvalidLocationException
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

class NlscService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("XmlClient") xmlClient: Retrofit.Builder,
) : NlscServiceStub(context) {

    override val privacyPolicyUrl = "https://www.nlsc.gov.tw/cp.aspx?n=1633"

    private val mNlscApi by lazy {
        xmlClient
            .baseUrl(NLSC_BASE_URL)
            .build()
            .create(NlscApi::class.java)
    }

    override val attributionLinks = mapOf(
        reverseGeocodingAttribution to "https://www.nlsc.gov.tw/"
    )

    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        return mNlscApi.getLocationCodes(
            lon = longitude,
            lat = latitude
        ).map { locationCodes ->
            if (locationCodes.townshipName?.value.isNullOrEmpty()) {
                throw InvalidLocationException()
            }

            listOf(
                LocationAddressInfo(
                    timeZoneId = "Asia/Taipei",
                    countryCode = "TW",
                    admin1 = locationCodes.countyName?.value,
                    admin1Code = locationCodes.countyCode?.value,
                    admin2 = locationCodes.townshipName.value,
                    admin2Code = locationCodes.townshipCode?.value,
                    admin3 = locationCodes.villageName?.value,
                    admin3Code = locationCodes.villageCode?.value,
                    city = locationCodes.townshipName.value,
                    district = locationCodes.villageName?.value
                )
            )
        }
    }

    companion object {
        private const val NLSC_BASE_URL = "https://api.nlsc.gov.tw/"
    }
}
