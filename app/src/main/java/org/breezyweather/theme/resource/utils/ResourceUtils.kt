package org.breezyweather.theme.resource.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.annotation.AnyRes

object ResourceUtils {
    @AnyRes
    fun getResId(context: Context, resName: String, type: String): Int {
        return try {
            context.classLoader
                .loadClass(context.packageName + ".R$" + type)
                .getField(resName)
                .getInt(null)
        } catch (e: Exception) {
            // TODO: Dirty way to avoid crashes on debug build (because of applicationIdSuffix ".debug")
            try {
                context.classLoader
                    .loadClass("org.breezyweather.R$$type")
                    .getField(resName)
                    .getInt(null)
            } catch (ignored: Exception) {
                0
            }
        }
    }

    fun getDrawableUri(pkgName: String, resType: String, resName: String): Uri {
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(pkgName)
            .appendPath(resType)
            .appendPath(resName)
            .build()
    }
}
