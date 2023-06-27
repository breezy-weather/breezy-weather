package org.breezyweather.extensions

import android.content.Context
import android.os.PowerManager
import androidx.core.content.getSystemService
import java.io.File

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