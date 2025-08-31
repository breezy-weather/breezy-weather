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

package org.breezyweather.ui.search

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceFeature
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableObserver
import org.breezyweather.BuildConfig
import org.breezyweather.common.rxjava.ObserverContainer
import org.breezyweather.common.rxjava.SchedulerTransformer
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.domain.location.model.applyDefaultPreset
import org.breezyweather.domain.settings.ConfigStore
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.sources.RefreshHelper
import org.breezyweather.sources.SourceManager
import org.breezyweather.ui.main.utils.RefreshErrorType
import javax.inject.Inject

class SearchActivityRepository @Inject internal constructor(
    @ApplicationContext context: Context,
    private val mRefreshHelper: RefreshHelper,
    private val mSourceManager: SourceManager,
    private val mCompositeDisposable: CompositeDisposable,
) {
    private val mConfig: ConfigStore = ConfigStore(context, PREFERENCE_SEARCH_CONFIG)

    fun searchLocationList(
        context: Context,
        query: String,
        locationSearchSource: String,
        callback: (t: Pair<List<LocationAddressInfo>?, RefreshErrorType?>?, done: Boolean) -> Unit,
    ) {
        mRefreshHelper
            .requestSearchLocations(context, query, locationSearchSource)
            .compose(SchedulerTransformer.create())
            .subscribe(
                ObserverContainer(
                    mCompositeDisposable,
                    object : DisposableObserver<List<LocationAddressInfo>>() {
                        override fun onNext(t: List<LocationAddressInfo>) {
                            callback(Pair<List<LocationAddressInfo>, RefreshErrorType?>(t, null), true)
                        }

                        override fun onError(e: Throwable) {
                            callback(
                                Pair<List<LocationAddressInfo>, RefreshErrorType?>(
                                    emptyList(),
                                    RefreshErrorType.getTypeFromThrowable(
                                        context,
                                        e,
                                        RefreshErrorType.LOCATION_SEARCH_FAILED
                                    )
                                ),
                                true
                            )
                        }

                        override fun onComplete() {
                            // do nothing.
                        }
                    }
                )
            )
    }

    suspend fun getLocationWithDisambiguatedCountryCode(
        location: Location,
        locationSearchSource: LocationSearchSource,
        context: Context,
    ): Location {
        return if (locationSearchSource.knownAmbiguousCountryCodes?.any { cc ->
                location.countryCode.equals(cc, ignoreCase = true)
            } != false ||
            location.countryCode.equals("AN", ignoreCase = true)
        ) {
            mRefreshHelper.getLocationWithDisambiguatedCountryCode(location, context)
        } else {
            location
        }
    }

    fun getLocationWithAppliedPreference(
        location: Location,
        context: Context,
    ): Location {
        val defaultSource = SettingsManager.getInstance(context).defaultForecastSource

        return when (defaultSource) {
            "auto" -> location.applyDefaultPreset(mSourceManager)
            else -> {
                val source = mSourceManager.getWeatherSource(defaultSource)
                if (source == null) {
                    location.applyDefaultPreset(mSourceManager)
                } else {
                    location.copy(
                        forecastSource = source.id,
                        currentSource = if (SourceFeature.CURRENT in
                            source.supportedFeatures &&
                            source.isFeatureSupportedForLocation(
                                location,
                                SourceFeature.CURRENT
                            )
                        ) {
                            source.id
                        } else {
                            null
                        },
                        airQualitySource = if (SourceFeature.AIR_QUALITY in
                            source.supportedFeatures &&
                            source.isFeatureSupportedForLocation(
                                location,
                                SourceFeature.AIR_QUALITY
                            )
                        ) {
                            source.id
                        } else {
                            null
                        },
                        pollenSource = if (SourceFeature.POLLEN in
                            source.supportedFeatures &&
                            source.isFeatureSupportedForLocation(
                                location,
                                SourceFeature.POLLEN
                            )
                        ) {
                            source.id
                        } else {
                            null
                        },
                        minutelySource = if (SourceFeature.MINUTELY in
                            source.supportedFeatures &&
                            source.isFeatureSupportedForLocation(
                                location,
                                SourceFeature.MINUTELY
                            )
                        ) {
                            source.id
                        } else {
                            null
                        },
                        alertSource = if (SourceFeature.ALERT in
                            source.supportedFeatures &&
                            source.isFeatureSupportedForLocation(
                                location,
                                SourceFeature.ALERT
                            )
                        ) {
                            source.id
                        } else {
                            null
                        },
                        normalsSource = if (SourceFeature.NORMALS in
                            source.supportedFeatures &&
                            source.isFeatureSupportedForLocation(
                                location,
                                SourceFeature.NORMALS
                            )
                        ) {
                            source.id
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }

    var lastSelectedLocationSearchSource: String
        set(value) {
            mConfig.edit().putString(KEY_LAST_DEFAULT_SOURCE, value).apply()
        }
        get() {
            return mConfig.getString(KEY_LAST_DEFAULT_SOURCE, null)
                ?: BuildConfig.DEFAULT_LOCATION_SEARCH_SOURCE
        }

    fun cancel() {
        mCompositeDisposable.clear()
    }

    companion object {
        private const val PREFERENCE_SEARCH_CONFIG = "SEARCH_CONFIG"
        private const val KEY_LAST_DEFAULT_SOURCE = "LAST_DEFAULT_SOURCE"
    }
}
