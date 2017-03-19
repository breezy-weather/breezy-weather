package wangdaye.com.geometricweather.basic;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.TimeUtils;

/**
 * Geometric activity.
 * */

public abstract class GeoActivity extends AppCompatActivity {
    // widget
    private List<GeoDialogFragment> dialogList;

    // data
    private boolean started;

    /** <br> life cycle. */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GeometricWeather.getInstance().addActivity(this);
        DisplayUtils.setWindowTopColor(this);
        DisplayUtils.setStatusBarTranslate(getWindow());
        DisplayUtils.setNavigationBarColor(this, TimeUtils.getInstance(this).isDayTime());

        this.dialogList = new ArrayList<>();
        this.started = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GeometricWeather.getInstance().removeActivity();
    }

    public void setStarted() {
        started = true;
    }

    public boolean isStarted() {
        return started;
    }

    public abstract View getSnackbarContainer();

    public List<GeoDialogFragment> getDialogList() {
        return dialogList;
    }

    public View provideSnackbarContainer() {
        if (dialogList.size() > 0) {
            return dialogList.get(dialogList.size() - 1).getSnackbarContainer();
        } else {
            return getSnackbarContainer();
        }
    }
}
