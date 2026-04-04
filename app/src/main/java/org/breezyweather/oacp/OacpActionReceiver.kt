package org.breezyweather.oacp

import android.content.Context
import org.oacp.android.OacpParams
import org.oacp.android.OacpReceiver
import org.oacp.android.OacpResult

/**
 * Handles background OACP actions for Breezy Weather.
 *
 * Only broadcast (background) actions live here:
 * - check_weather: returns current conditions
 * - check_forecast: returns forecast data
 *
 * The foreground action (open_weather) is handled by the main
 * Activity via an activity intent filter — Hark launches it
 * directly with startActivity().
 */
class OacpActionReceiver : OacpReceiver() {

    override fun onAction(
        context: Context,
        action: String,
        params: OacpParams,
        requestId: String?
    ): OacpResult? {
        return when {
            action.endsWith(".oacp.ACTION_CHECK_WEATHER") -> {
                val location = params.getString("location")
                // TODO: Query actual weather data from the app's database
                OacpResult.success("Weather check for ${location ?: "current location"}. Real data integration pending.")
            }
            action.endsWith(".oacp.ACTION_CHECK_FORECAST") -> {
                val location = params.getString("location")
                // TODO: Query actual forecast data from the app's database
                OacpResult.success("Forecast for ${location ?: "current location"}. Real data integration pending.")
            }
            else -> null
        }
    }
}
