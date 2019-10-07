package wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.core.graphics.ColorUtils;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.CardOrder;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
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

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        this.sizes = new int[] {metrics.widthPixels, metrics.heightPixels};

        CardOrder cardOrder = SettingsOptionManager.getInstance(getContext()).getCardOrder();
        if (cardOrder == CardOrder.DAILY_FIRST) {
            this.firstCardMarginTop = (int) (getResources().getDisplayMetrics().heightPixels
                    + getResources().getDimensionPixelSize(R.dimen.little_margin)
                    - DisplayUtils.dpToPx(
                            getContext(),
                        56 /* time icon */
                                + 16 * 2 /* first card title margin */
                                + 3 * 4 /* daily item text margin */
                                + 48 * 2 /* daily icon */
                                + 144 /* daily trend */
                                + 16 /* daily item bottom margin */)
                    - getResources().getDimensionPixelSize(R.dimen.title_text_size) // first card title
                    - getResources().getDimensionPixelSize(R.dimen.content_text_size) * 2); // daily item text
        } else {
            this.firstCardMarginTop = (int) (getResources().getDisplayMetrics().heightPixels
                    + getResources().getDimensionPixelSize(R.dimen.little_margin)
                    - DisplayUtils.dpToPx(
                            getContext(),
                        56 /* time icon */
                                + 16 * 2 /* first card title margin */
                                + 2 * 4 /* hourly item text margin */
                                + 48 /* hourly icon */
                                + 128 /* hourly trend */
                                + 16 /* hourly item bottom margin */)
                    - getResources().getDimensionPixelSize(R.dimen.title_text_size) // first card title
                    - getResources().getDimensionPixelSize(R.dimen.content_text_size)); // hourly item text
        }
        firstCardMarginTop = (int) Math.max(
                firstCardMarginTop,
                getResources().getDisplayMetrics().heightPixels * 0.6);
        scrollTransparentTriggerDistance = (int) (
                firstCardMarginTop
                        - DisplayUtils.getStatusBarHeight(getResources())
                        - DisplayUtils.dpToPx(getContext(), 56)
                        - getResources().getDimension(R.dimen.design_title_text_size)
                        - getResources().getDimension(R.dimen.normal_margin)
        );

        this.lastScrollRate = 0;
        this.scrollRate = 0;

        this.drawable = false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (getMeasuredWidth() != 0 && getMeasuredHeight() != 0) {
            sizes[0] = getMeasuredWidth();
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
            implementor.draw(
                    sizes, canvas,
                    displayRate, scrollRate,
                    (float) rotators[0].getRotation(), (float) rotators[1].getRotation()
            );
        }
        if (lastScrollRate >= 1 && scrollRate >= 1) {
            lastScrollRate = scrollRate;
            return;
        }

        lastScrollRate = scrollRate;

        postInvalidate();
    }

    private void setWeatherImplementor() {
        if (implementor != null) {
            backgroundColor = getBackgroundColor();
        }

        step = STEP_DISPLAY;
        implementor = WeatherImplementorFactory.getWeatherImplementor(weatherKind, daytime, sizes);
        rotators = new RotateController[] {
                new DelayRotateController(rotation2D),
                new DelayRotateController(rotation3D)
        };
    }

    private static int getBrighterColor(int color){
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] = hsv[1] - 0.25F;
        hsv[2] = hsv[2] + 0.25F;
        return Color.HSVToColor(hsv);
    }

    private static boolean isIgnoreDayNight(@WeatherKindRule int weatherKind) {
        return weatherKind == WeatherView.WEATHER_KIND_CLOUDY
                || weatherKind == WeatherView.WEATHER_KIND_FOG
                || weatherKind == WeatherView.WEATHER_KIND_HAZE
                || weatherKind == WeatherView.WEATHER_KIND_THUNDERSTORM
                || weatherKind == WeatherView.WEATHER_KIND_THUNDER
                || weatherKind == WeatherView.WEATHER_KIND_WIND;
    }

    // interface.

    // weather view.

    @Override
    public void setWeather(@WeatherKindRule int weatherKind, boolean daytime,
                           @Nullable ResourceProvider provider) {
        if (this.weatherKind == weatherKind
                && (isIgnoreDayNight(weatherKind) || this.daytime == daytime)) {
            return;
        }

        this.weatherKind = weatherKind;
        this.daytime = daytime;
        this.backgroundColor = getBackgroundColor();

        if (drawable) {
            // Set step to dismiss. The implementor will execute exit animation and call weather
            // view to resetWidget it.
            step = STEP_DISMISS;
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
    public int getFirstCardMarginTop() {
        return firstCardMarginTop;
    }

    public void setDrawable(boolean drawable) {
        if (this.drawable == drawable) {
            return;
        }
        this.drawable = drawable;

        if (drawable) {
            rotation2D = rotation3D = 0;
            if (sensorManager != null) {
                sensorManager.registerListener(
                        gravityListener, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
            }

            setWeatherImplementor();

            if (intervalComputer == null) {
                intervalComputer = new IntervalComputer();
            } else {
                intervalComputer.reset();
            }
        } else {
            // !drawable
            if (sensorManager != null) {
                sensorManager.unregisterListener(gravityListener, gravitySensor);
            }
        }
    }

    @Override
    public void setGravitySensorEnabled(boolean enabled) {
        this.gravitySensorEnabled = enabled;
    }
}
