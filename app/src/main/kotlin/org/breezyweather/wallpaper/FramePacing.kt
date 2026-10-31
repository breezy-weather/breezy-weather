/*
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

package org.breezyweather.wallpaper

import kotlin.math.floor

/**
 * Frame scheduling of the live wallpaper, without Android dependencies so that it can be unit tested.
 */
internal object FramePacing {

    const val NANOS_PER_MILLI = 1_000_000L
    const val NANOS_PER_SECOND = 1_000_000_000L
    const val DEFAULT_REFRESH_RATE = 60f

    // Some displays report refresh rates such as 119.88 Hz, which must still be considered as twice 60 Hz
    private const val REFRESH_RATE_TOLERANCE = 0.05f

    /**
     * Number of display refreshes between two frames: the highest one still drawing at least [frameRate] frames per
     * second, so that frames are always drawn at regular intervals, in sync with the display.
     * For example, on a 120 Hz display, 60 FPS is every 2 refreshes, and 30 FPS every 4 refreshes.
     */
    fun refreshesPerFrame(refreshRate: Float, frameRate: Int): Int {
        if (refreshRate <= 0f || frameRate <= 0) return 1
        return floor(refreshRate / frameRate + REFRESH_RATE_TOLERANCE).toInt().coerceAtLeast(1)
    }

    /**
     * Delay to give to Choreographer.postFrameCallbackDelayed() after drawing the frame of [frameTimeNanos], so that
     * the next frame is drawn [refreshesPerFrame] refreshes later.
     * Choreographer calls back on the first refresh after the delay, so it aims half a refresh before the target one.
     *
     * @return the delay in milliseconds, or 0 if the next frame must be drawn on the next refresh
     */
    fun nextFrameDelayMillis(frameTimeNanos: Long, nowNanos: Long, refreshRate: Float, frameRate: Int): Long {
        val safeRefreshRate = if (refreshRate > 0f) refreshRate else DEFAULT_REFRESH_RATE
        val refreshPeriodNanos = (NANOS_PER_SECOND / safeRefreshRate).toLong()
        val nextFrameTimeNanos = frameTimeNanos + refreshesPerFrame(refreshRate, frameRate) * refreshPeriodNanos
        return ((nextFrameTimeNanos - refreshPeriodNanos / 2 - nowNanos) / NANOS_PER_MILLI).coerceAtLeast(0L)
    }
}

/**
 * Converts frame times in nanoseconds into the whole milliseconds elapsed between frames expected by weather
 * animations. Remainders are carried over to the next frame, so that animations keep their exact speed.
 */
internal class FrameIntervalCounter {
    private var lastFrameTimeNanos = NOT_STARTED
    private var remainderNanos = 0L

    /** The next interval will be 0, for example when resuming after a pause. */
    fun reset() {
        lastFrameTimeNanos = NOT_STARTED
        remainderNanos = 0L
    }

    /**
     * @return the milliseconds elapsed since the previous frame. Long stalls are capped to [MAX_INTERVAL_NANOS], so
     * that animations never jump.
     */
    fun next(frameTimeNanos: Long): Long {
        val elapsedNanos = if (lastFrameTimeNanos == NOT_STARTED) {
            0L
        } else {
            (frameTimeNanos - lastFrameTimeNanos).coerceIn(0L, MAX_INTERVAL_NANOS)
        }
        lastFrameTimeNanos = frameTimeNanos
        val totalNanos = remainderNanos + elapsedNanos
        val intervalMillis = totalNanos / FramePacing.NANOS_PER_MILLI
        remainderNanos = totalNanos - intervalMillis * FramePacing.NANOS_PER_MILLI
        return intervalMillis
    }

    companion object {
        private const val NOT_STARTED = Long.MIN_VALUE
        const val MAX_INTERVAL_NANOS = 100 * FramePacing.NANOS_PER_MILLI
    }
}
