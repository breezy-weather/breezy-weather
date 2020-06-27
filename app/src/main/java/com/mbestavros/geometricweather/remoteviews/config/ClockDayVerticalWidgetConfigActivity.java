package com.mbestavros.geometricweather.remoteviews.config;

import android.view.View;
import android.widget.RemoteViews;

import com.mbestavros.geometricweather.R;
import com.mbestavros.geometricweather.remoteviews.presenter.ClockDayVerticalWidgetIMP;

/**
 * Clock day vertical widget config activity.
 * */

public class ClockDayVerticalWidgetConfigActivity extends AbstractWidgetConfigActivity {

    @Override
    public void initData() {
        super.initData();

        String[] widgetStyles = getResources().getStringArray(R.array.widget_styles);
        String[] widgetStyleValues = getResources().getStringArray(R.array.widget_style_values);

        this.viewTypeValueNow = "rectangle";
        this.viewTypes = new String[] {
                widgetStyles[0],
                widgetStyles[1],
                widgetStyles[2],
                widgetStyles[3],
                widgetStyles[6],
                widgetStyles[9]
        };
        this.viewTypeValues = new String[] {
                widgetStyleValues[0],
                widgetStyleValues[1],
                widgetStyleValues[2],
                widgetStyleValues[3],
                widgetStyleValues[6],
                widgetStyleValues[9]
        };
    }

    @Override
    public void initView() {
        super.initView();
        hideLunarContainer.setVisibility(View.GONE);
    }

    @Override
    public RemoteViews getRemoteViews() {
        return ClockDayVerticalWidgetIMP.getRemoteViews(
                this, getLocationNow(),
                viewTypeValueNow, cardStyleValueNow, cardAlpha, textColorValueNow, textSize,
                hideSubtitle, subtitleDataValueNow, clockFontValueNow
        );
    }

    @Override
    public String getSharedPreferencesName() {
        return getString(R.string.sp_widget_clock_day_vertical_setting);
    }
}