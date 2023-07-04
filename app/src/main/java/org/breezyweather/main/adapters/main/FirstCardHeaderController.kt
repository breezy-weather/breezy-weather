package org.breezyweather.main.adapters.main

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.widget.ImageViewCompat
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.utils.DisplayUtils
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.main.MainActivity
import org.breezyweather.main.utils.MainThemeColorProvider

@SuppressLint("InflateParams")
class FirstCardHeaderController(private val mActivity: GeoActivity, location: Location) : View.OnClickListener {
    private val mView: View = LayoutInflater.from(mActivity).inflate(R.layout.container_main_first_card_header, null)
    private val mFormattedId: String = location.formattedId
    private var mContainer: LinearLayout? = null

    init {
        if (location.weather != null && location.weather.alertList.isNotEmpty()) {
            mView.visibility = View.VISIBLE
            val alertIcon = mView.findViewById<AppCompatImageView>(R.id.container_main_first_card_header_alertIcon)
            val alert = mView.findViewById<TextView>(R.id.container_main_first_card_header_alert)
            val weather = location.weather
            val currentAlertList = weather.currentAlertList
            mView.setOnClickListener { IntentHelper.startAlertActivity(mActivity, mFormattedId) }
            alertIcon.contentDescription = mActivity.getString(R.string.alerts_count)
                .replace("$", "" + currentAlertList.size)
            ImageViewCompat.setImageTintList(
                alertIcon,
                ColorStateList.valueOf(MainThemeColorProvider.getColor(location, R.attr.colorTitleText))
            )
            alertIcon.setOnClickListener(this)
            if (currentAlertList.isEmpty()) {
                alert.text = mActivity.getString(R.string.alerts_to_follow)
            } else {
                val builder = StringBuilder()
                currentAlertList.forEach { currentAlert ->
                    if (builder.toString().isNotEmpty()) {
                        builder.append("\n")
                    }
                    builder.append(currentAlert.description)
                    if (currentAlert.startDate != null) {
                        val startDateDay = currentAlert.startDate.getFormattedDate(
                            location.timeZone, mActivity.getString(R.string.date_format_long)
                        )
                        builder.append(", ")
                            .append(startDateDay)
                            .append(", ")
                            .append(
                                currentAlert.startDate.getFormattedTime(location.timeZone, mActivity.is12Hour)
                            )
                        if (currentAlert.endDate != null) {
                            builder.append("-")
                            val endDateDay = currentAlert.endDate.getFormattedDate(
                                location.timeZone, mActivity.getString(R.string.date_format_long)
                            )
                            if (startDateDay != endDateDay) {
                                builder.append(endDateDay)
                                    .append(", ")
                            }
                            builder.append(
                                currentAlert.endDate.getFormattedTime(location.timeZone, mActivity.is12Hour)
                            )
                        }
                    }
                }
                alert.text = builder.toString()
            }
            alert.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorBodyText))
            alert.setOnClickListener(this)
        } else {
            mView.visibility = View.GONE
        }
    }

    fun bind(firstCardContainer: LinearLayout?) {
        mContainer = firstCardContainer
        mContainer!!.addView(mView, 0)
    }

    fun unbind() {
        mContainer?.let {
            it.removeViewAt(0)
            mContainer = null
        }
    }

    // interface.
    @SuppressLint("NonConstantResourceId")
    override fun onClick(v: View) {
        IntentHelper.startAlertActivity(mActivity, mFormattedId)
    }
}
