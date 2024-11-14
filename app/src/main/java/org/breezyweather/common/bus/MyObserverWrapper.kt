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

package org.breezyweather.common.bus

import androidx.lifecycle.Observer
import org.breezyweather.common.basic.livedata.BusLiveData
import java.lang.ref.WeakReference

internal class MyObserverWrapper<T> internal constructor(
    host: BusLiveData<T>,
    internal val observer: Observer<in T>,
    private var version: Int,
) : Observer<T> {

    private val host = WeakReference(host)

    override fun onChanged(value: T) {
        host.get()?.let {
            if (version >= it.version) {
                return
            }
            version = it.version
            observer.onChanged(value)
        }
    }
}
