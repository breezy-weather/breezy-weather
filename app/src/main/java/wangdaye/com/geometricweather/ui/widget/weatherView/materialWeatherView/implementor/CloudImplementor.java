package wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.MaterialWeatherView;

import java.util.Random;

/**
 * Cloud implementor.
 * */

public class CloudImplementor extends MaterialWeatherView.WeatherAnimationImplementor {

    private Paint paint;
    private Cloud[] clouds;
    private Star[] stars;
    private Thunder thunder;

    @ColorInt
    private int backgroundColor;

    public static final int TYPE_CLOUD_DAY = 1;
    public static final int TYPE_CLOUD_NIGHT = 2;
    public static final int TYPE_CLOUDY = 3;
    public static final int TYPE_THUNDER = 4;

    @IntDef({TYPE_CLOUD_DAY, TYPE_CLOUD_NIGHT, TYPE_CLOUDY, TYPE_THUNDER})
    @interface TypeRule {}

    private class Cloud {

        private float initCX;
        private float initCY;

        float centerX;
        float centerY;

        float radius;
        float initRadius;
        float scaleRatio;

        @ColorInt
        int color;
        float alpha;

        long duration;
        long progress;

        Cloud(float centerX, float centerY,
              float radius, float scaleRatio,
              @ColorInt int color, float alpha,
              long duration, long initProgress) {

            this.initCX = centerX;
            this.initCY = centerY;

            this.centerX = centerX;
            this.centerY = centerY;

            this.initRadius = radius;
            this.scaleRatio = scaleRatio;

            this.color = color;
            this.alpha = alpha;

            this.duration = duration;
            this.progress = initProgress % duration;

            computeRadius(duration, progress);
        }

        void move(long interval, float rotation2D, float rotation3D) {
            centerX = (float) (initCX + Math.sin(rotation2D * Math.PI / 180.0) * 0.40 * radius);
            centerY = (float) (initCY - Math.sin(rotation3D * Math.PI / 180.0) * 0.50 * radius);
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
            this.r = this.g = this. b = 255;
            init();
            computeFrame();
        }

