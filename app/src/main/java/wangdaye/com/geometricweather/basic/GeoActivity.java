package wangdaye.com.geometricweather.basic;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

    private @Nullable Set<GeoDialog> mDialogSet;

    private boolean mStarted = false;
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
        mStarted = true;
    }

    @CallSuper
    @Override
    protected void onResume() {
        super.onResume();
        mForeground = true;
    }

    @CallSuper
    @Override
    protected void onPause() {
        super.onPause();
        mForeground = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mStarted = false;
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

    public boolean isStarted() {
        return mStarted;
    }

    public boolean isForeground() {
        return mForeground;
    }

    public void addDialog(GeoDialog d) {
        if (mDialogSet == null) {
            mDialogSet = new HashSet<>();
        }
        mDialogSet.add(d);
    }

    public void removeDialog(GeoDialog d) {
        if (mDialogSet == null) {
            mDialogSet = new HashSet<>();
        }
        mDialogSet.remove(d);
    }

    @Nullable
    private GeoDialog getTopDialog() {
        if (mDialogSet == null) {
            mDialogSet = new HashSet<>();
        }

        for (GeoDialog dialog : mDialogSet) {
            if (dialog.isForeground()) {
                return dialog;
            }
        }
        return null;
    }

    protected Set<GeoDialog> getDialogSet() {
        if (mDialogSet == null) {
            mDialogSet = new HashSet<>();
        }
        return Collections.unmodifiableSet(mDialogSet);
    }

    public FitHorizontalSystemBarRootLayout getFitHorizontalSystemBarRootLayout() {
        return mFitHorizontalSystemBarRootLayout;
    }
}
