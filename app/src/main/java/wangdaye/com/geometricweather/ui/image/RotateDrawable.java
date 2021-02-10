package wangdaye.com.geometricweather.ui.image;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RotateDrawable extends Drawable {

    private @Nullable final Drawable mDrawable;
    private float mDegree;

    public RotateDrawable(@Nullable Drawable drawable) {
        mDrawable = drawable;
        mDegree = 0;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (mDrawable == null) {
            return;
        }

        final Rect innerBounds = mDrawable.getBounds();
        final float cx = (innerBounds.right - innerBounds.left) / 2f;
        final float cy = (innerBounds.bottom - innerBounds.top) / 2f;

        final int saveCount = canvas.save();
        canvas.rotate(mDegree, cx + innerBounds.left, cy + innerBounds.top);
        mDrawable.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override
    public void setAlpha(int alpha) {
        if (mDrawable != null) {
            mDrawable.setAlpha(alpha);
        }
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        if (mDrawable != null) {
            mDrawable.setColorFilter(colorFilter);
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        if (mDrawable != null) {
            mDrawable.setBounds(left, top, right, bottom);
        }
    }

    @Override
    public void setBounds(@NonNull Rect bounds) {
        setBounds(bounds.left, bounds.top, bounds.right, bounds.bottom);
    }

    public void rotate(float degree) {
        mDegree = degree;
    }
}
