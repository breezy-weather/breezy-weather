package wangdaye.com.geometricweather.common.ui.widgets.insets.both;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.ui.widgets.insets.Utils;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;

public class FitSystemBarNestedScrollView extends NestedScrollView
        implements FitBothSideBarView {

    private Rect mWindowInsets = new Rect(0, 0, 0, 0);

    private boolean mAdaptiveWidthEnabled = true;
    private @FitSide int mFitSide;
    private boolean mFitTopSideEnabled = true;
    private boolean mFitBottomSideEnabled = true;

    public FitSystemBarNestedScrollView(@NonNull Context context) {
        this(context, null);
    }

    public FitSystemBarNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FitSystemBarNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.FitSystemBarNestedScrollView, defStyleAttr, 0);
        mFitSide = a.getInt(R.styleable.FitSystemBarNestedScrollView_sv_side, SIDE_TOP | SIDE_BOTTOM);
        a.recycle();

        ViewCompat.setOnApplyWindowInsetsListener(this, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public void setOnApplyWindowInsetsListener(OnApplyWindowInsetsListener listener) {
        super.setOnApplyWindowInsetsListener((v, insets) -> {
            if (listener != null) {
                return listener.onApplyWindowInsets(v, insets);
            }

            Rect waterfull = Utils.getWaterfullInsets(insets);
            fitSystemWindows(
                    new Rect(
                            insets.getSystemWindowInsetLeft() + waterfull.left,
                            insets.getSystemWindowInsetTop() + waterfull.top,
                            insets.getSystemWindowInsetRight() + waterfull.right,
                            insets.getSystemWindowInsetBottom() + waterfull.bottom
                    )
            );
            return insets;
        });
    }

    @Override
    public boolean fitSystemWindows(Rect insets) {
        mWindowInsets = insets;
        requestLayout();
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int viewWidth = getMeasuredWidth();
        int adaptiveWidth = DisplayUtils.getTabletListAdaptiveWidth(getContext(), viewWidth);
        int paddingHorizontal = mAdaptiveWidthEnabled ? ((viewWidth - adaptiveWidth) / 2) : 0;
        setPadding(
                paddingHorizontal,
                ((mFitSide & SIDE_TOP) != 0 && mFitTopSideEnabled) ? mWindowInsets.top : 0,
                paddingHorizontal,
                ((mFitSide & SIDE_BOTTOM) != 0 && mFitBottomSideEnabled) ? mWindowInsets.bottom : 0
        );
    }

    public Rect getWindowInsets() {
        return mWindowInsets;
    }

    public void setAdaptiveWidthEnabled(boolean enabled) {
        mAdaptiveWidthEnabled = enabled;
        requestLayout();
    }

    public void addFitSide(@FitSide int side) {
        if ((mFitSide & side) != 0) {
            mFitSide |= side;
            requestLayout();
        }
    }

    public void removeFitSide(@FitSide int side) {
        if ((mFitSide & side) != 0) {
            mFitSide |= side;
            requestLayout();
        }
    }

    @Override
    public void setFitSystemBarEnabled(boolean top, boolean bottom) {
        if (mFitTopSideEnabled != top || mFitBottomSideEnabled != bottom) {
            mFitTopSideEnabled = top;
            mFitBottomSideEnabled = bottom;
            requestLayout();
        }
    }
}
