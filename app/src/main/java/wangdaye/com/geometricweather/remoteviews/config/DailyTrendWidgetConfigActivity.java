package wangdaye.com.geometricweather.remoteviews.config;

import android.view.View;
import android.widget.RemoteViews;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.remoteviews.presenter.DailyTrendWidgetIMP;

/**
 * Daily trend widget config activity.
 * */

public class DailyTrendWidgetConfigActivity extends AbstractWidgetConfigActivity {

    @Override
    public void initData() {
        super.initData();

        String[] cardStyles = getResources().getStringArray(R.array.widget_card_styles);
        String[] cardStyleValues = getResources().getStringArray(R.array.widget_card_style_values);

        this.cardStyleValueNow = "light";
        this.cardStyles = new String[] {cardStyles[2], cardStyles[3], cardStyles[1]};
        this.cardStyleValues = new String[] {cardStyleValues[2], cardStyleValues[3], cardStyleValues[1]};
    }

    @Override
    public void initView() {
        super.initView();
        viewTypeContainer.setVisibility(View.GONE);
        hideSubtitleContainer.setVisibility(View.GONE);
        subtitleDataContainer.setVisibility(View.GONE);
        textColorContainer.setVisibility(View.GONE);
        textSizeContainer.setVisibility(View.GONE);
        clockFontContainer.setVisibility(View.GONE);
    }

    @Override
    public RemoteViews getRemoteViews() {
        return DailyTrendWidgetIMP.getRemoteViews(
                this, locationNow,
                getResources().getDisplayMetrics().widthPixels,
                cardStyleValueNow, cardAlpha
        );
    }

    @Override
    public String getSharedPreferencesName() {
        return getString(R.string.sp_widget_daily_trend_setting);
    }
}