package org.breezyweather.common.source

import android.content.Context
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.basic.models.Location

/**
 * Location search source
 */
interface LocationSearchSource : Source {
    /**
     * Credits and acknowledgments that will be shown at the bottom of main screen
     * Please check terms of the source to be sure to put the correct term here
     * Example: MyGreatApi (CC BY 4.0)
     *
     * Will not be displayed if identical to weatherAttribution
     */
    val locationSearchAttribution: String

    /**
     * Returns a list of Breezy Weather Location results from a query
     */
    fun requestLocationSearch(context: Context, query: String): Observable<List<Location>>

}
