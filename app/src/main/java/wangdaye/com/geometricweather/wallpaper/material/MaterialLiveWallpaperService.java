package wangdaye.com.geometricweather.wallpaper.material;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.service.wallpaper.WallpaperService;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.annotation.Size;

import android.text.TextUtils;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.weather.WeatherCode;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherViewController;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.DelayRotateController;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.IntervalComputer;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.MaterialWeatherView;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.WeatherImplementorFactory;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.wallpaper.LiveWallpaperConfigManager;

public class MaterialLiveWallpaperService extends WallpaperService {

    private static final int STEP_DISPLAY = 1;
    private static final int STEP_DISMISS = -1;

    @IntDef({STEP_DISPLAY, STEP_DISMISS})
    private @interface StepRule {}

    private enum DeviceOrientation {
        TOP, LEFT, BOTTOM, RIGHT
    }

    private static final int SWITCH_ANIMATION_DURATION = 150;

    @Override
    public Engine onCreateEngine() {
        return new WeatherEngine();
    }

    private class WeatherEngine extends Engine {

        private SurfaceHolder holder;
        @Nullable private IntervalComputer intervalComputer;

        @Nullable private MaterialWeatherView.WeatherAnimationImplementor implementor;
        @Nullable private MaterialWeatherView.RotateController[] rotators;

        private boolean openGravitySensor;
        @Nullable private SensorManager sensorManager;
        @Nullable private Sensor gravitySensor;

        @Size(2) private int[] sizes;
        private float rotation2D;
        private float rotation3D;

        @WeatherView.WeatherKindRule private int weatherKind;
        private boolean daytime;

        private float displayRate;

        @StepRule
        private int step;
        private boolean visible;

        private DeviceOrientation deviceOrientation;

        @Nullable private Disposable disposable;
        private HandlerThread handlerThread;
        private Handler handler;
        private Runnable drawableRunnable = new Runnable() {

            private Canvas canvas;

            @Override
            public void run() {
                if (intervalComputer == null
                        || implementor == null
                        || rotators == null
                        || handler == null) {
                    return;
                }

                intervalComputer.invalidate();

                rotators[0].updateRotation(rotation2D, intervalComputer.getInterval());
                rotators[1].updateRotation(rotation3D, intervalComputer.getInterval());

                implementor.updateData(
                        sizes, (long) intervalComputer.getInterval(),
                        (float) rotators[0].getRotation(), (float) rotators[1].getRotation()
                );

                displayRate = (float) (displayRate
                        + (step == STEP_DISPLAY ? 1f : -1f)
                        * intervalComputer.getInterval()
                        / SWITCH_ANIMATION_DURATION);
                displayRate = Math.max(0, displayRate);
                displayRate = Math.min(1, displayRate);
                if (displayRate == 0) {
                    setWeatherImplementor();
                }

                try {
                    canvas = holder.lockCanvas();
                    if (canvas != null) {
                        sizes[0] = canvas.getWidth();
                        sizes[1] = canvas.getHeight();
                        implementor.draw(
                                sizes, canvas,
                                displayRate, 0,
                                (float) rotators[0].getRotation(), (float) rotators[1].getRotation()
                        );
                        holder.unlockCanvasAndPost(canvas);
                    }
                } catch (Exception ignored) {
                    // do nothing.
                }
            }
        };

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

        private OrientationEventListener orientationListener = new OrientationEventListener(getApplicationContext()) {
            @Override
            public void onOrientationChanged(int orientation) {
                deviceOrientation = getDeviceOrientation(orientation);
            }

            private DeviceOrientation getDeviceOrientation(int orientation) {
                if (DisplayUtils.isLandscape(getApplicationContext())) {
                    return (0 < orientation && orientation < 180)
                            ? DeviceOrientation.RIGHT : DeviceOrientation.LEFT;
                } else {
                    return (270 < orientation || orientation < 90)
                            ? DeviceOrientation.TOP : DeviceOrientation.BOTTOM;
                }
            }
        };

