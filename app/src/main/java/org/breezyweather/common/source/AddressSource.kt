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
     *
     * Below are cities / coordinates to test each ambiguity.
     * You need to test EACH location to validate that a country code is not ambiguous. Don’t assume that because
     *  the first ones succeed that the following will also. We have many evidences in the existing sources that
     *  exceptions are common
     * Tip: You can copy the following coordinates and make a bulk replace of , with "&lon=" to semi-generate URLs
     * AU:
     * - CX: Flying Fish Cove -10.421667,105.678056
     * - CC: Bantam -12.1178,96.8975
     * - HM: Mawson Peak -53.1,73.516667
     * - NF: Kingston -29.056,167.961
     * CN:
     * - HK: Hong Kong 22.38715,114.19534
     * - MO: Macau 22.19,113.54
     * - TW: Taipei 25.0375,121.5625
     * DK:
     * - FO: Tórshavn 62,-6.783333
     * - GL: Nuuk 64.176667,-51.736111
     * FI:
     * - AX: Eckerö 60.216667,19.55
     * FR:
     * - GP (971): Pointe-à-Pitre 16.2411,-61.5331
     * - MQ (972): Fort-de-France 14.6,-61.066667
     * - GF (973): Cayenne 4.9372,-52.326
     * - RE (974): Le Tampon -21.2781,55.5153
     * - PM (975): Saint-Pierre 46.7778,-56.1778
     * - YT (976): Mamoudzou -12.7806,45.2278
     * - BL (977): Gustavia 17.897908,-62.850556
     * - MF (978): Marigot 18.07,-63.01
     * - TF (984): Port-aux-Français -49.35,70.218889
     * - AQ (984): Dumont D'Urville -66.662778,140.001111
     * - WF (986): Mata Utu -13.283333,-176.183333
     * - PF (987): Papeete -17.566667,-149.6
     * - NC (988): Nouméa -22.266667,166.466667
     * - CP (ignore if you have no way to make it recognize as CP): Clipperton 10.3,-109.216667
     * GB:
     * - AI: The Valley 18.220833,-63.051667
     * - BM: Hamilton 32.296111,-64.782778
     * - IO: Diego Garcia -7.313333,72.411111
     * - KY: George Town 19.296389,-81.381667
     * - FK: Stanley -51.695278,-57.849444
     * - GI: Gibraltar 36.14,-5.35
     * - GG: St. Peter Port 49.4555,-2.5368
     * - IM: Douglas 54.15,-4.48
     * - JE: St Helier 49.19,-2.11
     * - MS: Brades 16.792778,-62.210556
     * - PN: Adamstown -25.066667,-130.1
     * - SH:
     * -- Ascension: Two Boats -7.937,-14.364
     * -- Saint Helena: Half Tree Hollow -15.933333,-5.72
     * -- Tristan da Cunha: Edinburgh of the Seven Seas -37.0675,-12.311111
     * - GS: King Edward Point -54.283333,-36.5
     * - TC: Grand Turk (Cockburn Town) 21.459,-71.139
     * - VG: Road Town 18.431389,-64.623056
     * IL:
     * - PS: Gaza city 31.516667,34.45
     * MA:
     * - EH: Tifariti 26.158056,-10.566944
     * NL:
     * - AW: Oranjestad (not to be confused with BQ) 12.518611,-70.035833
     * - BQ (old: AN):
     * -- Bonaire: Kralendijk 12.144444,-68.265556
     * -- Sint Eustatius: Oranjestad (not to be confused with AW) 17.483333,-62.983333
     * -- Saba: The Bottom 17.626111,-63.249167
     * - CW (old: AN): Willemstad 12.116667,-68.933333
     * - SX (old: AN): Lower Prince's Quarter 18.052778,-63.0425
     * NO:
     * - BV: Bouvet Island -54.42,3.36
     * - SJ:
     * -- Svalbard: Longyearbyen 78.22,15.65
     * -- Jan Mayen: Olonkinbyen 70.922,-8.715
     * NZ:
     * - TK: Atafu -8.557222,-172.470833
     * - CK: Avarua -21.206944,-159.770833
     * - NU: Alofi -19.053889,-169.92
     * RS:
     * - XK: Pristina 42.663333,21.162222
     * US:
     * - AS: Tāfuna -14.335833,-170.72
     * - GU: Dededo 13.509492,144.836528
     * - MP: Saipan 15.183333,145.75
     * - PR: Arecibo 18.375,-66.625
     * - UM: Baker Island 0.195833,-176.479167
     * - VI: Charlotte Amalie 18.35,-64.933333
     */
    val knownAmbiguousCountryCodes: Array<String>?

    companion object {
        /**
         * For technical reasons, we need to better identify each territory
         * Crimea is not included to let each location search/address lookup source resolves it the way they want
         *  and we will resolve the timezone as Europe/Simferopol whether identified as UA or RU
         * Also ignores Antarctica claims, because even if a source actually supports that claim, no one lives there
         */
        val ambiguousCountryCodes = arrayOf(
            "AU", // Territories: CX, CC, HM (uninhabited), NF
            "CN", // Territories: HK, MO. Claims: TW
            "DK", // Territories: FO, GL
            "FI", // Territories: AX
            "FR", // Territories: GF, PF, TF (uninhabited), GP, MQ, YT, NC, RE, BL, MF, PM, WF, CP. Claims: AQ
            "GB", // Territories: AI, BM, IO, KY, FK, GI, GG, IM, JE, MS, PN, SH, GS (uninhabited), TC, VG
            "IL", // Claims: PS
            "MA", // Claims: EH
            "NL", // Territories: AW, BQ, CW, SX
            "NO", // Territories: BV, SJ
            "NZ", // Territories: TK. Associated states: CK, NU
            "RS", // Claims: XK
            "US" // Territories: AS, GU, MP, PR, UM (uninhabited), VI
        )
    }
}
