package wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView;

/**
 * Delay Rotate controller.
 * */

public class DelayRotateController extends MaterialWeatherView.RotateController {

    private double targetRotation;
    private double currentRotation;
    private double velocity;
    private double acceleration;

    private static final double DEFAULT_ABS_ACCELERATION = 90.0 / 200.0 / 800.0;

    public DelayRotateController(double initRotation) {
        targetRotation = getRotationInScope(initRotation);
        currentRotation = targetRotation;
        velocity = 0;
        acceleration = 0;
    }

    @Override
    public void updateRotation(double rotation, double interval) {
        targetRotation = getRotationInScope(rotation);

        if (targetRotation == currentRotation) {
            // no need to move.
            acceleration = 0;
            velocity = 0;
            return;
        }

        double d;
        if (velocity == 0 || (targetRotation - currentRotation) * velocity < 0) {
            // start or turn around.
            acceleration = (targetRotation > currentRotation ? 1 : -1) * DEFAULT_ABS_ACCELERATION;
            d = acceleration * Math.pow(interval, 2) / 2.0;
            velocity = acceleration * interval;

        } else if (Math.pow(Math.abs(velocity), 2) / (2 * DEFAULT_ABS_ACCELERATION)
                < Math.abs(targetRotation - currentRotation)) {
            // speed up.
            acceleration = (targetRotation > currentRotation ? 1 : -1) * DEFAULT_ABS_ACCELERATION;
            d = velocity * interval + acceleration * Math.pow(interval, 2) / 2.0;
            velocity += acceleration * interval;

        } else {
            // slow down.
            acceleration = (targetRotation > currentRotation ? -1 : 1)
                    * Math.pow(velocity, 2) / (2.0 * Math.abs(targetRotation - currentRotation));
            d = velocity * interval + acceleration * Math.pow(interval, 2) / 2.0;
            velocity += acceleration * interval;
        }

        if (Math.abs(d) > Math.abs(targetRotation - currentRotation)) {
            acceleration = 0;
            currentRotation = targetRotation;
            velocity = 0;
        } else {
            currentRotation += d;
        }
    }

    @Override
    public double getRotation() {
        return currentRotation;
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
