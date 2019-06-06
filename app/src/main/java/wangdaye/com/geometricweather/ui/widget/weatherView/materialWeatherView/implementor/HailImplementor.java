package wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.Size;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import java.util.Random;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.MaterialWeatherView;

/**
 * Hail implementor.
 * */

public class HailImplementor extends MaterialWeatherView.WeatherAnimationImplementor {

    private Paint paint;
    private Path path;
    private Hail[] hails;

    private float lastDisplayRate;

    private float lastRotation3D;
    private static final float INITIAL_ROTATION_3D = 1000;

    @ColorInt
    private int backgroundColor;

    public static final int TYPE_HAIL_DAY = 1;
    public static final int TYPE_HAIL_NIGHT = 2;

    @IntDef({TYPE_HAIL_DAY, TYPE_HAIL_NIGHT})
    @interface TypeRule {}

    private class Hail {

        private float cX;
        private float cY;

        float centerX;
        float centerY;
        float size;

        float speed;

        @ColorInt
        int color;
        float scale;

        private int viewWidth;
        private int viewHeight;

        private int canvasSize;

        private Hail(int viewWidth, int viewHeight, @ColorInt int color, float scale) {
            this.viewWidth = viewWidth;
            this.viewHeight = viewHeight;

            this.canvasSize = (int) Math.pow(viewWidth * viewWidth + viewHeight * viewHeight, 0.5);

            this.size = (float) (0.0324 * viewWidth);
            this.speed = viewWidth / 125f;
            this.color = color;
            this.scale = scale;

            this.init(true);
        }

        private void init(boolean firstTime) {
            Random r = new Random();
            cX = r.nextInt(canvasSize);
            if (firstTime) {
                cY = r.nextInt((int) (canvasSize - size)) - canvasSize;
            } else {
                cY = -size;
            }
            computeCenterPosition();
        }

        private void computeCenterPosition() {
            centerX = (float) (cX - (canvasSize - viewWidth) * 0.5);
            centerY = (float) (cY - (canvasSize - viewHeight) * 0.5);
        }

        void move(long interval, float deltaRotation3D) {
            cY += speed * interval * (Math.pow(scale, 1.5) - 5 * Math.sin(deltaRotation3D * Math.PI / 180.0));
            if (cY - size >= canvasSize) {
                init(false);
            } else {
                computeCenterPosition();
            }
        }
    }

    public HailImplementor(@Size(2) int[] canvasSizes, @TypeRule int type) {
        this.paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        this.path = new Path();

        int[] colors = new int[3];
        switch (type) {
            case TYPE_HAIL_DAY:
                backgroundColor = Color.rgb(80, 116, 193);
                colors = new int[] {
                        Color.rgb(101, 134, 203),
                        Color.rgb(152, 175, 222),
                        Color.rgb(255, 255, 255),};
                break;

            case TYPE_HAIL_NIGHT:
                backgroundColor = Color.rgb(42, 52, 69);
                colors = new int[] {
                        Color.rgb(64, 67, 85),
                        Color.rgb(127, 131, 154),
                        Color.rgb(255, 255, 255),};
                break;
        }
        float[] scales = new float[] {0.6F, 0.8F, 1};

        this.hails = new Hail[51];
        for (int i = 0; i < hails.length; i ++) {
            hails[i] = new Hail(
                    canvasSizes[0], canvasSizes[1],
                    colors[i * 3 / hails.length], scales[i * 3 / hails.length]);
        }

        this.lastDisplayRate = 0;
        this.lastRotation3D = INITIAL_ROTATION_3D;
    }

    @Override
    public void updateData(@Size(2) int[] canvasSizes, long interval,
                           float rotation2D, float rotation3D) {
        for (Hail h : hails) {
            h.move(interval, lastRotation3D == INITIAL_ROTATION_3D ? 0 : rotation3D - lastRotation3D);
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

            for (Hail h : hails) {
                path.reset();
                path.moveTo(h.centerX - h.size, h.centerY);
                path.lineTo(h.centerX, h.centerY - h.size);
                path.lineTo(h.centerX + h.size, h.centerY);
                path.lineTo(h.centerX, h.centerY + h.size);
                path.close();
                paint.setColor(h.color);
                if (displayRate < lastDisplayRate) {
                    paint.setAlpha((int) (displayRate * (1 - scrollRate) * 255));
                } else {
                    paint.setAlpha(255);
                }
                canvas.drawPath(path, paint);
            }
        }

        lastDisplayRate = displayRate;
    }

    @ColorInt
    public static int getThemeColor(Context context, @TypeRule int type) {
        switch (type) {
            case TYPE_HAIL_DAY:
                return Color.rgb(80, 116, 193);

            case TYPE_HAIL_NIGHT:
                return Color.rgb(42, 52, 69);
        }
        return ContextCompat.getColor(context, R.color.colorPrimary);
    }
}
