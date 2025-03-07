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

package org.breezyweather.sources.rnsa.csv

import kotlinx.serialization.Serializable

@Serializable
data class RnsaDepartement(
    val number: String,
    val departements: String? = null,

    val cypres: Int? = null,
    val noisetier: Int? = null,
    val aulne: Int? = null,
    val peuplier: Int? = null,
    val saule: Int? = null,
    val frene: Int? = null,
    val charme: Int? = null,
    val bouleau: Int? = null,
    val platane: Int? = null,
    val chene: Int? = null,
    val olivier: Int? = null,
    val tilleul: Int? = null,
    val chataignier: Int? = null,
    val rumex: Int? = null,
    val graminees: Int? = null,
    val plantain: Int? = null,
    val urticacees: Int? = null,
    val armoises: Int? = null,
    val ambroisies: Int? = null,
)
