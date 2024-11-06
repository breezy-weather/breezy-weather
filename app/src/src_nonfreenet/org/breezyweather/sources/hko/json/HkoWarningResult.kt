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

package org.breezyweather.sources.hko.json

import kotlinx.serialization.Serializable

@Serializable
data class HkoWarningResult (
    val DYN_DAT_MINDS_WTCPRE8: Map<String, Map<String, String>>? = null,
    val DYN_DAT_MINDS_WTCB: Map<String, Map<String, String>>? = null,
    val DYN_DAT_MINDS_WRAINSA: Map<String, Map<String, String>>? = null,
    val DYN_DAT_MINDS_WTMW: Map<String, Map<String, String>>? = null,
    val DYN_DAT_MINDS_WFNTSA: Map<String, Map<String, String>>? = null,
    val DYN_DAT_MINDS_WMNB: Map<String, Map<String, String>>? = null,
    val DYN_DAT_MINDS_WLSA: Map<String, Map<String, String>>? = null,
    val DYN_DAT_MINDS_WTS: Map<String, Map<String, String>>? = null,
    val DYN_DAT_MINDS_WFIRE: Map<String, Map<String, String>>? = null,
    val DYN_DAT_MINDS_WHOT: Map<String, Map<String, String>>? = null,
    val DYN_DAT_MINDS_WCOLD: Map<String, Map<String, String>>? = null,
    val DYN_DAT_MINDS_WFROST: Map<String, Map<String, String>>? = null,
    val DYN_DAT_MINDS_MHEAD: Map<String, Map<String, String>>? = null
)