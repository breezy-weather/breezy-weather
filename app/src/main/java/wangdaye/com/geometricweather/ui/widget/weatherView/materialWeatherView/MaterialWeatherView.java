package wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.OrientationEventListener;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.core.graphics.ColorUtils;

import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Material Weather view.
 * */

public class MaterialWeatherView extends View implements WeatherView {

    @Nullable private IntervalComputer intervalComputer;

    @Nullable private WeatherAnimationImplementor implementor;
    @Nullable private RotateController[] rotators;

    private boolean gravitySensorEnabled;
    @Nullable private SensorManager sensorManager;
    @Nullable private Sensor gravitySensor;

    @Size(2) int[] sizes;
    private float rotation2D;
    private float rotation3D;

    @WeatherKindRule private int weatherKind;
    private boolean daytime;
    @ColorInt private int backgroundColor;

    private float displayRate;

    @StepRule
    private int step;
    private static final int STEP_DISPLAY = 1;
    private static final int STEP_DISMISS = -1;

    @IntDef({STEP_DISPLAY, STEP_DISMISS})
    private  @interface StepRule {}

    private int firstCardMarginTop;
    private int scrollTransparentTriggerDistance;

    private float lastScrollRate;
    private float scrollRate;

    private boolean drawable;

    private DeviceOrientation deviceOrientation;
    private enum DeviceOrientation {
        TOP, LEFT, BOTTOM, RIGHT
    }

    private static final int SWITCH_ANIMATION_DURATION = 150;

    /**
     * This class is used to implement different kinds of weather animations.
     * */
    public static abstract class WeatherAnimationImplementor {

        public abstract void updateData(@Size(2) int[] canvasSizes, long interval,
                                        float rotation2D, float rotation3D);

        // return true if finish drawing.
        public abstract void draw(@Size(2) int[] canvasSizes, Canvas canvas,
                                  float displayRatio, float scrollRate,
                                  float rotation2D, float rotation3D);
    }

    public static abstract class RotateController {

        public abstract void updateRotation(double rotation, double interval);

        public abstract double getRotation();
    }

