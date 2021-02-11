package wangdaye.com.geometricweather.ui.widgets.insets;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.utils.DisplayUtils;

public class FitBottomSystemBarRecyclerView extends RecyclerView {

    private Rect mWindowInsets = new Rect(0, 0, 0, 0);
    private boolean mAdaptiveWidthEnabled = true;

    public FitBottomSystemBarRecyclerView(@NonNull Context context) {
        super(context);
        ViewCompat.setOnApplyWindowInsetsListener(this, null);
    }

    public FitBottomSystemBarRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        ViewCompat.setOnApplyWindowInsetsListener(this, null);
    }

    public FitBottomSystemBarRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ViewCompat.setOnApplyWindowInsetsListener(this, null);
    }

    public void setmAdaptiveWidthEnabled(boolean enabled) {
        mAdaptiveWidthEnabled = enabled;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public void setOnApplyWindowInsetsListener(OnApplyWindowInsetsListener listener) {
        super.setOnApplyWindowInsetsListener((v, insets) -> {
            if (listener != null) {
                WindowInsets result = listener.onApplyWindowInsets(v, insets);
                fitSystemWindows(
                        new Rect(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom()));
                return result;
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
    protected boolean fitSystemWindows(Rect insets) {
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
                Math.max(paddingHorizontal, mWindowInsets.left),
                0,
                Math.max(paddingHorizontal, mWindowInsets.right),
                mWindowInsets.bottom
        );
    }

    public Rect getWindowInsets() {
        return mWindowInsets;
    }
}
