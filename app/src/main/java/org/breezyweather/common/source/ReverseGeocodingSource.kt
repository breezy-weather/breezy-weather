package org.breezyweather.common.source

import android.content.Context
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.basic.models.Location

/**
 * Reverse geocoding source
 */
interface ReverseGeocodingSource : Source {

    /**
     * Returns true if location can be used directly
     * or false if needs a reverse geocoding processing before it can be used
     */
    fun isUsable(location: Location): Boolean

    /**
     * Returns location converted to Breezy Weather Location object
     * cityId is mandatory
     */
    fun requestReverseGeocodingLocation(context: Context, location: Location): Observable<List<Location>>

}
