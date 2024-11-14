/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.common.utils.helpers

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

object AsyncHelper {
    fun <T> runOnIO(
        task: (emitter: Emitter<T>) -> Unit,
        callback: (t: T, done: Boolean) -> Unit,
    ): Controller {
        return Controller(
            Observable.create { emitter: ObservableEmitter<Data<T>> ->
                task(Emitter(emitter))
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { data: Data<T> -> callback(data.t, data.done) }
                .subscribe()
        )
    }

    fun runOnIO(runnable: Runnable): Controller {
        return Controller(
            Observable.create { _: ObservableEmitter<Any>? -> runnable.run() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        )
    }

    fun delayRunOnIO(runnable: Runnable, milliSeconds: Long): Controller {
        return Controller(
            Observable.timer(milliSeconds, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .doOnComplete { runnable.run() }
                .subscribe()
        )
    }

    fun delayRunOnUI(runnable: Runnable, milliSeconds: Long): Controller {
        return Controller(
            Observable.timer(milliSeconds, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete { runnable.run() }
                .subscribe()
        )
    }

    fun intervalRunOnUI(
        runnable: Runnable,
        intervalMilliSeconds: Long,
        initDelayMilliSeconds: Long,
    ): Controller {
        return Controller(
            Observable.interval(initDelayMilliSeconds, intervalMilliSeconds, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { runnable.run() }
        )
    }

    class Controller internal constructor(val inner: Disposable) {
        fun cancel() {
            inner.dispose()
        }
    }

    class Data<T> internal constructor(val t: T, val done: Boolean)
    class Emitter<T> internal constructor(inner: ObservableEmitter<Data<T>>) {
        val inner: ObservableEmitter<Data<T>>

        init {
            this.inner = inner
        }

        fun send(t: T, done: Boolean) {
            inner.onNext(Data(t, done))
        }
    }
}
