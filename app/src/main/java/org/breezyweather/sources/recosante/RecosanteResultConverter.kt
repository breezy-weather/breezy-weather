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

package org.breezyweather.sources.recosante

import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Pollen
import breezyweather.domain.weather.wrappers.PollenWrapper
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.common.extensions.toTimezoneNoHour
import org.breezyweather.sources.recosante.json.RecosanteRaepIndiceDetail
import org.breezyweather.sources.recosante.json.RecosanteResult
import java.util.Calendar
import java.util.Date

internal fun getPollen(
    location: Location,
    result: RecosanteResult,
): PollenWrapper? {
    if (result.raep?.indice?.details.isNullOrEmpty()) {
        // Don’t throw an error if empty or null
        // This can happen when the weekly bulletin has not been emitted yet on Friday
        // See also bug #804
        return null
    }

    val dayList = mutableListOf<Date>()
    if (result.raep!!.validity?.start != null && result.raep.validity!!.end != null) {
        var startDate = result.raep.validity.start!!.toDateNoHour(location.javaTimeZone)
        val endDate = result.raep.validity.end!!.toDateNoHour(location.javaTimeZone)
        if (startDate != null && endDate != null) {
            var i = 0
            while (true) {
                ++i
                if (i > 10 || startDate == endDate) {
                    // End the loop if we ran for more than 10 days (means something went wrong)
                    break
                } else {
                    dayList.add(startDate!!)
                    startDate = startDate.toCalendarWithTimeZone(location.javaTimeZone).apply {
                        add(Calendar.DAY_OF_MONTH, 1)
                    }.time
                }
            }
        } else {
            dayList.add(Date().toTimezoneNoHour(location.javaTimeZone)!!)
        }
    } else {
        dayList.add(Date().toTimezoneNoHour(location.javaTimeZone)!!)
    }

    return PollenWrapper(
        dailyForecast = getPollen(result.raep.indice!!.details!!).let { pollenData ->
            dayList.associateWith { pollenData }
        }
    )
}

private fun getPollen(details: List<RecosanteRaepIndiceDetail>): Pollen {
    var alder: Int? = null
    var ash: Int? = null
    var birch: Int? = null
    var chestnut: Int? = null
    var cypress: Int? = null
    var grass: Int? = null
    var hazel: Int? = null
    var hornbeam: Int? = null
    var linden: Int? = null
    var mugwort: Int? = null
    var oak: Int? = null
    var olive: Int? = null
    var plane: Int? = null
    var plantain: Int? = null
    var poplar: Int? = null
    var ragweed: Int? = null
    var sorrel: Int? = null
    var urticaceae: Int? = null
    var willow: Int? = null

    details
        .forEach { p ->
            when (p.label) {
                "ambroisies" -> ragweed = p.indice.value
                "armoises" -> mugwort = p.indice.value
                "aulne" -> alder = p.indice.value
                "bouleau" -> birch = p.indice.value
                "charme" -> hornbeam = p.indice.value
                "chataignier" -> chestnut = p.indice.value
                "chene" -> oak = p.indice.value
                "cypres" -> cypress = p.indice.value
                "frene" -> ash = p.indice.value
                "graminees" -> grass = p.indice.value
                "noisetier" -> hazel = p.indice.value
                "olivier" -> olive = p.indice.value
                "peuplier" -> poplar = p.indice.value
                "plantain" -> plantain = p.indice.value
                "platane" -> plane = p.indice.value
                "rumex" -> sorrel = p.indice.value
                "saule" -> willow = p.indice.value
                "tilleul" -> linden = p.indice.value
                "urticacees" -> urticaceae = p.indice.value
            }
        }

    return Pollen(
        alder = alder,
        ash = ash,
        birch = birch,
        chestnut = chestnut,
        cypress = cypress,
        grass = grass,
        hazel = hazel,
        hornbeam = hornbeam,
        linden = linden,
        mugwort = mugwort,
        oak = oak,
        olive = olive,
        plane = plane,
        plantain = plantain,
        poplar = poplar,
        ragweed = ragweed,
        sorrel = sorrel,
        urticaceae = urticaceae,
        willow = willow
    )
}
