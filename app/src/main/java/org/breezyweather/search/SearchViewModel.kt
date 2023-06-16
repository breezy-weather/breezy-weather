package org.breezyweather.search

import android.app.Application
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.breezyweather.common.basic.GeoViewModel
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.provider.WeatherSource
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    application: Application?,
    repository: SearchActivityRepository
) : GeoViewModel(application!!) {
    private val _listResource = MutableStateFlow<Pair<List<Location>, LoadableLocationStatus>>(Pair(emptyList(), LoadableLocationStatus.SUCCESS))
    val listResource = _listResource.asStateFlow()
    private val _enabledSource: MutableStateFlow<WeatherSource> = MutableStateFlow(repository.getValidWeatherSource(application))
    val enabledSource = _enabledSource.asStateFlow()
    private val mRepository: SearchActivityRepository = repository

    @JvmOverloads
    fun requestLocationList(str: String) {
        mRepository.cancel()
        mRepository.searchLocationList(
            application,
            str,
            enabledSource.value
        ) { locationList: List<Location>?, done: Boolean ->
            if (locationList != null) {
                _listResource.value = Pair(locationList, LoadableLocationStatus.SUCCESS)
            } else {
                _listResource.value = Pair(emptyList(), LoadableLocationStatus.ERROR)
            }
        }
        _listResource.value = Pair(emptyList(), LoadableLocationStatus.LOADING)
    }

    fun setEnabledSource(weatherSource: WeatherSource) {
        mRepository.setValidWeatherSource(weatherSource)
        _enabledSource.value = weatherSource
    }

    override fun onCleared() {
        super.onCleared()
        mRepository.cancel()
    }

    val enabledSourceValue: WeatherSource
        get() = enabledSource.value
}
