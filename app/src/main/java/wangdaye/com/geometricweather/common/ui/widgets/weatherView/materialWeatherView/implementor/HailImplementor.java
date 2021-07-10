package wangdaye.com.geometricweather.common.ui.widgets.weatherView.materialWeatherView.implementor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.Size;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import java.util.Random;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.ui.widgets.weatherView.materialWeatherView.MaterialWeatherView;

/**
 * Hail implementor.
 * */

public class HailImplementor extends MaterialWeatherView.WeatherAnimationImplementor {

    private final Paint mPaint;
    private final Hail[] mHails;

    private float mLastDisplayRate;

    private float mLastRotation3D;
    private static final float INITIAL_ROTATION_3D = 1000;

    @ColorInt
    private int mBackgroundColor;

    public static final int TYPE_HAIL_DAY = 1;
    public static final int TYPE_HAIL_NIGHT = 2;

    @IntDef({TYPE_HAIL_DAY, TYPE_HAIL_NIGHT})
    @interface TypeRule {}

    private static class Hail {

        private float cx;
        private float cy;

        float centerX;
        float centerY;
        float size;
        float rotation;

        float speedY;
        float speedX;
        float speedRotation;

        RectF rectF = new RectF();

        @ColorInt
        int color;
        float scale;

        private final int mViewWidth;
        private final int mViewHeight;

        private final int mCanvasSize;

        private Hail(int viewWidth, int viewHeight, @ColorInt int color, float scale) {
            mViewWidth = viewWidth;
            mViewHeight = viewHeight;

            mCanvasSize = (int) Math.pow(viewWidth * viewWidth + viewHeight * viewHeight, 0.5);

            this.size = (float) (0.0324 * viewWidth) * 0.8f;
            this.speedY = viewWidth / 200f;
            this.color = color;
            this.scale = scale;

            init(true);
        }

        private void init(boolean firstTime) {
            Random r = new Random();
            cx = r.nextInt(mCanvasSize);
            if (firstTime) {
                cy = r.nextInt((int) (mCanvasSize - size)) - mCanvasSize;
            } else {
                cy = -size;
            }
            rotation = 360 * r.nextFloat();

            speedRotation = 360.f / 500.f * r.nextFloat();
            speedX = 0.75f * (r.nextFloat() * speedY * (r.nextBoolean() ? 1 : -1));

            computeCenterPosition();
        }

        private void computeCenterPosition() {
            centerX = (float) (cx - (mCanvasSize - mViewWidth) * 0.5);
            centerY = (float) (cy - (mCanvasSize - mViewHeight) * 0.5);
        }

        void move(long interval, float deltaRotation3D) {
            cx += speedX * interval * Math.pow(scale, 1.5);
            cy += speedY * interval * (
                    Math.pow(scale, 1.5) - 5 * Math.sin(deltaRotation3D * Math.PI / 180.0)
            );
            rotation = (rotation + speedRotation * interval) % 360;

            if (cy - size >= mCanvasSize) {
                init(false);
            } else {
                computeCenterPosition();
            }

            rectF.set(
                    cx - size * scale,
                    cy - size * scale,
                    cx + size * scale,
                    cy + size * scale
            );
        }
    }

    public HailImplementor(@Size(2) int[] canvasSizes, @TypeRule int type) {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        int[] colors = new int[3];
        switch (type) {
            case TYPE_HAIL_DAY:
                mBackgroundColor = Color.rgb(80, 116, 193);
                colors = new int[] {
                        Color.rgb(101, 134, 203),
                        Color.rgb(152, 175, 222),
                        Color.rgb(255, 255, 255),};
                break;

            case TYPE_HAIL_NIGHT:
                mBackgroundColor = Color.rgb(42, 52, 69);
                colors = new int[] {
                        Color.rgb(64, 67, 85),
                        Color.rgb(127, 131, 154),
                        Color.rgb(255, 255, 255),};
                break;
        }
        float[] scales = new float[] {0.6F, 0.8F, 1};

        mHails = new Hail[51];
        for (int i = 0; i < mHails.length; i ++) {
            mHails[i] = new Hail(
                    canvasSizes[0], canvasSizes[1],
                    colors[i * 3 / mHails.length], scales[i * 3 / mHails.length]);
        }

        mLastDisplayRate = 0;
        mLastRotation3D = INITIAL_ROTATION_3D;
    }

    @Override
    public void updateData(@Size(2) int[] canvasSizes, long interval,
                           float rotation2D, float rotation3D) {
        for (Hail h : mHails) {
            h.move(interval, mLastRotation3D == INITIAL_ROTATION_3D ? 0 : rotation3D - mLastRotation3D);
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

            for (Hail h : mHails) {
                mPaint.setColor(h.color);
                if (displayRate < mLastDisplayRate) {
                    mPaint.setAlpha((int) (displayRate * (1 - scrollRate) * 255));
                } else {
                    mPaint.setAlpha(255);
                }

                canvas.rotate(h.rotation, h.cx, h.cy);
                canvas.drawRect(h.rectF, mPaint);
                canvas.rotate(-h.rotation, h.cx, h.cy);
            }
        }

        mLastDisplayRate = displayRate;
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
