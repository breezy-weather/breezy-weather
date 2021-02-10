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

    private final Paint mPaint;
    private final Xfermode mClearXfermode;

    @ColorInt private final int mCoreColor;

    private float mAlpha;
    private Rect mBounds;

    private float mCoreRadius;
    private float mCoreCenterX, mCoreCenterY;

    private float mShaderRadius;
    private float mShaderCenterX, mShaderCenterY;

    public PixelMoonDrawable() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mClearXfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

        mCoreColor = Color.rgb(180, 138, 255);

        mAlpha = 1;
        mBounds = getBounds();

        ensurePosition(mBounds);
    }

    private void ensurePosition(Rect bounds) {
        float boundSize = Math.min(bounds.width(), bounds.height());
        mCoreRadius = (float) ((Math.sin(Math.PI / 4) * boundSize / 2 + boundSize / 2) / 2 - 2);
        mCoreCenterX = (float) (1.0 * bounds.width() / 2 + bounds.left);
        mCoreCenterY = (float) (1.0 * bounds.height() / 2 + bounds.top);

        mShaderRadius = mCoreRadius * 0.9050f;
        mShaderCenterX = mCoreCenterX + mCoreRadius * 0.5914f;
        mShaderCenterY = mCoreCenterY - mCoreRadius * 0.5932f;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        mBounds = bounds;
        ensurePosition(bounds);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        mPaint.setAlpha((int) (mAlpha * 255));

        int layerId = canvas.saveLayer(
                mBounds.left, mBounds.top, mBounds.right, mBounds.bottom,
                null, Canvas.ALL_SAVE_FLAG
        );

        mPaint.setColor(mCoreColor);
        canvas.drawCircle(mCoreCenterX, mCoreCenterY, mCoreRadius, mPaint);

        mPaint.setXfermode(mClearXfermode);
        canvas.drawCircle(mShaderCenterX, mShaderCenterY, mShaderRadius, mPaint);
        mPaint.setXfermode(null);

        canvas.restoreToCount(layerId);
    }

    @Override
    public void setAlpha(int alpha) {
        mAlpha = alpha;
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public int getIntrinsicWidth() {
        return mBounds.width();
    }

    @Override
    public int getIntrinsicHeight() {
        return mBounds.height();
    }
}
