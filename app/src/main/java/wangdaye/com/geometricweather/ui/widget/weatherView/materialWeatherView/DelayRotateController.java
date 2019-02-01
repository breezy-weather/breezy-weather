package wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView;

/**
 * Base Rotate controller.
 * */

public class DelayRotateController extends MaterialWeatherView.RotateController {

    private double currentRotation;
    private double targetRotation;
    private double deltaRotation;

    private double currentSpeed;
    private double targetSpeed;

    private boolean rotating;

    private static double ROTATE_SLOP = 1;

    private static double MAX_SPEED_TRIGGER_ROTATION = 45;
    private static double MAX_ROTATE_SPEED = 360 / 2000.0;
    private static double MIN_ROTATE_SPEED = 360 / 50000.0;
    private static double MAX_ACCELERATION = (MAX_ROTATE_SPEED - MIN_ROTATE_SPEED) / 650.0;

    DelayRotateController(double rotate) {
        this.currentRotation = rotate;
        this.targetRotation = rotate;
        this.deltaRotation = 0;

        this.currentSpeed = 0;
        this.targetSpeed = 0;

        this.rotating = false;
    }

    @Override
    public void updateRotation(double rotate, double interval) {
        rotate = getRotateInScope(rotate);
        if ((rotating && Math.abs(this.targetRotation - rotate) > ROTATE_SLOP)
                || (!rotating && Math.abs(this.targetRotation - rotate) > ROTATE_SLOP)) {
            this.targetRotation = rotate;
        }

        if (targetRotation == currentRotation) {
            currentSpeed = 0;
            targetSpeed = 0;
            rotating = false;
            return;
        }

        deltaRotation = Math.abs(targetRotation - currentRotation);
        if (deltaRotation >= MAX_SPEED_TRIGGER_ROTATION) {
            targetSpeed = MAX_ROTATE_SPEED;
        } else {
            targetSpeed = deltaRotation / MAX_SPEED_TRIGGER_ROTATION * MAX_ROTATE_SPEED;
        }

        if (targetSpeed > currentSpeed) {
            currentSpeed += MAX_ACCELERATION * interval;
            currentSpeed = Math.min(MAX_ROTATE_SPEED, currentSpeed);
        } else if (targetSpeed < currentSpeed) {
            currentSpeed -= MAX_ACCELERATION * interval;
            currentSpeed = Math.max(MIN_ROTATE_SPEED, currentSpeed);
        }

        if (targetRotation > currentRotation) {
            rotating = true;
            currentRotation += currentSpeed * interval;
            if (currentRotation > targetRotation) {
                rotating = false;
                currentRotation = targetRotation;
            }
        } else if (targetRotation < currentRotation) {
            rotating = true;
            currentRotation -= currentSpeed * interval;
            if (currentRotation < targetRotation) {
                rotating = false;
                currentRotation = targetRotation;
            }
        }
    }

    @Override
    public double getRotate() {
        return currentRotation;
    }

    private double getRotateInScope(double rotate) {
        rotate %= 180;
        if (Math.abs(rotate) <= 90) {
            return rotate;
        } else { // Math.abs(rotate) < 180
            if (rotate > 0) {
                return 90 - (rotate - 90);
            } else {
                return -90 - (rotate + 90);
            }
        }
    }
}
