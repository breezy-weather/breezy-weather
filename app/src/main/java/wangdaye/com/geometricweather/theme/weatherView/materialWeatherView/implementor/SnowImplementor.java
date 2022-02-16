package wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.implementor;

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
import wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.MaterialWeatherView;

/**
 * Snow implementor.
 * */

public class SnowImplementor extends MaterialWeatherView.WeatherAnimationImplementor {

    private final Paint mPaint;
    private final Snow[] mSnows;

    private float mLastDisplayRate;

    private float mLastRotation3D;
    private static final float INITIAL_ROTATION_3D = 1000;

    private static final int SNOW_COUNT = 90;

    @ColorInt
    private int mBackgroundColor;

    public static final int TYPE_SNOW_DAY = 1;
    public static final int TYPE_SNOW_NIGHT = 2;

    @IntDef({TYPE_SNOW_DAY, TYPE_SNOW_NIGHT})
    @interface TypeRule {}

    private static class Snow {

        private float mCX;
        private float mCY;

        float centerX;
        float centerY;
        float radius;

        @ColorInt
        int color;
        float scale;

        float speedX;
        float speedY;

        private final int mViewWidth;
        private final int mViewHeight;

        private final int mCanvasSize;

        private Snow(int viewWidth, int viewHeight, @ColorInt int color, float scale) {
            mViewWidth = viewWidth;
            mViewHeight = viewHeight;

            mCanvasSize = (int) Math.pow(
                    viewWidth * viewWidth + viewHeight * viewHeight,
                    0.5
            );

            this.radius = (float) (
                    mCanvasSize * (
                            0.005 + new Random().nextDouble() * 0.007
                    ) * scale
            );

            this.color = color;
            this.scale = scale;

            this.speedY = (float) (
                    mCanvasSize / (
                            1000.0 * (2.5 + new Random().nextDouble())
                    ) * 3.0
            );

            init(true);
        }

        private void init(boolean firstTime) {
            Random r = new Random();
            mCX = r.nextInt(mCanvasSize);
            if (firstTime) {
                mCY = r.nextInt((int) (mCanvasSize - radius)) - mCanvasSize;
            } else {
                mCY = -radius;
            }
            speedX = r.nextInt((int) (2 * speedY)) - speedY;

            computeCenterPosition();
        }

        private void computeCenterPosition() {
            centerX = (int) (mCX - (mCanvasSize - mViewWidth) * 0.5);
            centerY = (int) (mCY - (mCanvasSize - mViewHeight) * 0.5);
        }

        void move(long interval, float deltaRotation3D) {
            mCX += speedX * interval * Math.pow(scale, 1.5);
            mCY += speedY * interval * (Math.pow(scale, 1.5) - 5 * Math.sin(deltaRotation3D * Math.PI / 180.0));

            if (centerY >= mCanvasSize) {
                init(false);
            } else {
                computeCenterPosition();
            }
        }
    }

    public SnowImplementor(@Size(2) int[] canvasSizes, @TypeRule int type) {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        int[] colors = new int[3];
        switch (type) {
            case TYPE_SNOW_DAY:
                mBackgroundColor = Color.rgb(104, 186, 255);
                colors = new int[] {
                        Color.rgb(128, 197, 255),
                        Color.rgb(185, 222, 255),
                        Color.rgb(255, 255, 255)};
                break;

            case TYPE_SNOW_NIGHT:
                mBackgroundColor = Color.rgb(26, 91, 146);
                colors = new int[] {
                        Color.rgb(40, 102, 155),
                        Color.rgb(99, 144, 182),
                        Color.rgb(255, 255, 255)};
                break;
        }
        float[] scales = new float[] {0.6F, 0.8F, 1};

        mSnows = new Snow[SNOW_COUNT];
        for (int i = 0; i < mSnows.length; i ++) {
            mSnows[i] = new Snow(
                    canvasSizes[0],
                    canvasSizes[1],
                    colors[i * 3 / mSnows.length],
                    scales[i * 3 / mSnows.length]
            );
        }

        mLastDisplayRate = 0;
        mLastRotation3D = INITIAL_ROTATION_3D;
    }

    @Override
    public void updateData(@Size(2) int[] canvasSizes, long interval,
                           float rotation2D, float rotation3D) {
        for (Snow s : mSnows) {
            s.move(interval, mLastRotation3D == INITIAL_ROTATION_3D ? 0 : rotation3D - mLastRotation3D);
        }
        mLastRotation3D = rotation3D;
    }

    @Override
    public void draw(@Size(2) int[] canvasSizes, Canvas canvas,
                     float displayRate, float scrollRate, float rotation2D, float rotation3D) {

        if (displayRate >= 1) {
            canvas.drawColor(mBackgroundColor);
        } else {
            canvas.drawColor(
                    ColorUtils.setAlphaComponent(
                            mBackgroundColor,
                            (int) (displayRate * 255)));
        }

        if (scrollRate < 1) {
            canvas.rotate(
                    rotation2D,
                    canvasSizes[0] * 0.5F,
                    canvasSizes[1] * 0.5F);
            for (Snow s : mSnows) {
                mPaint.setColor(s.color);
                if (displayRate < mLastDisplayRate) {
                    mPaint.setAlpha((int) (displayRate * (1 - scrollRate) * 255));
                } else {
                    mPaint.setAlpha((int) ((1 - scrollRate) * 255));
                }
                canvas.drawCircle(s.centerX, s.centerY, s.radius, mPaint);
            }
        }

        mLastDisplayRate = displayRate;
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
