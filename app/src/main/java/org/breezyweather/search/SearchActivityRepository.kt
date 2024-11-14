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

package org.breezyweather.search

import android.content.Context
import breezyweather.domain.location.model.Location
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableObserver
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import org.breezyweather.BuildConfig
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.exceptions.ApiLimitReachedException
import org.breezyweather.common.exceptions.ApiUnauthorizedException
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.exceptions.LocationSearchException
import org.breezyweather.common.exceptions.NoNetworkException
import org.breezyweather.common.exceptions.ParsingException
import org.breezyweather.common.extensions.getStringByLocale
import org.breezyweather.common.rxjava.ObserverContainer
import org.breezyweather.common.rxjava.SchedulerTransformer
import org.breezyweather.common.utils.helpers.LogHelper
import org.breezyweather.main.utils.RefreshErrorType
import org.breezyweather.settings.ConfigStore
import org.breezyweather.sources.RefreshHelper
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.ParseException
import javax.inject.Inject

class SearchActivityRepository @Inject internal constructor(
    @ApplicationContext context: Context,
    private val mRefreshHelper: RefreshHelper,
    private val mCompositeDisposable: CompositeDisposable,
) {
    private val mConfig: ConfigStore = ConfigStore(context, PREFERENCE_SEARCH_CONFIG)

    fun searchLocationList(
        context: Context,
        query: String,
        locationSearchSource: String,
        callback: (t: Pair<List<Location>?, RefreshErrorType?>?, done: Boolean) -> Unit,
    ) {
        mRefreshHelper
            .requestSearchLocations(context, query, locationSearchSource)
            .compose(SchedulerTransformer.create())
            .subscribe(ObserverContainer(mCompositeDisposable, object : DisposableObserver<List<Location>>() {
                override fun onNext(t: List<Location>) {
                    callback(Pair<List<Location>, RefreshErrorType?>(t, null), true)
                }

                override fun onError(e: Throwable) {
                    val refreshErrorType = when (e) {
                        is NoNetworkException -> RefreshErrorType.NETWORK_UNAVAILABLE
                        // Can mean different things but most of the time, it’s a network issue:
                        is UnknownHostException -> RefreshErrorType.NETWORK_UNAVAILABLE
                        is HttpException -> {
                            LogHelper.log(msg = "HttpException ${e.code()}")
                            when (e.code()) {
                                401, 403 -> RefreshErrorType.API_UNAUTHORIZED
                                409, 429 -> RefreshErrorType.API_LIMIT_REACHED
                                in 500..599 -> RefreshErrorType.SERVER_UNAVAILABLE
                                else -> {
                                    e.printStackTrace()
                                    RefreshErrorType.LOCATION_SEARCH_FAILED
                                }
                            }
                        }
                        is ApiLimitReachedException -> RefreshErrorType.API_LIMIT_REACHED
                        is ApiUnauthorizedException -> RefreshErrorType.API_UNAUTHORIZED
                        is SocketTimeoutException -> RefreshErrorType.SERVER_TIMEOUT
                        is ApiKeyMissingException -> RefreshErrorType.API_KEY_REQUIRED_MISSING
                        is MissingFieldException, is SerializationException, is ParsingException,
                        is ParseException -> {
                            e.printStackTrace()
                            RefreshErrorType.PARSING_ERROR
                        }
                        is LocationSearchException -> RefreshErrorType.LOCATION_SEARCH_FAILED
                        is InvalidOrIncompleteDataException -> RefreshErrorType.INVALID_INCOMPLETE_DATA
                        else -> {
                            e.printStackTrace()
                            RefreshErrorType.LOCATION_SEARCH_FAILED
                        }
                    }
                    LogHelper.log(msg = "Refresh error: ${context.getStringByLocale(refreshErrorType.shortMessage)}")
                    callback(
                        Pair<List<Location>, RefreshErrorType?>(emptyList(), refreshErrorType),
                        true
                    )
                }

                override fun onComplete() {
                    // do nothing.
                }
            }))
    }

    var lastSelectedLocationSearchSource: String
        set(value) {
            mConfig.edit().putString(KEY_LAST_DEFAULT_SOURCE, value).apply()
        }
        get() = if (BuildConfig.FLAVOR != "freenet") {
            mConfig.getString(KEY_LAST_DEFAULT_SOURCE, null) ?: BuildConfig.DEFAULT_LOCATION_SEARCH_SOURCE
        } else {
            "openmeteo"
        }

    fun cancel() {
        mCompositeDisposable.clear()
    }

    companion object {
        private const val PREFERENCE_SEARCH_CONFIG = "SEARCH_CONFIG"
        private const val KEY_LAST_DEFAULT_SOURCE = "LAST_DEFAULT_SOURCE"
    }
}
