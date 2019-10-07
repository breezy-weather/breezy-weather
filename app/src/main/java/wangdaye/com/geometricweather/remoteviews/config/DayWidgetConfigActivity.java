package wangdaye.com.geometricweather.remoteviews.config;

import android.view.View;
import android.widget.RemoteViews;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.remoteviews.presenter.DayWidgetIMP;

/**
 * Day widget config activity.
 * */

public class DayWidgetConfigActivity extends AbstractWidgetConfigActivity {

    @Override
    public void initView() {
        super.initView();
        clockFontContainer.setVisibility(View.GONE);
    }

    @Override
    public RemoteViews getRemoteViews() {
        return DayWidgetIMP.getRemoteViews(
                this, getLocationNow(),
                viewTypeValueNow, cardStyleValueNow, cardAlpha, textColorValueNow, textSize,
                hideSubtitle, subtitleDataValueNow
        );
    }

    @Override
    public String getSharedPreferencesName() {
        return getString(R.string.sp_widget_day_setting);
    }
}