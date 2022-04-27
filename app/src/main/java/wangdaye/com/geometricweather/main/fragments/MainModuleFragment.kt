package wangdaye.com.geometricweather.main.fragments

import wangdaye.com.geometricweather.common.basic.GeoFragment
import wangdaye.com.geometricweather.common.bus.EventBus

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