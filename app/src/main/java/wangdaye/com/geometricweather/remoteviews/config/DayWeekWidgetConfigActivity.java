package wangdaye.com.geometricweather.remoteviews.config;

import android.view.View;
import android.widget.RemoteViews;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.remoteviews.presenter.DayWeekWidgetIMP;

/**
 * Day week widget config activity.
 * */

public class DayWeekWidgetConfigActivity extends AbstractWidgetConfigActivity {

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
        };
        this.viewTypeValues = new String[] {
                widgetStyleValues[0],
                widgetStyleValues[1],
                widgetStyleValues[2],
        };
    }

    @Override
    public void initView() {
        super.initView();
        clockFontContainer.setVisibility(View.GONE);
    }

    @Override
    public RemoteViews getRemoteViews() {
        return DayWeekWidgetIMP.getRemoteViews(
                this, getLocationNow(),
                viewTypeValueNow, cardStyleValueNow, cardAlpha, textColorValueNow, textSize,
                hideSubtitle, subtitleDataValueNow
        );
    }

    @Override
    public String getSharedPreferencesName() {
        return getString(R.string.sp_widget_day_week_setting);
    }
}