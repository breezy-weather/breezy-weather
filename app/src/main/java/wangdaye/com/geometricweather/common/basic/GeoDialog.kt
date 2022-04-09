package wangdaye.com.geometricweather.common.basic

import android.content.Context
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

abstract class GeoDialog : DialogFragment() {

    companion object {
        fun injectStyle(f: DialogFragment) {
            f.lifecycle.addObserver(LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_CREATE -> if (f.view != null) {
                        f.setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Dialog_MinWidth)
                    }
                    Lifecycle.Event.ON_START -> if (f.view != null) {
                        f.requireDialog().window!!.setBackgroundDrawableResource(wangdaye.com.geometricweather.R.drawable.dialog_background)
                    }
                    else -> {}
                }
            })
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        injectStyle(this)
    }
}