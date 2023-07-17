package org.breezyweather.sources.noreversegeocoding

import android.content.Context
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.source.ReverseGeocodingSource
import javax.inject.Inject

/**
 * Default reverse geocoding implementation for sources without reverse geocoding
 *
 * Sources without reverse geocoding only need longitude and latitude
 * Just return the same location in that case
 * TimeZone is initialized with the TimeZone from the phone
 * (which, unless you like exotic configurations, will be the same as the current position)
 */
class NoReverseGeocodingService @Inject constructor() : ReverseGeocodingSource {

    override val id = "noreversegeocoding"
    override val name = "No reverse geocoding"

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location
    ): Observable<List<Location>> {
        return Observable.create { emitter ->
            emitter.onNext(listOf(location.copy(cityId = location.latitude.toString() + "," + location.longitude)))
        }
    }
}