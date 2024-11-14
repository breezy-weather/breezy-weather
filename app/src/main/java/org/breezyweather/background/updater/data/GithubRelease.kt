package org.breezyweather.background.updater.data

/**
 * Taken from Mihon
 * Apache License, Version 2.0
 *
 * https://github.com/mihonapp/mihon/blob/d29b7c4e5735dc137d578d3bcb3da1f0a02573e8/data/src/main/java/tachiyomi/data/release/GithubRelease.kt
 */

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.background.updater.model.Release

/**
 * Contains information about the latest release from GitHub.
 */
@Serializable
data class GithubRelease(
    @SerialName("tag_name") val version: String,
    @SerialName("body") val info: String,
    @SerialName("html_url") val releaseLink: String,
    @SerialName("assets") val assets: List<GitHubAssets>,
)

/**
 * Assets class containing download url.
 */
@Serializable
data class GitHubAssets(
    @SerialName("browser_download_url") val downloadLink: String,
)

val releaseMapper: (GithubRelease) -> Release = {
    Release(
        it.version,
        it.info,
        it.releaseLink,
        it.assets.map(GitHubAssets::downloadLink)
    )
}
