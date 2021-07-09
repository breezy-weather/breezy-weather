package wangdaye.com.geometricweather.common.bus

import androidx.lifecycle.Observer
import java.lang.ref.WeakReference

internal class MyObserverWrapper<T> internal constructor(
        host: LiveData<T>,
        internal val observer: Observer<in T>,
        private var version: Int
) : Observer<T> {

    private val host = WeakReference(host)

    override fun onChanged(t: T) {
        host.get()?.let {
            if (version >= it.version) {
                return
            }
            version = it.version
            observer.onChanged(t)
        }
    }
}