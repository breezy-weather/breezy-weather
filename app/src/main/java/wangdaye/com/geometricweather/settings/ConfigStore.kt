package wangdaye.com.geometricweather.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Looper
import androidx.preference.PreferenceManager
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.AbstractQueuedSynchronizer


class ConfigStore private constructor(sp: SharedPreferences) {

    companion object {

        private val spCache = HashMap<String, WeakReference<SharedPreferences>>()

        @JvmStatic
        @Synchronized
        fun getInstance(context: Context, name: String? = null): ConfigStore {
            val spName = name ?: context.packageName + "_preferences"

            val cache = spCache[spName]
            var sp = cache?.get()

            if (sp == null) {
                sp = if (name == null) {
                    PreferenceManager.getDefaultSharedPreferences(context)
                } else {
                    context.getSharedPreferences(name, Context.MODE_PRIVATE)
                }
                spCache[spName] = WeakReference(sp)
            }
            return ConfigStore(sp!!)
        }
    }

    private val preferences = sp
    private val configCache = ConcurrentHashMap<String, ValueWrapper>()

    @Volatile
    private var neverPreloaded = true
    private val preloadLock = NonReentrantLock()

    private class ValueWrapper(val value: Any?)

    private class NonReentrantLock : AbstractQueuedSynchronizer() {

        override fun isHeldExclusively(): Boolean {
            return state != 0
        }

        override fun tryAcquire(unused: Int): Boolean {
            if (compareAndSetState(0, 1)) {
                exclusiveOwnerThread = Thread.currentThread()
                return true
            }
            return false
        }

        override fun tryRelease(unused: Int): Boolean {
            exclusiveOwnerThread = null
            state = 0
            return true
        }

        fun lock() {
            acquire(1)
        }

        fun tryLock(): Boolean {
            return tryAcquire(1)
        }

        fun unlock() {
            release(1)
        }

        val isLocked: Boolean
            get() = isHeldExclusively
    }

    fun preload() {
        if (!preloadLock.tryLock()) {
            // there is already another thread is preloading.
            return
        }

        neverPreloaded = false

        synchronized(preferences) {
            val configs = preferences.all

            for (entry in configs) {
                configCache[entry.key] = ValueWrapper(entry.value)
            }
        }
    }

    private fun checkIsPreloading() {
        if (!neverPreloaded) {
            return
        }

        try {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                // is main thread.
                while (!preloadLock.tryLock()) {
                    // spin to wait.
                }
            } else {
                preloadLock.lock()
            }
        } finally {
            preloadLock.unlock()
        }
    }

    fun getString(key: String, defValue: String?): String? {
        checkIsPreloading()

        return configCache.getOrPut(key, {
            synchronized(preferences) {
                ValueWrapper(preferences.getString(key, defValue))
            }
        }).value as String?
    }

    fun getStringSet(key: String, defValues: Set<String?>?): Set<String>? {
        checkIsPreloading()

        return configCache.getOrPut(key, {
            synchronized(preferences) {
                ValueWrapper(preferences.getStringSet(key, defValues))
            }
        }).value as Set<String>?
    }

    fun getInt(key: String, defValue: Int): Int {
        checkIsPreloading()

        return configCache.getOrPut(key, {
            synchronized(preferences) {
                ValueWrapper(preferences.getInt(key, defValue))
            }
        }).value as Int
    }

    fun getLong(key: String, defValue: Long): Long {
        checkIsPreloading()

        return configCache.getOrPut(key, {
            synchronized(preferences) {
                ValueWrapper(preferences.getLong(key, defValue))
            }
        }).value as Long
    }

    fun getFloat(key: String, defValue: Float): Float {
        checkIsPreloading()

        return configCache.getOrPut(key, {
            synchronized(preferences) {
                ValueWrapper(preferences.getFloat(key, defValue))
            }
        }).value as Float
    }

    fun getBoolean(key: String, defValue: Boolean): Boolean {
        checkIsPreloading()

        return configCache.getOrPut(key, {
            synchronized(preferences) {
                ValueWrapper(preferences.getBoolean(key, defValue))
            }
        }).value as Boolean
    }

    fun contains(key: String): Boolean {
        checkIsPreloading()

        return configCache.getOrPut(key, {
            synchronized(preferences) {
                ValueWrapper(preferences.contains(key))
            }
        }).value as Boolean
    }

    fun edit(): Editor {
        return Editor(this)
    }

    class Editor internal constructor(private val host: ConfigStore) {

        private val editor = host.preferences.edit()
        private val pendingCache = HashMap<String, ValueWrapper>()

        fun putString(key: String, value: String?): Editor {
            pendingCache[key] = ValueWrapper(value)
            editor.putString(key, value)
            return this
        }

        fun putStringSet(key: String, values: Set<String?>?): Editor {
            pendingCache[key] = ValueWrapper(values)
            editor.putStringSet(key, values)
            return this
        }

        fun putInt(key: String, value: Int): Editor {
            pendingCache[key] = ValueWrapper(value)
            editor.putInt(key, value)
            return this
        }

        fun putLong(key: String, value: Long): Editor {
            pendingCache[key] = ValueWrapper(value)
            editor.putLong(key, value)
            return this
        }

        fun putFloat(key: String, value: Float): Editor {
            pendingCache[key] = ValueWrapper(value)
            editor.putFloat(key, value)
            return this
        }

        fun putBoolean(key: String, value: Boolean): Editor {
            pendingCache[key] = ValueWrapper(value)
            editor.putBoolean(key, value)
            return this
        }

        fun remove(key: String): Editor {
            pendingCache.remove(key)
            editor.remove(key)
            return this
        }

        fun clear(): Editor {
            pendingCache.clear()
            editor.clear()
            return this
        }

        fun commit(): Boolean {
            mergeCache()
            return editor.commit()
        }

        fun apply() {
            mergeCache()
            editor.apply()
        }

        private fun mergeCache() {
            for (entry in pendingCache) {
                host.configCache[entry.key] = entry.value
            }
        }
    }
}