package org.breezyweather.common.extensions

import android.content.Context
import android.os.PowerManager
import androidx.core.content.getSystemService
import java.io.File

/**
 * Taken from Tachiyomi
 * Apache License, Version 2.0
 *
 * https://github.com/tachiyomiorg/tachiyomi/blob/9a10656bf07a7dd35400fa6e42dd0e4889ddb177/app/src/main/java/eu/kanade/tachiyomi/util/system/ContextExtensions.kt
 */
val Context.powerManager: PowerManager
    get() = getSystemService()!!

fun Context.createFileInCacheDir(name: String): File {
    val file = File(externalCacheDir, name)
    if (file.exists()) {
        file.delete()
    }
    file.createNewFile()
    return file
}
