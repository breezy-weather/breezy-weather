package wangdaye.com.geometricweather.main.widgets;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.main.utils.MainThemeColorProvider;

public class TrendRecyclerViewScrollBar extends RecyclerView.ItemDecoration {

    private Paint mPaint = null;

    private int mScrollBarWidth;
    private int mScrollBarHeight;

    private boolean mThemeChanged;
    private @ColorInt int mEndPointsColor;
    private @ColorInt int mCenterColor;

    public TrendRecyclerViewScrollBar() {
        super();
    }

    public void resetColor(Location location) {
        mThemeChanged = true;

        mEndPointsColor = MainThemeColorProvider.getColor(location, R.attr.colorMainCardBackground);
        mCenterColor = DisplayUtils.blendColor(
//                lightTheme
//                        ? Color.argb((int) (0.02 * 255), 0, 0, 0)
//                        : Color.argb((int) (0.08 * 255), 0, 0, 0),
                ColorUtils.setAlphaComponent(
                        MainThemeColorProvider.getColor(location, androidx.appcompat.R.attr.colorPrimary),
                        (int) (0.05 * 255)
                ),
                MainThemeColorProvider.getColor(location, R.attr.colorMainCardBackground)
        );
    }

    @Override
    public void onDraw(@NonNull Canvas c,
                       @NonNull RecyclerView parent,
                       @NonNull RecyclerView.State state) {
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

    private boolean consumedThemeChanged() {
        if (mThemeChanged) {
            mThemeChanged = false;
            return true;
        } else {
            return false;
        }
    }
}
