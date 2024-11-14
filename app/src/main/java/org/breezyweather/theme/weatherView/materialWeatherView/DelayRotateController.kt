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

package org.breezyweather.theme.weatherView.materialWeatherView

import org.breezyweather.theme.weatherView.materialWeatherView.MaterialWeatherView.RotateController
import kotlin.math.abs
import kotlin.math.pow

/**
 * Delay Rotate controller.
 */
class DelayRotateController(
    initRotation: Double,
) : RotateController() {
    private var mTargetRotation: Double = getRotationInScope(initRotation)
    private var mCurrentRotation: Double = mTargetRotation
    private var mVelocity: Double = 0.0
    private var mAcceleration: Double = 0.0

    override fun updateRotation(rotation: Double, interval: Double) {
        mTargetRotation = getRotationInScope(rotation)
        val rotationDiff = mTargetRotation - mCurrentRotation

        // no need to move
        if (rotationDiff == 0.0) {
            mAcceleration = 0.0
            mVelocity = 0.0
            return
        }

        val accelSign = when (mTargetRotation > mCurrentRotation) {
            true -> 1
            false -> -1
        }
        val oldVelocity = mVelocity

        if (mVelocity == 0.0 || rotationDiff * mVelocity < 0) {
            // start or turn around.
            mAcceleration = accelSign * DEFAULT_ABS_ACCELERATION
            mVelocity = mAcceleration * interval
        } else if (mVelocity.pow(2.0) / (2.0 * DEFAULT_ABS_ACCELERATION) < abs(rotationDiff)) {
            // speed up
            mAcceleration = accelSign * DEFAULT_ABS_ACCELERATION
            mVelocity += mAcceleration * interval
        } else {
            // slow down
            mAcceleration = -1 * accelSign * mVelocity.pow(2.0) / (2.0 * abs(rotationDiff))
            mVelocity += mAcceleration * interval
        }

        val distance = oldVelocity * interval + mAcceleration * interval.pow(2.0) / 2.0

        if (abs(distance) > abs(rotationDiff)) {
            mAcceleration = 0.0
            mCurrentRotation = mTargetRotation
            mVelocity = 0.0
        } else {
            mCurrentRotation += distance
        }
    }

    override val rotation: Double
        get() = mCurrentRotation

    private fun getRotationInScope(rotationP: Double): Double {
        var rotation = rotationP
        rotation %= 180.0
        return if (abs(rotation) <= 90) {
            rotation
        } else { // abs(rotation) < 180
            if (rotation > 0) {
                90 - (rotation - 90)
            } else {
                -90 - (rotation + 90)
            }
        }
    }

    companion object {
        private const val DEFAULT_ABS_ACCELERATION = 90.0 / 200.0 / 800.0
    }
}
