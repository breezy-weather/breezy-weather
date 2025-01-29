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
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableObserver
import org.breezyweather.BuildConfig
import org.breezyweather.common.rxjava.ObserverContainer
import org.breezyweather.common.rxjava.SchedulerTransformer
import org.breezyweather.domain.settings.ConfigStore
import org.breezyweather.sources.RefreshHelper
import org.breezyweather.ui.main.utils.RefreshErrorType
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
            .subscribe(
                ObserverContainer(
                    mCompositeDisposable,
                    object : DisposableObserver<List<Location>>() {
                        override fun onNext(t: List<Location>) {
                            callback(Pair<List<Location>, RefreshErrorType?>(t, null), true)
                        }

                        override fun onError(e: Throwable) {
                            callback(
                                Pair<List<Location>, RefreshErrorType?>(
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
