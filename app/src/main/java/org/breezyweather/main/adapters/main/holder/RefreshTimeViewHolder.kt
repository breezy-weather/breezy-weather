package org.breezyweather.main.adapters.main.holder

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.main.widgets.TextRelativeClock
import org.breezyweather.theme.ThemeManager.Companion.getInstance
import org.breezyweather.theme.resource.providers.ResourceProvider
import org.breezyweather.theme.weatherView.WeatherView

class RefreshTimeViewHolder(parent: ViewGroup, weatherView: WeatherView) : AbstractMainViewHolder(
    LayoutInflater
        .from(parent.context)
        .inflate(R.layout.container_main_refresh_time, parent, false)
) {
    private val mContainer: LinearLayout = itemView.findViewById(R.id.container_main_refresh_time)
    private val mRefreshTimeText: TextRelativeClock = itemView.findViewById(R.id.container_main_refresh_time_text)

    init {
        mContainer.setOnClickListener { weatherView.onClick() }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindView(
        context: Context, location: Location, provider: ResourceProvider,
        listAnimationEnabled: Boolean, itemAnimationEnabled: Boolean
    ) {
        super.onBindView(context, location, provider, listAnimationEnabled, itemAnimationEnabled)
        val textColor = getInstance(context)
            .weatherThemeDelegate
            .getHeaderTextColor(context)
        mRefreshTimeText.setTextColor(textColor)
        location.weather?.let {
            mRefreshTimeText.setDate(it.base.updateDate)
        }
    }

    override fun getEnterAnimator(pendingAnimatorList: List<Animator>): Animator {
        val a: Animator = ObjectAnimator.ofFloat(itemView, "alpha", 0f, 1f)
        a.setDuration(300)
        a.startDelay = 100
        a.interpolator = FastOutSlowInInterpolator()
        return a
    }
}
