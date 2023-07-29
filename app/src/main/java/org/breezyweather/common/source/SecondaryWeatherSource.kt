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

    val isAirQualitySupported: Boolean
    val isAllergenSupported: Boolean
    val isMinutelySupported: Boolean
    val isAlertSupported: Boolean

    fun isAirQualitySupportedForLocation(location: Location): Boolean = true
    fun isAllergenSupportedForLocation(location: Location): Boolean = true
    fun isMinutelySupportedForLocation(location: Location): Boolean = true
    fun isAlertsSupportedForLocation(location: Location): Boolean = true

    val airQualityAttribution: String?
    val allergenAttribution: String?
    val minutelyAttribution: String?
    val alertAttribution: String?

    /**
     * Returns secondary weather converted to Breezy Weather Weather object
     * For efficiency reasons, we have one single functions, but don’t worry, you will never
     * be asked to provide allergen if you defined isAllergenSupported as false
     * Only process things you are asked to process and that you support
     * @param airQuality don’t return airQuality data if false
     * @param allergen don’t return allergen data if false
     * @param minutely don’t return minutely data if false
     * @param alerts don’t return alerts data if false
     * @return an Observable of the Secondary Weather wrapper containing elements asked
     */
    fun requestSecondaryWeather(
        context: Context,
        location: Location,
        airQuality: Boolean,
        allergen: Boolean,
        minutely: Boolean,
        alerts: Boolean
    ): Observable<SecondaryWeatherWrapper>

}
