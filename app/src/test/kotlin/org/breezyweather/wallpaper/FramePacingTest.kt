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

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class FramePacingTest {

    @Test
    fun refreshesPerFrame() {
        FramePacing.refreshesPerFrame(60f, 60) shouldBe 1
        FramePacing.refreshesPerFrame(60f, 30) shouldBe 2
        FramePacing.refreshesPerFrame(120f, 60) shouldBe 2
        FramePacing.refreshesPerFrame(120f, 30) shouldBe 4
        FramePacing.refreshesPerFrame(144f, 60) shouldBe 2
        FramePacing.refreshesPerFrame(90f, 30) shouldBe 3
        // Never below the requested frame rate
        FramePacing.refreshesPerFrame(90f, 60) shouldBe 1
        FramePacing.refreshesPerFrame(48f, 30) shouldBe 1
        // Approximate refresh rates
        FramePacing.refreshesPerFrame(119.88f, 60) shouldBe 2
        FramePacing.refreshesPerFrame(59.94f, 60) shouldBe 1
        FramePacing.refreshesPerFrame(60.0001f, 30) shouldBe 2
        // Unknown values
        FramePacing.refreshesPerFrame(0f, 60) shouldBe 1
        FramePacing.refreshesPerFrame(120f, 0) shouldBe 1
    }

    @Test
    fun nextFrameDelayMillis() {
        val ms = FramePacing.NANOS_PER_MILLI
        // 60 Hz, 60 FPS: next refresh in 16.7 ms, aims at 8.3 ms
        FramePacing.nextFrameDelayMillis(0L, 2 * ms, 60f, 60) shouldBe 6L
        // 120 Hz, 30 FPS: 4th refresh in 33.3 ms, aims at 29.2 ms
        FramePacing.nextFrameDelayMillis(0L, 2 * ms, 120f, 30) shouldBe 27L
        // 120 Hz, 60 FPS: 2nd refresh in 16.7 ms, aims at 12.5 ms
        FramePacing.nextFrameDelayMillis(1_000 * ms, 1_003 * ms, 120f, 60) shouldBe 9L
        // Late: draw on the next refresh
        FramePacing.nextFrameDelayMillis(0L, 12 * ms, 60f, 60) shouldBe 0L
    }

    @Test
    fun frameIntervalCounter() {
        val counter = FrameIntervalCounter()
        val frameTimeNanos = FramePacing.NANOS_PER_SECOND / 60
        counter.next(5_000_000_000L) shouldBe 0L

        // Remainders are carried over: 60 frames at 60 FPS last exactly 1 second
        val intervals = (1..60).map { counter.next(5_000_000_000L + it * FramePacing.NANOS_PER_SECOND / 60) }
        intervals.sum() shouldBe 1000L
        intervals.all { it == 16L || it == 17L } shouldBe true

        // Stalls are capped
        counter.next(10_000_000_000L) shouldBe 100L

        counter.reset()
        counter.next(20_000_000_000L) shouldBe 0L
        counter.next(20_000_000_000L + frameTimeNanos) shouldBe 16L
    }
}
