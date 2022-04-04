package wangdaye.com.geometricweather.common.ui.widgets.insets;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;

import com.google.android.material.appbar.AppBarLayout;

import wangdaye.com.geometricweather.common.basic.insets.FitBothSideBarHelper;
import wangdaye.com.geometricweather.common.basic.insets.FitBothSideBarView;

public class FitSystemBarAppBarLayout extends AppBarLayout
        implements FitBothSideBarView {

    private final FitBothSideBarHelper mHelper;

    public FitSystemBarAppBarLayout(@NonNull Context context) {
        this(context, null);
    }

    public FitSystemBarAppBarLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FitSystemBarAppBarLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ViewCompat.setOnApplyWindowInsetsListener(this, null);

        mHelper = new FitBothSideBarHelper(this, SIDE_TOP);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        return mHelper.onApplyWindowInsets(insets, this::fitSystemBar);
    }

    private void fitSystemBar() {
        setPadding(0, mHelper.top(), 0, 0);
    }

    @Override
    public void addFitSide(@FitSide int side) {
        // do nothing.
    }

    @Override
    public void removeFitSide(@FitSide int side) {
        // do nothing.
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
        return 0;
    }
}
