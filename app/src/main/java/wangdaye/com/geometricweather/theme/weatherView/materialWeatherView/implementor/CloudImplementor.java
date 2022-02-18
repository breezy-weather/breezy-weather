package wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.implementor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.core.content.ContextCompat;

import java.util.Random;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.MaterialWeatherView;

public class CloudImplementor extends MaterialWeatherView.WeatherAnimationImplementor {

    private Paint mPaint;
    private Cloud[] mClouds;
    private Star[] mStars;
    private final Thunder mThunder;
    private final Random mRandom;

    public static final int TYPE_CLOUD_DAY = 1;
    public static final int TYPE_CLOUD_NIGHT = 2;
    public static final int TYPE_CLOUDY_DAY = 3;
    public static final int TYPE_CLOUDY_NIGHT = 4;
    public static final int TYPE_THUNDER = 5;
    public static final int TYPE_FOG = 6;
    public static final int TYPE_HAZE = 7;

    @IntDef({
            TYPE_CLOUD_DAY,
            TYPE_CLOUD_NIGHT,
            TYPE_CLOUDY_DAY,
            TYPE_CLOUDY_NIGHT,
            TYPE_THUNDER,
            TYPE_FOG,
            TYPE_HAZE
    })
    @interface TypeRule {}

    private static class Cloud {

        private final float mInitCX;
        private final float mInitCY;

        float centerX;
        float centerY;

        float radius;
        float initRadius;
        float scaleRatio;
        float moveFactor;

        @ColorInt
        int color;
        float alpha;

        long duration;
        long progress;

        Cloud(float centerX, float centerY,
              float radius, float scaleRatio, float moveFactor,
              @ColorInt int color, float alpha,
              long duration, long initProgress) {

            mInitCX = centerX;
            mInitCY = centerY;

            this.centerX = centerX;
            this.centerY = centerY;

            this.initRadius = radius;
            this.scaleRatio = scaleRatio;
            this.moveFactor = moveFactor;

            this.color = color;
            this.alpha = alpha;

            this.duration = duration;
            this.progress = initProgress % duration;

            computeRadius(duration, progress);
        }

        void move(long interval, float rotation2D, float rotation3D) {
            centerX = (float) (
                    mInitCX + Math.sin(rotation2D * Math.PI / 180.0) * 0.40 * radius * moveFactor
            );
            centerY = (float) (
                    mInitCY - Math.sin(rotation3D * Math.PI / 180.0) * 0.50 * radius * moveFactor
            );
            progress = (progress + interval) % duration;
            computeRadius(duration, progress);
        }

        private void computeRadius(long duration, long progress) {
            if (progress < 0.5 * duration) {
                radius = (float) (
                        initRadius * (1 + (scaleRatio - 1) * progress / 0.5 / duration)
                );
            } else {
                radius = (float) (
                        initRadius * (scaleRatio - (scaleRatio - 1) * (progress - 0.5 * duration) / 0.5 / duration)
                );
            }
        }
    }

    private static class Star {

        float centerX;
        float centerY;
        float radius;

        @ColorInt
        int color;
        float alpha;

        long duration;
        long progress;

        Star(float centerX, float centerY, float radius,
             @ColorInt int color,
             long duration) {
            this.centerX = centerX;
            this.centerY = centerY;

            this.radius = (float) (radius * (0.7 + 0.3 * new Random().nextFloat()));

            this.color = color;

            this.duration = duration;
            this.progress = 0;

            computeAlpha(duration, progress);
        }

        void shine(long interval) {
            progress = (progress + interval) % duration;
            computeAlpha(duration, progress);
        }

        private void computeAlpha(long duration, long progress) {
            if (progress < 0.5 * duration) {
                alpha = (float) (progress / 0.5 / duration);
            } else {
                alpha = (float) (1 - (progress - 0.5 * duration) / 0.5 / duration);
            }
        }
    }

    private static class Thunder {

