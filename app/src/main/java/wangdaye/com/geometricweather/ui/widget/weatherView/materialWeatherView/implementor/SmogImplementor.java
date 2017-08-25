package wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.MaterialWeatherView;

/**
 * Smog implementor.
 * */

public class SmogImplementor extends MaterialWeatherView.WeatherAnimationImplementor {

    private Paint paint;
    private Circle[] circles;

    @ColorInt
    private int backgroundColor;

    public static final int TYPE_FOG = 1;
    public static final int TYPE_HAZE = 2;

    @IntDef({TYPE_FOG, TYPE_HAZE})
    @interface TypeRule {}

    private class Circle {

        private float initCX;
        private float initCY;

        float centerX;
        float centerY;

        float radius;
        float initRadius;

        @ColorInt
        int color;
        float alpha;

        long duration;
        long progress;

        Circle(float centerX, float centerY,
               float radius,
               @ColorInt int color, float alpha,
               long duration) {

            this.initCX = centerX;
            this.initCY = centerY;

            this.centerX = centerX;
            this.centerY = centerY;

            this.radius = radius;
            this.initRadius = radius;

            this.color = color;
            this.alpha = alpha;

            this.duration = duration;
            this.progress = 0;
        }

        void scale(long interval, float rotation2D, float rotation3D) {
            centerX = (float) (initCX + Math.sin(rotation2D * Math.PI / 180.0) * 0.25 * radius);
            centerY = (float) (initCY - Math.sin(rotation3D * Math.PI / 180.0) * 0.25 * radius);
            progress = (progress + interval) % duration;
            if (progress < 0.5 * duration) {
                radius = (float) (initRadius * (1 + 0.03 * progress / 0.5 / duration));
            } else {
                radius = (float) (initRadius * (1.03 - 0.03 * (progress - 0.5 * duration) / 0.5 / duration));
            }
        }
    }

    public SmogImplementor(MaterialWeatherView view, @TypeRule int type) {
        this.paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        float screenWidth = view.getMeasuredWidth();
        float cX = (float) (0.5000 * screenWidth);
        float cY = (float) (1.2565 * screenWidth);

        int color = 0;
        switch (type) {
            case TYPE_FOG:
                color = Color.rgb(66, 66, 66);
                backgroundColor = Color.rgb(84, 100, 121);
                break;

            case TYPE_HAZE:
                color = Color.rgb(0, 0, 0);
                backgroundColor = Color.rgb(66, 66, 66);
                break;
        }

        float alpha = type == TYPE_FOG ? 0.10F : 0.05F;
        this.circles = new Circle[5];
        circles[0] = new Circle(cX, cY, (float) (0.4440 * screenWidth), color, alpha, 5000);
        circles[1] = new Circle(cX, cY, (float) (0.5770 * screenWidth), color, alpha, 5000);
        circles[2] = new Circle(cX, cY, (float) (0.7106 * screenWidth), color, alpha, 5000);
        circles[3] = new Circle(cX, cY, (float) (0.8434 * screenWidth), color, alpha, 5000);
        circles[4] = new Circle(cX, cY, (float) (0.9769 * screenWidth), color, alpha, 5000);
    }

    @Override
    public void updateData(MaterialWeatherView view, float rotation2D, float rotation3D) {
        for (Circle c : circles) {
            c.scale(REFRESH_INTERVAL, rotation2D, rotation3D);
        }
    }

    @Override
    public void draw(MaterialWeatherView view, Canvas canvas,
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
            for (Circle c : circles) {
                paint.setColor(c.color);
                paint.setAlpha((int) (displayRate * (1 - scrollRate) * c.alpha * 255));
                canvas.drawCircle(
                        c.centerX,
                        c.centerY,
                        (float) (c.radius * (displayRate * 0.3 + 0.7)),
                        paint);
            }
        }
    }

    @ColorInt
    public static int getThemeColor(Context context, @TypeRule int type) {
        switch (type) {
            case TYPE_FOG:
                return Color.rgb(84, 100, 121);

            case TYPE_HAZE:
                return Color.rgb(66, 66, 66);
        }
        return ContextCompat.getColor(context, R.color.colorPrimary);
    }
}
