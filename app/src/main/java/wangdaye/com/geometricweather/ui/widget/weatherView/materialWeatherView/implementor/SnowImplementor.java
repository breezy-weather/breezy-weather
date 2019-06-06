package wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.Size;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import java.util.Random;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.MaterialWeatherView;

/**
 * Snow implementor.
 * */

public class SnowImplementor extends MaterialWeatherView.WeatherAnimationImplementor {

    private Paint paint;
    private Snow[] snows;

    private float lastDisplayRate;

    private float lastRotation3D;
    private static final float INITIAL_ROTATION_3D = 1000;

    @ColorInt
    private int backgroundColor;

    public static final int TYPE_SNOW_DAY = 1;
    public static final int TYPE_SNOW_NIGHT = 2;

    @IntDef({TYPE_SNOW_DAY, TYPE_SNOW_NIGHT})
    @interface TypeRule {}

    private class Snow {

        private float cX;
        private float cY;

        float centerX;
        float centerY;
        float radius;

        @ColorInt
        int color;
        float scale;

        float speedX;
        float speedY;

        private int viewWidth;
        private int viewHeight;

        private int canvasSize;

        private Snow(int viewWidth, int viewHeight, @ColorInt int color, float scale) {
            this.viewWidth = viewWidth;
            this.viewHeight = viewHeight;

            this.canvasSize = (int) Math.pow(viewWidth * viewWidth + viewHeight * viewHeight, 0.5);

            this.radius = (float) (viewWidth * 0.0213 * scale);

            this.color = color;
            this.scale = scale;

            this.speedY = viewWidth / 350f;

            this.init(true);
        }

        private void init(boolean firstTime) {
            Random r = new Random();
            cX = r.nextInt(canvasSize);
            if (firstTime) {
                cY = r.nextInt((int) (canvasSize - radius)) - canvasSize;
            } else {
                cY = -radius;
            }
            speedX = r.nextInt((int) (2 * speedY)) - speedY;

            computeCenterPosition();
        }

        private void computeCenterPosition() {
            centerX = (int) (cX - (canvasSize - viewWidth) * 0.5);
            centerY = (int) (cY - (canvasSize - viewHeight) * 0.5);
        }

        void move(long interval, float deltaRotation3D) {
            cX += speedX * interval * Math.pow(scale, 1.5);
            cY += speedY * interval * (Math.pow(scale, 1.5) - 5 * Math.sin(deltaRotation3D * Math.PI / 180.0));

            if (centerY >= canvasSize) {
                init(false);
            } else {
                computeCenterPosition();
            }
        }
    }

    public SnowImplementor(@Size(2) int[] canvasSizes, @TypeRule int type) {
        this.paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        int[] colors = new int[3];
        switch (type) {
            case TYPE_SNOW_DAY:
                backgroundColor = Color.rgb(104, 186, 255);
                colors = new int[] {
                        Color.rgb(128, 197, 255),
                        Color.rgb(185, 222, 255),
                        Color.rgb(255, 255, 255)};
                break;

            case TYPE_SNOW_NIGHT:
                backgroundColor = Color.rgb(26, 91, 146);
                colors = new int[] {
                        Color.rgb(40, 102, 155),
                        Color.rgb(99, 144, 182),
                        Color.rgb(255, 255, 255)};
                break;
        }
        float[] scales = new float[] {0.6F, 0.8F, 1};

        this.snows = new Snow[51];
        for (int i = 0; i < snows.length; i ++) {
            snows[i] = new Snow(
                    canvasSizes[0], canvasSizes[1],
                    colors[i * 3 / snows.length], scales[i * 3 / snows.length]);
        }

        this.lastDisplayRate = 0;
        this.lastRotation3D = INITIAL_ROTATION_3D;
    }

    @Override
    public void updateData(@Size(2) int[] canvasSizes, long interval,
                           float rotation2D, float rotation3D) {
        for (Snow s : snows) {
            s.move(interval, lastRotation3D == INITIAL_ROTATION_3D ? 0 : rotation3D - lastRotation3D);
        }
        lastRotation3D = rotation3D;
    }

    @Override
    public void draw(@Size(2) int[] canvasSizes, Canvas canvas,
                     float displayRate, float scrollRate, float rotation2D, float rotation3D) {

        if (displayRate >= 1) {
            canvas.drawColor(backgroundColor);
        } else {
            canvas.drawColor(
                    ColorUtils.setAlphaComponent(
                            backgroundColor,
                            (int) (displayRate * 255)));
        }

        if (scrollRate < 1) {
            canvas.rotate(
                    rotation2D,
                    canvasSizes[0] * 0.5F,
                    canvasSizes[1] * 0.5F);
            for (Snow s : snows) {
                paint.setColor(s.color);
                if (displayRate < lastDisplayRate) {
                    paint.setAlpha((int) (displayRate * (1 - scrollRate) * 255));
                } else {
                    paint.setAlpha((int) ((1 - scrollRate) * 255));
                }
                canvas.drawCircle(s.centerX, s.centerY, s.radius, paint);
            }
        }

        lastDisplayRate = displayRate;
    }

    @ColorInt
    public static int getThemeColor(Context context, @TypeRule int type) {
        switch (type) {
            case TYPE_SNOW_DAY:
                return Color.rgb(104, 186, 255);

            case TYPE_SNOW_NIGHT:
                return Color.rgb(26, 91, 146);
        }
        return ContextCompat.getColor(context, R.color.colorPrimary);
    }
}
