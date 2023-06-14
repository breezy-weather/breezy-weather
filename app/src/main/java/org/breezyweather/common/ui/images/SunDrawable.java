package org.breezyweather.common.ui.images;

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
import androidx.annotation.Size;

public class SunDrawable extends Drawable {

    private final Paint mPaint;

    @ColorInt private final int mCoreColor;
    @Size(2) @ColorInt private int[] mHaloColors;

    private float mAlpha;
    private Rect mBounds;

    private float mRadius;
    private float mCX, mCY;

    public SunDrawable() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mCoreColor = Color.rgb(254, 214, 117);
        mHaloColors = new int[] {
                Color.rgb(249, 183, 93),
                Color.rgb(252, 198, 101),
        };

        mAlpha = 1;
        mBounds = getBounds();

        ensurePosition(mBounds);
    }

    private void ensurePosition(Rect bounds) {
        float boundSize = Math.min(bounds.width(), bounds.height());
        mRadius = (float) (Math.sin(Math.PI / 4) * boundSize / 2) - 2;
        mCX = (float) (1.0 * bounds.width() / 2 + bounds.left);
        mCY = (float) (1.0 * bounds.height() / 2 + bounds.top);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        mBounds = bounds;
        ensurePosition(bounds);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        mPaint.setAlpha((int) (mAlpha * 255));

        mPaint.setColor(mHaloColors[0]);
        canvas.drawRect(
                mCX - mRadius,
                mCY - mRadius,
                mCX + mRadius,
                mCY + mRadius,
                mPaint
        );

        mPaint.setColor(mHaloColors[0]);
        int restoreCount = canvas.save();
        canvas.rotate(45, mCX, mCY);
        canvas.drawRect(
                mCX - mRadius,
                mCY - mRadius,
                mCX + mRadius,
                mCY + mRadius,
                mPaint
        );
        canvas.restoreToCount(restoreCount);

        mPaint.setColor(mCoreColor);
        canvas.drawCircle(mCX, mCY, mRadius, mPaint);
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
