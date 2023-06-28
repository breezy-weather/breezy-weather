package org.breezyweather.common.rxjava

import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableObserver

class ObserverContainer<T : Any>(
    private val compositeDisposable: CompositeDisposable,
    private val observer: Observer<T>
) : DisposableObserver<T>() {
    override fun onStart() {
        compositeDisposable.add(this)
        observer.onSubscribe(this)
    }

    override fun onNext(t: T) {
        observer.onNext(t)
    }

    override fun onError(e: Throwable) {
        observer.onError(e)
        compositeDisposable.remove(this)
    }

    override fun onComplete() {
        observer.onComplete()
        compositeDisposable.remove(this)
    }
}