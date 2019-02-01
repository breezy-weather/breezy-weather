package wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.v4.graphics.ColorUtils;

import java.util.Random;

import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.MaterialWeatherView;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Star track implementor.
 * */

@Deprecated
public class StarTracksImplementor extends MaterialWeatherView.WeatherAnimationImplementor {

    private Paint starPaint;
    private Paint polarisPaint;
    private Star[] stars;
    private Polaris polaris;

    @ColorInt
    private int backgroundColor;

    private float translateX;
    private float translateY;

    private static final int STAR_NUM = 700;
    private static final float MAX_ANGLE = 6;
    private static final float ROTATE_SPEED = MAX_ANGLE / (5 * 1000);
    private static final long CENTER_STAR_SHINNING_DURATION = 3000;

    private class Star {

        float radius;
        float startAngle;
        float sweepAngle;

        @ColorInt int color;
        float strokeWidth;
        float alpha;

        RectF rectF;
        boolean inDisplayArea;

        private Star(Random random, int centerX, int centerY, @ColorInt int[] colors,
                     float maxRadius, float maxStrokeWidth) {

            this.radius = (float) ((random.nextFloat() + 0.015) * maxRadius);
            this.startAngle = random.nextInt(360);
            this.sweepAngle = 0.1f;

            this.color = colors[random.nextInt(colors.length)];
            this.strokeWidth = random.nextFloat() * maxStrokeWidth;
            this.alpha = 1;

            this.rectF = new RectF(
                    centerX - radius,
                    centerY - radius,
                    centerX + radius,
                    centerY + radius);
            this.inDisplayArea = true;
        }

        void rotate(float speed, long interval, float maxAngle) {
            if (sweepAngle < maxAngle) {
                sweepAngle += speed * (0.1 + 0.9 * sweepAngle / maxAngle) * interval;
            } else {
                startAngle += speed * interval;
            }
        }
    }

    private class Polaris {

        int centerX;
        int centerY;
        float radius;

        @ColorInt int color;
        float alpha;

        long duration;
        long progress;

        Polaris(int centerX, int centerY, float radius, @ColorInt int color, long duration) {
            this.centerX = centerX;
            this.centerY = centerY;

            this.radius = (float) (radius * (0.5 + 0.5 * new Random().nextFloat()));

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
            alpha = alpha * 0.66f + 0.33f;
        }
    }

    public StarTracksImplementor(MaterialWeatherView view) {
        this.starPaint = new Paint();
        starPaint.setStyle(Paint.Style.STROKE);
        starPaint.setStrokeCap(Paint.Cap.ROUND);
        starPaint.setAntiAlias(true);

        this.polarisPaint = new Paint();
        polarisPaint.setStyle(Paint.Style.FILL);
        polarisPaint.setAntiAlias(true);

        this.stars = new Star[STAR_NUM];
        int centerX = view.getMeasuredWidth() / 2;
        int centerY = (int) (view.getMeasuredHeight() / 5.5);
        float maxRadius = view.getMeasuredHeight() / 2f;
        float maxStrokeWidth = DisplayUtils.dpToPx(view.getContext(), 2);
        /*
        int[] trackColors = new int[] {
                Color.rgb(180, 182, 242),
                Color.rgb(138, 145, 180),
                Color.rgb(116, 135, 203),
                Color.rgb(102, 120, 174),
                Color.rgb(63, 100, 168),
                Color.rgb(53, 71, 135),
                Color.rgb(50, 51, 86),
                Color.rgb(38, 49, 102)};
        */
        int[] trackColors = new int[] {
                Color.rgb(210, 247, 255),
                Color.rgb(208, 233, 255),
                Color.rgb(175, 201, 228),
                Color.rgb(164, 194, 220),
                Color.rgb(97, 171, 220),
                Color.rgb(74, 141, 193),
                Color.rgb(54, 66, 119),
                Color.rgb(34, 48, 74)};
        /*
        int[] accentColors = new int[] {
                Color.rgb(240, 220, 151),
                Color.rgb(241, 233, 207)};
        */
        int[] accentColors = new int[] {
                Color.rgb(236, 234, 213),
                Color.rgb(240, 220, 151)};
        Random r = new Random();
        for (int i = 0; i < stars.length; i ++) {
            if (i % 50 == 0) {
                stars[i] = new Star(r, centerX, centerY, accentColors, maxRadius, maxStrokeWidth);
            } else {
                stars[i] = new Star(r, centerX, centerY, trackColors, maxRadius, maxStrokeWidth);
            }
        }

        this.polaris = new Polaris(
                centerX, centerY, DisplayUtils.dpToPx(view.getContext(), 1),
                Color.rgb(240, 220, 151),
                CENTER_STAR_SHINNING_DURATION);

        this.backgroundColor = getThemeColor();
    }

    @Override
    public void updateData(MaterialWeatherView view, long interval,
                           float rotation2D, float rotation3D) {
        this.translateX = (float) (Math.sin(rotation2D * Math.PI / 180.0) * 0.33 * view.getMeasuredWidth());
        this.translateY = (float) (Math.sin(rotation3D * Math.PI / 180.0) * -0.08 * view.getMeasuredHeight());
        for (Star s : stars) {
            s.rotate(ROTATE_SPEED, interval, MAX_ANGLE);
            // s.setInDisplayArea(view.getMeasuredWidth(), view.getMeasuredHeight());
        }
        polaris.shine(interval);
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
            canvas.translate(translateX, translateY);

            for (Star s : stars) {
                if (s.inDisplayArea) {
                    starPaint.setStrokeWidth(s.strokeWidth);
                    starPaint.setColor(s.color);
                    starPaint.setAlpha((int) (displayRate * (1 - scrollRate) * s.alpha * 255));
                    canvas.drawArc(s.rectF, s.startAngle, s.sweepAngle, false, starPaint);
                }
            }

            polarisPaint.setColor(polaris.color);
            polarisPaint.setAlpha((int) (displayRate * (1 - scrollRate) * polaris.alpha * 255));
            canvas.drawCircle(polaris.centerX, polaris.centerY, polaris.radius, polarisPaint);
        }
    }

    @ColorInt
    public static int getThemeColor() {
        // return Color.rgb(33, 33, 66);
        return Color.rgb(20, 28, 44);
    }
}