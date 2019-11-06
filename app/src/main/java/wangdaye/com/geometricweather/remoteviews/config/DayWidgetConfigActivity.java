package wangdaye.com.geometricweather.remoteviews.config;

import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.remoteviews.presenter.DayWidgetIMP;

/**
 * Day widget config activity.
 * */

public class DayWidgetConfigActivity extends AbstractWidgetConfigActivity {

    @Override
    public void initData() {
        super.initData();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            String[] widgetStyles = getResources().getStringArray(R.array.widget_styles);
            String[] widgetStyleValues = getResources().getStringArray(R.array.widget_style_values);

            this.viewTypeValueNow = "rectangle";
            this.viewTypes = new String[] {
                    widgetStyles[0],
                    widgetStyles[1],
                    widgetStyles[2],
                    widgetStyles[3],
                    widgetStyles[4],
                    widgetStyles[5],
                    widgetStyles[6],
                    widgetStyles[7]
            };
            this.viewTypeValues = new String[] {
                    widgetStyleValues[0],
                    widgetStyleValues[1],
                    widgetStyleValues[2],
                    widgetStyleValues[3],
                    widgetStyleValues[4],
                    widgetStyleValues[5],
                    widgetStyleValues[6],
                    widgetStyleValues[7]
            };
        }
    }

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