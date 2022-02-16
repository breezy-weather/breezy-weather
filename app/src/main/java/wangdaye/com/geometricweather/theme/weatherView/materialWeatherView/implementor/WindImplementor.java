package wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.implementor;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.annotation.ColorInt;
import androidx.annotation.Size;
import androidx.core.graphics.ColorUtils;

import java.util.Random;

import wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.MaterialWeatherView;

public class WindImplementor extends MaterialWeatherView.WeatherAnimationImplementor {

    private final Paint mPaint;
    private final Wind[] mWinds;

    private float mLastDisplayRate;

    private float mLastRotation3D;
    private static final float INITIAL_ROTATION_3D = 1000;

    private static final int WIND_COUNT = 240;

    @ColorInt
    private final int mBackgroundColor;

    private static class Wind {

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

        private final float MAX_WIDTH;
        private final float MIN_WIDTH;
        private final float MAX_HEIGHT;
        private final float MIN_HEIGHT;

        private Wind(int viewWidth, int viewHeight, @ColorInt int color, float scale) {
            mViewWidth = viewWidth;
            mViewHeight = viewHeight;

            mCanvasSize = (int) Math.pow(
                    viewWidth * viewWidth + viewHeight * viewHeight,
                    0.5
            );

            this.rectF = new RectF();
            this.speed = (float) (
                    mCanvasSize / (
                            1000.0 * (0.5 + new Random().nextDouble())
                    ) * 4.0
            );
            this.color = color;
            this.scale = scale;

            this.MAX_HEIGHT = 0.006f * mCanvasSize;
            this.MIN_HEIGHT = 0.003f * mCanvasSize;
            this.MAX_WIDTH = this.MAX_HEIGHT * 10;
            this.MIN_WIDTH = this.MIN_HEIGHT * 6;

            init(true);
        }

        private void init(boolean firstTime) {
            Random r = new Random();
            y = r.nextInt(mCanvasSize);
            if (firstTime) {
                x = r.nextInt((int) (mCanvasSize - MAX_HEIGHT)) - mCanvasSize;
            } else {
                x = -MAX_HEIGHT;
            }
            width = MIN_WIDTH + r.nextFloat() * (MAX_WIDTH - MIN_WIDTH);
            height = MIN_HEIGHT + r.nextFloat() * (MAX_HEIGHT - MIN_HEIGHT);

            buildRectF();
        }

        private void buildRectF() {
            float x = (float) (this.x - (mCanvasSize - mViewWidth) * 0.5);
            float y = (float) (this.y - (mCanvasSize - mViewHeight) * 0.5);
            rectF.set(x, y, x + width * scale, y + height * scale);
        }

        void move(long interval, float deltaRotation3D) {
            x += speed * interval
                    * (Math.pow(scale, 1.5)
                    + 5 * Math.sin(deltaRotation3D * Math.PI / 180.0) * Math.cos(16 * Math.PI / 180.0));
            y -= speed * interval
                    * 5 * Math.sin(deltaRotation3D * Math.PI / 180.0) * Math.sin(16 * Math.PI / 180.0);

            if (x >= mCanvasSize) {
                init(false);
            } else {
                buildRectF();
            }
        }
    }

    public WindImplementor(@Size(2) int[] canvasSizes) {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        mBackgroundColor = Color.rgb(233, 158, 60);
        int[] colors = new int[] {
                Color.rgb(240, 200, 148),
                Color.rgb(237, 178, 100),
                Color.rgb(209, 142, 54),};
        float[] scales = new float[] {0.6F, 0.8F, 1};

        mWinds = new Wind[WIND_COUNT];
        for (int i = 0; i < mWinds.length; i ++) {
            mWinds[i] = new Wind(
                    canvasSizes[0],
                    canvasSizes[1],
                    colors[i * 3 / mWinds.length],
                    scales[i * 3 / mWinds.length]);
        }

        mLastDisplayRate = 0;
        mLastRotation3D = INITIAL_ROTATION_3D;
    }

    @Override
    public void updateData(@Size(2) int[] canvasSizes, long interval,
                           float rotation2D, float rotation3D) {
        for (Wind w : mWinds) {
            w.move(interval, mLastRotation3D == INITIAL_ROTATION_3D ? 0 : rotation3D - mLastRotation3D);
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
            rotation2D -= 16;
            canvas.rotate(
                    rotation2D,
                    canvasSizes[0] * 0.5F,
                    canvasSizes[1] * 0.5F);

            for (Wind w : mWinds) {
                mPaint.setColor(w.color);
                if (displayRate < mLastDisplayRate) {
                    mPaint.setAlpha((int) (displayRate * (1 - scrollRate) * 255));
                } else {
                    mPaint.setAlpha((int) ((1 - scrollRate) * 255));
                }
                canvas.drawRect(w.rectF, mPaint);
            }
        }

        mLastDisplayRate = displayRate;
    }

    @ColorInt
    public static int getThemeColor() {
        return Color.rgb(233, 158, 60);
    }
}
