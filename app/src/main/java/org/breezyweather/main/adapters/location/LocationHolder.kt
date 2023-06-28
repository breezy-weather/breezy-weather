package org.breezyweather.main.adapters.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.core.graphics.ColorUtils
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import org.breezyweather.databinding.ItemLocationCardBinding
import org.breezyweather.main.adapters.location.LocationAdapter.OnLocationItemClickListener
import org.breezyweather.main.adapters.location.LocationAdapter.OnLocationItemDragListener
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.R
import org.breezyweather.common.utils.DisplayUtils
import org.breezyweather.theme.resource.providers.ResourceProvider

class LocationHolder(
    private val mBinding: ItemLocationCardBinding,
    private val mClickListener: OnLocationItemClickListener,
    private val mDragListener: OnLocationItemDragListener?
) : RecyclerView.ViewHolder(mBinding.root) {
    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    fun onBindView(context: Context, model: LocationModel, resourceProvider: ResourceProvider) {
        val lightTheme = !DisplayUtils.isDarkMode(context)
        val elevatedSurfaceColor = DisplayUtils.getWidgetSurfaceColor(
            DisplayUtils.DEFAULT_CARD_LIST_ITEM_ELEVATION_DP,
            MainThemeColorProvider.getColor(lightTheme, androidx.appcompat.R.attr.colorPrimary),
            MainThemeColorProvider.getColor(lightTheme, com.google.android.material.R.attr.colorSurface)
        )
        if (model.selected) {
            mBinding.root.strokeWidth = DisplayUtils.dpToPx(context, 4f).toInt()
            mBinding.root.strokeColor = elevatedSurfaceColor
        } else {
            mBinding.root.strokeWidth = 0
        }
        val talkBackBuilder = StringBuilder()
        if (model.currentPosition) {
            talkBackBuilder.append(context.getString(R.string.location_current))
        }
        if (talkBackBuilder.toString().isNotEmpty()) {
            talkBackBuilder.append(", ")
        }
        talkBackBuilder.append(
        context.getString(R.string.weather_data_by)
            .replace("$", model.weatherSource.getVoice(context))
        )
        mBinding.container.swipe(0f)
        mBinding.container.iconResStart = R.drawable.ic_delete
        if (model.currentPosition) {
            mBinding.container.iconResEnd = R.drawable.ic_settings
        } else {
            mBinding.container.iconResEnd =
                if (model.residentPosition) R.drawable.ic_tag_off else R.drawable.ic_tag_plus
        }
        mBinding.container.backgroundColorStart =
            MainThemeColorProvider.getColor(lightTheme, com.google.android.material.R.attr.colorErrorContainer)
        mBinding.container.backgroundColorEnd = if (model.location.isCurrentPosition) MainThemeColorProvider.getColor(
            lightTheme,
            com.google.android.material.R.attr.colorTertiaryContainer
        ) else MainThemeColorProvider.getColor(lightTheme, com.google.android.material.R.attr.colorSecondaryContainer)
        mBinding.container.tintColorStart =
            MainThemeColorProvider.getColor(lightTheme, com.google.android.material.R.attr.colorOnErrorContainer)
        mBinding.container.tintColorEnd = if (model.location.isCurrentPosition) MainThemeColorProvider.getColor(
            lightTheme,
            com.google.android.material.R.attr.colorOnTertiaryContainer
        ) else MainThemeColorProvider.getColor(lightTheme, com.google.android.material.R.attr.colorOnSecondaryContainer)
        mBinding.item.setBackgroundColor(
            if (model.selected) DisplayUtils.blendColor(
                ColorUtils.setAlphaComponent(elevatedSurfaceColor, (255 * 0.5).toInt()),
                MainThemeColorProvider.getColor(lightTheme, com.google.android.material.R.attr.colorSurfaceVariant)
            ) else elevatedSurfaceColor
        )
        ImageViewCompat.setImageTintList(
            mBinding.sortButton,
            ColorStateList.valueOf(
                MainThemeColorProvider.getColor(lightTheme, androidx.appcompat.R.attr.colorPrimary)
            )
        )
        if (mDragListener == null) {
            mBinding.sortButton.visibility = View.GONE
            mBinding.content.setPaddingRelative(
                context.resources.getDimensionPixelSize(R.dimen.normal_margin), 0, 0, 0
            )
        } else {
            mBinding.sortButton.visibility = View.VISIBLE
            mBinding.content.setPaddingRelative(0, 0, 0, 0)
        }
        mBinding.residentIcon.visibility = if (model.residentPosition) View.VISIBLE else View.GONE
        if (model.weatherCode != null) {
            mBinding.weatherIcon.visibility = View.VISIBLE
            mBinding.weatherIcon.setImageDrawable(
                resourceProvider.getWeatherIcon(
                    model.weatherCode,
                    model.location.isDaylight
                )
            )
        } else {
            mBinding.weatherIcon.visibility = View.GONE
        }
        mBinding.title1.setTextColor(
            if (model.selected) MainThemeColorProvider.getColor(
                lightTheme,
                com.google.android.material.R.attr.colorOnPrimaryContainer
            ) else MainThemeColorProvider.getColor(lightTheme, R.attr.colorTitleText)
        )
        mBinding.title1.text = model.title
        if (model.body.isEmpty()) {
            mBinding.title2.visibility = View.GONE
        } else {
            mBinding.title2.visibility = View.VISIBLE
            mBinding.title2.setTextColor(
                MainThemeColorProvider.getColor(lightTheme, R.attr.colorBodyText)
            )
            mBinding.title2.text = model.body
        }

        // source.
        mBinding.source.text = context.getString(R.string.weather_data_by).replace("$", model.weatherSource.sourceUrl)
        mBinding.source.setTextColor(model.weatherSource.sourceColor)
        mBinding.container.setOnClickListener { mClickListener.onClick(model.location.formattedId) }
        // TODO
        mBinding.sortButton.setOnTouchListener(if (mDragListener == null) null else OnTouchListener { _: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                mDragListener.onDrag(this)
            }
            false
        })
        if (mDragListener != null) {
            talkBackBuilder.append(", ").append(
                context.getString(R.string.location_swipe_to_delete)
            )
        }
        itemView.contentDescription = talkBackBuilder.toString()
    }
}
