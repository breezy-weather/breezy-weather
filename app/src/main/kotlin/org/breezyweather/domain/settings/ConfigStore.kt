/*
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

package org.breezyweather.domain.settings

import android.content.Context

/**
 * TODO: When migrating to extensions, we should make this class read only
 * and only give main app write access
 * TODO: Should we migrate to Android DataStore?
 */
open class ConfigStore(
    context: Context,
    name: String = context.packageName + "_preferences",
) {

    private val preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    fun getString(key: String, defValue: String?): String? {
        return preferences.getString(key, defValue)
    }

    fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
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