        int r;
        int g;
        int b;
        float alpha;

        private long progress;
        private long duration;
        private long delay;

        Thunder() {
            this.r = 81;
            this.g = 67;
            this.b = 168;
            init();
            computeFrame();
        }

        private void init() {
            progress = 0;
            duration = 300;
            delay = new Random().nextInt(5000) + 2000;
        }

        private void computeFrame() {
            if (progress < duration) {
                if (progress < 0.25 * duration) {
                    alpha = (float) (progress / 0.25 / duration);
                } else if (progress < 0.5 * duration) {
                    alpha = (float) (1 - (progress - 0.25 * duration) / 0.25 / duration);
                } else if (progress < 0.75 * duration) {
                    alpha = (float) ((progress - 0.5 * duration) / 0.25 / duration);
                } else {
                    alpha = (float) (1 - (progress - 0.75 * duration) / 0.25 / duration);
                }
            } else {
                alpha = 0;
            }
        }

        void shine(long interval) {
            progress += interval;
            if (progress > duration + delay) {
                init();
            }
            computeFrame();
        }
    }

    @SuppressLint("SwitchIntDef")
    public CloudImplementor(@Size(2) int[] canvasSizes, @TypeRule int type) {
        int viewWidth = canvasSizes[0];
        int viewHeight = canvasSizes[1];
        mRandom = new Random();

        if (type == TYPE_FOG || type == TYPE_HAZE) {
            int[] cloudColors = type == TYPE_FOG ? new int[] {
                    Color.rgb(85, 99, 110),
                    Color.rgb(91, 104, 114),
                    Color.rgb(99, 113, 123),
            } : new int[]{
                    Color.rgb(179, 158, 132),
                    Color.rgb(179, 158, 132),
                    Color.rgb(179, 158, 132),
            };

            float[] cloudAlphas = type == TYPE_FOG
                    ? new float[] {0.8F, 0.8F, 0.8F}
                    : new float[] {0.3F, 0.3F, 0.3F};

            Cloud[] clouds = new Cloud[9];
            clouds[0] = new Cloud(
                    viewWidth * 1.0699f,
                    viewWidth * (1.1900f * 0.2286f + 0.11f),
                    viewWidth * (0.4694f * 0.9f),
                    1.1f,
                    getRandomFactor(1.3f, 1.8f),
                    cloudColors[0],
                    cloudAlphas[0],
                    9000,
                    0
            );
            clouds[1] = new Cloud(
                    viewWidth * 0.4866f,
                    viewWidth * (0.4866f * 0.6064f + 0.085f),
                    viewWidth * (0.3946f * 0.9f),
                    1.1f,
                    getRandomFactor(1.3f, 1.8f),
                    cloudColors[0],
                    cloudAlphas[0],
                    10500,
                    0
            );
            clouds[2] = new Cloud(
                    viewWidth * 0.351f,
                    viewWidth * (0.1701f * 1.4327f + 0.11f),
                    viewWidth * (0.4627f * 0.9f),
                    1.1f,
                    getRandomFactor(1.3f, 1.8f),
                    cloudColors[0],
                    cloudAlphas[0],
                    9000,
                    0
            );
            clouds[3] = new Cloud(
                    viewWidth * 0.8831f,
                    viewWidth * (1.0270f * 0.1671f + 0.07f),
                    viewWidth * (0.3238f * 0.9f),
                    1.15f,
                    getRandomFactor(1.6f, 2f),
                    cloudColors[1],
                    cloudAlphas[1],
                    7000,
                    0
            );
            clouds[4] = new Cloud(
                    viewWidth * 0.4663f,
                    viewWidth * (0.4663f * 0.3520f + 0.050f),
                    viewWidth * (0.2906f * 0.9f),
                    1.15f,
                    getRandomFactor(1.6f, 2f),
                    cloudColors[1],
                    cloudAlphas[1],
                    8500,
                    0
            );
            clouds[5] = new Cloud(
                    viewWidth * 0.1229f,
                    viewWidth * (0.0234f * 5.7648f + 0.07f),
                    viewWidth * (0.2972f * 0.9f),
                    1.15f,
                    getRandomFactor(1.6f, 2f),
                    cloudColors[1],
                    cloudAlphas[1],
                    7000,
                    0
            );
            clouds[6] = new Cloud(
                    viewWidth * 0.9250f,
                    viewWidth * (0.9250f * 0.0249f + 0.1500f),
                    viewWidth * 0.3166f,
                    1.15f,
                    getRandomFactor(1.8f, 2.2f),
                    cloudColors[2],
                    cloudAlphas[2],
                    7000,
                    0
            );
            clouds[7] = new Cloud(
                    viewWidth * 0.4694f,
                    viewWidth * (0.4694f * 0.0489f + 0.1500f),
                    viewWidth * 0.3166f,
                    1.15f,
                    getRandomFactor(1.8f, 2.2f),
                    cloudColors[2],
                    cloudAlphas[2],
                    8200,
                    0
            );
            clouds[8] = new Cloud(
                    viewWidth * 0.0250f,
                    viewWidth * (0.0250f * 0.6820f + 0.1500f),
                    viewWidth * 0.3166f,
                    1.15f,
                    getRandomFactor(1.8f, 2.2f),
                    cloudColors[2],
                    cloudAlphas[2],
                    7700,
                    0
            );

            initialize(clouds, null);
        } else if (type == TYPE_CLOUDY_DAY || type == TYPE_CLOUDY_NIGHT || type == TYPE_THUNDER) {
            int[] cloudColors = new int[] {Color.DKGRAY, Color.LTGRAY};
            float[] cloudAlphas = new float[] {0.3f, 0.8f};

            switch (type) {
                case TYPE_CLOUDY_DAY:
                    cloudColors = new int[]{
                            Color.rgb(160, 179, 191),
                            Color.rgb(160, 179, 191),
                    };
                    cloudAlphas = new float[] {0.3f, 0.3f};
                    break;

                case TYPE_CLOUDY_NIGHT:
                    cloudColors = new int[]{
                            Color.rgb(95, 104, 108),
                            Color.rgb(95, 104, 108),
                    };
                    cloudAlphas = new float[] {0.3f, 0.3f};
                    break;

                case TYPE_THUNDER:
                    cloudColors = new int[]{
                            Color.rgb(43, 30, 66),
                            Color.rgb(53, 38, 78)
                    };
                    cloudAlphas = new float[] {0.8f, 0.8f};
                    break;
            }

            Cloud[] clouds = new Cloud[6];
            clouds[0] = new Cloud(
                    viewWidth * 1.0699f,
                    viewWidth * (1.1900f * 0.2286f + 0.11f),
                    viewWidth * (0.4694f * 0.9f),
                    1.1f,
                    getRandomFactor(1.3f, 1.8f),
                    cloudColors[0],
                    cloudAlphas[0],
                    9000,
                    0
            );
            clouds[1] = new Cloud(
                    viewWidth * 0.4866f,
                    viewWidth * (0.4866f * 0.6064f + 0.085f),
                    viewWidth * (0.3946f * 0.9f),
                    1.1f,
                    getRandomFactor(1.3f, 1.8f),
                    cloudColors[0],
                    cloudAlphas[0],
                    10500,
                    0
            );
            clouds[2] = new Cloud(
                    viewWidth * 0.351f,
                    viewWidth * (0.1701f * 1.4327f + 0.11f),
                    viewWidth * (0.4627f * 0.9f),
                    1.1f,
                    getRandomFactor(1.3f, 1.8f),
                    cloudColors[0],
                    cloudAlphas[0],
                    9000,
                    0
            );
            clouds[3] = new Cloud(
                    viewWidth * 0.8831f,
                    viewWidth * (1.0270f * 0.1671f + 0.07f),
                    viewWidth * (0.3238f * 0.9f),
                    1.15f,
                    getRandomFactor(1.6f, 2f),
                    cloudColors[1],
                    cloudAlphas[1],
                    7000,
                    0
            );
            clouds[4] = new Cloud(
                    viewWidth * 0.4663f,
                    viewWidth * (0.4663f * 0.3520f + 0.050f),
                    viewWidth * (0.2906f * 0.9f),
                    1.15f,
                    getRandomFactor(1.6f, 2f),
                    cloudColors[1],
                    cloudAlphas[1],
                    8500,
                    0
            );
            clouds[5] = new Cloud(
                    viewWidth * 0.1229f,
                    viewWidth * (0.0234f * 5.7648f + 0.07f),
                    viewWidth * (0.2972f * 0.9f),
                    1.15f,
                    getRandomFactor(1.6f, 2f),
                    cloudColors[1],
                    cloudAlphas[1],
                    7000,
                    0
            );

            initialize(clouds, null);
        } else {
            int cloudColor;
            float[] cloudAlphas;
            if (type == TYPE_CLOUD_DAY) {
                cloudColor = Color.rgb(203, 245, 255);
            } else {
                cloudColor = Color.rgb(151, 168, 202);
            }
            cloudAlphas = new float[] {0.40F, 0.10F};

            Cloud[] clouds = new Cloud[6];
            clouds[0] = new Cloud(
                    (float) (viewWidth * 0.1529),
                    (float) (viewWidth * 0.1529 * 0.5568 + viewWidth * 0.050),
                    (float) (viewWidth * 0.2649),
                    1.20F,
                    getRandomFactor(1.5f, 1.8f),
                    cloudColor,
                    cloudAlphas[0],
                    7000,
                    0
            );
            clouds[1] = new Cloud(
                    (float) (viewWidth * 0.4793),
                    (float) (viewWidth * 0.4793 * 0.2185 + viewWidth * 0.050),
                    (float) (viewWidth * 0.2426),
                    1.20F,
                    getRandomFactor(1.5f, 1.8f),
                    cloudColor,
                    cloudAlphas[0],
                    8500,
                    0
            );
            clouds[2] = new Cloud(
                    (float) (viewWidth * 0.8531),
                    (float) (viewWidth * 0.8531 * 0.1286 + viewWidth * 0.050),
                    (float) (viewWidth * 0.2970),
                    1.20F,
                    getRandomFactor(1.5f, 1.8f),
                    cloudColor,
                    cloudAlphas[0],
                    7050,
                    0
            );
            clouds[3] = new Cloud(
                    (float) (viewWidth * 0.0551),
                    (float) (viewWidth * 0.0551 * 2.8600 + viewWidth * 0.050),
                    (float) (viewWidth * 0.4125),
                    1.15F,
                    getRandomFactor(1.3f, 1.5f),
                    cloudColor,
                    cloudAlphas[1],
                    9500,
                    0
            );
            clouds[4] = new Cloud(
                    (float) (viewWidth * 0.4928),
                    (float) (viewWidth * 0.4928 * 0.3897 + viewWidth * 0.050),
                    (float) (viewWidth * 0.3521),
                    1.15F,
                    getRandomFactor(1.3f, 1.5f),
                    cloudColor,
                    cloudAlphas[1],
                    10500,
                    0
            );
            clouds[5] = new Cloud(
                    (float) (viewWidth * 1.0499),
                    (float) (viewWidth * 1.0499 * 0.1875 + viewWidth * 0.050),
                    (float) (viewWidth * 0.4186),
                    1.15F,
                    getRandomFactor(1.3f, 1.5f),
                    cloudColor,
                    cloudAlphas[1],
                    9000,
                    0
            );

            if (type == TYPE_CLOUD_DAY) {
                initialize(clouds, null);
            } else {
                int[] colors =  new int[] {
                        Color.rgb(210, 247, 255),
                        Color.rgb(208, 233, 255),
                        Color.rgb(175, 201, 228),
                        Color.rgb(164, 194, 220),
                        Color.rgb(97, 171, 220),
                        Color.rgb(74, 141, 193),
                        Color.rgb(54, 66, 119),
                        Color.rgb(34, 48, 74),
                        Color.rgb(236, 234, 213),
                        Color.rgb(240, 220, 151),
                };

                Star[] stars = new Star[50];
                Random r = new Random();
                int canvasSize = (int) Math.sqrt(Math.pow(viewWidth, 2) + Math.pow(viewHeight, 2));
                int width = (int) (1.0 * canvasSize);
                int height = (int) ((canvasSize - viewHeight) * 0.5 + viewWidth * 1.1111);
                float radius = (float) (0.00125 * canvasSize * (0.5 + r.nextFloat()));
                for (int i = 0; i < stars.length; i ++) {
                    int x = (int) (r.nextInt(width) - 0.5 * (canvasSize - viewWidth));
                    int y = (int) (r.nextInt(height) - 0.5 * (canvasSize - viewHeight));

                    long duration = (long) (2500 + r.nextFloat() * 2500);
                    stars[i] = new Star(
                            x,
                            y,
                            radius,
                            colors[i % colors.length],
                            duration
                    );
                }
                initialize(clouds, stars);
            }
        }

        if (type == TYPE_THUNDER) {
            mThunder = new Thunder();
        } else {
            mThunder = null;
        }
    }

