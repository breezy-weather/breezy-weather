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

package org.breezyweather.ui.main.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.RemoteViews.RemoteView
import android.widget.TextView
import org.breezyweather.R
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getRelativeTime
import org.breezyweather.common.extensions.uncapitalize
import java.util.Date
import kotlin.time.Duration.Companion.minutes

/**
 *
 * `TextRelativeClock` can display the current relative time as a formatted string.
 */
@SuppressLint("AppCompatCustomView")
@RemoteView
class TextRelativeClock @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : TextView(context, attrs, defStyleAttr, defStyleRes) {
    private var mShouldRunTicker = false
    private var mDate = Date()
    private val mTicker: Runnable = object : Runnable {
        override fun run() {
            removeCallbacks(this)
            if (!mShouldRunTicker) {
                return
            }
            onTimeChanged()

            // Date now = new Date();
            val millisUntilNextTick = 1.minutes.inWholeMilliseconds
            // It is currently refreshing every minute
            // It's not precise (but enough for our use case) as it won't refresh on second 0 of next minute
            // but rather on the same second on next minute
            // Plus it's refreshing every minute when > 1 hour, which is not optimized
            // TODO: We should optimize this function one day, for Green IT purposes
            /*long secondsDifference = (now.time - mDate.time) / 1000;
            if (secondsDifference <= 1.hours.inWholeSeconds) { // < 1 hour

            } else if (secondsDifference <= 1.days.inWholeSeconds) { // < 24 hours
                // Calculate modulo for next hour
            } else { // More than 24 hours

            }*/
            postDelayed(this, millisUntilNextTick)
        }
    }

    init {
        runTicker()
    }

    fun setDate(date: Date) {
        mDate = date
        onTimeChanged()
    }

    /**
     * Run ticker if required
     */
    private fun runTicker() {
        if (mShouldRunTicker) {
            mTicker.run()
        }
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        if (!mShouldRunTicker && isVisible) {
            mShouldRunTicker = true
            mTicker.run()
        } else if (mShouldRunTicker && !isVisible) {
            mShouldRunTicker = false
            removeCallbacks(mTicker)
        }
    }

    /**
     * Update the displayed time if this view and its ancestors and window is visible
     */
    private fun onTimeChanged() {
        val relativeTimeFormatted = mDate.getRelativeTime(context)
        text = relativeTimeFormatted
        contentDescription = context.getString(
            R.string.location_last_updated_x,
            relativeTimeFormatted.uncapitalize(context.currentLocale)
        )
    }
}
