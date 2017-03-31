package wangdaye.com.geometricweather.ui.popup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.ui.widget.switchButton.RecyclerSwitchImageButton;
import wangdaye.com.geometricweather.ui.widget.switchButton.SwitchImageButton;
import wangdaye.com.geometricweather.utils.manager.ChartStyleManager;

/**
 * Daily preview settings popup window.
 * */

public class DailyPreviewSettingsPopupWindow extends PopupWindow {

    /** <br> life cycle. */

    public DailyPreviewSettingsPopupWindow(Context c, View anchor,
                                           boolean showDailyPop, boolean showDate, int previewTime,
                                           SwitchImageButton.OnSwitchListener popListener,
                                           SwitchImageButton.OnSwitchListener dateListener,
                                           RecyclerSwitchImageButton.OnRecyclerSwitchListener timeListener) {
        super(c);
        this.initialize(
                c, anchor,
                showDailyPop, showDate, previewTime,
                popListener, dateListener, timeListener);
    }

    @SuppressLint("InflateParams")
    private void initialize(Context c, View anchor,
                            boolean showDailyPop, boolean showDate, int previewTime,
                            SwitchImageButton.OnSwitchListener popListener,
                            SwitchImageButton.OnSwitchListener dateListener,
                            RecyclerSwitchImageButton.OnRecyclerSwitchListener timeListener) {
        View v = LayoutInflater.from(c).inflate(R.layout.popup_daily_preview_settings, null);
        setContentView(v);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        initWidget(
                showDailyPop, showDate, previewTime,
                popListener, dateListener, timeListener);

        setFocusable(true);
        setTouchable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setElevation(10);
        }
        showAsDropDown(anchor, 0, 0, Gravity.CENTER);
    }

    /** <br> UI. */

    private void initWidget(boolean showDailyPop, boolean showDate, int previewTime,
                            SwitchImageButton.OnSwitchListener popListener,
                            SwitchImageButton.OnSwitchListener dateListener,
                            RecyclerSwitchImageButton.OnRecyclerSwitchListener timeListener) {
        View v = getContentView();

        SwitchImageButton popBtn = (SwitchImageButton) v.findViewById(R.id.popup_daily_preview_settings_popBtn);
        popBtn.initSwitchState(showDailyPop);
        popBtn.setOnSwitchListener(popListener);

        SwitchImageButton dateBtn = (SwitchImageButton) v.findViewById(R.id.popup_daily_preview_settings_dateBtn);
        dateBtn.initSwitchState(showDate);
        dateBtn.setOnSwitchListener(dateListener);

        List<Integer> stateList = new ArrayList<>();
        stateList.add(ChartStyleManager.PREVIEW_TIME_AUTO);
        stateList.add(ChartStyleManager.PREVIEW_TIME_DAY);
        stateList.add(ChartStyleManager.PREVIEW_TIME_NIGHT);

        List<Integer> resList = new ArrayList<>();
        resList.add(R.drawable.ic_auto_off);
        resList.add(R.drawable.ic_day_on);
        resList.add(R.drawable.ic_night_on);

        RecyclerSwitchImageButton previewTimeBtn = (RecyclerSwitchImageButton) v.findViewById(R.id.popup_daily_preview_settings_previewTimeBtn);
        previewTimeBtn.setStateAndRes(stateList, resList, previewTime);
        previewTimeBtn.setOnSwitchListener(timeListener);
    }
}