    private void initialize(Cloud[] clouds, @Nullable Star[] stars) {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        mClouds = clouds;

        if (stars == null) {
            stars = new Star[0];
        }
        mStars = stars;
    }

    private float getRandomFactor(float from, float to) {
        return from + mRandom.nextFloat() % (to - from);
    }

    @Override
    public void updateData(@Size(2) int[] canvasSizes, long interval,
                           float rotation2D, float rotation3D) {
        for (Cloud c : mClouds) {
            c.move(interval, rotation2D, rotation3D);
        }
        for (Star s : mStars) {
            s.shine(interval);
        }
        if (mThunder != null) {
            mThunder.shine(interval);
        }
    }

    @Override
    public void draw(@Size(2) int[] canvasSizes, Canvas canvas,
                     float scrollRate, float rotation2D, float rotation3D) {
        if (scrollRate < 1) {
            if (mThunder != null) {
                canvas.drawColor(
                        Color.argb(
                                (int) ((1 - scrollRate) * mThunder.alpha * 255 * 0.66),
                                mThunder.r,
                                mThunder.g,
                                mThunder.b));
            }
            for (Star s : mStars) {
                mPaint.setColor(s.color);
                mPaint.setAlpha((int) ((1 - scrollRate) * s.alpha * 255));
                canvas.drawCircle(s.centerX, s.centerY, s.radius, mPaint);
            }
            for (Cloud c : mClouds) {
                mPaint.setColor(c.color);
                mPaint.setAlpha((int) ((1 - scrollRate) * c.alpha * 255));
                canvas.drawCircle(c.centerX, c.centerY, c.radius, mPaint);
            }
        }
    }

    @ColorInt
    public static int getThemeColor(Context context, @TypeRule int type) {
        switch (type) {
            case TYPE_CLOUDY_DAY:
                return Color.rgb(96, 121, 136);

            case TYPE_CLOUDY_NIGHT:
                return Color.rgb(38, 50, 56);

            case TYPE_CLOUD_DAY:
                return Color.rgb(0, 165, 217);

            case TYPE_CLOUD_NIGHT:
                return Color.rgb(34, 45, 67);

            case TYPE_THUNDER:
                return Color.rgb(43, 29, 69);

            case TYPE_FOG:
                return Color.rgb(79, 93, 104);

            case TYPE_HAZE:
                return Color.rgb(66, 66, 66);
        }
        return ContextCompat.getColor(context, R.color.colorPrimary);
    }
}
