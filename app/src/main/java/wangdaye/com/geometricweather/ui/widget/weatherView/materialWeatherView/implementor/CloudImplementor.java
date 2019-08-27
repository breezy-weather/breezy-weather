package wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor;

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
import androidx.core.graphics.ColorUtils;

import java.util.Random;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.MaterialWeatherView;

/**
 * Cloud implementor.
 * */

public class CloudImplementor extends MaterialWeatherView.WeatherAnimationImplementor {

    private Paint paint;
    private Cloud[] clouds;
    private Star[] stars;
    private Thunder thunder;
    private Random random;

    @ColorInt
    private int backgroundColor;

    public static final int TYPE_CLOUD_DAY = 1;
    public static final int TYPE_CLOUD_NIGHT = 2;
    public static final int TYPE_CLOUDY_DAY = 3;
    public static final int TYPE_CLOUDY_NIGHT = 4;
    public static final int TYPE_THUNDER = 5;
    public static final int TYPE_FOG = 6;
    public static final int TYPE_HAZE = 7;

    @IntDef({TYPE_CLOUD_DAY, TYPE_CLOUD_NIGHT, TYPE_CLOUDY_DAY, TYPE_CLOUDY_NIGHT, TYPE_THUNDER, TYPE_FOG, TYPE_HAZE})
    @interface TypeRule {}

    private class Cloud {

        private float initCX;
        private float initCY;

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

            this.initCX = centerX;
            this.initCY = centerY;

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
            centerX = (float) (initCX + Math.sin(rotation2D * Math.PI / 180.0) * 0.40 * radius * moveFactor);
            centerY = (float) (initCY - Math.sin(rotation3D * Math.PI / 180.0) * 0.50 * radius * moveFactor);
            progress = (progress + interval) % duration;
            computeRadius(duration, progress);
        }

