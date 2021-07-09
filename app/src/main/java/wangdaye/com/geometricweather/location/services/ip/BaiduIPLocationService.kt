package wangdaye.com.geometricweather.location.services.ip

import android.content.Context
import kotlinx.coroutines.coroutineScope
import wangdaye.com.geometricweather.location.services.LocationService
import wangdaye.com.geometricweather.settings.SettingsManager
import javax.inject.Inject

class BaiduIPLocationService @Inject constructor(private val api: BaiduIPLocationApi) : LocationService() {

    override suspend fun getLocation(context: Context): Result = coroutineScope {
        val ipResult = api.getLocation(
                SettingsManager.getInstance(context).getProviderBaiduIpLocationAk(true),
                "gcj02"
        )
        val result = Result(ipResult.content.point.y.toFloat(), ipResult.content.point.x.toFloat())

        result.setGeocodeInformation(
                "中国",
                ipResult.content.address_detail.province,
                ipResult.content.address_detail.city,
                ipResult.content.address_detail.district
        )
        result.inChina = true
        return@coroutineScope result
    }

    override fun getPermissions(): Array<String> = emptyArray()

    override fun hasPermissions(context: Context?): Boolean {
        return true
    }
}