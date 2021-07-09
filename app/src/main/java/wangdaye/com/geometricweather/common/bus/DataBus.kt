package wangdaye.com.geometricweather.common.bus

import android.os.Handler
import android.os.Looper
import java.util.*

class DataBus private constructor() {

    companion object {

        @JvmStatic
        val instance: DataBus by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            DataBus()
        }
    }

    private val liveDataMap = HashMap<String, LiveData<Any>>()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun <T> with(key: String, type: Class<T>): LiveData<T> {
        if (!liveDataMap.containsKey(key)) {
            liveDataMap[key] = LiveData(mainHandler)
        }
        return liveDataMap[key] as LiveData<T>
    }

    fun with(key: String): LiveData<Any> {
        return with(key, Any::class.java)
    }
}