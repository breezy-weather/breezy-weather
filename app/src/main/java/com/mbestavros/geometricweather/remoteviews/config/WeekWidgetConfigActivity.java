package com.mbestavros.geometricweather.remoteviews.config;

import android.view.View;
import android.widget.RemoteViews;

import com.mbestavros.geometricweather.R;
import com.mbestavros.geometricweather.remoteviews.presenter.WeekWidgetIMP;

/**
 * Week widget config activity.
 * */

public class WeekWidgetConfigActivity extends AbstractWidgetConfigActivity {

    @Override
    public void initData() {
        super.initData();
        String[] widgetStyles = getResources().getStringArray(R.array.week_widget_styles);
        String[] widgetStyleValues = getResources().getStringArray(R.array.week_widget_style_values);

        this.viewTypeValueNow = "5_days";
        this.viewTypes = new String[] {
                widgetStyles[0],
                widgetStyles[1]
        };
        this.viewTypeValues = new String[] {
                widgetStyleValues[0],
                widgetStyleValues[1]
        };
    }

    @Override
    public void initView() {
        super.initView();
        hideSubtitleContainer.setVisibility(View.GONE);
        subtitleDataContainer.setVisibility(View.GONE);
        clockFontContainer.setVisibility(View.GONE);
        hideLunarContainer.setVisibility(View.GONE);
    }

    @Override
    public RemoteViews getRemoteViews() {
        return WeekWidgetIMP.getRemoteViews(
                this, getLocationNow(),
                viewTypeValueNow, cardStyleValueNow, cardAlpha, textColorValueNow, textSize
        );
    }

    @Override
    public String getSharedPreferencesName() {
        return getString(R.string.sp_widget_week_setting);
    }
}