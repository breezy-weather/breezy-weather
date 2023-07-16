package org.breezyweather.location.baiduip

import android.content.Context
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.breezyweather.R
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.exceptions.LocationException
import org.breezyweather.common.rxjava.SchedulerTransformer
import org.breezyweather.location.LocationService
import org.breezyweather.settings.SettingsManager
import javax.inject.Inject

class BaiduIPLocationService @Inject constructor(
    private val mApi: BaiduIPLocationApi,
    private val compositeDisposable: CompositeDisposable
) : LocationService() {

    override fun requestLocation(context: Context): Observable<Result> {
        val apiKey = SettingsManager.getInstance(context).providerBaiduIpLocationAk
        if (apiKey.isEmpty()) {
            return Observable.error(ApiKeyMissingException())
        }
        return mApi.getLocation(apiKey, "gcj02")
            .compose(SchedulerTransformer.create())
            .map { t ->
                if (t.content?.point == null
                    || t.content.point.y.isNullOrEmpty()
                    || t.content.point.x.isNullOrEmpty()
                ) {
                    throw LocationException()
                } else {
                    try {
                        Result(
                            t.content.point.y.toFloat(),
                            t.content.point.x.toFloat()
                        )
                    } catch (ignore: Exception) {
                        throw LocationException()
                    }
                }
            }
    }

    override fun cancel() {
        compositeDisposable.clear()
    }

    override fun hasPermissions(context: Context) = true

    override val permissions: Array<String> = emptyArray()
}
