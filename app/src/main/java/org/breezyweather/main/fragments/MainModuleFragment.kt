package org.breezyweather.main.fragments

import org.breezyweather.common.bus.EventBus

class ModifyMainSystemBarMessage

abstract class MainModuleFragment: org.breezyweather.common.basic.GeoFragment() {

    protected fun checkToSetSystemBarStyle() {
        EventBus
            .instance
            .with(ModifyMainSystemBarMessage::class.java)
            .postValue(ModifyMainSystemBarMessage())
    }

    abstract fun setSystemBarStyle()
}