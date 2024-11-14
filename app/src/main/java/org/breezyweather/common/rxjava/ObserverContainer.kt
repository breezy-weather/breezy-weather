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

package org.breezyweather.common.rxjava

import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableObserver

class ObserverContainer<T : Any>(
    private val compositeDisposable: CompositeDisposable,
    private val observer: Observer<T>,
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
