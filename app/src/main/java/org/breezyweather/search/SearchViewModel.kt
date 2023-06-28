package org.breezyweather.search

import android.app.Application
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.breezyweather.common.basic.GeoViewModel
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.provider.WeatherSource
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.main.utils.RequestErrorType
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    application: Application?,
    repository: SearchActivityRepository
) : GeoViewModel(application!!) {
    private val _listResource = MutableStateFlow<Pair<List<Location>, LoadableLocationStatus>>(Pair(emptyList(), LoadableLocationStatus.SUCCESS))
    val listResource = _listResource.asStateFlow()
    private val _enabledSource: MutableStateFlow<WeatherSource> = MutableStateFlow(repository.lastSelectedWeatherSource)
    val enabledSource = _enabledSource.asStateFlow()
    private val mRepository: SearchActivityRepository = repository

    @JvmOverloads
    fun requestLocationList(str: String) {
        mRepository.cancel()
        mRepository.searchLocationList(
            application,
            str,
            enabledSource.value
        ) { result: Pair<List<Location>?, RequestErrorType?>?, _: Boolean ->
            result?.second?.let {
                // TODO: Also show actions
                SnackbarHelper.showSnackbar(application.getString(it.shortMessage))
                _listResource.value = Pair(emptyList(), LoadableLocationStatus.ERROR)
            } ?: run {
                result?.first?.let {
                    _listResource.value = Pair(it, LoadableLocationStatus.SUCCESS)
                } ?: {
                    _listResource.value = Pair(emptyList(), LoadableLocationStatus.ERROR)
                }
            }
        }
        _listResource.value = Pair(emptyList(), LoadableLocationStatus.LOADING)
    }

    fun setEnabledSource(weatherSource: WeatherSource) {
        mRepository.lastSelectedWeatherSource = weatherSource
        _enabledSource.value = weatherSource
    }

    override fun onCleared() {
        super.onCleared()
        mRepository.cancel()
    }

    val enabledSourceValue: WeatherSource
        get() = enabledSource.value
}
