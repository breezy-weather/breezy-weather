package wangdaye.com.geometricweather.common.ui.widgets.insets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.insets.FitBothSideBarHelper;
import wangdaye.com.geometricweather.common.basic.insets.FitBothSideBarView;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;

public class FitSystemBarRecyclerView extends RecyclerView
        implements FitBothSideBarView {

    private final FitBothSideBarHelper mHelper;
    private boolean mAdaptiveWidthEnabled = true;

    public FitSystemBarRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public FitSystemBarRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FitSystemBarRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.FitSystemBarRecyclerView, defStyleAttr, 0);
        int fitSide = a.getInt(R.styleable.FitSystemBarRecyclerView_rv_side, SIDE_TOP | SIDE_BOTTOM);
        a.recycle();

        mHelper = new FitBothSideBarHelper(this, fitSide);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        return mHelper.onApplyWindowInsets(insets);
    }

    @Override
    public boolean fitSystemWindows(Rect insets) {
        return mHelper.fitSystemWindows(insets);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int viewWidth = getMeasuredWidth();
        int adaptiveWidth = DisplayUtils.getTabletListAdaptiveWidth(getContext(), viewWidth);
        int paddingHorizontal = mAdaptiveWidthEnabled ? ((viewWidth - adaptiveWidth) / 2) : 0;
        setPadding(paddingHorizontal, mHelper.top(), paddingHorizontal, mHelper.bottom());
    }

    public void setAdaptiveWidthEnabled(boolean enabled) {
        mAdaptiveWidthEnabled = enabled;
        requestLayout();
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
