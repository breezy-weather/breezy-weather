package wangdaye.com.geometricweather.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class ConfigStore private constructor(private val preferences: SharedPreferences) {

    companion object {

        @JvmStatic
        fun getInstance(context: Context, name: String? = null) = if (name == null) {
            ConfigStore(PreferenceManager.getDefaultSharedPreferences(context))
        } else {
            ConfigStore(context.getSharedPreferences(name, Context.MODE_PRIVATE))
        }
    }

    fun getString(key: String?, defValue: String?): String? {
        return preferences.getString(key, defValue)
    }

    fun getStringSet(key: String?, defValues: Set<String?>?): Set<String>? {
        return preferences.getStringSet(key, defValues)
    }

    fun getInt(key: String?, defValue: Int): Int {
        return preferences.getInt(key, defValue)
    }

    fun getLong(key: String?, defValue: Long): Long {
        return preferences.getLong(key, defValue)
    }

    fun getFloat(key: String?, defValue: Float): Float {
        return preferences.getFloat(key, defValue)
    }

    fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return preferences.getBoolean(key, defValue)
    }

    operator fun contains(key: String?): Boolean {
        return preferences.contains(key)
    }

    fun edit(): Editor {
        return Editor(preferences)
    }

    class Editor internal constructor(sp: SharedPreferences) {

        private val mEditor: SharedPreferences.Editor = sp.edit()

        fun putString(key: String?, value: String?): Editor {
            mEditor.putString(key, value)
            return this
        }

        fun putStringSet(key: String?, values: Set<String?>?): Editor {
            mEditor.putStringSet(key, values)
            return this
        }

        fun putInt(key: String?, value: Int): Editor {
            mEditor.putInt(key, value)
            return this
        }

        fun putLong(key: String?, value: Long): Editor {
            mEditor.putLong(key, value)
            return this
        }

        fun putFloat(key: String?, value: Float): Editor {
            mEditor.putFloat(key, value)
            return this
        }

        fun putBoolean(key: String?, value: Boolean): Editor {
            mEditor.putBoolean(key, value)
            return this
        }

        fun remove(key: String?): Editor {
            mEditor.remove(key)
            return this
        }

        fun clear(): Editor {
            mEditor.clear()
            return this
        }

        fun commit(): Boolean {
            return mEditor.commit()
        }

        fun apply() {
            mEditor.apply()
        }
    }
}