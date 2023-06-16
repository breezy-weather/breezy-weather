package org.breezyweather.main.fragments

import org.breezyweather.common.basic.GeoFragment
import org.breezyweather.common.bus.EventBus

class ModifyMainSystemBarMessage

abstract class MainModuleFragment: GeoFragment() {

    protected fun checkToSetSystemBarStyle() {
        EventBus
            .instance
            .with(ModifyMainSystemBarMessage::class.java)
            .postValue(ModifyMainSystemBarMessage())
    }

    abstract fun setSystemBarStyle()
}