        private void init() {
            progress = 0;
            duration = 300;
            delay = new Random().nextInt(2000) + 1000;
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

    public CloudImplementor(MaterialWeatherView view, @TypeRule int type) {
        int viewWidth = view.getMeasuredWidth();
        int viewHeight = view.getMeasuredHeight();

        if (type == TYPE_CLOUDY || type == TYPE_THUNDER) {
            int backgroundColor = type == TYPE_CLOUDY ?
                    Color.rgb(96, 121, 136) : Color.rgb(43, 29, 69);
            int cloudColor = type == TYPE_CLOUDY ?
                    Color.rgb(171, 171, 171) : Color.rgb(0, 0, 0);
            float[] cloudAlphas = type == TYPE_CLOUDY ?
                    new float[] {0.20F, 0.10F} : new float[] {0.10F, 0.10F};

            Cloud[] clouds = new Cloud[6];
            clouds[0] = new Cloud(
                    (float) (viewWidth * -0.0234),
                    (float) (viewWidth * 0.0234 * 5.7648 + viewWidth * 0.050),
                    (float) (viewWidth * 0.3975), 1.15F,
                    cloudColor, cloudAlphas[0],
                    7000, 0);
            clouds[1] = new Cloud(
                    (float) (viewWidth * 0.4663),
                    (float) (viewWidth * 0.4663 * 0.3520 + viewWidth * 0.050),
                    (float) (viewWidth * 0.3886), 1.15F,
                    cloudColor, cloudAlphas[0],
                    7000, 1500);
            clouds[2] = new Cloud(
                    (float) (viewWidth * 1.0270),
                    (float) (viewWidth * 1.0270 * 0.1671 + viewWidth * 0.050),
                    (float) (viewWidth * 0.4330), 1.15F,
                    cloudColor, cloudAlphas[0],
                    7000, 0);
            clouds[3] = new Cloud(
                    (float) (viewWidth * -0.1701),
                    (float) (viewWidth * 0.1701 * 1.4327 + viewWidth * 0.050),
                    (float) (viewWidth * 0.6188), 1.10F,
                    cloudColor, cloudAlphas[1],
                    7000, 2000);
            clouds[4] = new Cloud(
                    (float) (viewWidth * 0.4866),
                    (float) (viewWidth * 0.4866 * 0.6064 + viewWidth * 0.050),
                    (float) (viewWidth * 0.5277), 1.10F,
                    cloudColor, cloudAlphas[1],
                    7000, 3500);
            clouds[5] = new Cloud(
                    (float) (viewWidth * 1.3223),
                    (float) (viewWidth * 1.3223 * 0.2286 + viewWidth * 0.050),
                    (float) (viewWidth * 0.6277), 1.10F,
                    cloudColor, cloudAlphas[1],
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
                    (float) (viewWidth * 0.2649), 1.20F,
                    cloudColor, cloudAlphas[0],
                    7000, 0);
            clouds[1] = new Cloud(
                    (float) (viewWidth * 0.4793),
                    (float) (viewWidth * 0.4793 * 0.2185 + viewWidth * 0.050),
                    (float) (viewWidth * 0.2426), 1.20F,
                    cloudColor, cloudAlphas[0],
                    7000, 1500);
            clouds[2] = new Cloud(
                    (float) (viewWidth * 0.8531),
                    (float) (viewWidth * 0.8531 * 0.1286 + viewWidth * 0.050),
                    (float) (viewWidth * 0.2970), 1.20F,
                    cloudColor, cloudAlphas[0],
                    7000, 0);
            clouds[3] = new Cloud(
                    (float) (viewWidth * 0.0551),
                    (float) (viewWidth * 0.0551 * 2.8600 + viewWidth * 0.050),
                    (float) (viewWidth * 0.4125), 1.15F,
                    cloudColor, cloudAlphas[1],
                    7000, 2000);
            clouds[4] = new Cloud(
                    (float) (viewWidth * 0.4928),
                    (float) (viewWidth * 0.4928 * 0.3897 + viewWidth * 0.050),
                    (float) (viewWidth * 0.3521), 1.15F,
                    cloudColor, cloudAlphas[1],
                    7000, 3500);
            clouds[5] = new Cloud(
                    (float) (viewWidth * 1.0499),
                    (float) (viewWidth * 1.0499 * 0.1875 + viewWidth * 0.050),
                    (float) (viewWidth * 0.4186), 1.15F,
                    cloudColor, cloudAlphas[1],
                    7000, 2000);

            if (type == TYPE_CLOUD_DAY) {
                this.initialize(clouds, null, Color.rgb(0, 165, 217));
            } else {
                Star[] stars = new Star[30];
                Random r = new Random();
                int canvasSize = (int) Math.pow(
                        Math.pow(viewWidth, 2) + Math.pow(viewHeight, 2),
                        0.5);
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

    @Override
    public void updateData(MaterialWeatherView view, float rotation2D, float rotation3D) {
        for (Cloud c : clouds) {
            c.move(REFRESH_INTERVAL, rotation2D, rotation3D);
        }
        for (Star s : stars) {
            s.shine(REFRESH_INTERVAL);
        }
        if (thunder != null) {
            thunder.shine(REFRESH_INTERVAL);
        }
    }

    @Override
    public void draw(MaterialWeatherView view, Canvas canvas,
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
            case TYPE_CLOUDY:
                return Color.rgb(96, 121, 136);

            case TYPE_CLOUD_DAY:
                return Color.rgb(0, 165, 217);

            case TYPE_CLOUD_NIGHT:
                return Color.rgb(34, 45, 67);

            case TYPE_THUNDER:
                return Color.rgb(43, 29, 69);
        }
        return ContextCompat.getColor(context, R.color.colorPrimary);
    }
}
