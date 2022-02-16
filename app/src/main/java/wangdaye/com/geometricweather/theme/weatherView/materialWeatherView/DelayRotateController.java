package wangdaye.com.geometricweather.theme.weatherView.materialWeatherView;

/**
 * Delay Rotate controller.
 * */

public class DelayRotateController extends MaterialWeatherView.RotateController {

    private double mTargetRotation;
    private double mCurrentRotation;
    private double mVelocity;
    private double mAcceleration;

    private static final double DEFAULT_ABS_ACCELERATION = 90.0 / 200.0 / 800.0;

    public DelayRotateController(double initRotation) {
        mTargetRotation = getRotationInScope(initRotation);
        mCurrentRotation = mTargetRotation;
        mVelocity = 0;
        mAcceleration = 0;
    }

    @Override
    public void updateRotation(double rotation, double interval) {
        mTargetRotation = getRotationInScope(rotation);

        if (mTargetRotation == mCurrentRotation) {
            // no need to move.
            mAcceleration = 0;
            mVelocity = 0;
            return;
        }

        double d;
        if (mVelocity == 0 || (mTargetRotation - mCurrentRotation) * mVelocity < 0) {
            // start or turn around.
            mAcceleration = (mTargetRotation > mCurrentRotation ? 1 : -1) * DEFAULT_ABS_ACCELERATION;
            d = mAcceleration * Math.pow(interval, 2) / 2.0;
            mVelocity = mAcceleration * interval;

        } else if (Math.pow(Math.abs(mVelocity), 2) / (2 * DEFAULT_ABS_ACCELERATION)
                < Math.abs(mTargetRotation - mCurrentRotation)) {
            // speed up.
            mAcceleration = (mTargetRotation > mCurrentRotation ? 1 : -1) * DEFAULT_ABS_ACCELERATION;
            d = mVelocity * interval + mAcceleration * Math.pow(interval, 2) / 2.0;
            mVelocity += mAcceleration * interval;

        } else {
            // slow down.
            mAcceleration = (mTargetRotation > mCurrentRotation ? -1 : 1)
                    * Math.pow(mVelocity, 2) / (2.0 * Math.abs(mTargetRotation - mCurrentRotation));
            d = mVelocity * interval + mAcceleration * Math.pow(interval, 2) / 2.0;
            mVelocity += mAcceleration * interval;
        }

        if (Math.abs(d) > Math.abs(mTargetRotation - mCurrentRotation)) {
            mAcceleration = 0;
            mCurrentRotation = mTargetRotation;
            mVelocity = 0;
        } else {
            mCurrentRotation += d;
        }
    }

    @Override
    public double getRotation() {
        return mCurrentRotation;
    }

    private double getRotationInScope(double rotation) {
        rotation %= 180;
        if (Math.abs(rotation) <= 90) {
            return rotation;
        } else { // Math.abs(rotation) < 180
            if (rotation > 0) {
                return 90 - (rotation - 90);
            } else {
                return -90 - (rotation + 90);
            }
        }
    }
}
