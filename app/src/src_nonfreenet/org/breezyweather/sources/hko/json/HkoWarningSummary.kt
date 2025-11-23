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

package org.breezyweather.sources.hko.json

import kotlinx.serialization.Serializable

@Serializable
data class HkoWarningSummary(
    val Warning_Name: String?,
    val Warning_Action: String?,
    val Warning_Code: String?,
    val Warning_Type: String?,
    val Warning_Description: String?,
    val Bulletin_Date: String?,
    val Bulletin_Time: String?,
    val Expiry_Date: String?,
    val Expiry_Time: String?,
    val Issue_Date: String?,
    val Issue_Time: String?,
)
