/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.common.bus

import android.os.Handler
import android.os.Looper
import org.breezyweather.common.basic.livedata.BusLiveData

class EventBus private constructor() {

    companion object {

        val instance: EventBus by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            EventBus()
        }
    }

    private val liveDataMap = HashMap<String, BusLiveData<Any>>()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun <T> with(type: Class<T>): BusLiveData<T> {
        val key = key(type = type)

        if (!liveDataMap.containsKey(key)) {
            liveDataMap[key] = BusLiveData(mainHandler)
        }
        return liveDataMap[key] as BusLiveData<T>
    }

    private fun <T> key(type: Class<T>) = type.name
}