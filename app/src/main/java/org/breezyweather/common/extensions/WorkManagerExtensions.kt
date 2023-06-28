package org.breezyweather.common.extensions

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager

/**
 * Taken from Tachiyomi
 * Apache License, Version 2.0
 *
 * https://github.com/tachiyomiorg/tachiyomi/blob/859601a46e5d32cd14979177832edaf342615e19/app/src/main/java/eu/kanade/tachiyomi/util/system/WorkManagerExtensions.kt
 */

val Context.workManager: WorkManager
    get() = WorkManager.getInstance(this)

fun WorkManager.isRunning(tag: String): Boolean {
    val list = this.getWorkInfosByTag(tag).get()
    return list.any { it.state == WorkInfo.State.RUNNING }
}