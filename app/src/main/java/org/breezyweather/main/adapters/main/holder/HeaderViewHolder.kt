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

package org.breezyweather.main.adapters.main.holder

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Current
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.appearance.DetailDisplay
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.extensions.isLandscape
import org.breezyweather.common.ui.widgets.NumberAnimTextView
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.ThemeManager
import org.breezyweather.theme.compose.BreezyWeatherTheme
import org.breezyweather.theme.resource.providers.ResourceProvider
import org.breezyweather.theme.weatherView.WeatherView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class HeaderViewHolder(parent: ViewGroup, weatherView: WeatherView) : AbstractMainViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.container_main_header, parent, false)
) {
    private val mContainer: LinearLayout = itemView.findViewById(R.id.container_main_header)
    private val mTemperature: NumberAnimTextView = itemView.findViewById(R.id.container_main_header_temperature_value)
    private val mTemperatureUnitView: TextView = itemView.findViewById(R.id.container_main_header_temperature_unit)
    private val mWeatherText: TextView = itemView.findViewById(R.id.container_main_header_weather_text)
    private var mTemperatureCFrom = 0.0
    private var mTemperatureCTo = 0.0
    private var mTemperatureUnit: TemperatureUnit? = null

    init {
        mContainer.setOnClickListener { weatherView.onClick() }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindView(
        context: Context,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
    ) {
        super.onBindView(context, location, provider, listAnimationEnabled, itemAnimationEnabled)
        val textColor = ThemeManager
            .getInstance(context)
            .weatherThemeDelegate
            .getHeaderTextColor(context)
        mTemperature.setTextColor(textColor)
        mTemperatureUnitView.setTextColor(textColor)
        mWeatherText.setTextColor(textColor)
        mTemperatureUnit = SettingsManager.getInstance(context).temperatureUnit
        location.weather?.current?.let { current ->
            current.temperature?.temperature?.let {
                mTemperatureCFrom = mTemperatureCTo
                mTemperatureCTo = it
                mTemperature.isAnimEnabled = itemAnimationEnabled
                // no longer than 2 seconds.
                mTemperature.duration =
                    max(2000.0, abs(mTemperatureCTo - mTemperatureCFrom) / 10f * 1000).toLong()
                mTemperatureUnitView.text = mTemperatureUnit!!.getName(context)
            }
            if (!current.weatherText.isNullOrEmpty()) {
                mWeatherText.visibility = View.VISIBLE
                mWeatherText.text = current.weatherText
            } else {
                mWeatherText.visibility = View.GONE
            }

            itemView.findViewById<ComposeView>(R.id.container_main_header_details).setContent {
                BreezyWeatherTheme(lightTheme = MainThemeColorProvider.isLightTheme(context, location)) {
                    val detailList = SettingsManager.getInstance(context).detailDisplayList.filter {
                        it.getCurrentValue(LocalContext.current, current, location.isDaylight) != null
                    }
                    HeaderDetails(
                        detailList.subList(
                            0,
                            min(
                                detailList.size,
                                if (context.isLandscape) NB_CURRENT_ITEMS_LANDSCAPE else NB_CURRENT_ITEMS_PORTRAIT
                            )
                        ).toImmutableList(),
                        current,
                        location.isDaylight
                    )
                }
            }
        }
        val params = mContainer.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = ThemeManager
            .getInstance(context)
            .weatherThemeDelegate
            .getHeaderTopMargin(context)
        mContainer.layoutParams = params
    }

    @Composable
    private fun HeaderDetails(
        detailDisplayList: ImmutableList<DetailDisplay>,
        current: Current,
        isDaylight: Boolean = true,
    ) {
        if (detailDisplayList.isNotEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.5f),
                    thickness = 0.5.dp,
                    modifier = Modifier.width(200.dp)
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.large_margin)))
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    detailDisplayList.forEach { detailDisplay ->
                        detailDisplay.getCurrentValue(LocalContext.current, current, isDaylight)?.let { currentValue ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(horizontal = 5.dp)
                                // .background(Color.Blue) // For debugging purposes
                            ) {
                                Icon(
                                    painterResource(detailDisplay.iconId),
                                    contentDescription = detailDisplay.getName(LocalContext.current),
                                    tint = Color.White
                                )
                                Text(
                                    currentValue,
                                    color = Color.White,
                                    fontSize = dimensionResource(
                                        R.dimen.current_weather_details_value_text_size
                                    ).value.sp,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    detailDisplay.getShortName(LocalContext.current),
                                    color = Color.White,
                                    fontSize = dimensionResource(
                                        R.dimen.current_weather_details_name_text_size
                                    ).value.sp,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Light,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getEnterAnimator(pendingAnimatorList: List<Animator>): Animator {
        val a: Animator = ObjectAnimator.ofFloat(itemView, "alpha", 0f, 1f)
        a.setDuration(300)
        a.startDelay = 100
        a.interpolator = FastOutSlowInInterpolator()
        return a
    }

    @SuppressLint("DefaultLocale")
    override fun onEnterScreen() {
        super.onEnterScreen()
        mTemperature.setNumberString(
            String.format("%d", mTemperatureUnit!!.getValueWithoutUnit(mTemperatureCFrom).roundToInt()),
            String.format("%d", mTemperatureUnit!!.getValueWithoutUnit(mTemperatureCTo).roundToInt())
        )
    }

    val headerTop: Int
        get() {
            return mContainer.top
        }

    companion object {
        const val NB_CURRENT_ITEMS_PORTRAIT = 4
        const val NB_CURRENT_ITEMS_LANDSCAPE = 5
    }
}
