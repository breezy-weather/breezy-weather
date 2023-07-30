package org.breezyweather.common.source

import android.content.Context
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.wrappers.SecondaryWeatherWrapper

/**
 * Initial implementation of secondary weather source
 * Interface may change any time
 */
interface SecondaryWeatherSource : Source {

    val supportedFeatures: List<SecondaryWeatherSourceFeature>

    fun isFeatureSupportedForLocation(feature: SecondaryWeatherSourceFeature, location: Location): Boolean = true

    // TODO: Improve
    val airQualityAttribution: String?
    val allergenAttribution: String?
    val minutelyAttribution: String?
    val alertAttribution: String?

    /**
     * Returns secondary weather converted to Breezy Weather Weather object
     * For efficiency reasons, we have one single functions, but don’t worry, you will never
     * be asked to provide allergen if you don’t support allergen
     * Only process things you are asked to process and that you support
     * @return an Observable of the Secondary Weather wrapper containing elements asked
     */
    fun requestSecondaryWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<SecondaryWeatherWrapper>

}
