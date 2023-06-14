package org.breezyweather.common.utils.helpers

import android.widget.ImageView
import androidx.annotation.DrawableRes
import coil.load

fun load(imageView: ImageView, @DrawableRes resId: Int) {
    imageView.load(resId)
}
