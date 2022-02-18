package wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.implementor;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.annotation.ColorInt;
import androidx.annotation.Size;

import java.util.Random;

import wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.MaterialWeatherView;

/**
 * Meteor shower implementor.
 * */

public class MeteorShowerImplementor extends MaterialWeatherView.WeatherAnimationImplementor {

    private final Paint mPaint;
    private final Meteor[] mMeteors;
    private final Star[] mStars;

    private float mLastRotation3D;
    private static final float INITIAL_ROTATION_3D = 1000;

    private static class Meteor {

        float x;
        float y;
        float width;
        float height;

        RectF rectF;
        float speed;

        @ColorInt
        int color;
        float scale;

        private final int mViewWidth;
        private final int mViewHeight;

        private final int mCanvasSize;

        private final float MAX_HEIGHT;
        private final float MIN_HEIGHT;

        private Meteor(int viewWidth, int viewHeight, @ColorInt int color, float scale) { // 1, 0.7, 0.4
            mViewWidth = viewWidth;
            mViewHeight = viewHeight;

            mCanvasSize = (int) Math.pow(viewWidth * viewWidth + viewHeight * viewHeight, 0.5);

            this.width = (float) (viewWidth * 0.0088 * scale);

            this.rectF = new RectF();
            this.speed = viewWidth / 200f;
            this.color = color;
            this.scale = scale;

            this.MAX_HEIGHT = (float) (1.1 * viewWidth / Math.cos(60.0 * Math.PI / 180.0));
            this.MIN_HEIGHT = (float) (MAX_HEIGHT * 0.7);

            init(true);
        }

        private void init(boolean firstTime) {
            Random r = new Random();
            x = r.nextInt(mCanvasSize);
            if (firstTime) {
                y = r.nextInt(mCanvasSize) - MAX_HEIGHT - mCanvasSize;
            } else {
                y = -MAX_HEIGHT;
            }
            height = MIN_HEIGHT + r.nextFloat() * (MAX_HEIGHT - MIN_HEIGHT);

            buildRectF();
        }

        private void buildRectF() {
            float x = (float) (this.x - (mCanvasSize - mViewWidth) * 0.5);
            float y = (float) (this.y - (mCanvasSize - mViewHeight) * 0.5);
            rectF.set(x, y, x + width, y + height);
        }

        void move(long interval, float deltaRotation3D) {
            x -= speed * interval * 5
                    * Math.sin(deltaRotation3D * Math.PI / 180.0) * Math.cos(60 * Math.PI / 180.0);
            y += speed * interval
                    * (Math.pow(scale, 0.5)
                    - 5 * Math.sin(deltaRotation3D * Math.PI / 180.0) * Math.sin(60 * Math.PI / 180.0));

            if (y >= mCanvasSize) {
                init(false);
            } else {
                buildRectF();
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
            alpha = alpha * 0.66f + 0.33f;
        }
    }

    public MeteorShowerImplementor(@Size(2) int[] canvasSizes) {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);

        Random random = new Random();
        int viewWidth = canvasSizes[0];
        int viewHeight = canvasSizes[1];
        int[] colors = new int[] {
                Color.rgb(210, 247, 255),
                Color.rgb(208, 233, 255),
                Color.rgb(175, 201, 228),
                Color.rgb(164, 194, 220),
                Color.rgb(97, 171, 220),
                Color.rgb(74, 141, 193),
                Color.rgb(54, 66, 119),
                Color.rgb(34, 48, 74),
                Color.rgb(236, 234, 213),
                Color.rgb(240, 220, 151)};
        /*
        int[] colors = new int[]{
                Color.rgb(170, 215, 252),
                Color.rgb(255, 255, 255),
                Color.rgb(255, 255, 255)};
        */
        mMeteors = new Meteor[15];
        for (int i = 0; i < mMeteors.length; i ++) {
            mMeteors[i] = new Meteor(
                    viewWidth, viewHeight,
                    colors[random.nextInt(colors.length)], random.nextFloat());
        }
        mStars = new Star[50];
        int canvasSize = (int) Math.pow(
                Math.pow(viewWidth, 2) + Math.pow(viewHeight, 2),
                0.5);
        int width = (int) (1.0 * canvasSize);
        int height = (int) ((canvasSize - viewHeight) * 0.5 + viewWidth * 1.1111);
        float radius = (float) (0.00125 * canvasSize * (0.5 + random.nextFloat()));
        for (int i = 0; i < mStars.length; i ++) {
            int x = (int) (random.nextInt(width) - 0.5 * (canvasSize - viewWidth));
            int y = (int) (random.nextInt(height) - 0.5 * (canvasSize - viewHeight));

            long duration = (long) (2500 + random.nextFloat() * 2500);
            mStars[i] = new Star(
                    x,
                    y,
                    radius,
                    colors[i % colors.length],
                    duration
            );
        }

        mLastRotation3D = INITIAL_ROTATION_3D;
    }

    @Override
    public void updateData(@Size(2) int[] canvasSizes, long interval,
                           float rotation2D, float rotation3D) {

        for (Meteor m : mMeteors) {
            m.move(interval, mLastRotation3D == INITIAL_ROTATION_3D ? 0 : rotation3D - mLastRotation3D);
        }
        for (Star s : mStars) {
            s.shine(interval);
        }
        mLastRotation3D = rotation3D;
    }

    @Override
    public void draw(@Size(2) int[] canvasSizes, Canvas canvas,
                     float scrollRate, float rotation2D, float rotation3D) {

        if (scrollRate < 1) {
            canvas.rotate(
                    rotation2D,
                    canvasSizes[0] * 0.5F,
                    canvasSizes[1] * 0.5F);
            for (Star s : mStars) {
                mPaint.setColor(s.color);
                mPaint.setAlpha((int) ((1 - scrollRate) * s.alpha * 255));
                mPaint.setStrokeWidth(s.radius * 2);
                canvas.drawPoint(s.centerX, s.centerY, mPaint);
            }

            canvas.rotate(
                    60,
                    canvasSizes[0] * 0.5F,
                    canvasSizes[1] * 0.5F);
            for (Meteor m : mMeteors) {
                mPaint.setColor(m.color);
                mPaint.setStrokeWidth(m.rectF.width());
                mPaint.setAlpha((int) ((1 - scrollRate) * 255));
                canvas.drawLine(
                        m.rectF.centerX(), m.rectF.top,
                        m.rectF.centerX(), m.rectF.bottom,
                        mPaint);
            }
        }
    }

    @ColorInt
    public static int getThemeColor() {
        return Color.rgb(20, 28, 44);
    }
}
