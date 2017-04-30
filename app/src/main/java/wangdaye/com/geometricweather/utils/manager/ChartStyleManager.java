package wangdaye.com.geometricweather.utils.manager;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Chart style manager.
 * */

public class ChartStyleManager {

    private static ChartStyleManager instance;

    public static ChartStyleManager getInstance(Context context) {
        if (instance == null) {
            synchronized (ChartStyleManager.class) {
                if (instance == null) {
                    instance = new ChartStyleManager(context);
                }
            }
        }
        return instance;
    }

    private boolean showDailyPop;
    private boolean showDate;
    private int previewTime;

    private static final String PREFERENCE_NAME = "sp_trend_view";
    private static final String KEY_DAILY_POP_SWITCH = "daily_pop_switch";
    private static final String KEY_DATE_SWITCH = "date_switch";
    private static final String KEY_PREVIEW_TIME = "preview_time";

    public static final int PREVIEW_TIME_AUTO = 1;
    public static final int PREVIEW_TIME_DAY = 2;
    public static final int PREVIEW_TIME_NIGHT = 3;

    private ChartStyleManager(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE);
        showDailyPop = sharedPreferences.getBoolean(KEY_DAILY_POP_SWITCH, false);
        showDate = sharedPreferences.getBoolean(KEY_DATE_SWITCH, false);
        previewTime = sharedPreferences.getInt(KEY_PREVIEW_TIME, PREVIEW_TIME_AUTO);
    }

    public boolean isShowDailyPop() {
        return showDailyPop;
    }

    public void setShowDailyPop(Context context, boolean showDailyPop) {
        this.showDailyPop = showDailyPop;
        SharedPreferences.Editor editor = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(KEY_DAILY_POP_SWITCH, showDailyPop);
        editor.apply();
    }

    public boolean isShowDate() {
        return showDate;
    }

    public void setShowDate(Context context, boolean showDate) {
        this.showDate = showDate;
        SharedPreferences.Editor editor = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(KEY_DATE_SWITCH, showDate);
        editor.apply();
    }

    public int getPreviewTime() {
        return previewTime;
    }

    public void setPreviewTime(Context context, int previewTime) {
        this.previewTime = previewTime;
        SharedPreferences.Editor editor = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(KEY_PREVIEW_TIME, previewTime);
        editor.apply();
    }
}
