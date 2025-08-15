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

package org.breezyweather.common.activities.livedata

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import org.breezyweather.common.bus.MyObserverWrapper

class BusLiveData<T>(
    private val mainHandler: Handler,
) : MutableLiveData<T>() {

    companion object {
        const val START_VERSION = -1
    }

    private val wrapperMap = HashMap<Observer<in T>, MyObserverWrapper<T>>()
    internal var version = START_VERSION

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        runOnMainThread {
            innerObserver(owner, MyObserverWrapper(this, observer, version))
        }
    }

    fun observeAutoRemove(owner: LifecycleOwner, observer: Observer<in T>) {
        runOnMainThread {
            owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    removeObserver(observer)
                }
            })
            innerObserver(owner, MyObserverWrapper(this, observer, version))
        }
    }

    fun observeStickily(owner: LifecycleOwner, observer: Observer<in T>) {
        runOnMainThread {
            innerObserver(owner, MyObserverWrapper(this, observer, START_VERSION))
        }
    }

    private fun innerObserver(owner: LifecycleOwner, wrapper: MyObserverWrapper<T>) {
        wrapperMap[wrapper.observer] = wrapper
        super.observe(owner, wrapper)
    }

    override fun observeForever(observer: Observer<in T>) {
        runOnMainThread {
            innerObserverForever(MyObserverWrapper(this, observer, version))
        }
    }

    fun observeStickilyForever(observer: Observer<in T>) {
        runOnMainThread {
            innerObserverForever(MyObserverWrapper(this, observer, START_VERSION))
        }
    }

    private fun innerObserverForever(wrapper: MyObserverWrapper<T>) {
        wrapperMap[wrapper.observer] = wrapper
        super.observeForever(wrapper)
    }

    override fun removeObserver(observer: Observer<in T>) {
        runOnMainThread {
            val wrapper = wrapperMap.remove(observer)
            if (wrapper != null) {
                super.removeObserver(wrapper)
            }
        }
    }

    override fun setValue(value: T) {
        ++version
        super.setValue(value)
    }

    override fun postValue(value: T) {
        runOnMainThread { setValue(value) }
    }

    private fun runOnMainThread(r: Runnable) {
        if (Looper.getMainLooper().thread === Thread.currentThread()) {
            r.run()
        } else {
            mainHandler.post(r)
        }
    }
}
