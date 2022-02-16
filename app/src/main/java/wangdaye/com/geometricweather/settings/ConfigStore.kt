package wangdaye.com.geometricweather.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class ConfigStore private constructor(sp: SharedPreferences) {

    companion object {

        @JvmStatic
        fun getInstance(context: Context) = getInstance(context, null)

        @JvmStatic
        fun getInstance(context: Context, name: String? = null): ConfigStore {
            return ConfigStore(
                if (name == null) {
                    PreferenceManager.getDefaultSharedPreferences(context)
                } else {
                    context.getSharedPreferences(name, Context.MODE_PRIVATE)
                }
            )
        }
    }

    private val preferences = sp

    fun preload() {
        // do nothing.
    }

    fun getString(key: String, defValue: String?): String? {
        return preferences.getString(key, defValue)
    }

    fun getStringSet(key: String, defValues: Set<String?>?): Set<String>? {
        return preferences.getStringSet(key, defValues)
    }

    fun getInt(key: String, defValue: Int): Int {
        return preferences.getInt(key, defValue)
    }

    fun getLong(key: String, defValue: Long): Long {
        return preferences.getLong(key, defValue)
    }

    fun getFloat(key: String, defValue: Float): Float {
        return preferences.getFloat(key, defValue)
    }

    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return preferences.getBoolean(key, defValue)
    }

    fun contains(key: String): Boolean {
        return preferences.contains(key)
    }

    fun edit(): Editor {
        return Editor(this)
    }

    class Editor internal constructor(host: ConfigStore) {

        private val editor = host.preferences.edit()

        fun putString(key: String, value: String?): Editor {
            editor.putString(key, value)
            return this
        }

        fun putStringSet(key: String, values: Set<String?>?): Editor {
            editor.putStringSet(key, values)
            return this
        }

        fun putInt(key: String, value: Int): Editor {
            editor.putInt(key, value)
            return this
        }

        fun putLong(key: String, value: Long): Editor {
            editor.putLong(key, value)
            return this
        }

        fun putFloat(key: String, value: Float): Editor {
            editor.putFloat(key, value)
            return this
        }

        fun putBoolean(key: String, value: Boolean): Editor {
            editor.putBoolean(key, value)
            return this
        }

        fun remove(key: String): Editor {
            editor.remove(key)
            return this
        }

        fun clear(): Editor {
            editor.clear()
            return this
        }

        fun apply() {
            editor.apply()
        }
    }
}