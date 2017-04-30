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
        v.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
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
        show(anchor, 0, 0);
    }

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

    @SuppressLint("RtlHardcoded")
    protected void show(View anchor, int offsetX, int offsetY) {
        int[] locations = new int[2];
        anchor.getLocationOnScreen(locations);
        locations[0] += offsetX;
        locations[1] += offsetY;

        int[] screenSizes = new int[] {
                anchor.getContext().getResources().getDisplayMetrics().widthPixels,
                anchor.getContext().getResources().getDisplayMetrics().heightPixels};
        int[] triggers = new int[] {
                (int) (0.5 * screenSizes[0]),
                screenSizes[1] - getContentView().getMeasuredHeight()
                        - 3 * anchor.getResources().getDimensionPixelSize(R.dimen.normal_margin)};

        if (locations[0] <= triggers[0]) {
            if (locations[1] <= triggers[1]) {
                setAnimationStyle(R.style.PopupWindowAnimation_Top_Left);
                showAsDropDown(anchor, offsetX, offsetY, Gravity.LEFT);
            } else {
                setAnimationStyle(R.style.PopupWindowAnimation_Bottom_Left);
                showAsDropDown(
                        anchor,
                        offsetX,
                        offsetY - anchor.getMeasuredHeight() - getContentView().getMeasuredHeight(),
                        Gravity.LEFT);
            }
        } else {
            if (locations[1] <= triggers[1]) {
                setAnimationStyle(R.style.PopupWindowAnimation_Top_Right);
                showAsDropDown(anchor, offsetX, offsetY, Gravity.RIGHT);
            } else {
                setAnimationStyle(R.style.PopupWindowAnimation_Bottom_Right);
                showAsDropDown(
                        anchor,
                        offsetX,
                        offsetY - anchor.getMeasuredHeight() - getContentView().getMeasuredHeight(),
                        Gravity.RIGHT);
            }
        }
    }
}
