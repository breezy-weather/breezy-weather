package org.breezyweather.remoteviews.config;

import android.view.View;
import android.widget.RemoteViews;

import dagger.hilt.android.AndroidEntryPoint;
import org.breezyweather.remoteviews.presenters.WeekWidgetIMP;
import org.breezyweather.R;

/**
 * Week widget config activity.
 * */

@AndroidEntryPoint
public class WeekWidgetConfigActivity extends AbstractWidgetConfigActivity {

    @Override
    public void initData() {
        super.initData();
        String[] widgetStyles = getResources().getStringArray(R.array.widget_week_styles);
        String[] widgetStyleValues = getResources().getStringArray(R.array.widget_week_style_values);

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
        mViewTypeContainer.setVisibility(View.VISIBLE);
        mCardStyleContainer.setVisibility(View.VISIBLE);
        mCardAlphaContainer.setVisibility(View.VISIBLE);
        mTextColorContainer.setVisibility(View.VISIBLE);
        mTextSizeContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public RemoteViews getRemoteViews() {
        return WeekWidgetIMP.getRemoteViews(
                this, getLocationNow(),
                viewTypeValueNow, cardStyleValueNow, cardAlpha, textColorValueNow, textSize
        );
    }

    @Override
    public String getConfigStoreName() {
        return getString(R.string.sp_widget_week_setting);
    }
}