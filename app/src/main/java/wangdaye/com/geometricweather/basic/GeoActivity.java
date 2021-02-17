package wangdaye.com.geometricweather.basic;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widgets.insets.FitHorizontalSystemBarRootLayout;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.LanguageUtils;
import wangdaye.com.geometricweather.utils.managers.ThemeManager;

/**
 * Geometric weather activity.
 * */

public abstract class GeoActivity extends AppCompatActivity {

    private CoordinatorLayout mSnackbarContainer;
    private FitHorizontalSystemBarRootLayout mFitHorizontalSystemBarRootLayout;

    private @Nullable GeoDialog mTopDialog;

    private boolean mForeground = false;

    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFitHorizontalSystemBarRootLayout = new FitHorizontalSystemBarRootLayout(this);
        mFitHorizontalSystemBarRootLayout.setRootColor(ThemeManager.getInstance(this).getRootColor(this));
        mFitHorizontalSystemBarRootLayout.setLineColor(ThemeManager.getInstance(this).getLineColor(this));

        mSnackbarContainer = new CoordinatorLayout(this);
        mSnackbarContainer.addView(
                mFitHorizontalSystemBarRootLayout,
                new CoordinatorLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

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

        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        ViewGroup decorChild = (ViewGroup) decorView.getChildAt(0);

        decorView.removeView(decorChild);
        if (mSnackbarContainer.getParent() != null) {
            decorView.removeView(mSnackbarContainer);
        }
        decorView.addView(mSnackbarContainer);

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

    public ViewGroup getSnackbarContainer() {
        return mFitHorizontalSystemBarRootLayout;
    }

    public ViewGroup provideSnackbarContainer(boolean[] fromActivity) {
        GeoDialog topDialog = getTopDialog();
        if (topDialog == null) {
            fromActivity[0] = true;
            return getSnackbarContainer();
        } else {
            fromActivity[0] = false;
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
