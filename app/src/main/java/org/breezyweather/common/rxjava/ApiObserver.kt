package org.breezyweather.common.rxjava

import io.reactivex.rxjava3.observers.DisposableObserver
import org.breezyweather.BreezyWeather
import retrofit2.HttpException

abstract class ApiObserver<T : Any> : DisposableObserver<T>() {
    var statusCode: Int? = null
        protected set

    val isApiLimitReached: Boolean = statusCode != null && (statusCode == 409 || statusCode == 429)
    val isApiUnauthorized: Boolean = statusCode != null && (statusCode == 401 || statusCode == 403)

    abstract fun onSucceed(t: T)

    override fun onNext(t: T) {
        onSucceed(t)
    }

    override fun onError(e: Throwable) {
        if (e is HttpException) {
            statusCode = e.code()
        }
        if (BreezyWeather.instance.debugMode) {
            e.printStackTrace()
        }
        onFailed()
    }

    abstract fun onFailed()

    override fun onComplete() {
        // do nothing.
    }
}