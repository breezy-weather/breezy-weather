package org.breezyweather.common.source

import android.content.Context
import androidx.annotation.ColorInt
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.basic.models.Location

/**
 * Weather service.
 */
interface WeatherSource : Source {
    /**
     * Official color used by the source
     */
    @get:ColorInt
    val color: Int

    /**
     * Credits and acknowledgments that will be shown at the bottom of main screen
     * Please check terms of the source to be sure to put the correct term here
     * Example: MyGreatApi CC BY 4.0
     */
    val weatherAttribution: String

    /**
     * Returns weather converted to Breezy Weather Weather object
     */
    fun requestWeather(context: Context, location: Location): Observable<WeatherResultWrapper>

}
