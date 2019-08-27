package wangdaye.com.geometricweather.ui.widget.windowInsets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;

public class ApplyWindowInsetsLayout extends FrameLayout {

    @Nullable private Rect windowInsets = null;
    @Nullable private OnApplyWindowInsetsListener listener = null;

    private Paint paint;

    public ApplyWindowInsetsLayout(Context context) {
        this(context, null);
    }

    public ApplyWindowInsetsLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ApplyWindowInsetsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);

        paint = new Paint();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            paint.setColor(ContextCompat.getColor(getContext(), R.color.colorRoot));
        } else {
            paint.setColor(Color.BLACK);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        GeometricWeather.getInstance().setWindowInsets(
                insets.getSystemWindowInsetLeft(),
                insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(),
                insets.getSystemWindowInsetBottom()
        );
        consumeInsets(
                insets.getSystemWindowInsetLeft(),
                insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(),
                insets.getSystemWindowInsetBottom()
        );
        return super.onApplyWindowInsets(insets);
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        GeometricWeather.getInstance().setWindowInsets(
                insets.left, insets.top, insets.right, insets.bottom);
        consumeInsets(insets.left, insets.top, insets.right, insets.bottom);
        return super.fitSystemWindows(insets);
    }

    private void consumeInsets(int left, int top, int right, int bottom) {
        setPadding(left, 0, right, 0);

        boolean changed = false;
        if (windowInsets == null) {
            changed = true;
            windowInsets = new Rect(left, top, right, bottom);
        } else if (windowInsets.left != left || windowInsets.top != top
                || windowInsets.right != right || windowInsets.bottom != bottom) {
            changed = true;
            windowInsets.set(left, top, right, bottom);
        }
        if (changed && listener != null) {
            listener.onApplyWindowInsets();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (windowInsets == null) {
            return;
        }

        if (windowInsets.left != 0) {
            canvas.drawRect(0, 0, windowInsets.left, getMeasuredHeight(), paint);
        }
        if (windowInsets.right != 0) {
            canvas.drawRect(
                    getMeasuredWidth() - windowInsets.right,
                    0,
                    getMeasuredWidth(),
                    getMeasuredHeight(),
                    paint
            );
        }
    }

    public interface OnApplyWindowInsetsListener {
        void onApplyWindowInsets();
    }

    public void setOnApplyWindowInsetsListener(@Nullable OnApplyWindowInsetsListener l) {
        this.listener = l;
    }
}
