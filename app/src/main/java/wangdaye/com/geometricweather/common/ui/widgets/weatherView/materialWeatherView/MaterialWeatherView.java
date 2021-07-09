package wangdaye.com.geometricweather.common.ui.widgets.weatherView.materialWeatherView;

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
import android.view.Window;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.core.graphics.ColorUtils;

import wangdaye.com.geometricweather.common.ui.widgets.weatherView.WeatherView;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.resource.providers.ResourceProvider;

/**
 * Material Weather view.
 * */

public class MaterialWeatherView extends View implements WeatherView {

    @Nullable private IntervalComputer mIntervalComputer;

    @Nullable private WeatherAnimationImplementor mImplementor;
    @Nullable private RotateController[] mRotators;

    private boolean mGravitySensorEnabled;
    @Nullable private SensorManager mSensorManager;
    @Nullable private Sensor mGravitySensor;

    @Size(2) int[] mSizes;
    private float mRotation2D;
    private float mRotation3D;

    @WeatherKindRule private int mWeatherKind;
    private boolean mDaytime;
    @ColorInt private int mBackgroundColor;

    private float mDisplayRate;

    @StepRule
    private int mStep;
    private static final int STEP_DISPLAY = 1;
    private static final int STEP_DISMISS = -1;

    @IntDef({STEP_DISPLAY, STEP_DISMISS})
    private  @interface StepRule {}

    private int mFirstCardMarginTop;
    private int mScrollTransparentTriggerDistance;

    private float mLastScrollRate;
    private float mScrollRate;

    private boolean mDrawable;

    private DeviceOrientation mDeviceOrientation;
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

