package wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.implementor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.Size;
import androidx.core.content.ContextCompat;

import java.util.Random;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.MaterialWeatherView;

/**
 * Rain implementor.
 * */

public class RainImplementor extends MaterialWeatherView.WeatherAnimationImplementor {

    private final Paint mPaint;
    private Rain[] mRains;
    private Thunder mThunder;

    private float mLastRotation3D;
    private static final float INITIAL_ROTATION_3D = 1000;

    public static final int TYPE_RAIN_DAY = 1;
    public static final int TYPE_RAIN_NIGHT = 2;
    public static final int TYPE_THUNDERSTORM = 3;
    public static final int TYPE_SLEET_DAY = 4;
    public static final int TYPE_SLEET_NIGHT = 5;

    @IntDef({TYPE_RAIN_DAY, TYPE_RAIN_NIGHT, TYPE_THUNDERSTORM, TYPE_SLEET_DAY, TYPE_SLEET_NIGHT})
    @interface TypeRule {}

    private static final int RAIN_COUNT = 75;
    private static final int SLEET_COUNT = 45;

    private static class Rain {

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

        private Rain(int viewWidth, int viewHeight, @ColorInt int color, float scale) {
            mViewWidth = viewWidth;
            mViewHeight = viewHeight;

            mCanvasSize = (int) Math.pow(viewWidth * viewWidth + viewHeight * viewHeight, 0.5);

            this.rectF = new RectF();
            this.speed = (float) (
                    mCanvasSize / (
                            1000.0 * (1.75 + new Random().nextDouble())
                    ) * 5.0
            );
            this.color = color;
            this.scale = scale;

            this.MAX_WIDTH = (float) (0.006 * mCanvasSize);
            this.MIN_WIDTH = (float) (0.003 * mCanvasSize);
            this.MAX_HEIGHT = MAX_WIDTH * 10;
            this.MIN_HEIGHT = MIN_WIDTH * 6;

            init(true);
        }

        private void init(boolean firstTime) {
            Random r = new Random();
            x = r.nextInt(mCanvasSize);
            if (firstTime) {
                y = r.nextInt((int) (mCanvasSize - MAX_HEIGHT)) - mCanvasSize;
            } else {
                y = -MAX_HEIGHT * (1 + 2 * r.nextFloat());
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
            y += speed * interval
                    * (Math.pow(scale, 1.5)
                    - 5 * Math.sin(deltaRotation3D * Math.PI / 180.0) * Math.cos(8 * Math.PI / 180.0));
            x -= speed * interval
                    * 5 * Math.sin(deltaRotation3D * Math.PI / 180.0) * Math.sin(8 * Math.PI / 180.0);

            if (y >= mCanvasSize) {
                init(false);
            } else {
                buildRectF();
            }
        }
    }

    private static class Thunder {

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
            this.b = 168;
            init();
            computeFrame();
        }

        private void init() {
            progress = 0;
            duration = 300;
            delay = new Random().nextInt(5000) + 3000;
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

    public RainImplementor(@Size(2) int[] canvasSizes, @TypeRule int type) {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        int[] colors = new int[3];
        switch (type) {
            case TYPE_RAIN_DAY:
                mRains = new Rain[RAIN_COUNT];
                mThunder = null;
                colors = new int[]{
                        Color.rgb(223, 179, 114),
                        Color.rgb(152, 175, 222),
                        Color.rgb(255, 255, 255)};
                break;

            case TYPE_RAIN_NIGHT:
                mRains = new Rain[RAIN_COUNT];
                mThunder = null;
                colors = new int[]{
                        Color.rgb(182, 142, 82),
                        Color.rgb(88, 92, 113),
                        Color.rgb(255, 255, 255)};
                break;

            case TYPE_THUNDERSTORM:
                mRains = new Rain[RAIN_COUNT];
                mThunder = new Thunder();
                colors = new int[]{
                        Color.rgb(182, 142, 82),
                        Color.rgb(88, 92, 113),
                        Color.rgb(255, 255, 255)};
                break;

            case TYPE_SLEET_DAY:
                mRains = new Rain[SLEET_COUNT];
                mThunder = null;
                colors = new int[] {
                        Color.rgb(128, 197, 255),
                        Color.rgb(185, 222, 255),
                        Color.rgb(255, 255, 255)};
                break;

            case TYPE_SLEET_NIGHT:
                mRains = new Rain[SLEET_COUNT];
                mThunder = null;
                colors = new int[] {
                        Color.rgb(40, 102, 155),
                        Color.rgb(99, 144, 182),
                        Color.rgb(255, 255, 255)};
                break;
        }

        float[] scales = new float[] {0.6F, 0.8F, 1};
        assert mRains != null;
        for (int i = 0; i < mRains.length; i ++) {
            mRains[i] = new Rain(
                    canvasSizes[0],
                    canvasSizes[1],
                    colors[i * 3 / mRains.length],
                    scales[i * 3 / mRains.length]
            );
        }

        mLastRotation3D = INITIAL_ROTATION_3D;
    }

    @Override
    public void updateData(@Size(2) int[] canvasSizes, long interval,
                           float rotation2D, float rotation3D) {

        for (Rain r : mRains) {
            r.move(
                    interval,
                    mLastRotation3D == INITIAL_ROTATION_3D
                            ? 0
                            : rotation3D - mLastRotation3D
            );
        }
        if (mThunder != null) {
            mThunder.shine(interval);
        }
        mLastRotation3D = rotation3D;
    }

    @Override
    public void draw(@Size(2) int[] canvasSizes, Canvas canvas,
                     float scrollRate, float rotation2D, float rotation3D) {

        if (scrollRate < 1) {
            rotation2D += 8;
            canvas.rotate(
                    rotation2D,
                    canvasSizes[0] * 0.5F,
                    canvasSizes[1] * 0.5F);

            for (Rain r : mRains) {
                mPaint.setColor(r.color);
                mPaint.setAlpha((int) ((1 - scrollRate) * 255));
                canvas.drawRoundRect(r.rectF, r.width / 2f, r.width / 2f, mPaint);
            }
            if (mThunder != null) {
                canvas.drawColor(
                        Color.argb(
                                (int) ((1 - scrollRate) * mThunder.alpha * 255 * 0.66),
                                mThunder.r,
                                mThunder.g,
                                mThunder.b));
            }
        }
    }

    @ColorInt
    public static int getThemeColor(Context context, @TypeRule int type) {
        switch (type) {
            case TYPE_RAIN_DAY:
                return Color.rgb(64, 151, 231);

            case TYPE_RAIN_NIGHT:
                return Color.rgb(38, 78, 143);

            case TYPE_SLEET_DAY:
                return Color.rgb(104, 186, 255);

            case TYPE_SLEET_NIGHT:
                return Color.rgb(26, 91, 146);

            case TYPE_THUNDERSTORM:
                return Color.rgb(43, 29, 69);
        }
        return ContextCompat.getColor(context, R.color.colorPrimary);
    }
}
