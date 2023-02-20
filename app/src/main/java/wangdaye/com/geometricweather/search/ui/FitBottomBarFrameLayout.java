package wangdaye.com.geometricweather.search.ui;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import wangdaye.com.geometricweather.common.basic.insets.FitBothSideBarHelper;
import wangdaye.com.geometricweather.common.basic.insets.FitBothSideBarView;

public class FitBottomBarFrameLayout extends FrameLayout implements FitBothSideBarView {

    private final FitBothSideBarHelper mHelper;

    public FitBottomBarFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public FitBottomBarFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FitBottomBarFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                                   int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHelper = new FitBothSideBarHelper(this, FitBothSideBarView.SIDE_BOTTOM);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        return mHelper.onApplyWindowInsets(insets, () -> setPadding(
                0, 0, 0, mHelper.bottom()));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void addFitSide(@FitSide int side) {
        mHelper.addFitSide(side);
    }

    @Override
    public void removeFitSide(@FitSide int side) {
        mHelper.removeFitSide(side);
    }

    @Override
    public void setFitSystemBarEnabled(boolean top, boolean bottom) {
        mHelper.setFitSystemBarEnabled(top, bottom);
    }

    @Override
    public int getTopWindowInset() {
        return mHelper.top();
    }

    @Override
    public int getBottomWindowInset() {
        return mHelper.bottom();
    }
}
