package wangdaye.com.geometricweather.service;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.view.SurfaceHolder;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.ui.widget.weatherView.RenderRunnable;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherViewController;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.DelayRotateController;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.MaterialWeatherView;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.CloudImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.HailImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.MeteorShowerImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.RainImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.SnowImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.SunImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.WindImplementor;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;
import wangdaye.com.geometricweather.utils.manager.TimeManager;

public class LiveWallpaperService extends WallpaperService {

    private static final int STEP_DISPLAY = 1;
    private static final int STEP_DISMISS = -1;

    @IntDef({STEP_DISPLAY, STEP_DISMISS})
    private @interface StepRule {}

    private static final int SWITCH_WEATHER_ANIMATION_DURATION = 150;
    protected static long DATA_UPDATE_INTERVAL = 8;
    protected static long DRAW_WEATHER_INTERVAL = 16;

    private class WeatherEngine extends Engine {

        private SurfaceHolder holder;
        @Nullable private UpdateDataRunnable updateDataRunnable;
        @Nullable private DrawableRunnable drawableRunnable;

        @Nullable private MaterialWeatherView.WeatherAnimationImplementor implementor;
        @Nullable private MaterialWeatherView.RotateController[] rotators;

        private boolean openGravitySensor;
        @Nullable private SensorManager sensorManager;
        @Nullable private Sensor gravitySensor;

        @Size(2) int[] sizes;
        private float rotation2D;
        private float rotation3D;

        @WeatherView.WeatherKindRule
        private int weatherKind;

        private float displayRate;

        @StepRule
        private int step;
        private boolean visible;

        private class UpdateDataRunnable extends RenderRunnable {

