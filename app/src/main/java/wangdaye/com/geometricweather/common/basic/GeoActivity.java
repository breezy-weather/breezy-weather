package wangdaye.com.geometricweather.common.basic;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.snackbar.SnackbarContainer;
import wangdaye.com.geometricweather.common.basic.insets.FitHorizontalSystemBarRootLayout;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.common.utils.LanguageUtils;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;

/**
 * Geometric weather activity.
 * */

public abstract class GeoActivity extends AppCompatActivity {

    FitHorizontalSystemBarRootLayout fitHorizontalSystemBarRootLayout;

    private @Nullable GeoDialog mTopDialog;

    private boolean mForeground = false;

    private static class KeyboardResizeBugWorkaround {

        // For more information, see https://issuetracker.google.com/issues/36911528
        // To use this class, simply invoke assistActivity() on an Activity that already has its content view set.

        public static void assistActivity (GeoActivity activity) {
            new KeyboardResizeBugWorkaround(activity);
        }

        private final FitHorizontalSystemBarRootLayout mRoot;
        private final ViewGroup.LayoutParams mRootParams;
        private int mUsableHeightPrevious;

        private KeyboardResizeBugWorkaround(GeoActivity activity) {
            mRoot = activity.fitHorizontalSystemBarRootLayout;
            mRoot.getViewTreeObserver().addOnGlobalLayoutListener(this::possiblyResizeChildOfContent);
            mRootParams = mRoot.getLayoutParams();
        }

        private void possiblyResizeChildOfContent() {
            int usableHeightNow = computeUsableHeight();
            if (usableHeightNow != mUsableHeightPrevious) {

                int screenHeight = mRoot.getRootView().getHeight();
                boolean keyboardExpanded;

                if (screenHeight - usableHeightNow > screenHeight / 5) {
                    // keyboard probably just became visible.
                    keyboardExpanded = true;
                    mRootParams.height = usableHeightNow;
                } else {
                    // keyboard probably just became hidden.
                    keyboardExpanded = false;
                    mRootParams.height = screenHeight;
                }
                mUsableHeightPrevious = usableHeightNow;

                mRoot.setFitKeyboardExpanded(keyboardExpanded);
            }
        }

        private int computeUsableHeight() {
            Rect r = new Rect();
            DisplayUtils.getVisibleDisplayFrame(mRoot, r);
            return r.bottom; // - r.top; --> Do not reduce the height of status bar.
        }
    }

    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fitHorizontalSystemBarRootLayout = new FitHorizontalSystemBarRootLayout(this);
        fitHorizontalSystemBarRootLayout.setRootColor(ContextCompat.getColor(this, R.color.colorRoot));
        fitHorizontalSystemBarRootLayout.setLineColor(ContextCompat.getColor(this, R.color.colorLine));

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

        // decor -> fit horizontal system bar -> decor child.

        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        ViewGroup decorChild = (ViewGroup) decorView.getChildAt(0);

        decorView.removeView(decorChild);
        decorView.addView(fitHorizontalSystemBarRootLayout);

        fitHorizontalSystemBarRootLayout.removeAllViews();
        fitHorizontalSystemBarRootLayout.addView(decorChild);

        KeyboardResizeBugWorkaround.assistActivity(this);
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
        return fitHorizontalSystemBarRootLayout;
    }
}
