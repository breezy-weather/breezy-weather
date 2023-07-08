package org.breezyweather.common.basic

import android.app.Application
import androidx.lifecycle.AndroidViewModel

// TODO: Issue with getter on application when converted to Kotlin
open class GeoViewModel(application: Application) : AndroidViewModel(application) {
    private var mNewInstance = true
    fun checkIsNewInstance(): Boolean {
        val result = mNewInstance
        mNewInstance = false
        return result
    }
}