    private final SensorEventListener mGravityListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent ev) {
            // x : (+) fall to the left / (-) fall to the right.
            // y : (+) stand / (-) head stand.
            // z : (+) look down / (-) look up.
            // rotation2D : (+) anticlockwise / (-) clockwise.
            // rotation3D : (+) look down / (-) look up.
            if (mGravitySensorEnabled) {
                float aX = ev.values[0];
                float aY = ev.values[1];
                float aZ = ev.values[2];
                double g2D = Math.sqrt(aX * aX + aY * aY);
                double g3D = Math.sqrt(aX * aX + aY * aY + aZ * aZ);
                double cos2D = Math.max(Math.min(1, aY / g2D), -1);
                double cos3D = Math.max(Math.min(1, g2D * (aY >= 0 ? 1 : -1) / g3D), -1);
                mRotation2D = (float) Math.toDegrees(Math.acos(cos2D)) * (aX >= 0 ? 1 : -1);
                mRotation3D = (float) Math.toDegrees(Math.acos(cos3D)) * (aZ >= 0 ? 1 : -1);

                switch (mDeviceOrientation) {
                    case TOP:
                        break;

                    case LEFT:
                        mRotation2D -= 90;
                        break;

                    case RIGHT:
                        mRotation2D += 90;
                        break;

                    case BOTTOM:
                        if (mRotation2D > 0) {
                            mRotation2D -= 180;
                        } else {
                            mRotation2D += 180;
                        }
                        break;
                }

                if (60 < Math.abs(mRotation3D) && Math.abs(mRotation3D) < 120) {
                    mRotation2D *= Math.abs(Math.abs(mRotation3D) - 90) / 30.0;
                }
            } else {
                mRotation2D = 0;
                mRotation3D = 0;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            // do nothing.
        }
    };

    private final OrientationEventListener mOrientationListener = new OrientationEventListener(getContext()) {
        @Override
        public void onOrientationChanged(int orientation) {
            mDeviceOrientation = getDeviceOrientation(orientation);
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
        initialize();
    }

    public MaterialWeatherView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public MaterialWeatherView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mGravitySensorEnabled = true;
            mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        }

        mStep = STEP_DISPLAY;
        setWeather(WeatherView.WEATHER_KING_NULL, true, null);

        Resources res = getResources();
        DisplayMetrics metrics = res.getDisplayMetrics();
        mSizes = new int[] {metrics.widthPixels, metrics.heightPixels};

        mFirstCardMarginTop = (int) (res.getDisplayMetrics().heightPixels * 0.66);
        mScrollTransparentTriggerDistance = mFirstCardMarginTop;

        mLastScrollRate = 0;
        mScrollRate = 0;

        mDrawable = false;

        mDeviceOrientation = DeviceOrientation.TOP;
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        mScrollTransparentTriggerDistance = mFirstCardMarginTop - insets.top;
        return false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (getMeasuredWidth() != 0 && getMeasuredHeight() != 0) {
            final int width = DisplayUtils.getTabletListAdaptiveWidth(getContext(), getMeasuredWidth());
            final int height = getMeasuredHeight();
            if (mSizes[0] != width || mSizes[1] != height) {
                mSizes[0] = width;
                mSizes[1] = height;
                setWeatherImplementor();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mIntervalComputer == null || mRotators == null || mImplementor == null) {
            canvas.drawColor(getBackgroundColor());
            return;
        }

        mIntervalComputer.invalidate();
        mRotators[0].updateRotation(mRotation2D, mIntervalComputer.getInterval());
        mRotators[1].updateRotation(mRotation3D, mIntervalComputer.getInterval());

        mImplementor.updateData(
                mSizes, (long) mIntervalComputer.getInterval(),
                (float) mRotators[0].getRotation(), (float) mRotators[1].getRotation()
        );

        mDisplayRate = (float) (
                mDisplayRate
                        + (mStep == STEP_DISPLAY ? 1f : -1f)
                        * mIntervalComputer.getInterval()
                        / SWITCH_ANIMATION_DURATION
        );
        mDisplayRate = Math.max(0, mDisplayRate);
        mDisplayRate = Math.min(1, mDisplayRate);

        if (mDisplayRate == 0) {
            setWeatherImplementor();
        }

        canvas.drawColor(mBackgroundColor);
        if (mImplementor != null && mRotators != null) {
            canvas.save();
            canvas.translate(
                    (getMeasuredWidth() - mSizes[0]) / 2f,
                    (getMeasuredHeight() - mSizes[1]) / 2f
            );
            mImplementor.draw(
                    mSizes, canvas,
                    mDisplayRate, mScrollRate,
                    (float) mRotators[0].getRotation(), (float) mRotators[1].getRotation()
            );
            canvas.restore();
        }
        if (mLastScrollRate >= 1 && mScrollRate >= 1) {
            mLastScrollRate = mScrollRate;
            setIntervalComputer();
            return;
        }

        mLastScrollRate = mScrollRate;

        postInvalidate();
    }

    private void resetDrawer() {
        mRotation2D = mRotation3D = 0;
        if (mSensorManager != null) {
            mSensorManager.registerListener(
                    mGravityListener, mGravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (mOrientationListener.canDetectOrientation()) {
            mOrientationListener.enable();
        }

        setWeatherImplementor();
        setIntervalComputer();

        postInvalidate();
    }

    private void setWeatherImplementor() {
        mImplementor = WeatherImplementorFactory.getWeatherImplementor(mWeatherKind, mDaytime, mSizes);
        mRotators = new RotateController[] {
                new DelayRotateController(mRotation2D),
                new DelayRotateController(mRotation3D)
        };
        if (mImplementor != null) {
            mStep = STEP_DISPLAY;
            mBackgroundColor = getBackgroundColor();
        }
    }

    private void setIntervalComputer() {
        if (mIntervalComputer == null) {
            mIntervalComputer = new IntervalComputer(getContext());
        } else {
            mIntervalComputer.reset(getContext());
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
        if (mWeatherKind == weatherKind && mDaytime == daytime) {
            return;
        }

        mWeatherKind = weatherKind;
        mDaytime = daytime;
        mBackgroundColor = getBackgroundColor();

        if (mDrawable) {
            if (mImplementor == null) {
                resetDrawer();
            } else {
                // Set step to dismiss. The implementor will execute exit animation and call weather
                // view to resetWidget it.
                mStep = STEP_DISMISS;
            }
        }
    }

    @Override
    public void onClick() {
        // do nothing.
    }

    @Override
    public void onScroll(int scrollY) {
        mScrollRate = (float) (Math.min(1, 1.0 * scrollY / mScrollTransparentTriggerDistance));
        if (mLastScrollRate >= 1 && mScrollRate < 1) {
            postInvalidate();
        }
    }

    @Override
    public int getWeatherKind() {
        return mWeatherKind;
    }

    @Override
    public int[] getThemeColors(boolean lightTheme) {
        int color = getBackgroundColor();
        if (!lightTheme) {
            color = getBrighterColor(color);
        }
        return new int[] {color, color, ColorUtils.setAlphaComponent(color, (int) (0.5 * 255))};
    }

    public static int[] getThemeColors(Context context,
                                       @WeatherKindRule int weatherKind, boolean lightTheme) {
        int color = innerGetBackgroundColor(context, weatherKind, lightTheme);
        if (!lightTheme) {
            color = getBrighterColor(color);
        }
        return new int[] {color, color, ColorUtils.setAlphaComponent(color, (int) (0.5 * 255))};
    }

    @Override
    public int getBackgroundColor() {
        return innerGetBackgroundColor(getContext(), mWeatherKind, mDaytime);
    }

    private static int innerGetBackgroundColor(Context context,
                                               @WeatherKindRule int weatherKind, boolean daytime) {
        return WeatherImplementorFactory.getWeatherThemeColor(context, weatherKind, daytime);
    }

    @Override
    public int getHeaderHeight() {
        return mFirstCardMarginTop;
    }

    public void setDrawable(boolean drawable) {
        if (mDrawable == drawable) {
            return;
        }
        mDrawable = drawable;

        if (drawable) {
            resetDrawer();
        } else {
            // !drawable
            if (mSensorManager != null) {
                mSensorManager.unregisterListener(mGravityListener, mGravitySensor);
            }
            mOrientationListener.disable();
        }
    }

    @Override
    public void setGravitySensorEnabled(boolean enabled) {
        mGravitySensorEnabled = enabled;
    }

    @Override
    public void setSystemBarStyle(Context context, Window window,
                                  boolean statusShader, boolean lightStatus,
                                  boolean navigationShader, boolean lightNavigation) {
        DisplayUtils.setSystemBarStyle(context, window, true,
                statusShader, lightNavigation, navigationShader, lightNavigation);
    }

    @Override
    public void setSystemBarColor(Context context, Window window,
                                  boolean statusShader, boolean lightStatus,
                                  boolean navigationShader, boolean lightNavigation) {
        DisplayUtils.setSystemBarColor(context, window, true,
                statusShader, lightNavigation, navigationShader, lightNavigation);
    }
}