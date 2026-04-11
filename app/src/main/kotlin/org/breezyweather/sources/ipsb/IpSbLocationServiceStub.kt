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

package org.breezyweather.sources.ipsb

import android.content.Context
import breezyweather.domain.source.SourceContinent
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationSource
import org.breezyweather.common.source.NonFreeNetSource

/**
 * The actual implementation is in the src_freenet and src_nonfreenet folders
 */
abstract class IpSbLocationServiceStub() :
    HttpSource(),
    LocationSource,
    NonFreeNetSource {

    override val id = "ipsb"
    override val name = "IP.SB"
    override val continent = SourceContinent.WORLDWIDE

    override fun hasPermissions(context: Context) = true

    override val permissions: Array<String> = emptyArray()
}