    private SensorEventListener gravityListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent ev) {
            // x : (+) fall to the left / (-) fall to the right.
            // y : (+) stand / (-) head stand.
            // z : (+) look down / (-) look up.
            // rotation2D : (+) anticlockwise / (-) clockwise.
            // rotation3D : (+) look down / (-) look up.
            if (gravitySensorEnabled) {
                float aX = ev.values[0];
                float aY = ev.values[1];
                float aZ = ev.values[2];
                double g2D = Math.sqrt(aX * aX + aY * aY);
                double g3D = Math.sqrt(aX * aX + aY * aY + aZ * aZ);
                double cos2D = Math.max(Math.min(1, aY / g2D), -1);
                double cos3D = Math.max(Math.min(1, g2D * (aY >= 0 ? 1 : -1) / g3D), -1);
                rotation2D = (float) Math.toDegrees(Math.acos(cos2D)) * (aX >= 0 ? 1 : -1);
                rotation3D = (float) Math.toDegrees(Math.acos(cos3D)) * (aZ >= 0 ? 1 : -1);

                switch (deviceOrientation) {
                    case TOP:
                        break;

                    case LEFT:
                        rotation2D -= 90;
                        break;

                    case RIGHT:
                        rotation2D += 90;
                        break;

                    case BOTTOM:
                        if (rotation2D > 0) {
                            rotation2D -= 180;
                        } else {
                            rotation2D += 180;
                        }
                        break;
                }

                if (60 < Math.abs(rotation3D) && Math.abs(rotation3D) < 120) {
                    rotation2D *= Math.abs(Math.abs(rotation3D) - 90) / 30.0;
                }
            } else {
                rotation2D = 0;
                rotation3D = 0;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            // do nothing.
        }
    };

    private OrientationEventListener orientationListener = new OrientationEventListener(getContext()) {
        @Override
        public void onOrientationChanged(int orientation) {
            deviceOrientation = getDeviceOrientation(orientation);
        }

        private DeviceOrientation getDeviceOrientation(int orientation) {
            if (DisplayUtils.isLandscape(getContext())) {
                return (0 < orientation && orientation < 180)
                        ? DeviceOrientation.RIGHT : DeviceOrientation.LEFT;
            } else {
                return (270 < orientation || orientation < 90)
                        ? DeviceOrientation.TOP : DeviceOrientation.BOTTOM;
            }
        }
    };

    public MaterialWeatherView(Context context) {
        super(context);
        this.initialize();
    }

    public MaterialWeatherView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public MaterialWeatherView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    private void initialize() {
        this.sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            this.gravitySensorEnabled = true;
            this.gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        }

        this.step = STEP_DISPLAY;
        setWeather(WeatherView.WEATHER_KING_NULL, true, null);

        Resources res = getResources();
        DisplayMetrics metrics = res.getDisplayMetrics();
        this.sizes = new int[] {metrics.widthPixels, metrics.heightPixels};

        this.firstCardMarginTop = (int) (res.getDisplayMetrics().heightPixels * 0.55);
        this.scrollTransparentTriggerDistance = firstCardMarginTop;

        this.lastScrollRate = 0;
        this.scrollRate = 0;

        this.drawable = false;

        this.deviceOrientation = DeviceOrientation.TOP;
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        this.scrollTransparentTriggerDistance = firstCardMarginTop - insets.top;
        return false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (getMeasuredWidth() != 0 && getMeasuredHeight() != 0) {
            sizes[0] = Math.min(
                    getMeasuredWidth(),
                    DisplayUtils.getTabletListAdaptiveWidth(getContext(), getMeasuredWidth())
            );
            sizes[1] = getMeasuredHeight();
        }
        setWeatherImplementor();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (intervalComputer == null || rotators == null || implementor == null) {
            canvas.drawColor(getBackgroundColor());
            return;
        }

        intervalComputer.invalidate();

        rotators[0].updateRotation(rotation2D, intervalComputer.getInterval());
        rotators[1].updateRotation(rotation3D, intervalComputer.getInterval());

        implementor.updateData(
                sizes, (long) intervalComputer.getInterval(),
                (float) rotators[0].getRotation(), (float) rotators[1].getRotation()
        );

        displayRate = (float) (
                displayRate
                        + (step == STEP_DISPLAY ? 1f : -1f)
                        * intervalComputer.getInterval()
                        / SWITCH_ANIMATION_DURATION
        );
        displayRate = Math.max(0, displayRate);
        displayRate = Math.min(1, displayRate);

        if (displayRate == 0) {
            setWeatherImplementor();
        }

        canvas.drawColor(backgroundColor);
        if (implementor != null && rotators != null) {
            canvas.save();
            canvas.translate(
                    (getMeasuredWidth() - sizes[0]) / 2,
                    (getMeasuredHeight() - sizes[1]) / 2
            );
            implementor.draw(
                    sizes, canvas,
                    displayRate, scrollRate,
                    (float) rotators[0].getRotation(), (float) rotators[1].getRotation()
            );
            canvas.restore();
        }
        if (lastScrollRate >= 1 && scrollRate >= 1) {
            lastScrollRate = scrollRate;
            return;
        }

        lastScrollRate = scrollRate;

        postInvalidate();
    }

    private void resetDrawer() {
        rotation2D = rotation3D = 0;
        if (sensorManager != null) {
            sensorManager.registerListener(
                    gravityListener, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (orientationListener.canDetectOrientation()) {
            orientationListener.enable();
        }

        setWeatherImplementor();

        if (intervalComputer == null) {
            intervalComputer = new IntervalComputer();
        } else {
            intervalComputer.reset();
        }

        postInvalidate();
    }

    private void setWeatherImplementor() {
        implementor = WeatherImplementorFactory.getWeatherImplementor(weatherKind, daytime, sizes);
        rotators = new RotateController[] {
                new DelayRotateController(rotation2D),
                new DelayRotateController(rotation3D)
        };
        if (implementor != null) {
            step = STEP_DISPLAY;
            backgroundColor = getBackgroundColor();
        }
    }

    private static int getBrighterColor(int color){
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] = hsv[1] - 0.25F;
        hsv[2] = hsv[2] + 0.25F;
        return Color.HSVToColor(hsv);
    }

    // interface.

    // weather view.

    @Override
    public void setWeather(@WeatherKindRule int weatherKind, boolean daytime,
                           @Nullable ResourceProvider provider) {
        if (this.weatherKind == weatherKind && this.daytime == daytime) {
            return;
        }

        this.weatherKind = weatherKind;
        this.daytime = daytime;
        this.backgroundColor = getBackgroundColor();

        if (drawable) {
            if (implementor == null) {
                resetDrawer();
            } else {
                // Set step to dismiss. The implementor will execute exit animation and call weather
                // view to resetWidget it.
                step = STEP_DISMISS;
            }
        }
    }

    @Override
    public void onClick() {
        // do nothing.
    }

    @Override
    public void onScroll(int scrollY) {
        scrollRate = (float) (Math.min(1, 1.0 * scrollY / scrollTransparentTriggerDistance));
        if (lastScrollRate >= 1 && scrollRate < 1) {
            postInvalidate();
        }
    }

    @Override
    public int getWeatherKind() {
        return weatherKind;
    }

    @Override
    public int[] getThemeColors(boolean lightTheme) {
        int color = getBackgroundColor();
        if (!lightTheme) {
            color = getBrighterColor(color);
            return new int[] {color, color, ColorUtils.setAlphaComponent(color, (int) (0.5 * 255))};
        } else {
            return new int[] {color, color, ColorUtils.setAlphaComponent(color, (int) (0.5 * 255))};
        }
    }

    public static int[] getThemeColors(Context context,
                                       @WeatherKindRule int weatherKind, boolean lightTheme) {
        int color = innerGetBackgroundColor(context, weatherKind, lightTheme);
        if (!lightTheme) {
            color = getBrighterColor(color);
            return new int[] {color, color, ColorUtils.setAlphaComponent(color, (int) (0.5 * 255))};
        } else {
            return new int[] {color, color, ColorUtils.setAlphaComponent(color, (int) (0.5 * 255))};
        }
    }

    @Override
    public int getBackgroundColor() {
        return innerGetBackgroundColor(getContext(), weatherKind, daytime);
    }

    private static int innerGetBackgroundColor(Context context,
                                               @WeatherKindRule int weatherKind, boolean daytime) {
        return WeatherImplementorFactory.getWeatherThemeColor(context, weatherKind, daytime);
    }

    @Override
    public int getHeaderHeight() {
        return firstCardMarginTop;
    }

    public void setDrawable(boolean drawable) {
        if (this.drawable == drawable) {
            return;
        }
        this.drawable = drawable;

        if (drawable) {
            resetDrawer();
        } else {
            // !drawable
            if (sensorManager != null) {
                sensorManager.unregisterListener(gravityListener, gravitySensor);
            }
            orientationListener.disable();
        }
    }

    @Override
    public void setGravitySensorEnabled(boolean enabled) {
        this.gravitySensorEnabled = enabled;
    }
}