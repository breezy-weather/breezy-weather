package wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.annotation.ColorInt;
import androidx.annotation.Size;

import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.MaterialWeatherView;

/**
 * Clear day implementor.
 * */

public class SunImplementor extends MaterialWeatherView.WeatherAnimationImplementor {

    private Paint paint;
    private float[] angles;
    private float[] unitSizes;

    public SunImplementor(@Size(2) int[] canvasSizes) {
        this.paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(Color.rgb(253, 84, 17));

        this.angles = new float[3];

        this.unitSizes = new float[3];
        unitSizes[0] = (float) (0.5 * 0.47 * canvasSizes[0]);
        unitSizes[1] = (float) (1.7794 * unitSizes[0]);
        unitSizes[2] = (float) (3.0594 * unitSizes[0]);
    }

    @Override
    public void updateData(@Size(2) int[] canvasSizes, long interval,
                           float rotation2D, float rotation3D) {
        for (int i = 0; i < angles.length; i ++) {
            angles[i] = (float) ((angles[i] + (90.0 / (3000 + 1000 * i) * interval)) % 90);
        }
    }

    @Override
    public void draw(@Size(2) int[] canvasSizes, Canvas canvas,
                     float displayRate, float scrollRate, float rotation2D, float rotation3D) {

        canvas.drawColor(Color.argb((int) (displayRate * 255), 253, 188, 76));

        if (scrollRate < 1) {
            float deltaX = (float) (Math.sin(rotation2D * Math.PI / 180.0) * 0.3 * canvasSizes[0]);
            float deltaY = (float) (Math.sin(rotation3D * Math.PI / 180.0) * -0.3 * canvasSizes[0]);

            canvas.translate(
                    canvasSizes[0] + deltaX,
                    (float) (0.0333 * canvasSizes[0] + deltaY));

            paint.setAlpha((int) (displayRate * (1 - scrollRate) * 255 * 0.40));
            canvas.rotate(angles[0]);
            for (int i = 0; i < 4; i ++) {
                canvas.drawRect(-unitSizes[0], -unitSizes[0], unitSizes[0], unitSizes[0], paint);
                canvas.rotate(22.5F);
            }
            canvas.rotate(-90 - angles[0]);

            paint.setAlpha((int) (displayRate * (1 - scrollRate) * 255 * 0.16));
            canvas.rotate(angles[1]);
            for (int i = 0; i < 4; i ++) {
                canvas.drawRect(-unitSizes[1], -unitSizes[1], unitSizes[1], unitSizes[1], paint);
                canvas.rotate(22.5F);
            }
            canvas.rotate(-90 - angles[1]);

            paint.setAlpha((int) (displayRate * (1 - scrollRate) * 255 * 0.08));
            canvas.rotate(angles[2]);
            for (int i = 0; i < 4; i ++) {
                canvas.drawRect(-unitSizes[2], -unitSizes[2], unitSizes[2], unitSizes[2], paint);
                canvas.rotate(22.5F);
            }
            canvas.rotate(-90 - angles[2]);
        }
    }

    @ColorInt
    public static int getThemeColor() {
        return Color.rgb(253, 188, 76);
    }
}
