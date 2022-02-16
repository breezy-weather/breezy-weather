package wangdaye.com.geometricweather.wallpaper;

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
import android.text.TextUtils;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.annotation.Size;

import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.weather.WeatherCode;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.settings.SettingsManager;
import wangdaye.com.geometricweather.theme.weatherView.WeatherView;
import wangdaye.com.geometricweather.theme.weatherView.WeatherViewController;
import wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.DelayRotateController;
import wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.IntervalComputer;
import wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.MaterialWeatherView;
import wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.WeatherImplementorFactory;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.common.utils.helpers.AsyncHelper;

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

        private SurfaceHolder mHolder;
        @Nullable private IntervalComputer mIntervalComputer;

        @Nullable private MaterialWeatherView.WeatherAnimationImplementor mImplementor;
        @Nullable private MaterialWeatherView.RotateController[] mRotators;

        private boolean mOpenGravitySensor;
        @Nullable private SensorManager mSensorManager;
        @Nullable private Sensor mGravitySensor;

        @Size(2) private int[] mSizes;
        private float mRotation2D;
        private float mRotation3D;

        @WeatherView.WeatherKindRule private int mWeatherKind;
        private boolean mDaytime;

        private float mDisplayRate;

        @StepRule
        private int mStep;
        private boolean mVisible;

        private DeviceOrientation mDeviceOrientation;

        @Nullable private AsyncHelper.Controller mIntervalController;
        private HandlerThread mHandlerThread;
        private Handler mHandler;
        private final Runnable mDrawableRunnable = new Runnable() {

            @Override
            public void run() {
                if (mIntervalComputer == null
                        || mImplementor == null
                        || mRotators == null
                        || mHandler == null) {
                    return;
                }

                mIntervalComputer.invalidate();

                mRotators[0].updateRotation(mRotation2D, mIntervalComputer.getInterval());
                mRotators[1].updateRotation(mRotation3D, mIntervalComputer.getInterval());

                mImplementor.updateData(
                        mSizes, (long) mIntervalComputer.getInterval(),
                        (float) mRotators[0].getRotation(), (float) mRotators[1].getRotation()
                );

                mDisplayRate = (float) (mDisplayRate
                        + (mStep == STEP_DISPLAY ? 1f : -1f)
                        * mIntervalComputer.getInterval()
                        / SWITCH_ANIMATION_DURATION);
                mDisplayRate = Math.max(0, mDisplayRate);
                mDisplayRate = Math.min(1, mDisplayRate);
                if (mDisplayRate == 0) {
                    setWeatherImplementor();
                }

                Canvas canvas = mHolder.lockCanvas();
                if (canvas != null) {
                    try {
                        mSizes[0] = canvas.getWidth();
                        mSizes[1] = canvas.getHeight();
                        mImplementor.draw(
                                mSizes,
                                canvas,
                                mDisplayRate,
                                0,
                                (float) mRotators[0].getRotation(),
                                (float) mRotators[1].getRotation()
                        );
                    } catch (Exception ignored) {
                        // do nothing.
                    }
                    mHolder.unlockCanvasAndPost(canvas);
                }
            }
        };

        private final SensorEventListener mGravityListener = new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent ev) {
                // x : (+) fall to the left / (-) fall to the right.
                // y : (+) stand / (-) head stand.
                // z : (+) look down / (-) look up.
                // rotation2D : (+) anticlockwise / (-) clockwise.
                // rotation3D : (+) look down / (-) look up.
                if (mOpenGravitySensor) {
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

        private final OrientationEventListener mOrientationListener = new OrientationEventListener(getApplicationContext()) {
            @Override
            public void onOrientationChanged(int orientation) {
                mDeviceOrientation = getDeviceOrientation(orientation);
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

        private void setWeather(@WeatherView.WeatherKindRule int weatherKind, boolean daytime) {
            mWeatherKind = weatherKind;
            mDaytime = daytime;
        }

        private void setWeatherImplementor() {
            mStep = STEP_DISPLAY;
            mImplementor = WeatherImplementorFactory.getWeatherImplementor(mWeatherKind, mDaytime, mSizes);
            mRotators = new MaterialWeatherView.RotateController[] {
                    new DelayRotateController(mRotation2D),
                    new DelayRotateController(mRotation3D)
            };
        }

        private void setIntervalComputer() {
            if (mIntervalComputer == null) {
                mIntervalComputer = new IntervalComputer();
            } else {
                mIntervalComputer.reset();
            }
        }

        private void setOpenGravitySensor(boolean openGravitySensor) {
            mOpenGravitySensor = openGravitySensor;
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            mDeviceOrientation = DeviceOrientation.TOP;

            mHandlerThread = new HandlerThread(
                    String.valueOf(System.currentTimeMillis()),
                    Process.THREAD_PRIORITY_FOREGROUND
            );
            mHandlerThread.start();
            mHandler = new Handler(mHandlerThread.getLooper());

            mSizes = new int[] {0, 0};

            mHolder = surfaceHolder;
            mHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {

                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    mSizes[0] = width;
                    mSizes[1] = height;
                    setWeatherImplementor();
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {

                }
            });
            mHolder.setFormat(PixelFormat.RGBA_8888);

            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (mSensorManager != null) {
                mOpenGravitySensor = true;
                mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            }

            mStep = STEP_DISPLAY;
            mVisible = false;
            setWeather(WeatherView.WEATHER_KING_NULL, true);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (mVisible != visible) {
                mVisible = visible;
                if (visible) {
                    mRotation2D = 0;
                    mRotation3D = 0;
                    if (mSensorManager != null) {
                        mSensorManager.registerListener(
                                mGravityListener,
                                mGravitySensor,
                                SensorManager.SENSOR_DELAY_FASTEST
                        );
                    }
                    if (mOrientationListener.canDetectOrientation()) {
                        mOrientationListener.enable();
                    }

                    Location location = DatabaseHelper.getInstance(
                            MaterialLiveWallpaperService.this
                    ).readLocationList().get(0);
                    location.setWeather(
                            DatabaseHelper.getInstance(
                                    MaterialLiveWallpaperService.this
                            ).readWeather(location)
                    );

                    LiveWallpaperConfigManager configManager = LiveWallpaperConfigManager.getInstance(
                            MaterialLiveWallpaperService.this
                    );
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
                            daytime = location.isDaylight();
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
                    setIntervalComputer();
                    setOpenGravitySensor(
                            SettingsManager.getInstance(getApplicationContext()).isGravitySensorEnabled());

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
                    mIntervalController = AsyncHelper.intervalRunOnUI(
                            () -> mHandler.post(mDrawableRunnable),
                            (long) (1000.0 / screenRefreshRate),
                            0
                    );
                } else {
                    if (mIntervalController != null) {
                        mIntervalController.cancel();
                        mIntervalController = null;
                    }
                    mHandler.removeCallbacksAndMessages(null);
                    if (mSensorManager != null) {
                        mSensorManager.unregisterListener(mGravityListener, mGravitySensor);
                    }
                    mOrientationListener.disable();
                }
            }
        }

        @Override
        public void onDestroy() {
            onVisibilityChanged(false);
            mHandlerThread.quit();
        }
    }
}
