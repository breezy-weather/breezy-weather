package wangdaye.com.geometricweather.common.bus

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.*

class LiveData<T> internal constructor(private val mainHandler: Handler) : MutableLiveData<T>() {

    companion object {
        const val START_VERSION = -1
    }

    private val wrapperMap = HashMap<Observer<in T>, MyObserverWrapper<T>>()
    internal var version = START_VERSION

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        runOnMainThread {
            innerObserver(owner,
                    MyObserverWrapper(this, observer, version))
        }
    }

    fun observeStickily(owner: LifecycleOwner, observer: Observer<in T>) {
        runOnMainThread {
            innerObserver(owner,
                    MyObserverWrapper(this, observer, START_VERSION))
        }
    }

    private fun innerObserver(owner: LifecycleOwner, wrapper: MyObserverWrapper<T>) {
        wrapperMap[wrapper.observer] = wrapper
        super.observe(owner, wrapper)
    }

    override fun observeForever(observer: Observer<in T>) {
        runOnMainThread {
            innerObserverForever(
                    MyObserverWrapper(this, observer, version))
        }
    }

    fun observeStickilyForever(observer: Observer<in T>) {
        runOnMainThread {
            innerObserverForever(
                    MyObserverWrapper(this, observer, START_VERSION))
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
        version ++
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