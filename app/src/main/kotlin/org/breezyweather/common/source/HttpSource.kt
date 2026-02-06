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

package org.breezyweather.common.source

import breezyweather.domain.source.SourceContinent

/**
 * TODO: We should inject Retrofit.Builder here, however I still haven’t figure out how to do it yet
 */
abstract class HttpSource : Source {

    /**
     * Privacy policy of the website, like: https://mysite.com/privacy
     */
    abstract val privacyPolicyUrl: String

    /**
     * Add a link each time a string appears in an attribution
     * Example: <"Open-Meteo", "https://open-meteo.com/">
     */
    open val attributionLinks: Map<String, String> = emptyMap()

    /**
     * The continent the source is mainly based of
     *
     * Worldwide sources will use `SourceContinent.WORLDWIDE`
     * National sources even if supporting worldwide will use the continent their mainland is based on
     * E.g. Météo-France will use `SourceContinent.EUROPE` even if it supports oversea territories on other continents
     * E.g. Türkiye will use `SourceContinent.ASIA` even if 10% of its territory is technically in Europe
     */
    abstract val continent: SourceContinent
}
