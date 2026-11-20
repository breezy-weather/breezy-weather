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

package org.breezyweather.sources.blitzortung

import android.content.Context
import org.breezyweather.ui.radar.INITIAL_ZOOM_LEVEL
import javax.inject.Inject

class BlitzortungService @Inject constructor() : BlitzortungServiceStub() {

    override val allowedDomains = arrayOf(
        "map.blitzortung.org",
        "tiles.blitzortung.org"
    )

    override val blockedUrls = arrayOf(
        "https://map.blitzortung.org/CSS/index_advertisment.css"
    )

    override fun getWebViewUrl(
        context: Context,
        longitude: Double,
        latitude: Double,
    ): String {
        return "https://map.blitzortung.org/" +
            "#$INITIAL_ZOOM_LEVEL" +
            "/$latitude" +
            "/$longitude"
    }
}
