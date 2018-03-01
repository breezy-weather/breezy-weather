package wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.CloudImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.HailImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.MeteorShowerImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.RainImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.SmogImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.SnowImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.SunImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.WindImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.rotateController.BaseRotateController;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.rotateController.DecelerateRotateController;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Material Weather view.
 * */

public class MaterialWeatherView extends SurfaceView
        implements WeatherView, SurfaceHolder.Callback, Runnable {

    private SurfaceHolder holder;
    private final Object surfaceLock = new Object();

    @Nullable
    private WeatherAnimationImplementor implementor;
    @Nullable
    private RotateController[] rotators;
    private boolean running;

    private boolean openGravitySensor;
    @Nullable
    private SensorManager sensorManager;
    @Nullable
    private Sensor gravitySensor;

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
    private float scrollRate;

    /**
     * This class is used to implement different kinds of weather animations.
     * */
    public static abstract class WeatherAnimationImplementor {

        public static long REFRESH_INTERVAL = 16;

        public abstract void updateData(MaterialWeatherView view, float rotation2D, float rotation3D);

        // return true if finish drawing.
        public abstract void draw(MaterialWeatherView view, Canvas canvas,
                                  float displayRatio, float scrollRate,
                                  float rotation2D, float rotation3D);
    }

    public static abstract class RotateController {

        public abstract void updateRotation(double rotation);

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

        this.firstCardMarginTop = (int) (getResources().getDisplayMetrics().widthPixels * 1.1761);
        if (getResources().getDisplayMetrics().heightPixels - firstCardMarginTop
                < DisplayUtils.dpToPx(getContext(), 214)) {
            firstCardMarginTop = (int) (getResources().getDisplayMetrics().heightPixels
                    - DisplayUtils.dpToPx(getContext(), 214));
            if (firstCardMarginTop < getResources().getDisplayMetrics().heightPixels * 0.5) {
                firstCardMarginTop = (int) (getResources().getDisplayMetrics().heightPixels * 0.5);
            }
        }

        this.scrollRate = 0;
    }

    private void setWeatherImplementor() {
        step = STEP_DISPLAY;
        switch (weatherKind) {
            case WeatherView.WEATHER_KIND_CLEAR_DAY:
                implementor = new SunImplementor(this);
                rotators = new RotateController[] {
                        new DecelerateRotateController(rotation2D),
                        new DecelerateRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_CLEAR_NIGHT:
                implementor = new MeteorShowerImplementor(this);
                rotators = new RotateController[] {
                        new BaseRotateController(rotation2D),
                        new BaseRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_CLOUDY:
                implementor = new CloudImplementor(this, CloudImplementor.TYPE_CLOUDY);
                rotators = new RotateController[] {
                        new DecelerateRotateController(rotation2D),
                        new DecelerateRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_CLOUD_DAY:
                implementor = new CloudImplementor(this, CloudImplementor.TYPE_CLOUD_DAY);
                rotators = new RotateController[] {
                        new DecelerateRotateController(rotation2D),
                        new DecelerateRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_CLOUD_NIGHT:
                implementor = new CloudImplementor(this, CloudImplementor.TYPE_CLOUD_NIGHT);
                rotators = new RotateController[] {
                        new DecelerateRotateController(rotation2D),
                        new DecelerateRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_FOG:
                implementor = new SmogImplementor(this, SmogImplementor.TYPE_FOG);
                rotators = new RotateController[] {
                        new DecelerateRotateController(rotation2D),
                        new DecelerateRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_HAIL_DAY:
                implementor = new HailImplementor(this, HailImplementor.TYPE_HAIL_DAY);
                rotators = new RotateController[] {
                        new BaseRotateController(rotation2D),
                        new BaseRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_HAIL_NIGHT:
                implementor = new HailImplementor(this, HailImplementor.TYPE_HAIL_NIGHT);
                rotators = new RotateController[] {
                        new BaseRotateController(rotation2D),
                        new BaseRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_HAZE:
                implementor = new SmogImplementor(this, SmogImplementor.TYPE_HAZE);
                rotators = new RotateController[] {
                        new DecelerateRotateController(rotation2D),
                        new DecelerateRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_RAINY_DAY:
                implementor = new RainImplementor(this, RainImplementor.TYPE_RAIN_DAY);
                rotators = new RotateController[] {
                        new BaseRotateController(rotation2D),
                        new BaseRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_RAINY_NIGHT:
                implementor = new RainImplementor(this, RainImplementor.TYPE_RAIN_NIGHT);
                rotators = new RotateController[] {
                        new BaseRotateController(rotation2D),
                        new BaseRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_SNOW_DAY:
                implementor = new SnowImplementor(this, SnowImplementor.TYPE_SNOW_DAY);
                rotators = new RotateController[] {
                        new BaseRotateController(rotation2D),
                        new BaseRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_SNOW_NIGHT:
                implementor = new SnowImplementor(this, SnowImplementor.TYPE_SNOW_NIGHT);
                rotators = new RotateController[] {
                        new BaseRotateController(rotation2D),
                        new BaseRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_THUNDERSTORM:
                implementor = new RainImplementor(this, RainImplementor.TYPE_THUNDERSTORM);
                rotators = new RotateController[] {
                        new BaseRotateController(rotation2D),
                        new BaseRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_THUNDER:
                implementor = new CloudImplementor(this, CloudImplementor.TYPE_THUNDER);
                rotators = new RotateController[] {
                        new DecelerateRotateController(rotation2D),
                        new DecelerateRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_WIND:
                implementor = new WindImplementor(this);
                rotators = new RotateController[] {
                        new BaseRotateController(rotation2D),
                        new BaseRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_SLEET_DAY:
                implementor = new RainImplementor(this, RainImplementor.TYPE_SLEET_DAY);
                rotators = new RotateController[] {
                        new BaseRotateController(rotation2D),
                        new BaseRotateController(rotation3D)};
                break;

            case WeatherView.WEATHER_KIND_SLEET_NIGHT:
                implementor = new RainImplementor(this, RainImplementor.TYPE_SLEET_NIGHT);
                rotators = new RotateController[] {
                        new BaseRotateController(rotation2D),
                        new BaseRotateController(rotation3D)};
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

    // interface.

    // weather view.

    @Override
    public void setWeather(@WeatherView.WeatherKindRule int weatherKind) {
        if (this.weatherKind == weatherKind) {
            return;
        }
        this.weatherKind = weatherKind;
        if (running) {
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
        scrollRate = (float) (Math.min(1, 1.0 * scrollY / firstCardMarginTop));
    }

    @Override
    public int getWeatherKind() {
        return weatherKind;
    }

    @Override
    public int[] getThemeColors() {
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
                color = SmogImplementor.getThemeColor(getContext(), SmogImplementor.TYPE_FOG);
                break;

            case WeatherView.WEATHER_KIND_HAIL_DAY:
                color = HailImplementor.getThemeColor(getContext(), HailImplementor.TYPE_HAIL_DAY);
                break;

            case WeatherView.WEATHER_KIND_HAIL_NIGHT:
                color = HailImplementor.getThemeColor(getContext(), HailImplementor.TYPE_HAIL_NIGHT);
                break;

            case WeatherView.WEATHER_KIND_HAZE:
                color = SmogImplementor.getThemeColor(getContext(), SmogImplementor.TYPE_HAZE);
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
        return new int[] {color, color, ColorUtils.setAlphaComponent(color, (int) (0.5 * 255))};
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

        running = true;
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        // do nothing.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        synchronized (surfaceLock) {
            if (sensorManager != null) {
                sensorManager.unregisterListener(gravityListener, gravitySensor);
            }
            running = false;
        }
    }

    // runnable.

    @Override
    public void run() {
        Canvas canvas;
        long timestamp;
        long remaining;
        while (running) {
            timestamp = SystemClock.currentThreadTimeMillis();
            if (implementor != null && rotators != null) {
                rotators[0].updateRotation(rotation2D);
                rotators[1].updateRotation(rotation3D);

                implementor.updateData(
                        this,
                        (float) rotators[0].getRotate(), (float) rotators[1].getRotate());

                synchronized (surfaceLock) {
                    if (running) {
                        canvas = holder.lockCanvas();
                        if (canvas != null) {
                            if (step == STEP_DISPLAY) {
                                displayRate = (float) Math.min(
                                        1, displayRate + WeatherAnimationImplementor.REFRESH_INTERVAL / 150.0);
                            } else {
                                displayRate = (float) Math.max(
                                        0, displayRate - WeatherAnimationImplementor.REFRESH_INTERVAL / 150.0);
                            }
                            implementor.draw(
                                    this, canvas,
                                    displayRate, scrollRate,
                                    (float) rotators[0].getRotate(), (float) rotators[1].getRotate());
                            if (displayRate == 0) {
                                setWeatherImplementor();
                            }
                            holder.unlockCanvasAndPost(canvas);
                        }
                    }
                }
            }
            remaining = WeatherAnimationImplementor.REFRESH_INTERVAL
                    - (SystemClock.currentThreadTimeMillis() - timestamp);
            if (remaining > 0) {
                try {
                    Thread.sleep(remaining);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