            @Override
            protected void onRender(long interval) {
                if (implementor != null && rotators != null) {
                    rotators[0].updateRotation(rotation2D, interval);
                    rotators[1].updateRotation(rotation3D, interval);

                    implementor.updateData(
                            sizes, interval,
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

            @Override
            protected void onRender(long interval) {
                if (isRunning() && implementor != null && rotators != null) {
                    try {
                        canvas = holder.lockCanvas();
                        if (canvas != null) {
                            sizes[0] = canvas.getWidth();
                            sizes[1] = canvas.getHeight();
                            implementor.draw(
                                    sizes, canvas,
                                    displayRate, 0,
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

        private void setWeather(@WeatherView.WeatherKindRule int weatherKind) {
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

        private void setWeatherImplementor() {
            step = STEP_DISPLAY;
            switch (weatherKind) {
                case WeatherView.WEATHER_KIND_CLEAR_DAY:
                    implementor = new SunImplementor(sizes);
                    rotators = new MaterialWeatherView.RotateController[] {
                            new DelayRotateController(rotation2D),
                            new DelayRotateController(rotation3D)};
                    break;

                case WeatherView.WEATHER_KIND_CLEAR_NIGHT:
                    implementor = new MeteorShowerImplementor(sizes);
                    rotators = new MaterialWeatherView.RotateController[] {
                            new DelayRotateController(rotation2D),
                            new DelayRotateController(rotation3D)};
                    break;

                case WeatherView.WEATHER_KIND_CLOUDY:
                    implementor = new CloudImplementor(sizes, CloudImplementor.TYPE_CLOUDY);
                    rotators = new MaterialWeatherView.RotateController[] {
                            new DelayRotateController(rotation2D),
                            new DelayRotateController(rotation3D)};
                    break;

                case WeatherView.WEATHER_KIND_CLOUD_DAY:
                    implementor = new CloudImplementor(sizes, CloudImplementor.TYPE_CLOUD_DAY);
                    rotators = new MaterialWeatherView.RotateController[] {
                            new DelayRotateController(rotation2D),
                            new DelayRotateController(rotation3D)};
                    break;

                case WeatherView.WEATHER_KIND_CLOUD_NIGHT:
                    implementor = new CloudImplementor(sizes, CloudImplementor.TYPE_CLOUD_NIGHT);
                    rotators = new MaterialWeatherView.RotateController[] {
                            new DelayRotateController(rotation2D),
                            new DelayRotateController(rotation3D)};
                    break;

                case WeatherView.WEATHER_KIND_FOG:
                    implementor = new CloudImplementor(sizes, CloudImplementor.TYPE_FOG);
                    rotators = new MaterialWeatherView.RotateController[] {
                            new DelayRotateController(rotation2D),
                            new DelayRotateController(rotation3D)};
                    break;

                case WeatherView.WEATHER_KIND_HAIL_DAY:
                    implementor = new HailImplementor(sizes, HailImplementor.TYPE_HAIL_DAY);
                    rotators = new MaterialWeatherView.RotateController[] {
                            new DelayRotateController(rotation2D),
                            new DelayRotateController(rotation3D)};
                    break;

                case WeatherView.WEATHER_KIND_HAIL_NIGHT:
                    implementor = new HailImplementor(sizes, HailImplementor.TYPE_HAIL_NIGHT);
                    rotators = new MaterialWeatherView.RotateController[] {
                            new DelayRotateController(rotation2D),
                            new DelayRotateController(rotation3D)};
                    break;

                case WeatherView.WEATHER_KIND_HAZE:
                    implementor = new CloudImplementor(sizes, CloudImplementor.TYPE_HAZE);
                    rotators = new MaterialWeatherView.RotateController[] {
                            new DelayRotateController(rotation2D),
                            new DelayRotateController(rotation3D)};
                    break;

                case WeatherView.WEATHER_KIND_RAINY_DAY:
                    implementor = new RainImplementor(sizes, RainImplementor.TYPE_RAIN_DAY);
                    rotators = new MaterialWeatherView.RotateController[] {
                            new DelayRotateController(rotation2D),
                            new DelayRotateController(rotation3D)};
                    break;

                case WeatherView.WEATHER_KIND_RAINY_NIGHT:
                    implementor = new RainImplementor(sizes, RainImplementor.TYPE_RAIN_NIGHT);
                    rotators = new MaterialWeatherView.RotateController[] {
                            new DelayRotateController(rotation2D),
                            new DelayRotateController(rotation3D)};
                    break;

                case WeatherView.WEATHER_KIND_SNOW_DAY:
                    implementor = new SnowImplementor(sizes, SnowImplementor.TYPE_SNOW_DAY);
                    rotators = new MaterialWeatherView.RotateController[] {
                            new DelayRotateController(rotation2D),
                            new DelayRotateController(rotation3D)};
                    break;

                case WeatherView.WEATHER_KIND_SNOW_NIGHT:
                    implementor = new SnowImplementor(sizes, SnowImplementor.TYPE_SNOW_NIGHT);
                    rotators = new MaterialWeatherView.RotateController[] {
                            new DelayRotateController(rotation2D),
                            new DelayRotateController(rotation3D)};
                    break;

                case WeatherView.WEATHER_KIND_THUNDERSTORM:
                    implementor = new RainImplementor(sizes, RainImplementor.TYPE_THUNDERSTORM);
                    rotators = new MaterialWeatherView.RotateController[] {
                            new DelayRotateController(rotation2D),
                            new DelayRotateController(rotation3D)};
                    break;

                case WeatherView.WEATHER_KIND_THUNDER:
                    implementor = new CloudImplementor(sizes, CloudImplementor.TYPE_THUNDER);
                    rotators = new MaterialWeatherView.RotateController[] {
                            new DelayRotateController(rotation2D),
                            new DelayRotateController(rotation3D)};
                    break;

                case WeatherView.WEATHER_KIND_WIND:
                    implementor = new WindImplementor(sizes);
                    rotators = new MaterialWeatherView.RotateController[] {
                            new DelayRotateController(rotation2D),
                            new DelayRotateController(rotation3D)};
                    break;

                case WeatherView.WEATHER_KIND_SLEET_DAY:
                    implementor = new RainImplementor(sizes, RainImplementor.TYPE_SLEET_DAY);
                    rotators = new MaterialWeatherView.RotateController[] {
                            new DelayRotateController(rotation2D),
                            new DelayRotateController(rotation3D)};
                    break;

                case WeatherView.WEATHER_KIND_SLEET_NIGHT:
                    implementor = new RainImplementor(sizes, RainImplementor.TYPE_SLEET_NIGHT);
                    rotators = new MaterialWeatherView.RotateController[] {
                            new DelayRotateController(rotation2D),
                            new DelayRotateController(rotation3D)};
                    break;

                case WeatherView.WEATHER_KING_NULL:
                    implementor = null;
                    rotators = null;
                    break;
            }
        }

        private void setOpenGravitySensor(boolean openGravitySensor) {
            this.openGravitySensor = openGravitySensor;
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            this.sizes = new int[] {0, 0};

            this.holder = surfaceHolder;
            holder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {

                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    sizes[0] = width;
                    sizes[1] = height;
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {

                }
            });
            holder.setFormat(PixelFormat.RGBA_8888);

            this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                this.openGravitySensor = true;
                this.gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            }

            this.step = STEP_DISPLAY;
            this.visible = false;
            setWeather(WeatherView.WEATHER_KING_NULL);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (this.visible != visible) {
                this.visible = visible;
                if (visible) {
                    this.rotation2D = 0;
                    this.rotation3D = 0;
                    if (sensorManager != null) {
                        sensorManager.registerListener(gravityListener, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
                    }

                    Location location = DatabaseHelper.getInstance(LiveWallpaperService.this)
                            .readLocationList()
                            .get(0);
                    location.weather = DatabaseHelper.getInstance(LiveWallpaperService.this)
                            .readWeather(location);
                    if (location.weather != null) {
                        setWeather(
                                WeatherViewController.getWeatherViewWeatherKind(
                                        location.weather.realTime.weatherKind,
                                        TimeManager.getInstance(LiveWallpaperService.this).isDayTime()));
                    }
                    setWeatherImplementor();
                    setOpenGravitySensor(
                            PreferenceManager.getDefaultSharedPreferences(LiveWallpaperService.this)
                                    .getBoolean(getString(R.string.key_gravity_sensor_switch), true));

                    if (updateDataRunnable == null || !updateDataRunnable.isRunning()) {
                        updateDataRunnable = new UpdateDataRunnable();
                        new Thread(updateDataRunnable).start();
                    }
                    if (drawableRunnable == null || !drawableRunnable.isRunning()) {
                        drawableRunnable = new DrawableRunnable();
                        new Thread(drawableRunnable).start();
                    }
                } else {
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
        }

        @Override
        public void onDestroy() {
            onVisibilityChanged(false);
        }
    }

    @Override
    public Engine onCreateEngine() {
        return new WeatherEngine();
    }
}
