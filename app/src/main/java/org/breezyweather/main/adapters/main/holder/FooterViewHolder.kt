package org.breezyweather.main.adapters.main.holder

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Button
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import io.github.giangpham96.expandable_text_compose.ExpandableText
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.provider.WeatherSource
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.theme.ThemeManager.Companion.getInstance
import org.breezyweather.theme.resource.providers.ResourceProvider

class FooterViewHolder(parent: ViewGroup) : AbstractMainViewHolder(
    LayoutInflater
        .from(parent.context)
        .inflate(R.layout.container_main_footer, parent, false)
) {
    private val mCredits: ComposeView = itemView.findViewById(R.id.container_main_footer_credits)
    private val mEditButton: Button = itemView.findViewById(R.id.container_main_footer_editButton)

    @SuppressLint("SetTextI18n")
    override fun onBindView(
        context: Context, location: Location, provider: ResourceProvider,
        listAnimationEnabled: Boolean, itemAnimationEnabled: Boolean
    ) {
        super.onBindView(context, location, provider, listAnimationEnabled, itemAnimationEnabled)
        val cardMarginsVertical = getInstance(context)
            .weatherThemeDelegate
            .getHomeCardMargins(context).toFloat()
        val params = itemView.layoutParams as MarginLayoutParams
        if (cardMarginsVertical != 0f) {
            params.setMargins(0, -cardMarginsVertical.toInt(), 0, 0)
        }
        itemView.layoutParams = params

        mCredits.setContent {
            CreditsText(location.weatherSource)
        }

        mEditButton.setTextColor(
            getInstance(context)
                .weatherThemeDelegate
                .getHeaderTextColor(mCredits.context)
        )
        mEditButton.setOnClickListener { IntentHelper.startCardDisplayManageActivity(context as Activity) }
    }

    @Composable
    fun CreditsText(weatherSource: WeatherSource) {
        var expand by remember { mutableStateOf(false) }

        val creditsText = StringBuilder()
        creditsText.append(
            context.getString(R.string.weather_data_by)
                .replace("$", weatherSource.sourceUrl)
        )
        if (weatherSource.airQualityPollenSource != null) {
            creditsText.append("\n")
                .append(
                    context.getString(R.string.weather_air_quality_and_pollen_data_by)
                        .replace("$", weatherSource.airQualityPollenSource)
                )
        }
        ExpandableText(
            originalText = creditsText.toString(),
            expandAction = stringResource(R.string.action_see_more),
            expand = expand,
            color = Color.White,
            expandActionColor = Color.White,
            limitedMaxLines = 3,
            animationSpec = spring(),
            modifier = Modifier
                .clickable { expand = !expand },
        )
    }

    override fun getEnterAnimator(pendingAnimatorList: List<Animator>): Animator {
        val a: Animator = ObjectAnimator.ofFloat(itemView, "alpha", 0f, 1f)
        a.setDuration(450)
        a.interpolator = FastOutSlowInInterpolator()
        a.startDelay = pendingAnimatorList.size * 150L
        return a
    }
}
