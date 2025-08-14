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

package org.breezyweather.common.source

interface AddressSource : Source {

    /**
     * Known ambiguous country codes used by the source
     * Use `null` to let Breezy Weather process all country codes known to be potentially ambiguous.
     *  Use this value if you have no idea what to do, or don’t want to bother testing every territory and claim
     * Use an empty array if the source respects all known ISO 3166-1 alpha-2 codes
     * Specify each ISO 3166-1 alpha-2 code otherwise
     * The list of all country codes known to be potentially ambiguous can be found in the companion object of this
     *  interface
     */
    val knownAmbiguousCountryCodes: Array<String>?

    companion object {
        /**
         * For technical reasons, we need to better identify each territory
         * Crimea is not included to let each location search/address lookup source resolves it the way they want
         *  and we will resolve the timezone as Europe/Simferopol whether identified as UA or RU
         */
        val ambiguousCountryCodes = arrayOf(
            "AR", // Claims: AQ
            "AU", // Territories: CX, CC, HM (uninhabited), NF. Claims: AQ
            "CL", // Claims: AQ
            "CN", // Territories: HK, MO. Claims: TW
            "DK", // Territories: FO, GL
            "FI", // Territories: AX
            "FR", // Territories: GF, PF, TF (uninhabited), GP, MQ, YT, NC, RE, BL, MF, PM, WF. Claims: AQ
            "GB", // Territories: AI, BM, IO, KY, FK, GI, GG, IM, JE, MS, PN, SH, GS (uninhabited), TC, VG. Claims: AQ
            "IL", // Claims: PS
            "MA", // Claims: EH
            "NL", // Territories: AW, BQ, CW, SX
            "NO", // Territories: BV, SJ. Claims: AQ
            "NZ", // Territories: TK. Associated states: CK, NU. Claims: AQ
            "RS", // Claims: XK
            "US" // Territories: AS, GU, MP, PR, UM (uninhabited), VI
        )
    }
}
