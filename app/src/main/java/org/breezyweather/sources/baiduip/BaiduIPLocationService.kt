package org.breezyweather.sources.baiduip

import android.content.Context
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.exceptions.LocationException
import org.breezyweather.common.rxjava.SchedulerTransformer
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationPositionWrapper
import org.breezyweather.common.source.LocationSource
import org.breezyweather.settings.SettingsManager
import retrofit2.Retrofit
import javax.inject.Inject

class BaiduIPLocationService @Inject constructor(
    client: Retrofit.Builder
) : HttpSource(), LocationSource {

    override val id = "baidu_ip"
    override val name = "百度IP定位"
    override val privacyPolicyUrl = "https://lbs.baidu.com/index.php?title=openprivacy"

    private val mApi by lazy {
        client
            .baseUrl(BAIDU_IP_LOCATION_BASE_URL)
            .build()
            .create(BaiduIPLocationApi::class.java)
    }

    override fun requestLocation(context: Context): Observable<LocationPositionWrapper> {
        val apiKey = SettingsManager.getInstance(context).providerBaiduIpLocationAk
        if (apiKey.isEmpty()) {
            return Observable.error(ApiKeyMissingException())
        }
        return mApi.getLocation(apiKey, "gcj02")
            .compose(SchedulerTransformer.create())
            .map { t ->
                if (t.status != 0) {
                    // 0 = OK
                    // 1 = IP not supported (outside China)
                    // Don’t know about other cases, doing != 0 for safety
                    throw LocationException()
                }
                if (t.content?.point == null
                    || t.content.point.y.isNullOrEmpty()
                    || t.content.point.x.isNullOrEmpty()
                ) {
                    throw LocationException()
                } else {
                    try {
                        LocationPositionWrapper(
                            t.content.point.y.toFloat(),
                            t.content.point.x.toFloat()
                        )
                    } catch (ignore: Exception) {
                        throw LocationException()
                    }
                }
            }
    }

    override fun hasPermissions(context: Context) = true

    override val permissions: Array<String> = emptyArray()

    companion object {
        private const val BAIDU_IP_LOCATION_BASE_URL = "https://api.map.baidu.com/"
    }
}
