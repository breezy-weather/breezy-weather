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
import androidx.core.graphics.ColorUtils;
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
import wangdaye.com.geometricweather.utils.manager.TimeManager;
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

    @WorkerThread
    private static void innerRefreshWidget(Context context, Location location,
                                           @Nullable Weather weather, @Nullable History history) {
        WidgetConfig config = getWidgetConfig(
                context,
                context.getString(R.string.sp_widget_hourly_trend_setting)
        );
        if (config.cardStyle.equals("none")) {
            config.cardStyle = "light";
        }

        AppWidgetManager.getInstance(context).updateAppWidget(
                new ComponentName(context, WidgetTrendHourlyProvider.class),
                getRemoteViews(
                        context, location, weather, history,
                        context.getResources().getDisplayMetrics().widthPixels,
                        config.cardStyle, config.cardAlpha
                )
        );
    }

    @WorkerThread @Nullable
    @SuppressLint({"InflateParams, SimpleDateFormat", "WrongThread"})
    private static View getDrawableView(Context context,
                                        @Nullable Weather weather, @Nullable History history,
                                        boolean lightTheme) {
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
            TrendLinearLayout trendParent = drawableView.findViewById(R.id.widget_trend_hourly);
            trendParent.setData(
                    new int[] {history.maxiTemp, history.miniTemp}, highestTemp, lowestTemp, false
            );
            trendParent.setColor(lightTheme);
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
                            provider, hourly.weatherKind, hourly.dayTime, minimalIcon, lightTheme
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
                    lightTheme
                            ? ColorUtils.setAlphaComponent(Color.BLACK, (int) (255 * 0.05))
                            : ColorUtils.setAlphaComponent(Color.WHITE, (int) (255 * 0.1))
            );
            items[i].getTrendItemView().setShadowColors(lightTheme);
            items[i].getTrendItemView().setTextColors(
                    lightTheme
                            ? ContextCompat.getColor(context, R.color.colorTextContent_light)
                            : ContextCompat.getColor(context, R.color.colorTextContent_dark),
                    lightTheme
                            ? ContextCompat.getColor(context, R.color.colorTextSubtitle_light)
                            : ContextCompat.getColor(context, R.color.colorTextSubtitle_dark)
            );
            items[i].getTrendItemView().setPrecipitationAlpha(lightTheme ? 0.2f : 0.5f);

            items[i].setBottomIconDrawable(null);

            items[i].setColor(lightTheme);
        }

        return drawableView;
    }

    @SuppressLint("WrongThread")
    @WorkerThread
    private static RemoteViews getRemoteViews(Context context, @Nullable View drawableView,
                                              Location location, int width,
                                              boolean darkCard, int cardAlpha) {
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

        views.setImageViewResource(
                R.id.widget_remote_card,
                getCardBackgroundId(context, darkCard, cardAlpha)
        );

        boolean touchToRefresh = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.key_click_widget_to_refresh), false);
        setOnClickPendingIntent(context, views, location, touchToRefresh);

        return views;
    }

    @WorkerThread
    public static RemoteViews getRemoteViews(Context context, Location location,
                                             @Nullable Weather weather, @Nullable History history,
                                             int width, String cardStyle, int cardAlpha) {
        boolean lightTheme;
        switch (cardStyle) {
            case "light":
                lightTheme = true;
                break;

            case "dark":
                lightTheme = false;
                break;

            default:
                lightTheme = TimeManager.isDaylight(weather);
                break;
        }
        return getRemoteViews(
                context,
                getDrawableView(context, weather, history, lightTheme),
                location,
                width,
                !lightTheme, cardAlpha
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