        WeatherEngine() {
            super();

            deviceOrientation = DeviceOrientation.TOP;

            handlerThread = new HandlerThread(
                    String.valueOf(System.currentTimeMillis()),
                    Process.THREAD_PRIORITY_FOREGROUND
            );
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
        }

        private void setWeather(@WeatherView.WeatherKindRule int weatherKind, boolean daytime) {
            this.weatherKind = weatherKind;
            this.daytime = daytime;
        }

        private void setWeatherImplementor() {
            step = STEP_DISPLAY;
            implementor = WeatherImplementorFactory.getWeatherImplementor(weatherKind, daytime, sizes);
            rotators = new MaterialWeatherView.RotateController[] {
                    new DelayRotateController(rotation2D),
                    new DelayRotateController(rotation3D)
            };
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
                    setWeatherImplementor();
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
            setWeather(WeatherView.WEATHER_KING_NULL, true);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (this.visible != visible) {
                this.visible = visible;
                if (visible) {
                    this.rotation2D = 0;
                    this.rotation3D = 0;
                    if (sensorManager != null) {
                        sensorManager.registerListener(
                                gravityListener, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
                    }
                    if (orientationListener.canDetectOrientation()) {
                        orientationListener.enable();
                    }

                    Location location = DatabaseHelper.getInstance(MaterialLiveWallpaperService.this)
                            .readLocationList()
                            .get(0);
                    location.setWeather(
                            DatabaseHelper.getInstance(MaterialLiveWallpaperService.this)
                                    .readWeather(location)
                    );

                    LiveWallpaperConfigManager configManager
                            = LiveWallpaperConfigManager.getInstance(MaterialLiveWallpaperService.this);
                    String weatherKind = configManager.getWeatherKind();
                    if (weatherKind.equals("auto")) {
                        weatherKind = location.getWeather() != null
                                ? location.getWeather().getCurrent().getWeatherCode().name()
                                : null;
                    }
                    String dayNightType = configManager.getDayNightType();
                    boolean daytime = true;
                    switch (dayNightType) {
                        case "auto":
                            daytime = TimeManager.isDaylight(location);
                            break;

                        case "day":
                            daytime = true;
                            break;

                        case "night":
                            daytime = false;
                            break;
                    }

                    if (!TextUtils.isEmpty(weatherKind)) {
                        setWeather(
                                WeatherViewController.getWeatherKind(
                                        WeatherCode.valueOf(weatherKind)
                                ),
                                daytime
                        );
                    }
                    setWeatherImplementor();
                    setOpenGravitySensor(
                            SettingsOptionManager.getInstance(MaterialLiveWallpaperService.this)
                                    .isGravitySensorEnabled()
                    );

                    if (intervalComputer == null) {
                        intervalComputer = new IntervalComputer();
                    } else {
                        intervalComputer.reset();
                    }

                    float screenRefreshRate;
                    WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                    if (windowManager != null) {
                        screenRefreshRate = windowManager.getDefaultDisplay().getRefreshRate();
                    } else {
                        screenRefreshRate = 60;
                    }
                    if (screenRefreshRate < 60) {
                        screenRefreshRate = 60;
                    }
                    disposable = Observable.interval(
                            0,
                            (long) (1000.0 / screenRefreshRate),
                            TimeUnit.MILLISECONDS
                    ).subscribe(aLong -> handler.post(drawableRunnable));
                } else {
                    if (disposable != null) {
                        disposable.dispose();
                        disposable = null;
                    }
                    handler.removeCallbacksAndMessages(null);
                    if (sensorManager != null) {
                        sensorManager.unregisterListener(gravityListener, gravitySensor);
                    }
                    orientationListener.disable();
                }
            }
        }

        @Override
        public void onDestroy() {
            onVisibilityChanged(false);
        }
    }
}
