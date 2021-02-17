package wangdaye.com.geometricweather.common.ui.widgets.trend;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.common.utils.managers.ThemeManager;

public class TrendRecyclerViewScrollBar extends RecyclerView.ItemDecoration {

    private Paint mPaint = null;
    private int mScrollBarWidth;
    private int mScrollBarHeight;

    private @Nullable Boolean mLightTheme;
    private boolean mThemeChanged;
    private @ColorInt int mEndPointsColor;
    private @ColorInt int mCenterColor;

    public TrendRecyclerViewScrollBar(Context context) {
        mLightTheme = null;
        ensureColor(context);
    }

    @Override
    public void onDraw(@NonNull Canvas c,
                       @NonNull RecyclerView parent,
                       @NonNull RecyclerView.State state) {
        ensureColor(parent.getContext());

        if (mPaint == null && parent.getChildCount() > 0) {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mScrollBarWidth = parent.getChildAt(0).getMeasuredWidth();
            mScrollBarHeight = parent.getChildAt(0).getMeasuredHeight();
        }

        if (mPaint != null) {
            if (consumedThemeChanged()) {
                mPaint.setShader(
                        new LinearGradient(
                                0,
                                0,
                                0,
                                mScrollBarHeight / 2f,
                                mEndPointsColor, mCenterColor,
                                Shader.TileMode.MIRROR
                        )
                );
            }

            int extent = parent.computeHorizontalScrollExtent();
            int range = parent.computeHorizontalScrollRange();
            int offset = parent.computeHorizontalScrollOffset();

            float offsetPercent = 1f * offset / (range - extent);

            float scrollBarOffsetX = offsetPercent * (parent.getMeasuredWidth() - mScrollBarWidth);
            c.drawRect(
                    scrollBarOffsetX,
                    0,
                    mScrollBarWidth + scrollBarOffsetX,
                    mScrollBarHeight,
                    mPaint
            );
        }
    }

    private void ensureColor(Context context) {
        boolean lightTheme = ThemeManager.getInstance(context).isLightTheme();

        if (mLightTheme == null || mLightTheme != lightTheme) {
            mLightTheme = lightTheme;
            mThemeChanged = true;

            mEndPointsColor = ThemeManager.getInstance(context).getRootColor(context);
            mCenterColor = DisplayUtils.blendColor(
                    lightTheme
                            ? Color.argb((int) (0.02 * 255), 0, 0, 0)
                            : Color.argb((int) (0.08 * 255), 0, 0, 0),
                    mEndPointsColor
            );
        }
    }

    private boolean consumedThemeChanged() {
        if (mThemeChanged) {
            mThemeChanged = false;
            return true;
        } else {
            return false;
        }
    }
}
