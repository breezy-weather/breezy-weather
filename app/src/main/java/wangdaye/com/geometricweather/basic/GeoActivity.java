package wangdaye.com.geometricweather.basic;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widget.windowInsets.ApplyWindowInsetsLayout;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.LanguageUtils;

/**
 * Geometric weather activity.
 * */

public abstract class GeoActivity extends AppCompatActivity {

    private List<GeoDialogFragment> dialogList;
    private boolean foreground;

    @Nullable private ApplyWindowInsetsLayout applyWindowInsetsLayout;
    @Nullable private OnRequestPermissionsResultListener permissionsListener;

    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GeometricWeather.getInstance().addActivity(this);

        LanguageUtils.setLanguage(
                this,
                SettingsOptionManager.getInstance(this).getLanguage().getLocale()
        );

        boolean darkMode = DisplayUtils.isDarkMode(this);
        DisplayUtils.setWindowTopColor(this, 0);
        DisplayUtils.setSystemBarStyle(
                getWindow(), false, false, !darkMode);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DisplayUtils.setNavigationBarColor(
                    this, ContextCompat.getColor(this, R.color.colorRootDark));
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        bindApplyWindowInsetsLayout();
    }

    protected void bindApplyWindowInsetsLayout() {
        applyWindowInsetsLayout = new ApplyWindowInsetsLayout(this);

        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        ViewGroup contentView = (ViewGroup) decorView.getChildAt(0);

        decorView.removeView(contentView);
        applyWindowInsetsLayout.addView(contentView);

        decorView.addView(applyWindowInsetsLayout, 0);
    }

    @Nullable
    protected ApplyWindowInsetsLayout getApplyWindowInsetsLayout() {
        return applyWindowInsetsLayout;
    }

    @CallSuper
    @Override
    protected void onResume() {
        super.onResume();
        foreground = true;
    }

    @CallSuper
    @Override
    protected void onPause() {
        super.onPause();
        foreground = false;
    }

    @CallSuper
    @Override
    protected void onDestroy() {
        super.onDestroy();
        GeometricWeather.getInstance().removeActivity(this);
    }

    public View provideSnackbarContainer() {
        if (getDialogList().size() > 0) {
            return getDialogList().get(getDialogList().size() - 1).getSnackbarContainer();
        } else {
            return getSnackbarContainer();
        }
    }

    public abstract View getSnackbarContainer();

    public boolean isForeground() {
        return foreground;
    }

    public List<GeoDialogFragment> getDialogList() {
        if (dialogList == null) {
            dialogList = new ArrayList<>();
        }
        return dialogList;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestPermissions(@NonNull String[] permissions, int requestCode,
                                   @Nullable OnRequestPermissionsResultListener l) {
        permissionsListener = l;
        requestPermissions(permissions, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permission, @NonNull int[] grantResult) {
        super.onRequestPermissionsResult(requestCode, permission, grantResult);
        if (permissionsListener != null) {
            permissionsListener.onRequestPermissionsResult(requestCode, permission, grantResult);
            permissionsListener = null;
        }
    }

    public interface OnRequestPermissionsResultListener {
        void onRequestPermissionsResult(int requestCode,
                                        @NonNull String[] permission, @NonNull int[] grantResult);
    }
}
