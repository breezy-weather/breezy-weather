package wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.rotateController;

import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.MaterialWeatherView;

/**
 * Decelerate Rotate controller.
 * */

public class DecelerateRotateController extends MaterialWeatherView.RotateController {

    private double rotationStart;
    private double rotationEnd;
    private double rotationNow;

    private long duration;
    private long time;

    private boolean rotating;

    private static long BASE_DURATION = 200;
    private static long UNIT_DURATION = 200;
    private static double UNIT_ROTATION = 90;

    private static float FACTOR = 1;

    public DecelerateRotateController(double rotate) {
        setRotationWork(0, getRotateInScope(rotate));
    }

    @Override
    public void updateRotation(double rotate) {
        rotate = getRotateInScope(rotate);
        if (rotating) {
            if (Math.abs(rotate - rotationEnd) > 10) {
                setRotationWork(rotationNow, rotate);
            }
            computeRotate();
        } else {
            if (Math.abs(rotate - rotationNow) <= 2.5) {
                rotationNow = rotate;
            } else {
                setRotationWork(rotationNow, rotate);
            }
            if (rotating) {
                computeRotate();
            }
        }
    }

    @Override
    public double getRotate() {
        return rotationNow;
    }

    private void setRotationWork(double from, double to) {
        rotationStart = from;
        rotationEnd = to;
        rotationNow = from;

        duration = (long) (BASE_DURATION + UNIT_DURATION * Math.abs(to - from) / UNIT_ROTATION);
        time = 0;

        rotating = true;
    }

    private void computeRotate() {
        // real time = 1 - (1 - proportion of time)^(2 * factor)
        time = Math.min(
                duration,
                time + MaterialWeatherView.WeatherAnimationImplementor.REFRESH_INTERVAL);
        rotationNow = (rotationEnd - rotationStart) * (1 - Math.pow(1 - 1.0 * time / duration, 2 * FACTOR))
                + rotationStart;
        if (time == duration) {
            // finish.
            rotating = false;
        }
    }

    private double getRotateInScope(double rotate) {
        if (Math.abs(rotate) <= 90) {
            return rotate;
        } else if (Math.abs(rotate) <= 180) {
            if (rotate > 0) {
                return 90 - (rotate - 90);
            } else {
                return -90 - (rotate + 90);
            }
        } else {
            while (Math.abs(rotate) <= 180) {
                if (rotate > 0) {
                    rotate -= 360;
                } else {
                    rotate += 360;
                }
            }
            return rotate;
        }
    }
}
