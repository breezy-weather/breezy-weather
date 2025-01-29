package org.breezyweather.background.updater.interactor

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.breezyweather.background.updater.data.ReleaseService
import org.breezyweather.background.updater.model.Release
import org.breezyweather.domain.settings.SettingsManager
import java.util.Date
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

/**
 * Taken from Mihon
 * Apache License, Version 2.0
 *
 * https://github.com/mihonapp/mihon/blob/aa498360db90350f2642e6320dc55e7d474df1fd/domain/src/main/java/tachiyomi/domain/release/interactor/GetApplicationRelease.kt
 */
class GetApplicationRelease @Inject constructor(
    @ApplicationContext val context: Context,
    val service: ReleaseService,
) {

    suspend fun await(
        arguments: Arguments,
    ): Result {
        val now = Date().time

        val lastChecked = SettingsManager.getInstance(context).appUpdateCheckLastTimestamp

        // Limit checks to once every day at most
        if (!arguments.forceCheck && now < lastChecked + 1.days.inWholeMilliseconds) {
            return Result.NoNewUpdate
        }

        val release = service.latest(arguments.org, arguments.repository)

        SettingsManager.getInstance(context).appUpdateCheckLastTimestamp = now

        // Check if latest version is different from current version
        val isNewVersion = isNewVersion(
            arguments.versionName,
            release.version
        )
        return when {
            isNewVersion -> Result.NewUpdate(release)
            else -> Result.NoNewUpdate
        }
    }

    private fun isNewVersion(
        versionName: String,
        versionTag: String,
    ): Boolean {
        // Removes "v" prefixes
        val newVersion = versionTag.replace("[^\\d.]".toRegex(), "")
        val oldVersion = versionName.replace("[^\\d.]".toRegex(), "")

        val newSemVer = newVersion.split(".").map { it.toInt() }
        val oldSemVer = oldVersion.split(".").map { it.toInt() }

        oldSemVer.mapIndexed { index, i ->
            // Useful in case of pre-releases, where the newer stable version is older than the pre-release
            if (newSemVer[index] < i) {
                return false
            }
            if (newSemVer[index] > i) {
                return true
            }
        }

        return false
    }

    data class Arguments(
        val versionName: String,
        val org: String,
        val repository: String,
        val forceCheck: Boolean = false,
    )

    sealed interface Result {
        data class NewUpdate(val release: Release) : Result
        data object NoNewUpdate : Result
        data object OsTooOld : Result
    }
}
