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

package org.breezyweather.sources.knmi

import breezyweather.domain.location.model.Location

// This code is based on the iOS version of the KNMI app: https://gitlab.com/KNMI-OSS/KNMI-App/knmi-app-ios/-/blob/35a30d8543995b8f297612341b24d5f7f7e8a366/KNMI/SharedLibrary/Sources/GridDefinition/GridDefinition.swift
// Which is licensed under the EUPL 1.2.
enum class KnmiGridDirection(val stringRepresentation: String) {
    southWestColumnRow("SWCR"), northWestColumnRow("NWCR")
}

data class KnmiGridSteps(
    val latitude: Int,
    val longitude: Int,
)

data class KnmiGridDefinition(
    val southWest: Location,
    val northEast: Location,
    val steps: KnmiGridSteps,
    val prefix: String, // these are currently unused but may or may not become useful later
    val proj: String,
    val direction: KnmiGridDirection,
)

