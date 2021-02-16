package wangdaye.com.geometricweather.basic;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
        if (mFitHorizontalSystemBarRootLayout.getParent() != null) {
            decorView.removeView(mFitHorizontalSystemBarRootLayout);
        }
        decorView.addView(mFitHorizontalSystemBarRootLayout);

        mFitHorizontalSystemBarRootLayout.removeAllViews();
        mFitHorizontalSystemBarRootLayout.addView(decorChild);
    }

    @Override
    protected void onStart() {
        super.onStart();
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

    public abstract View getSnackbarContainer();

    public View provideSnackbarContainer(boolean[] fromActivity) {
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
