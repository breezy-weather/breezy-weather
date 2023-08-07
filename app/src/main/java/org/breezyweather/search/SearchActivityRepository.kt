/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.search

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableObserver
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import org.breezyweather.BuildConfig
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.exceptions.ApiLimitReachedException
import org.breezyweather.common.exceptions.LocationSearchException
import org.breezyweather.common.exceptions.NoNetworkException
import org.breezyweather.common.exceptions.ParsingException
import org.breezyweather.common.exceptions.UpdateNotAvailableYetException
import org.breezyweather.common.rxjava.ObserverContainer
import org.breezyweather.common.rxjava.SchedulerTransformer
import org.breezyweather.main.utils.RequestErrorType
import org.breezyweather.settings.ConfigStore
import org.breezyweather.sources.WeatherHelper
import retrofit2.HttpException
import java.net.SocketTimeoutException
import javax.inject.Inject

class SearchActivityRepository @Inject internal constructor(
    @ApplicationContext context: Context,
    private val mWeatherHelper: WeatherHelper,
    private val mCompositeDisposable: CompositeDisposable
) {
    private val mConfig: ConfigStore = ConfigStore(context, PREFERENCE_SEARCH_CONFIG)

    fun searchLocationList(
        context: Context, query: String, enabledSource: String,
        callback: (t: Pair<List<Location>?, RequestErrorType?>?, done: Boolean) -> Unit
    ) {
        mWeatherHelper
            .requestSearchLocations(context, query, enabledSource)
            .compose(SchedulerTransformer.create())
            .subscribe(ObserverContainer(mCompositeDisposable, object : DisposableObserver<List<Location>>() {
                override fun onNext(t: List<Location>) {
                    callback(Pair<List<Location>, RequestErrorType?>(t, null), true)
                }

                override fun onError(e: Throwable) {
                    val requestErrorType = when (e) {
                        is NoNetworkException -> RequestErrorType.NETWORK_UNAVAILABLE
                        is HttpException -> {
                            when (e.code()) {
                                401, 403 -> RequestErrorType.API_UNAUTHORIZED
                                409, 429 -> RequestErrorType.API_LIMIT_REACHED
                                else -> {
                                    e.printStackTrace()
                                    RequestErrorType.LOCATION_SEARCH_FAILED
                                }
                            }
                        }
                        is ApiLimitReachedException -> RequestErrorType.API_LIMIT_REACHED
                        is SocketTimeoutException -> RequestErrorType.SERVER_TIMEOUT
                        is ApiKeyMissingException -> RequestErrorType.API_KEY_REQUIRED_MISSING
                        is UpdateNotAvailableYetException -> RequestErrorType.UPDATE_NOT_YET_AVAILABLE
                        is MissingFieldException, is SerializationException, is ParsingException -> {
                            e.printStackTrace()
                            RequestErrorType.PARSING_ERROR
                        }
                        is LocationSearchException -> RequestErrorType.LOCATION_SEARCH_FAILED
                        else -> {
                            e.printStackTrace()
                            RequestErrorType.LOCATION_SEARCH_FAILED
                        }
                    }
                    callback(
                        Pair<List<Location>, RequestErrorType?>(emptyList(), requestErrorType),
                        true
                    )
                }

                override fun onComplete() {
                    // do nothing.
                }
            }))
    }

    var lastSelectedWeatherSource: String
        set(value) {
            mConfig.edit().putString(KEY_LAST_DEFAULT_SOURCE, value).apply()
        }
        get() = mConfig.getString(KEY_LAST_DEFAULT_SOURCE, null) ?: BuildConfig.DEFAULT_WEATHER_SOURCE

    fun cancel() {
        mCompositeDisposable.clear()
    }

    companion object {
        private const val PREFERENCE_SEARCH_CONFIG = "SEARCH_CONFIG"
        private const val KEY_LAST_DEFAULT_SOURCE = "LAST_DEFAULT_SOURCE"
    }
}
