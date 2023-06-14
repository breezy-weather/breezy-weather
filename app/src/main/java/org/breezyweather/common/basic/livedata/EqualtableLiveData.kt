package org.breezyweather.common.basic.livedata

import androidx.lifecycle.MutableLiveData

class EqualtableLiveData<T>(
    value: T? = null
): MutableLiveData<T>(value) {

    override fun setValue(value: T) {
        if (value == this.value) {
            return
        }
        super.setValue(value)
    }

    override fun postValue(value: T) {
        // this.value is a volatile value.
        if (value == this.value) {
            return
        }
        super.postValue(value)
    }
}