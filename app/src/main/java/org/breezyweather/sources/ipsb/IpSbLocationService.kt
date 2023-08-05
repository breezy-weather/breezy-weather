package org.breezyweather.sources.ipsb

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.LocationException
import org.breezyweather.common.rxjava.SchedulerTransformer
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationPositionWrapper
import org.breezyweather.common.source.LocationSource
import retrofit2.Retrofit
import java.util.TimeZone
import javax.inject.Inject

class IpSbLocationService @Inject constructor(
    @ApplicationContext context: Context,
    client: Retrofit.Builder
) : HttpSource(), LocationSource {

    override val id = "ipsb"
    override val name = "IP.SB"
    override val privacyPolicyUrl = "https://ip.sb/privacy-policy/"

    private val mApi by lazy {
        client
            .baseUrl(IP_SB_BASE_URL)
            .build()
            .create(IpSbLocationApi::class.java)
    }

    override fun requestLocation(context: Context): Observable<LocationPositionWrapper> {
        return mApi.getLocation()
            .compose(SchedulerTransformer.create())
            .map { t ->
                if (t.longitude == 0.0 && t.latitude == 0.0) {
                    throw LocationException()
                }
                LocationPositionWrapper(
                    latitude = t.latitude.toFloat(),
                    longitude = t.longitude.toFloat(),
                    timeZone = if (!t.timezone.isNullOrEmpty()) TimeZone.getTimeZone(t.timezone) else null,
                    country = t.country,
                    countryCode = t.countryCode,
                    province = t.region,
                    city = t.city
                )
            }
    }

    override fun hasPermissions(context: Context) = true

    override val permissions: Array<String> = emptyArray()

    companion object {
        private const val IP_SB_BASE_URL = "https://api.ip.sb/"
    }
}
