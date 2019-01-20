package wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.ui.widget.weatherView.RenderRunnable;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.CloudImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.HailImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.MeteorShowerImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.RainImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.SnowImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.SunImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.WindImplementor;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Material Weather view.
 * */

public class MaterialWeatherView extends SurfaceView
        implements WeatherView, SurfaceHolder.Callback {

    private SurfaceHolder holder;
    @Nullable private UpdateDataRunnable updateDataRunnable;
    @Nullable private DrawableRunnable drawableRunnable;

    @Nullable private WeatherAnimationImplementor implementor;
    @Nullable private RotateController[] rotators;

    private boolean openGravitySensor;
    @Nullable private SensorManager sensorManager;
    @Nullable private Sensor gravitySensor;

    private float rotation2D;
    private float rotation3D;

    @WeatherView.WeatherKindRule
    private int weatherKind;

    private float displayRate;

    @StepRule
    private int step;
    private static final int STEP_DISPLAY = 1;
    private static final int STEP_DISMISS = -1;

    @IntDef({STEP_DISPLAY, STEP_DISMISS})
    private  @interface StepRule {}

    private int firstCardMarginTop;
    private int scrollTransparentTriggerDistance;
    private float scrollRate;

    private static final int SWITCH_WEATHER_ANIMATION_DURATION = 150;
    protected static long DATA_UPDATE_INTERVAL = 8;
    protected static long DRAW_WEATHER_INTERVAL = 16;


    private class UpdateDataRunnable extends RenderRunnable {

        @Override
        protected void onRender(long interval) {
            if (implementor != null && rotators != null) {
                rotators[0].updateRotation(rotation2D, interval);
                rotators[1].updateRotation(rotation3D, interval);

                implementor.updateData(
                        MaterialWeatherView.this, interval,
                        (float) rotators[0].getRotate(), (float) rotators[1].getRotate());
                if (step == STEP_DISPLAY) {
                    displayRate = (float) Math.min(
                            1, displayRate + 1.0 * interval / SWITCH_WEATHER_ANIMATION_DURATION);
                } else {
                    displayRate = (float) Math.max(
                            0, displayRate - 1.0 * interval / SWITCH_WEATHER_ANIMATION_DURATION);
                }
                if (displayRate == 0) {
                    setWeatherImplementor();
                }
            }
        }

        @Override
        protected long getInterval() {
            return DATA_UPDATE_INTERVAL;
        }
    }

    private class DrawableRunnable extends RenderRunnable {

        @Nullable private Canvas canvas;
        private float lastScrollRate;

        @Override
        protected void onRender(long interval) {
            if (lastScrollRate >= 1 && scrollRate >= 1) {
                lastScrollRate = scrollRate;
            } else if (isRunning() && implementor != null && rotators != null) {
                lastScrollRate = scrollRate;
                try {
                    canvas = holder.lockCanvas();
                    if (canvas != null) {
                        implementor.draw(
                                MaterialWeatherView.this, canvas,
                                displayRate, scrollRate,
                                (float) rotators[0].getRotate(), (float) rotators[1].getRotate());
                        holder.unlockCanvasAndPost(canvas);
                    }
                } catch (Exception ignored) {
                    // do nothing.
                }
            }
        }

        @Override
        protected long getInterval() {
            return DRAW_WEATHER_INTERVAL;
        }
    }

    /**
     * This class is used to implement different kinds of weather animations.
     * */
    public static abstract class WeatherAnimationImplementor {

        public abstract void updateData(MaterialWeatherView view, long interval,
                                 float rotation2D, float rotation3D);

        // return true if finish drawing.
        public abstract void draw(MaterialWeatherView view, Canvas canvas,
                                  float displayRatio, float scrollRate,
                                  float rotation2D, float rotation3D);
    }

    public static abstract class RotateController {

        public abstract void updateRotation(double rotation, double interval);

        public abstract double getRotate();
    }

    private SensorEventListener gravityListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent ev) {
            // x : (+) fall to the left / (-) fall to the right.
            // y : (+) stand / (-) head stand.
            // z : (+) look down / (-) look up.
            // rotation2D : (+) anticlockwise / (-) clockwise.
            // rotation3D : (+) look down / (-) look up.
            if (openGravitySensor) {
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
        this.holder = getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.RGBA_8888);

        this.sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            this.openGravitySensor = true;
            this.gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        }

        this.step = STEP_DISPLAY;
        setWeather(WeatherView.WEATHER_KING_NULL);

        if (GeometricWeather.getInstance().getCardOrder().equals("daily_first")) {
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
        scrollTransparentTriggerDistance = (int) (firstCardMarginTop
                - DisplayUtils.getStatusBarHeight(getResources())
                - DisplayUtils.dpToPx(getContext(), 56)
                - getResources().getDimension(R.dimen.design_title_text_size)
                - getResources().getDimension(R.dimen.normal_margin));

        this.scrollRate = 0;
    }

    private void setWeatherImplementor() {
        step = STEP_DISPLAY;
        switch (weatherKind) {
            case WeatherView.WEATHER_KIND_CLEAR_DAY:
                implementor = new SunImplementor(this);
                rotators = new RotateController[] {
                        new DelayRotateController(rotation2D),
                        new DelayRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_CLEAR_NIGHT:
                implementor = new MeteorShowerImplementor(this);
                rotators = new RotateController[] {
                        new DelayRotateController(rotation2D),
                        new DelayRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_CLOUDY:
                implementor = new CloudImplementor(this, CloudImplementor.TYPE_CLOUDY);
                rotators = new RotateController[] {
                        new DelayRotateController(rotation2D),
                        new DelayRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_CLOUD_DAY:
                implementor = new CloudImplementor(this, CloudImplementor.TYPE_CLOUD_DAY);
                rotators = new RotateController[] {
                        new DelayRotateController(rotation2D),
                        new DelayRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_CLOUD_NIGHT:
                implementor = new CloudImplementor(this, CloudImplementor.TYPE_CLOUD_NIGHT);
                rotators = new RotateController[] {
                        new DelayRotateController(rotation2D),
                        new DelayRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_FOG:
                implementor = new CloudImplementor(this, CloudImplementor.TYPE_FOG);
                rotators = new RotateController[] {
                        new DelayRotateController(rotation2D),
                        new DelayRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_HAIL_DAY:
                implementor = new HailImplementor(this, HailImplementor.TYPE_HAIL_DAY);
                rotators = new RotateController[] {
                        new DelayRotateController(rotation2D),
                        new DelayRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_HAIL_NIGHT:
                implementor = new HailImplementor(this, HailImplementor.TYPE_HAIL_NIGHT);
                rotators = new RotateController[] {
                        new DelayRotateController(rotation2D),
                        new DelayRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_HAZE:
                implementor = new CloudImplementor(this, CloudImplementor.TYPE_HAZE);
                rotators = new RotateController[] {
                        new DelayRotateController(rotation2D),
                        new DelayRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_RAINY_DAY:
                implementor = new RainImplementor(this, RainImplementor.TYPE_RAIN_DAY);
                rotators = new RotateController[] {
                        new DelayRotateController(rotation2D),
                        new DelayRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_RAINY_NIGHT:
                implementor = new RainImplementor(this, RainImplementor.TYPE_RAIN_NIGHT);
                rotators = new RotateController[] {
                        new DelayRotateController(rotation2D),
                        new DelayRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_SNOW_DAY:
                implementor = new SnowImplementor(this, SnowImplementor.TYPE_SNOW_DAY);
                rotators = new RotateController[] {
                        new DelayRotateController(rotation2D),
                        new DelayRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_SNOW_NIGHT:
                implementor = new SnowImplementor(this, SnowImplementor.TYPE_SNOW_NIGHT);
                rotators = new RotateController[] {
                        new DelayRotateController(rotation2D),
                        new DelayRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_THUNDERSTORM:
                implementor = new RainImplementor(this, RainImplementor.TYPE_THUNDERSTORM);
                rotators = new RotateController[] {
                        new DelayRotateController(rotation2D),
                        new DelayRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_THUNDER:
                implementor = new CloudImplementor(this, CloudImplementor.TYPE_THUNDER);
                rotators = new RotateController[] {
                        new DelayRotateController(rotation2D),
                        new DelayRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_WIND:
                implementor = new WindImplementor(this);
                rotators = new RotateController[] {
                        new DelayRotateController(rotation2D),
                        new DelayRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_SLEET_DAY:
                implementor = new RainImplementor(this, RainImplementor.TYPE_SLEET_DAY);
                rotators = new RotateController[] {
                        new DelayRotateController(rotation2D),
                        new DelayRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_SLEET_NIGHT:
                implementor = new RainImplementor(this, RainImplementor.TYPE_SLEET_NIGHT);
                rotators = new RotateController[] {
                        new DelayRotateController(rotation2D),
                        new DelayRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KING_NULL:
                implementor = null;
                rotators = null;
                break;
        }
    }

    private void initSensorData() {
        this.rotation2D = 0;
        this.rotation3D = 0;
    }

    public void setOpenGravitySensor(boolean openGravitySensor) {
        this.openGravitySensor = openGravitySensor;
    }

    private int getBrighterColor(int color){
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] = hsv[1] - 0.25F;
        hsv[2] = hsv[2] + 0.25F;
        return Color.HSVToColor(hsv);
    }

    // interface.

    // weather view.

    @Override
    public void setWeather(@WeatherView.WeatherKindRule int weatherKind) {
        if (this.weatherKind == weatherKind) {
            return;
        }
        this.weatherKind = weatherKind;
        if (updateDataRunnable != null && updateDataRunnable.isRunning()
                && drawableRunnable != null && drawableRunnable.isRunning()) {
            // Set step to dismiss. The implementor will execute exit animation and call weather
            // view to reset it.
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
    }

    @Override
    public int getWeatherKind() {
        return weatherKind;
    }

    @Override
    public int[] getThemeColors() {
        int color = getBackgroundColor();
        if (DisplayUtils.isDarkMode(getContext())) {
            color = getBrighterColor(color);
            return new int[] {color, color, ColorUtils.setAlphaComponent(color, (int) (0.5 * 255))};
        } else {
            return new int[] {color, color, ColorUtils.setAlphaComponent(color, (int) (0.5 * 255))};
        }
    }

    @Override
    public int getBackgroundColor() {
        int color = ContextCompat.getColor(getContext(), R.color.colorPrimary);
        switch (weatherKind) {
            case WeatherView.WEATHER_KIND_CLEAR_DAY:
                color = SunImplementor.getThemeColor();
                break;

            case WeatherView.WEATHER_KIND_CLEAR_NIGHT:
                color = MeteorShowerImplementor.getThemeColor();
                break;

            case WeatherView.WEATHER_KIND_CLOUDY:
                color = CloudImplementor.getThemeColor(getContext(), CloudImplementor.TYPE_CLOUDY);
                break;

            case WeatherView.WEATHER_KIND_CLOUD_DAY:
                color = CloudImplementor.getThemeColor(getContext(), CloudImplementor.TYPE_CLOUD_DAY);
                break;

            case WeatherView.WEATHER_KIND_CLOUD_NIGHT:
                color = CloudImplementor.getThemeColor(getContext(), CloudImplementor.TYPE_CLOUD_NIGHT);
                break;

            case WeatherView.WEATHER_KIND_FOG:
                color = CloudImplementor.getThemeColor(getContext(), CloudImplementor.TYPE_FOG);
                break;

            case WeatherView.WEATHER_KIND_HAIL_DAY:
                color = HailImplementor.getThemeColor(getContext(), HailImplementor.TYPE_HAIL_DAY);
                break;

            case WeatherView.WEATHER_KIND_HAIL_NIGHT:
                color = HailImplementor.getThemeColor(getContext(), HailImplementor.TYPE_HAIL_NIGHT);
                break;

            case WeatherView.WEATHER_KIND_HAZE:
                color = CloudImplementor.getThemeColor(getContext(), CloudImplementor.TYPE_HAZE);
                break;

            case WeatherView.WEATHER_KIND_RAINY_DAY:
                color = RainImplementor.getThemeColor(getContext(), RainImplementor.TYPE_RAIN_DAY);
                break;

            case WeatherView.WEATHER_KIND_RAINY_NIGHT:
                color = RainImplementor.getThemeColor(getContext(), RainImplementor.TYPE_RAIN_NIGHT);
                break;

            case WeatherView.WEATHER_KIND_SLEET_DAY:
                color = RainImplementor.getThemeColor(getContext(), RainImplementor.TYPE_SLEET_DAY);
                break;

            case WeatherView.WEATHER_KIND_SLEET_NIGHT:
                color = RainImplementor.getThemeColor(getContext(), RainImplementor.TYPE_SLEET_NIGHT);
                break;

            case WeatherView.WEATHER_KIND_SNOW_DAY:
                color = SnowImplementor.getThemeColor(getContext(), SnowImplementor.TYPE_SNOW_DAY);
                break;

            case WeatherView.WEATHER_KIND_SNOW_NIGHT:
                color = SnowImplementor.getThemeColor(getContext(), SnowImplementor.TYPE_SNOW_NIGHT);
                break;

            case WeatherView.WEATHER_KIND_THUNDERSTORM:
                color = RainImplementor.getThemeColor(getContext(), RainImplementor.TYPE_THUNDERSTORM);
                break;

            case WeatherView.WEATHER_KIND_THUNDER:
                color = CloudImplementor.getThemeColor(getContext(), CloudImplementor.TYPE_THUNDER);
                break;

            case WeatherView.WEATHER_KIND_WIND:
                color = WindImplementor.getThemeColor();
                break;

            case WeatherView.WEATHER_KING_NULL:
                break;
        }
        return color;
    }

    @Override
    public int getFirstCardMarginTop() {
        return firstCardMarginTop;
    }

    // callback.

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        initSensorData();
        if (sensorManager != null) {
            sensorManager.registerListener(gravityListener, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
        }

        setWeatherImplementor();

        if (updateDataRunnable == null || !updateDataRunnable.isRunning()) {
            updateDataRunnable = new UpdateDataRunnable();
            new Thread(updateDataRunnable).start();
        }
        if (drawableRunnable == null || !drawableRunnable.isRunning()) {
            drawableRunnable = new DrawableRunnable();
            new Thread(drawableRunnable).start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        // do nothing.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (sensorManager != null) {
            sensorManager.unregisterListener(gravityListener, gravitySensor);
        }
        if (updateDataRunnable != null) {
            updateDataRunnable.setRunning(false);
            updateDataRunnable = null;
        }
        if (drawableRunnable != null) {
            drawableRunnable.setRunning(false);
            drawableRunnable = null;
        }
    }
}
