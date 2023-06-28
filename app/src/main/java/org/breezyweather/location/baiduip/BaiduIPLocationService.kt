package org.breezyweather.location.baiduip

import android.content.Context
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.breezyweather.common.rxjava.ApiObserver
import org.breezyweather.common.rxjava.ObserverContainer
import org.breezyweather.common.rxjava.SchedulerTransformer
import org.breezyweather.location.LocationService
import org.breezyweather.location.baiduip.json.BaiduIPLocationResult
import org.breezyweather.settings.SettingsManager
import javax.inject.Inject

class BaiduIPLocationService @Inject constructor(
    private val mApi: BaiduIPLocationApi,
    private val compositeDisposable: CompositeDisposable
) : LocationService() {
    override fun requestLocation(context: Context, callback: LocationCallback) {
        val apiKey = SettingsManager.getInstance(context).providerBaiduIpLocationAk
        if (apiKey.isEmpty()) {
            callback.onCompleted(null)
            return
        }
        mApi.getLocation(apiKey, "gcj02")
            .compose(SchedulerTransformer.create())
            .subscribe(ObserverContainer(compositeDisposable, object : ApiObserver<BaiduIPLocationResult>() {
                override fun onSucceed(t: BaiduIPLocationResult) {
                    if (t.content?.point == null
                        || t.content.point.y.isNullOrEmpty()
                        || t.content.point.x.isNullOrEmpty()
                    ) {
                        callback.onCompleted(null)
                    } else {
                        try {
                            val result = Result(
                                t.content.point.y.toFloat(),
                                t.content.point.x.toFloat()
                            )
                            callback.onCompleted(result)
                        } catch (ignore: Exception) {
                            callback.onCompleted(null)
                        }
                    }
                }

                override fun onFailed() {
                    callback.onCompleted(null)
                }
            }))
    }

    override fun cancel() {
        compositeDisposable.clear()
    }

    override fun hasPermissions(context: Context) = true

    override val permissions: Array<String> = emptyArray()
}
