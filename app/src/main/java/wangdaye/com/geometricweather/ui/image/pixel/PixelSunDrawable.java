package wangdaye.com.geometricweather.ui.image.pixel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PixelSunDrawable extends Drawable {

    private Paint paint;

    @ColorInt private int color;

    private float alpha;
    private Rect bounds;

    private float radius;
    private float cx, cy;

    public PixelSunDrawable() {
        this.paint = new Paint();
        paint.setAntiAlias(true);

        this.color = Color.rgb(255, 215, 5);

        this.alpha = 1;
        this.bounds = getBounds();

        ensurePosition(bounds);
    }

    private void ensurePosition(Rect bounds) {
        float boundSize = Math.min(bounds.width(), bounds.height());
        radius = (float) ((Math.sin(Math.PI / 4) * boundSize / 2 + boundSize / 2) / 2 - 2);
        cx = (float) (1.0 * bounds.width() / 2 + bounds.left);
        cy = (float) (1.0 * bounds.height() / 2 + bounds.top);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        this.bounds = bounds;
        ensurePosition(bounds);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        paint.setAlpha((int) (alpha * 255));

        paint.setColor(color);
        canvas.drawCircle(cx, cy, radius, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public int getIntrinsicWidth() {
        return bounds.width();
    }

    @Override
    public int getIntrinsicHeight() {
        return bounds.height();
    }
}
