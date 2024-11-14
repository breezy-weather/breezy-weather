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

import android.app.Application
import breezyweather.domain.location.model.Location
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.breezyweather.BreezyWeather
import org.breezyweather.common.basic.GeoViewModel
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.main.utils.RefreshErrorType
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    application: Application?,
    repository: SearchActivityRepository,
) : GeoViewModel(application!!) {
    private val _listResource = MutableStateFlow<Pair<List<Location>, LoadableLocationStatus>>(
        Pair(emptyList(), LoadableLocationStatus.SUCCESS)
    )
    val listResource = _listResource.asStateFlow()
    private val _locationSearchSource: MutableStateFlow<String> = MutableStateFlow(
        repository.lastSelectedLocationSearchSource
    )
    val locationSearchSource = _locationSearchSource.asStateFlow()
    private val mRepository: SearchActivityRepository = repository

    fun requestLocationList(str: String) {
        mRepository.cancel()
        mRepository.searchLocationList(
            getApplication(),
            str.trim(),
            locationSearchSource.value
        ) { result: Pair<List<Location>?, RefreshErrorType?>?, _: Boolean ->
            result?.second?.let { msg ->
                msg.showDialogAction?.let { showDialogAction ->
                    SnackbarHelper.showSnackbar(
                        content = (getApplication() as Application).getString(msg.shortMessage),
                        action = (getApplication() as Application).getString(msg.actionButtonMessage)
                    ) {
                        BreezyWeather.instance.topActivity?.let { topActivity ->
                            showDialogAction(topActivity)
                        }
                    }
                } ?: SnackbarHelper.showSnackbar((getApplication() as Application).getString(msg.shortMessage))
                _listResource.value = Pair(emptyList(), LoadableLocationStatus.ERROR)
            } ?: run {
                result?.first?.let {
                    _listResource.value = Pair(it, LoadableLocationStatus.SUCCESS)
                } ?: run {
                    _listResource.value = Pair(emptyList(), LoadableLocationStatus.ERROR)
                }
            }
        }
        _listResource.value = Pair(emptyList(), LoadableLocationStatus.LOADING)
    }

    fun setEnabledSource(locSearchSource: String) {
        mRepository.lastSelectedLocationSearchSource = locSearchSource
        _locationSearchSource.value = locSearchSource
    }

    override fun onCleared() {
        super.onCleared()
        mRepository.cancel()
    }
}
