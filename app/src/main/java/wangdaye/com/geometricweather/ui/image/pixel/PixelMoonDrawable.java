package wangdaye.com.geometricweather.ui.image.pixel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PixelMoonDrawable extends Drawable {

    private Paint paint;
    private Xfermode clearXfermode;

    @ColorInt private int coreColor;

    private float alpha;
    private Rect bounds;

    private float coreRadius;
    private float coreCenterX, coreCenterY;

    private float shaderRadius;
    private float shaderCenterX, shaderCenterY;

    public PixelMoonDrawable() {
        this.paint = new Paint();
        paint.setAntiAlias(true);

        this.clearXfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

        this.coreColor = Color.rgb(180, 138, 255);

        this.alpha = 1;
        this.bounds = getBounds();

        ensurePosition(bounds);
    }

    private void ensurePosition(Rect bounds) {
        float boundSize = Math.min(bounds.width(), bounds.height());
        coreRadius = (float) ((Math.sin(Math.PI / 4) * boundSize / 2 + boundSize / 2) / 2 - 2);
        coreCenterX = (float) (1.0 * bounds.width() / 2 + bounds.left);
        coreCenterY = (float) (1.0 * bounds.height() / 2 + bounds.top);

        shaderRadius = coreRadius * 0.9050f;
        shaderCenterX = coreCenterX + coreRadius * 0.5914f;
        shaderCenterY = coreCenterY - coreRadius * 0.5932f;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        this.bounds = bounds;
        ensurePosition(bounds);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        paint.setAlpha((int) (alpha * 255));

        int layerId = canvas.saveLayer(
                bounds.left, bounds.top, bounds.right, bounds.bottom,
                null, Canvas.ALL_SAVE_FLAG
        );

        paint.setColor(coreColor);
        canvas.drawCircle(coreCenterX, coreCenterY, coreRadius, paint);

        paint.setXfermode(clearXfermode);
        canvas.drawCircle(shaderCenterX, shaderCenterY, shaderRadius, paint);
        paint.setXfermode(null);

        canvas.restoreToCount(layerId);
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
