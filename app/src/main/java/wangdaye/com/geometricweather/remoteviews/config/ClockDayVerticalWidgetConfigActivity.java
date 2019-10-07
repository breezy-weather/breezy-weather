package wangdaye.com.geometricweather.remoteviews.config;

import android.widget.RemoteViews;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.remoteviews.presenter.ClockDayVerticalWidgetIMP;

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
                widgetStyles[6]
        };
        this.viewTypeValues = new String[] {
                widgetStyleValues[0],
                widgetStyleValues[1],
                widgetStyleValues[2],
                widgetStyleValues[3],
                widgetStyleValues[6]
        };
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