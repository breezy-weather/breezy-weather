package wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.implementor;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.annotation.ColorInt;
import androidx.annotation.Size;

import wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.MaterialWeatherView;

/**
 * Clear day implementor.
 * */

public class SunImplementor extends MaterialWeatherView.WeatherAnimationImplementor {

    private final Paint mPaint;
    private final float[] mAngles;
    private final float[] mUnitSizes;

    public SunImplementor(@Size(2) int[] canvasSizes) {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.rgb(253, 84, 17));

        mAngles = new float[3];

        mUnitSizes = new float[3];
        mUnitSizes[0] = (float) (0.5 * 0.47 * canvasSizes[0]);
        mUnitSizes[1] = (float) (1.7794 * mUnitSizes[0]);
        mUnitSizes[2] = (float) (3.0594 * mUnitSizes[0]);
    }

    @Override
    public void updateData(@Size(2) int[] canvasSizes, long interval,
                           float rotation2D, float rotation3D) {
        for (int i = 0; i < mAngles.length; i ++) {
            mAngles[i] = (float) ((mAngles[i] + (90.0 / (3000 + 1000 * i) * interval)) % 90);
        }
    }

    @Override
    public void draw(@Size(2) int[] canvasSizes, Canvas canvas,
                     float scrollRate, float rotation2D, float rotation3D) {
        if (scrollRate < 1) {
            float deltaX = (float) (Math.sin(rotation2D * Math.PI / 180.0) * 0.3 * canvasSizes[0]);
            float deltaY = (float) (Math.sin(rotation3D * Math.PI / 180.0) * -0.3 * canvasSizes[0]);

            canvas.translate(
                    canvasSizes[0] + deltaX,
                    (float) (0.0333 * canvasSizes[0] + deltaY));

            mPaint.setAlpha((int) ((1 - scrollRate) * 255 * 0.40));
            canvas.rotate(mAngles[0]);
            for (int i = 0; i < 4; i ++) {
                canvas.drawRect(-mUnitSizes[0], -mUnitSizes[0], mUnitSizes[0], mUnitSizes[0], mPaint);
                canvas.rotate(22.5F);
            }
            canvas.rotate(-90 - mAngles[0]);

            mPaint.setAlpha((int) ((1 - scrollRate) * 255 * 0.16));
            canvas.rotate(mAngles[1]);
            for (int i = 0; i < 4; i ++) {
                canvas.drawRect(-mUnitSizes[1], -mUnitSizes[1], mUnitSizes[1], mUnitSizes[1], mPaint);
                canvas.rotate(22.5F);
            }
            canvas.rotate(-90 - mAngles[1]);

            mPaint.setAlpha((int) ((1 - scrollRate) * 255 * 0.08));
            canvas.rotate(mAngles[2]);
            for (int i = 0; i < 4; i ++) {
                canvas.drawRect(-mUnitSizes[2], -mUnitSizes[2], mUnitSizes[2], mUnitSizes[2], mPaint);
                canvas.rotate(22.5F);
            }
            canvas.rotate(-90 - mAngles[2]);
        }
    }

    @ColorInt
    public static int getThemeColor() {
        return Color.rgb(253, 188, 76);
    }
}
