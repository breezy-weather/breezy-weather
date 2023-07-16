package org.breezyweather.common.rxjava

import io.reactivex.rxjava3.observers.DisposableObserver

abstract class ApiObserver<T : Any> : DisposableObserver<T>() {

    abstract fun onSucceed(t: T)

    override fun onNext(t: T) {
        onSucceed(t)
    }

    override fun onError(e: Throwable) {
        onFailed()
    }

    abstract fun onFailed()

    override fun onComplete() {
        // do nothing.
    }
}