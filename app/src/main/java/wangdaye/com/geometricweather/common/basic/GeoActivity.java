package wangdaye.com.geometricweather.common.basic;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Queue;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.snackbar.SnackbarContainer;
import wangdaye.com.geometricweather.common.ui.widgets.insets.FitHorizontalSystemBarRootLayout;
import wangdaye.com.geometricweather.common.ui.widgets.insets.both.FitBothSideBarView;
import wangdaye.com.geometricweather.common.ui.widgets.insets.both.FitSystemBarCoordinatorLayout;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.common.utils.LanguageUtils;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;

/**
 * Geometric weather activity.
 * */

public abstract class GeoActivity extends AppCompatActivity {

    private FitHorizontalSystemBarRootLayout mFitHorizontalSystemBarRootLayout;

    private @Nullable GeoDialog mTopDialog;

    private boolean mForeground = false;

    @Nullable OnKeyboardStateChangedListener mKeyboardListener;

    private static class KeyboardResizeBugWorkaround {

        // For more information, see https://issuetracker.google.com/issues/36911528
        // To use this class, simply invoke assistActivity() on an Activity that already has its content view set.

        public static void assistActivity (GeoActivity activity) {
            new KeyboardResizeBugWorkaround(activity);
        }

        private final WeakReference<GeoActivity> mHost;

        private final View mContentChild;
        private final FrameLayout.LayoutParams mContentChildParams;
        private int mUsableHeightPrevious;

        private KeyboardResizeBugWorkaround(GeoActivity activity) {
            mHost = new WeakReference<>(activity);

            FrameLayout content = activity.findViewById(android.R.id.content);
            mContentChild = content.getChildAt(0);
            mContentChild.getViewTreeObserver().addOnGlobalLayoutListener(this::possiblyResizeChildOfContent);
            mContentChildParams = (FrameLayout.LayoutParams) mContentChild.getLayoutParams();
        }

        private void possiblyResizeChildOfContent() {
            int usableHeightNow = computeUsableHeight();
            if (usableHeightNow != mUsableHeightPrevious) {
                int contentViewHeight = mContentChild.getRootView().getHeight();
                if (contentViewHeight - usableHeightNow > contentViewHeight / 5) {
                    // keyboard probably just became visible.
                    mContentChildParams.height = usableHeightNow;
                } else {
                    // keyboard probably just became hidden.
                    mContentChildParams.height = contentViewHeight;
                }
                mContentChild.requestLayout();
                mUsableHeightPrevious = usableHeightNow;

                setChildrenFitBottomBarEnabled(mContentChild,
                        mContentChildParams.height != usableHeightNow);
                notifyListener(mContentChildParams.height == usableHeightNow);
            }
        }

        private int computeUsableHeight() {
            Rect r = new Rect();
            mContentChild.getWindowVisibleDisplayFrame(r);
            return r.bottom; // - r.top; --> Do not reduce the height of status bar.
        }

        private void notifyListener(boolean expanded) {
            GeoActivity a = mHost.get();
            if (a != null && a.mKeyboardListener != null) {
                a.mKeyboardListener.onKeyboardStateChanged(expanded);
            }
        }

        private void setChildrenFitBottomBarEnabled(View view, boolean enabled) {
            Queue<View> queue = new LinkedList<>();
            queue.add(view);

            while (!queue.isEmpty()) {
                view = queue.poll();

                if (view instanceof FitBothSideBarView) {
                    ((FitBothSideBarView) view).setFitSystemBarEnabled(true, enabled);

                    if (!(view instanceof FitSystemBarCoordinatorLayout)) {
                        continue;
                    }
                }

                if (view instanceof ViewGroup) {
                    for (int i = 0; i < ((ViewGroup) view).getChildCount(); i ++) {
                        queue.add(((ViewGroup) view).getChildAt(i));
                    }
                }
            }
        }
    }

    public interface OnKeyboardStateChangedListener {
        void onKeyboardStateChanged(boolean expanded);
    }

    public void setOnKeyboardStateChangedListener(@Nullable OnKeyboardStateChangedListener l) {
        mKeyboardListener = l;
    }

    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFitHorizontalSystemBarRootLayout = new FitHorizontalSystemBarRootLayout(this);
        mFitHorizontalSystemBarRootLayout.setRootColor(ContextCompat.getColor(this, R.color.colorRoot));
        mFitHorizontalSystemBarRootLayout.setLineColor(ContextCompat.getColor(this, R.color.colorLine));

        GeometricWeather.getInstance().addActivity(this);

        LanguageUtils.setLanguage(
                this,
                SettingsOptionManager.getInstance(this).getLanguage().getLocale()
        );

        boolean darkMode = DisplayUtils.isDarkMode(this);
        DisplayUtils.setSystemBarStyle(this, getWindow(),
                false, false, true, !darkMode);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        KeyboardResizeBugWorkaround.assistActivity(this);

        // decor -> snackbar container -> fit horizontal system bar -> decor child.

        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        ViewGroup decorChild = (ViewGroup) decorView.getChildAt(0);

        decorView.removeView(decorChild);
        decorView.addView(
                mFitHorizontalSystemBarRootLayout,
                new CoordinatorLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        mFitHorizontalSystemBarRootLayout.removeAllViews();
        mFitHorizontalSystemBarRootLayout.addView(decorChild);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        GeometricWeather.getInstance().setTopActivity(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        GeometricWeather.getInstance().setTopActivity(this);
    }

    @CallSuper
    @Override
    protected void onResume() {
        super.onResume();
        mForeground = true;
        GeometricWeather.getInstance().setTopActivity(this);
    }

    @CallSuper
    @Override
    protected void onPause() {
        super.onPause();
        mForeground = false;
        GeometricWeather.getInstance().checkToCleanTopActivity(this);
    }

    @CallSuper
    @Override
    protected void onDestroy() {
        super.onDestroy();
        GeometricWeather.getInstance().removeActivity(this);
    }

    public SnackbarContainer getSnackbarContainer() {
        return new SnackbarContainer(
                this,
                (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0),
                true
        );
    }

    public final SnackbarContainer provideSnackbarContainer() {
        GeoDialog topDialog = getTopDialog();
        if (topDialog == null) {
            return getSnackbarContainer();
        } else {
            return topDialog.getSnackbarContainer();
        }
    }

    public boolean isForeground() {
        return mForeground;
    }

    @Nullable
    private GeoDialog getTopDialog() {
        return mTopDialog;
    }

    public void setTopDialog(@NonNull GeoDialog d) {
        mTopDialog = d;
    }

    public void checkToCleanTopDialog(@NonNull GeoDialog d) {
        if (mTopDialog == d) {
            mTopDialog = null;
        }
    }

    public FitHorizontalSystemBarRootLayout getFitHorizontalSystemBarRootLayout() {
        return mFitHorizontalSystemBarRootLayout;
    }
}
