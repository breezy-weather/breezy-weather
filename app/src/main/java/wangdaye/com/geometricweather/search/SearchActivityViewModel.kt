package wangdaye.com.geometricweather.search

import android.app.Application
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import wangdaye.com.geometricweather.common.basic.GeoViewModel
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource
import java.util.*

class SearchActivityViewModel @ViewModelInject constructor(
        application: Application,
        private val repository: SearchActivityRepository
) : GeoViewModel(application) {

    val listResource = MutableLiveData<LoadableLocationList>()
    private val query = MutableLiveData<String>()
    val enabledSources = MutableLiveData<List<WeatherSource>>()

    // coroutines.
    private var searchRequest: Job? = null

    init {
        listResource.value = LoadableLocationList(ArrayList(), LoadableLocationList.Status.SUCCESS)
        query.value = ""
        enabledSources.value = repository.getValidWeatherSources(getApplication())
    }

    @JvmOverloads
    fun requestLocationList(q /* query */ : String = getQueryValue()) {
        val oldList = innerGetLocationList()

        searchRequest?.cancel()

        listResource.value = LoadableLocationList(oldList, LoadableLocationList.Status.LOADING)
        query.value = q

        searchRequest = viewModelScope.launch {
            val results = repository.searchLocationList(
                    getApplication(), q, getEnabledSourcesValue())

            if (results.isNotEmpty()) {
                listResource.setValue(
                        LoadableLocationList(results, LoadableLocationList.Status.SUCCESS))
            } else {
                listResource.setValue(
                        LoadableLocationList(oldList, LoadableLocationList.Status.ERROR))
            }
        }
    }

    fun setEnabledSources(enabledSources: List<WeatherSource>) {
        repository.setValidWeatherSources(enabledSources)
        this.enabledSources.value = enabledSources
    }

    private fun innerGetLocationList(): List<Location> {
        return if (listResource.value == null) {
            emptyList()
        } else {
            listResource.value!!.dataList
        }
    }

    fun getLocationList(): List<Location> = Collections.unmodifiableList(innerGetLocationList())

    fun getLocationCount(): Int {
        return innerGetLocationList().size
    }

    fun getQuery(): MutableLiveData<String> {
        return query
    }

    fun getQueryValue(): String {
        return if (query.value == null) {
            ""
        } else {
            query.value!!
        }
    }

    fun getEnabledSourcesValue(): List<WeatherSource> {
        return if (enabledSources.value == null) {
            emptyList()
        } else {
            enabledSources.value!!
        }
    }
}