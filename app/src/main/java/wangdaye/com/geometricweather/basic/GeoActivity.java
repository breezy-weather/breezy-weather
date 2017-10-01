package wangdaye.com.geometricweather.basic;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Geometric weather activity.
 * */

public abstract class GeoActivity extends AppCompatActivity {

    private List<GeoDialogFragment> dialogList;
    private boolean started;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
