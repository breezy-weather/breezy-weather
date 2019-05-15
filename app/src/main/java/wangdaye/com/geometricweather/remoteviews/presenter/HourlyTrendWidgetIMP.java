package wangdaye.com.geometricweather.remoteviews.presenter;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.receiver.widget.WidgetTrendHourlyProvider;
import wangdaye.com.geometricweather.basic.model.History;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Hourly;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.ui.widget.trendView.TrendItemView;
import wangdaye.com.geometricweather.ui.widget.trendView.appwidget.TrendLinearLayout;
import wangdaye.com.geometricweather.ui.widget.trendView.appwidget.WidgetItemView;
import wangdaye.com.geometricweather.utils.manager.ThreadManager;
import wangdaye.com.geometricweather.weather.WeatherHelper;

/**
 * Hourly trend widget utils.
 * */
public class HourlyTrendWidgetIMP extends AbstractRemoteViewsPresenter {

    public static void refreshWidgetView(Context context, Location location,
                                         @Nullable Weather weather, @Nullable History history) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            innerRefreshWidget(context, location, weather, history);
            return;
        }

        ThreadManager.getInstance().execute(() -> innerRefreshWidget(context, location, weather, history));
    }

    private static void innerRefreshWidget(Context context, Location location,
                                           @Nullable Weather weather, @Nullable History history) {
        View drawableView = getDrawableView(context, weather, history);
        AppWidgetManager.getInstance(context).updateAppWidget(
                new ComponentName(context, WidgetTrendHourlyProvider.class),
                getRemoteViews(
                        context,
                        drawableView,
                        location,
                        context.getResources().getDisplayMetrics().widthPixels
                )
        );
    }

    @WorkerThread @Nullable
    @SuppressLint("InflateParams, SimpleDateFormat")
    private static View getDrawableView(Context context,
                                        @Nullable Weather weather, @Nullable History history) {
        if (weather == null) {
            return null;
        }

        ResourceProvider provider = ResourcesProviderFactory.getNewInstance();

        int itemCount = 5;
        float[] temps;
        int highestTemp;
        int lowestTemp;

        boolean minimalIcon = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                context.getString(R.string.key_widget_minimal_icon),
                false
        );

        temps = new float[Math.max(0, itemCount * 2 - 1)];
        for (int i = 0; i < temps.length; i += 2) {
            temps[i] = weather.hourlyList.get(i / 2).temp;
        }
        for (int i = 1; i < temps.length; i += 2) {
            temps[i] = (temps[i - 1] + temps[i + 1]) * 0.5F;
        }

        highestTemp = history == null ? Integer.MIN_VALUE : history.maxiTemp;
        lowestTemp = history == null ? Integer.MAX_VALUE : history.miniTemp;
        for (int i = 0; i < itemCount; i ++) {
            if (weather.hourlyList.get(i).temp > highestTemp) {
                highestTemp = weather.hourlyList.get(i).temp;
            }
            if (weather.hourlyList.get(i).temp < lowestTemp) {
                lowestTemp = weather.hourlyList.get(i).temp;
            }
        }

        View drawableView = LayoutInflater.from(context)
                .inflate(R.layout.widget_trend_hourly, null, false);
        if (history != null) {
            ((TrendLinearLayout) drawableView.findViewById(R.id.widget_trend_hourly)).setData(
                    new int[] {history.maxiTemp, history.miniTemp}, highestTemp, lowestTemp, false
            );
        }
        WidgetItemView[] items = new WidgetItemView[] {
                drawableView.findViewById(R.id.widget_trend_hourly_item_1),
                drawableView.findViewById(R.id.widget_trend_hourly_item_2),
                drawableView.findViewById(R.id.widget_trend_hourly_item_3),
                drawableView.findViewById(R.id.widget_trend_hourly_item_4),
                drawableView.findViewById(R.id.widget_trend_hourly_item_5),
        };
        for (int i = 0; i < items.length; i ++) {
            Hourly hourly = weather.hourlyList.get(i);

            items[i].setTitleText(hourly.time);
            items[i].setSubtitleText(null);

            items[i].setTopIconDrawable(
                    WeatherHelper.getWidgetNotificationIcon(
                            provider, hourly.weatherKind, hourly.dayTime, minimalIcon, true
                    )
            );

            items[i].getTrendItemView().setData(
                    buildTempArrayForItem(temps, i),
                    null,
                    hourly.precipitation,
                    highestTemp,
                    lowestTemp
            );
            items[i].getTrendItemView().setLineColors(
                    ContextCompat.getColor(context, R.color.lightPrimary_5),
                    ContextCompat.getColor(context, R.color.darkPrimary_1),
                    Color.parseColor("#f1f1f1")
            );
            items[i].getTrendItemView().setTextColors(
                    ContextCompat.getColor(context, R.color.colorTextDark2nd),
                    ContextCompat.getColor(context, R.color.colorTextGrey2nd)
            );
            items[i].getTrendItemView().setPrecipitationAlpha(0.2f);

            items[i].setBottomIconDrawable(null);
        }

        return drawableView;
    }

    @WorkerThread
    private static RemoteViews getRemoteViews(Context context, @Nullable View drawableView,
                                              Location location, int width) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_remote);
        if (drawableView == null) {
            return views;
        }

        WidgetItemView[] items = new WidgetItemView[] {
                drawableView.findViewById(R.id.widget_trend_hourly_item_1),
                drawableView.findViewById(R.id.widget_trend_hourly_item_2),
                drawableView.findViewById(R.id.widget_trend_hourly_item_3),
                drawableView.findViewById(R.id.widget_trend_hourly_item_4),
                drawableView.findViewById(R.id.widget_trend_hourly_item_5),
        };
        for (WidgetItemView i : items) {
            i.setSize(width / 5f);
        }
        drawableView.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        drawableView.layout(
                0,
                0,
                drawableView.getMeasuredWidth(),
                drawableView.getMeasuredHeight()
        );

        Bitmap cache = Bitmap.createBitmap(
                drawableView.getMeasuredWidth(),
                drawableView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(cache);
        drawableView.draw(canvas);

        views.setImageViewBitmap(R.id.widget_remote_drawable, cache);
        views.setViewVisibility(R.id.widget_remote_progress, View.GONE);

        boolean touchToRefresh = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.key_click_widget_to_refresh), false);
        setOnClickPendingIntent(context, views, location, touchToRefresh);

        return views;
    }

    @WorkerThread
    private static RemoteViews getRemoteViews(Context context, Location location,
                                              @Nullable Weather weather, @Nullable History history,
                                              int width) {
        return getRemoteViews(
                context,
                getDrawableView(context, weather, history),
                location,
                width
        );
    }

    public static boolean isEnable(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, WidgetTrendHourlyProvider.class));
        return widgetIds != null && widgetIds.length > 0;
    }

    private static float[] buildTempArrayForItem(float[] temps, int index) {
        float[] a = new float[3];
        a[1] = temps[2 * index];
        if (2 * index - 1 < 0) {
            a[0] = TrendItemView.NONEXISTENT_VALUE;
        } else {
            a[0] = temps[2 * index - 1];
        }
        if (2 * index + 1 >= temps.length) {
            a[2] = TrendItemView.NONEXISTENT_VALUE;
        } else {
            a[2] = temps[2 * index + 1];
        }
        return a;
    }

    private static void setOnClickPendingIntent(Context context, RemoteViews views,
                                                Location location, boolean touchToRefresh) {
        if (touchToRefresh) {
            views.setOnClickPendingIntent(
                    R.id.widget_remote_drawable,
                    getRefreshPendingIntent(
                            context,
                            GeometricWeather.WIDGET_TREND_HOURLY_PENDING_INTENT_CODE_REFRESH
                    )
            );
        } else {
            views.setOnClickPendingIntent(
                    R.id.widget_remote_drawable,
                    getWeatherPendingIntent(
                            context,
                            location,
                            GeometricWeather.WIDGET_TREND_HOURLY_PENDING_INTENT_CODE_WEATHER
                    )
            );
        }
    }
}