        private void computeRadius(long duration, long progress) {
            if (progress < 0.5 * duration) {
                radius = (float) (initRadius * (1 + (scaleRatio - 1) * progress / 0.5 / duration));
            } else {
                radius = (float) (initRadius * (scaleRatio - (scaleRatio - 1) * (progress - 0.5 * duration) / 0.5 / duration));
            }
        }
    }

    private class Star {

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
             long duration, long initProgress) {
            this.centerX = centerX;
            this.centerY = centerY;

            this.radius = (float) (radius * (0.7 + 0.3 * new Random().nextFloat()));

            this.color = color;

            this.duration = duration;
            this.progress = initProgress % duration;

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

    private class Thunder {

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
            this. b = 108;
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
        this.random = new Random();

        if (type == TYPE_FOG || type == TYPE_HAZE) {
            int backgroundColor = type == TYPE_FOG ?
                    Color.rgb(79, 93, 104) : Color.rgb(66, 66, 66);
            int[] cloudColors = type == TYPE_FOG ?
                    new int[]{
                            Color.rgb(85, 99, 110),
                            Color.rgb(91, 104, 114),
                            Color.rgb(99, 113, 123),}
                    : new int[]{
                    Color.rgb(57, 57, 57),
                    Color.rgb(48, 48, 48),
                    Color.rgb(44, 44, 44)};

            float[] cloudAlphas = type == TYPE_FOG ?
                    new float[] {0.8F, 0.8F, 0.8F} : new float[] {0.8F, 0.8F, 0.8F};

            Cloud[] clouds = new Cloud[9];
            clouds[8] = new Cloud(
                    (float) (viewWidth * 0.0250),
                    (float) (viewWidth * 0.0250 * 0.6820 + viewWidth * 0.1500),
                    (float) (viewWidth * 0.3166), 1.15F, getRandomFactor(1.8f, 2.6f),
                    cloudColors[2], cloudAlphas[2],
                    7000, 700);
            clouds[7] = new Cloud(
                    (float) (viewWidth * 0.4694),
                    (float) (viewWidth * 0.4694 * 0.0489 + viewWidth * 0.1500),
                    (float) (viewWidth * 0.3166), 1.15F, getRandomFactor(1.8f, 2.6f),
                    cloudColors[2], cloudAlphas[2],
                    7000, 1200);
            clouds[6] = new Cloud(
                    (float) (viewWidth * 0.9250),
                    (float) (viewWidth * 0.9250 * 0.0249 + viewWidth * 0.1500),
                    (float) (viewWidth * 0.3166), 1.15F, getRandomFactor(1.8f, 2.6f),
                    cloudColors[2], cloudAlphas[2],
                    7000, 0);
            clouds[5] = new Cloud(
                    (float) (viewWidth * 0.1000),
                    (float) (viewWidth * 0.1000 * 3.0462 + viewWidth * 0.1500),
                    (float) (viewWidth * 0.3166), 1.15F, getRandomFactor(1.4f, 2.2f),
                    cloudColors[1], cloudAlphas[1],
                    7000, 300);
            clouds[4] = new Cloud(
                    (float) (viewWidth * 0.5444),
                    (float) (viewWidth * 0.5444 * 0.4880 + viewWidth * 0.1500),
                    (float) (viewWidth * 0.3166), 1.15F, getRandomFactor(1.4f, 2.2f),
                    cloudColors[1], cloudAlphas[1],
                    7000, 1500);
            clouds[3] = new Cloud(
                    (float) (viewWidth * 1.0000),
                    (float) (viewWidth * 1.0000 * 0.3046 + viewWidth * 0.1500),
                    (float) (viewWidth * 0.3166), 1.15F, getRandomFactor(1.4f, 2.2f),
                    cloudColors[1], cloudAlphas[1],
                    7000, 0);
            clouds[2] = new Cloud(
                    (float) (viewWidth * 0.0388),
                    (float) (viewWidth * 0.0388 * 14.3333 + viewWidth * 0.1500),
                    (float) (viewWidth * 0.3166), 1.15F, getRandomFactor(1f, 1.8f),
                    cloudColors[0], cloudAlphas[0],
                    7000, 1700);
            clouds[1] = new Cloud(
                    (float) (viewWidth * 0.4833),
                    (float) (viewWidth * 0.4833 * 1.0727 + viewWidth * 0.1500),
                    (float) (viewWidth * 0.3166), 1.15F, getRandomFactor(1f, 1.8f),
                    cloudColors[0], cloudAlphas[0],
                    7000, 3500);
            clouds[0] = new Cloud(
                    (float) (viewWidth * 0.9388),
                    (float) (viewWidth * 0.9388 * 0.6101 + viewWidth * 0.1500),
                    (float) (viewWidth * 0.3166), 1.15F, getRandomFactor(1f, 1.8f),
                    cloudColors[0], cloudAlphas[0],
                    7000, 2000);

            this.initialize(clouds, null, backgroundColor);
        } else if (type == TYPE_CLOUDY_DAY || type == TYPE_CLOUDY_NIGHT || type == TYPE_THUNDER) {
            int backgroundColor = Color.BLACK;
            int[] cloudColors = new int[] {Color.DKGRAY, Color.LTGRAY};
            float[] cloudAlphas = new float[] {0.5F, 0.5F};

            switch (type) {
                case TYPE_CLOUDY_DAY:
                    backgroundColor = Color.rgb(96, 121, 136);
                    cloudColors = new int[]{
                            Color.rgb(107, 129, 143),
                            Color.rgb(117, 135, 147)
                    };
                    cloudAlphas = new float[] {0.7F, 0.7F};
                    break;

                case TYPE_CLOUDY_NIGHT:
                    backgroundColor = Color.rgb(38, 50, 56);
                    cloudColors = new int[]{
                            Color.rgb(16, 32, 39),
                            Color.rgb(16, 32, 39)
                    };
                    cloudAlphas = new float[] {0.3F, 0.3F};
                    break;

                case TYPE_THUNDER:
                    backgroundColor = Color.rgb(35, 23, 57);
                    cloudColors = new int[]{
                            Color.rgb(43, 30, 66),
                            Color.rgb(53, 38, 78)
                    };
                    cloudAlphas = new float[] {0.8F, 0.8F};
                    break;
            }

            Cloud[] clouds = new Cloud[6];
            clouds[5] = new Cloud(
                    (float) (viewWidth * 0.1229),
                    (float) (viewWidth * 0.0234 * 5.7648 + viewWidth * 0.07),
                    (float) (viewWidth * 0.2972 * 0.9/*0.3975*/), 1.15F, getRandomFactor(1.6f, 2f),
                    cloudColors[1], cloudAlphas[1],
                    7000, 0);
            clouds[4] = new Cloud(
                    (float) (viewWidth * 0.4663),
                    (float) (viewWidth * 0.4663 * 0.3520 + viewWidth * 0.05),
                    (float) (viewWidth * 0.2906 * 0.9/*0.3886*/), 1.15F, getRandomFactor(1.6f, 2f),
                    cloudColors[1], cloudAlphas[1],
                    7000, 1500);
            clouds[3] = new Cloud(
                    (float) (viewWidth * 0.8831),
                    (float) (viewWidth * 1.0270 * 0.1671 + viewWidth * 0.07),
                    (float) (viewWidth * 0.3238 * 0.9/*0.4330*/), 1.15F, getRandomFactor(1.6f, 2f),
                    cloudColors[1], cloudAlphas[1],
                    7000, 0);
            clouds[2] = new Cloud(
                    (float) (viewWidth * 0.0351),
                    (float) (viewWidth * 0.1701 * 1.4327 + viewWidth * 0.11),
                    (float) (viewWidth * 0.4627 * 0.9/*0.6188*/), 1.10F, getRandomFactor(1.3f, 1.8f),
                    cloudColors[0], cloudAlphas[0],
                    7000, 2000);
            clouds[1] = new Cloud(
                    (float) (viewWidth * 0.4866),
                    (float) (viewWidth * 0.4866 * 0.6064 + viewWidth * 0.085),
                    (float) (viewWidth * 0.3946 * 0.9/*0.5277*/), 1.10F, getRandomFactor(1.3f, 1.8f),
                    cloudColors[0], cloudAlphas[0],
                    7000, 3500);
            clouds[0] = new Cloud(
                    (float) (viewWidth * 1.0699),
                    (float) (viewWidth * 1.1900 * 0.2286 + viewWidth * 0.11),
                    (float) (viewWidth * 0.4694 * 0.9/*0.6277*/), 1.10F, getRandomFactor(1.3f, 1.8f),
                    cloudColors[0], cloudAlphas[0],
                    7000, 2000);

            this.initialize(clouds, null, backgroundColor);
        } else {
            int cloudColor;
            float[] cloudAlphas;
            if (type == TYPE_CLOUD_DAY) {
                cloudColor = Color.rgb(203, 245, 255);
                cloudAlphas = new float[] {0.40F, 0.10F};
            } else {
                cloudColor = Color.rgb(151, 168, 202);
                cloudAlphas = new float[] {0.40F, 0.10F};
            }

            Cloud[] clouds = new Cloud[6];
            clouds[0] = new Cloud(
                    (float) (viewWidth * 0.1529),
                    (float) (viewWidth * 0.1529 * 0.5568 + viewWidth * 0.050),
                    (float) (viewWidth * 0.2649), 1.20F, getRandomFactor(1.5f, 1.8f),
                    cloudColor, cloudAlphas[0],
                    7000, 0);
            clouds[1] = new Cloud(
                    (float) (viewWidth * 0.4793),
                    (float) (viewWidth * 0.4793 * 0.2185 + viewWidth * 0.050),
                    (float) (viewWidth * 0.2426), 1.20F, getRandomFactor(1.5f, 1.8f),
                    cloudColor, cloudAlphas[0],
                    7000, 1500);
            clouds[2] = new Cloud(
                    (float) (viewWidth * 0.8531),
                    (float) (viewWidth * 0.8531 * 0.1286 + viewWidth * 0.050),
                    (float) (viewWidth * 0.2970), 1.20F, getRandomFactor(1.5f, 1.8f),
                    cloudColor, cloudAlphas[0],
                    7000, 0);
            clouds[3] = new Cloud(
                    (float) (viewWidth * 0.0551),
                    (float) (viewWidth * 0.0551 * 2.8600 + viewWidth * 0.050),
                    (float) (viewWidth * 0.4125), 1.15F, getRandomFactor(1.3f, 1.5f),
                    cloudColor, cloudAlphas[1],
                    7000, 2000);
            clouds[4] = new Cloud(
                    (float) (viewWidth * 0.4928),
                    (float) (viewWidth * 0.4928 * 0.3897 + viewWidth * 0.050),
                    (float) (viewWidth * 0.3521), 1.15F, getRandomFactor(1.3f, 1.5f),
                    cloudColor, cloudAlphas[1],
                    7000, 3500);
            clouds[5] = new Cloud(
                    (float) (viewWidth * 1.0499),
                    (float) (viewWidth * 1.0499 * 0.1875 + viewWidth * 0.050),
                    (float) (viewWidth * 0.4186), 1.15F, getRandomFactor(1.3f, 1.5f),
                    cloudColor, cloudAlphas[1],
                    7000, 2000);

            if (type == TYPE_CLOUD_DAY) {
                this.initialize(clouds, null, Color.rgb(0, 165, 217));
            } else {
                Star[] stars = new Star[30];
                Random r = new Random();
                int canvasSize = (int) Math.sqrt(Math.pow(viewWidth, 2) + Math.pow(viewHeight, 2));
                int width = (int) (1.0 * canvasSize);
                int height = (int) ((canvasSize - viewHeight) * 0.5 + viewWidth * 1.1111);
                float radius = (float) (0.0028 * viewWidth);
                int color = Color.rgb(255, 255, 255);
                for (int i = 0; i < stars.length; i ++) {
                    int x = (int) (r.nextInt(width) - 0.5 * (canvasSize - viewWidth));
                    int y = (int) (r.nextInt(height) - 0.5 * (canvasSize - viewHeight));
                    boolean newPosition = true;
                    for (int j = 0; j < i; j ++) {
                        if (stars[j].centerX == x && stars[j].centerY == y) {
                            newPosition = false;
                            break;
                        }
                    }
                    if (newPosition) {
                        long duration = 1500 + r.nextInt(3) * 500;
                        stars[i] = new Star(x, y, radius, color, duration, r.nextInt());
                    } else {
                        i --;
                    }
                }
                this.initialize(clouds, stars, Color.rgb(34, 45, 67));
            }
        }

        if (type == TYPE_THUNDER) {
            thunder = new Thunder();
        } else {
            thunder = null;
        }
    }

    private void initialize(Cloud[] clouds, @Nullable Star[] stars, @ColorInt int backgroundColor) {
        this.paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        this.clouds = clouds;

        if (stars == null) {
            stars = new Star[0];
        }
        this.stars = stars;

        this.backgroundColor = backgroundColor;
    }

    private float getRandomFactor(float from, float to) {
        return from + random.nextFloat() % (to - from);
    }

    @Override
    public void updateData(@Size(2) int[] canvasSizes, long interval,
                           float rotation2D, float rotation3D) {
        for (Cloud c : clouds) {
            c.move(interval, rotation2D, rotation3D);
        }
        for (Star s : stars) {
            s.shine(interval);
        }
        if (thunder != null) {
            thunder.shine(interval);
        }
    }

    @Override
    public void draw(@Size(2) int[] canvasSizes, Canvas canvas,
                     float displayRate, float scrollRate, float rotation2D, float rotation3D) {
        if (displayRate >=1) {
            canvas.drawColor(backgroundColor);
        } else {
            canvas.drawColor(
                    ColorUtils.setAlphaComponent(
                            backgroundColor,
                            (int) (displayRate * 255)));
        }

        if (scrollRate < 1) {
            if (thunder != null) {
                canvas.drawColor(
                        Color.argb(
                                (int) (displayRate * (1 - scrollRate) * thunder.alpha * 255),
                                thunder.r,
                                thunder.g,
                                thunder.b));
            }
            for (Star s : stars) {
                paint.setColor(s.color);
                paint.setAlpha((int) (displayRate * (1 - scrollRate) * s.alpha * 255));
                canvas.drawCircle(s.centerX, s.centerY, s.radius, paint);
            }
            for (Cloud c : clouds) {
                paint.setColor(c.color);
                paint.setAlpha((int) (displayRate * (1 - scrollRate) * c.alpha * 255));
                canvas.drawCircle(c.centerX, c.centerY, c.radius, paint);
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
