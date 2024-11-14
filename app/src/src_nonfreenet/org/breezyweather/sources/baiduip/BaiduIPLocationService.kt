/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.sources.baiduip

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.exceptions.ApiLimitReachedException
import org.breezyweather.common.exceptions.ApiUnauthorizedException
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.exceptions.LocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.rxjava.SchedulerTransformer
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationPositionWrapper
import org.breezyweather.common.source.LocationSource
import org.breezyweather.settings.SourceConfigStore
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

class BaiduIPLocationService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), LocationSource, ConfigurableSource {

    override val id = "baidu_ip"
    override val name by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("zh") -> "百度IP定位"
                else -> "Baidu IP location"
            }
        }
    }
    override val privacyPolicyUrl = "https://lbs.baidu.com/index.php?title=openprivacy"

    private val mApi by lazy {
        client
            .baseUrl(BAIDU_IP_LOCATION_BASE_URL)
            .build()
            .create(BaiduIPLocationApi::class.java)
    }

    override fun requestLocation(context: Context): Observable<LocationPositionWrapper> {
        if (!isConfigured) {
            return Observable.error(ApiKeyMissingException())
        }
        return mApi.getLocation(getApiKeyOrDefault(), "gcj02")
            .compose(SchedulerTransformer.create())
            .map { t ->
                if (t.status != 0) {
                    if (t.status == 200) {
                        // Key is invalid
                        throw ApiUnauthorizedException()
                    }
                    if (t.status == 302) {
                        throw ApiLimitReachedException()
                    }

                    // 0 = OK
                    // 1 = IP not supported (outside China)
                    // Don’t know about other cases, doing != 0 for safety
                    throw LocationException()
                }
                if (t.content?.point == null ||
                    t.content.point.y.isNullOrEmpty() ||
                    t.content.point.x.isNullOrEmpty()
                ) {
                    throw InvalidOrIncompleteDataException()
                } else {
                    try {
                        LocationPositionWrapper(
                            t.content.point.y.toDouble(),
                            t.content.point.x.toDouble()
                        )
                    } catch (ignore: Exception) {
                        throw LocationException()
                    }
                }
            }
    }

    override fun hasPermissions(context: Context) = true

    override val permissions: Array<String> = emptyArray()

    // CONFIG
    private val config = SourceConfigStore(context, id)
    private var apikey: String
        set(value) {
            config.edit().putString("apikey", value).apply()
        }
        get() = config.getString("apikey", null) ?: ""

    private fun getApiKeyOrDefault(): String {
        return apikey.ifEmpty { BuildConfig.BAIDU_IP_LOCATION_AK }
    }
    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

    override val isRestricted
        get() = apikey.isEmpty()

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_location_source_baidu_ip_api_key,
                summary = { c, content ->
                    content.ifEmpty {
                        c.getString(R.string.settings_source_default_value)
                    }
                },
                content = apikey,
                onValueChanged = {
                    apikey = it
                }
            )
        )
    }

    companion object {
        private const val BAIDU_IP_LOCATION_BASE_URL = "https://api.map.baidu.com/"
    }
}
