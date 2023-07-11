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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.appearance.DetailDisplay
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.basic.models.weather.Current
import org.breezyweather.common.ui.widgets.NumberAnimTextView
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.ThemeManager
import org.breezyweather.theme.resource.providers.ResourceProvider
import org.breezyweather.theme.weatherView.WeatherView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

class HeaderViewHolder(parent: ViewGroup, weatherView: WeatherView) : AbstractMainViewHolder(
    LayoutInflater
        .from(parent.context)
        .inflate(R.layout.container_main_header, parent, false)
) {
    private val mContainer: LinearLayout = itemView.findViewById(R.id.container_main_header)
    private val mTemperature: NumberAnimTextView = itemView.findViewById(R.id.container_main_header_temperature_value)
    private val mTemperatureUnitView: TextView = itemView.findViewById(R.id.container_main_header_temperature_unit)
    private val mWeatherText: TextView = itemView.findViewById(R.id.container_main_header_weather_text)
    private var mTemperatureCFrom = 0f
    private var mTemperatureCTo = 0f
    private var mTemperatureUnit: TemperatureUnit? = null

    init {
        mContainer.setOnClickListener { weatherView.onClick() }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindView(
        context: Context, location: Location, provider: ResourceProvider,
        listAnimationEnabled: Boolean, itemAnimationEnabled: Boolean
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
                    max(2000f, abs(mTemperatureCTo - mTemperatureCFrom) / 10f * 1000).toLong()
                mTemperatureUnitView.text = mTemperatureUnit!!.getName(context)
            }
            if (!current.weatherText.isNullOrEmpty()) {
                mWeatherText.visibility = View.VISIBLE
                mWeatherText.text = current.weatherText
            } else {
                mWeatherText.visibility = View.GONE
            }

            itemView.findViewById<ComposeView>(R.id.container_main_header_details).setContent {
                HeaderDetails(SettingsManager.getInstance(context).detailDisplayList, current, location.isDaylight)
            }
        }
        /*val params = mContainer.layoutParams as ViewGroup.MarginLayoutParams
        params.height = ThemeManager
            .getInstance(context)
            .weatherThemeDelegate
            .getHeaderHeight(context)
        if (params.height > mContainer.measuredHeight) {
            mContainer.layoutParams = params
        }*/
    }

    @Composable
    private fun HeaderDetails(detailDisplayList: List<DetailDisplay>, current: Current, isDaylight: Boolean = true) {
        var firstItem = true
        Column {
            detailDisplayList.forEach { detailDisplay ->
                detailDisplay.getCurrentValue(LocalContext.current, current, isDaylight)?.let {
                    if (!firstItem) {
                        Divider(color = Color.White, thickness = 0.5.dp)
                    } else {
                        firstItem = false
                    }
                    ListItem(
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent,
                            leadingIconColor = Color.White
                        ),
                        headlineContent = {
                            Text(
                                detailDisplay.getName(LocalContext.current),
                                color = Color.White,
                                fontSize = dimensionResource(R.dimen.current_weather_details_name_text_size).value.sp
                            )
                        },
                        trailingContent = {
                            Text(
                                detailDisplay.getCurrentValue(LocalContext.current, current, isDaylight)!!,
                                color = Color.White,
                                fontSize = dimensionResource(R.dimen.current_weather_details_value_text_size).value.sp,
                                fontWeight = FontWeight.Light
                            )
                        },
                        leadingContent = {
                            Icon(
                                painterResource(detailDisplay.iconId),
                                contentDescription = detailDisplay.getName(LocalContext.current),
                            )
                        }
                    )
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

    val currentTemperatureHeight: Int = mContainer.measuredHeight - mTemperature.top
}
