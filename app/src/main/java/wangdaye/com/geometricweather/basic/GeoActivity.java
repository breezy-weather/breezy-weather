package wangdaye.com.geometricweather.basic;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.LanguageUtils;

/**
 * Geometric weather activity.
 * */

public abstract class GeoActivity extends AppCompatActivity {

    private boolean foreground;

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
        DisplayUtils.setSystemBarStyle(this, getWindow(),
                false, false, true, !darkMode);
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

    public abstract View getSnackbarContainer();

    public boolean isForeground() {
        return foreground;
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
