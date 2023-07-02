package org.breezyweather.theme.weatherView.materialWeatherView

import org.breezyweather.theme.weatherView.materialWeatherView.MaterialWeatherView.RotateController
import kotlin.math.abs
import kotlin.math.pow

/**
 * Delay Rotate controller.
 */
class DelayRotateController(initRotation: Double) : RotateController() {
    private var mTargetRotation: Double = getRotationInScope(initRotation)
    private var mCurrentRotation: Double = mTargetRotation
    private var mVelocity: Double = 0.0
    private var mAcceleration: Double = 0.0

    override fun updateRotation(rotation: Double, interval: Double) {
        mTargetRotation = getRotationInScope(rotation)
        if (mTargetRotation == mCurrentRotation) {
            // no need to move.
            mAcceleration = 0.0
            mVelocity = 0.0
            return
        }
        val d: Double
        if (mVelocity == 0.0 || (mTargetRotation - mCurrentRotation) * mVelocity < 0) {
            // start or turn around.
            mAcceleration = (if (mTargetRotation > mCurrentRotation) 1 else -1) * DEFAULT_ABS_ACCELERATION
            d = mAcceleration * interval.pow(2.0) / 2.0
            mVelocity = mAcceleration * interval
        } else if (abs(mVelocity).pow(2.0) / (2 * DEFAULT_ABS_ACCELERATION)
            < abs(mTargetRotation - mCurrentRotation)
        ) {
            // speed up.
            mAcceleration = (if (mTargetRotation > mCurrentRotation) 1 else -1) * DEFAULT_ABS_ACCELERATION
            d = mVelocity * interval + mAcceleration * interval.pow(2.0) / 2.0
            mVelocity += mAcceleration * interval
        } else {
            // slow down.
            mAcceleration = if (mTargetRotation > mCurrentRotation) -1.0 else {
                1 * mVelocity.pow(2.0) / (2.0 * abs(mTargetRotation - mCurrentRotation))
            }
            d = mVelocity * interval + mAcceleration * interval.pow(2.0) / 2.0
            mVelocity += mAcceleration * interval
        }
        if (abs(d) > abs(mTargetRotation - mCurrentRotation)) {
            mAcceleration = 0.0
            mCurrentRotation = mTargetRotation
            mVelocity = 0.0
        } else {
            mCurrentRotation += d
        }
    }

    override fun getRotation(): Double = mCurrentRotation

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
