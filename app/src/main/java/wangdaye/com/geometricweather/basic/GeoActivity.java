package wangdaye.com.geometricweather.basic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.LanguageUtils;

/**
 * Geometric weather activity.
 * */

public abstract class GeoActivity extends AppCompatActivity {

    private List<GeoDialogFragment> dialogList;
    private boolean started;

    private BroadcastReceiver localeChangedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                    && !TextUtils.isEmpty(intent.getAction())
                    && intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
                LanguageUtils.setLanguage(
                        GeoActivity.this,
                        GeometricWeather.getInstance().getLanguage());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerReceiver(localeChangedReceiver, new IntentFilter(Intent.ACTION_LOCALE_CHANGED));
        LanguageUtils.setLanguage(this, GeometricWeather.getInstance().getLanguage());

        GeometricWeather.getInstance().addActivity(this);
        DisplayUtils.setWindowTopColor(this, 0);
        DisplayUtils.setStatusBarTranslate(getWindow());
        DisplayUtils.setNavigationBarColor(this, 0);

        this.dialogList = new ArrayList<>();
        this.started = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GeometricWeather.getInstance().removeActivity();
        unregisterReceiver(localeChangedReceiver);
    }

    public View provideSnackbarContainer() {
        if (dialogList.size() > 0) {
            return dialogList.get(dialogList.size() - 1).getSnackbarContainer();
        } else {
            return getSnackbarContainer();
        }
    }

    public abstract View getSnackbarContainer();

    public boolean isStarted() {
        return started;
    }

    public void setStarted() {
        started = true;
    }

    public List<GeoDialogFragment> getDialogList() {
        return dialogList;
    }
}
