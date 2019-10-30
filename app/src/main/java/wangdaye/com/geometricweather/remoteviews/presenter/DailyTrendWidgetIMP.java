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

import java.util.Date;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.receiver.widget.WidgetTrendDailyProvider;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.basic.model.weather.Daily;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.resource.ResourceHelper;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.remoteviews.trend.TrendLinearLayout;
import wangdaye.com.geometricweather.remoteviews.trend.WidgetItemView;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherViewController;
import wangdaye.com.geometricweather.utils.manager.ThreadManager;
import wangdaye.com.geometricweather.utils.manager.TimeManager;

public class DailyTrendWidgetIMP extends AbstractRemoteViewsPresenter {

    public static void updateWidgetView(Context context, Location location) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            innerUpdateWidget(context, location);
            return;
        }

        ThreadManager.getInstance().execute(() -> innerUpdateWidget(context, location));
    }

    @WorkerThread
    private static void innerUpdateWidget(Context context, Location location) {
        WidgetConfig config = getWidgetConfig(
                context,
                context.getString(R.string.sp_widget_daily_trend_setting)
        );
        if (config.cardStyle.equals("none")) {
            config.cardStyle = "light";
        }

        AppWidgetManager.getInstance(context).updateAppWidget(
                new ComponentName(context, WidgetTrendDailyProvider.class),
                getRemoteViews(
                        context, location,
                        context.getResources().getDisplayMetrics().widthPixels,
                        config.cardStyle, config.cardAlpha
                )
        );
    }

    @WorkerThread @Nullable
    @SuppressLint({"InflateParams", "WrongThread"})
    private static View getDrawableView(Context context, Location location,
                                        boolean lightTheme) {
        Weather weather = location.getWeather();
        if (weather == null) {
            return null;
        }

        ResourceProvider provider = ResourcesProviderFactory.getNewInstance();

        int itemCount = 5;
        float[] daytimeTemperatures;
        float[] nighttimeTemperatures;
        int highestTemperature;
        int lowestTemperature;

        boolean minimalIcon = SettingsOptionManager.getInstance(context).isWidgetMinimalIconEnabled();
        TemperatureUnit temperatureUnit = SettingsOptionManager.getInstance(context).getTemperatureUnit();

        daytimeTemperatures = new float[itemCount * 2 - 1];
        for (int i = 0; i < daytimeTemperatures.length; i += 2) {
            daytimeTemperatures[i] = weather.getDailyForecast().get(i / 2).day().getTemperature().getTemperature();
        }
        for (int i = 1; i < daytimeTemperatures.length; i += 2) {
            daytimeTemperatures[i] = (daytimeTemperatures[i - 1] + daytimeTemperatures[i + 1]) * 0.5F;
        }

        nighttimeTemperatures = new float[itemCount * 2 - 1];
        for (int i = 0; i < nighttimeTemperatures.length; i += 2) {
            nighttimeTemperatures[i] = weather.getDailyForecast().get(i / 2).night().getTemperature().getTemperature();
        }
        for (int i = 1; i < nighttimeTemperatures.length; i += 2) {
            nighttimeTemperatures[i] = (nighttimeTemperatures[i - 1] + nighttimeTemperatures[i + 1]) * 0.5F;
        }

        highestTemperature = weather.getYesterday() == null
                ? Integer.MIN_VALUE
                : weather.getYesterday().getDaytimeTemperature();
        lowestTemperature = weather.getYesterday() == null
                ? Integer.MAX_VALUE
                : weather.getYesterday().getNighttimeTemperature();
        for (int i = 0; i < itemCount; i ++) {
            if (weather.getDailyForecast().get(i).day().getTemperature().getTemperature() > highestTemperature) {
                highestTemperature = weather.getDailyForecast().get(i).day().getTemperature().getTemperature();
            }
            if (weather.getDailyForecast().get(i).night().getTemperature().getTemperature() < lowestTemperature) {
                lowestTemperature = weather.getDailyForecast().get(i).night().getTemperature().getTemperature();
            }
        }

        View drawableView = LayoutInflater.from(context)
                .inflate(R.layout.widget_trend_daily, null, false);
        if (weather.getYesterday() != null) {
            TrendLinearLayout trendParent = drawableView.findViewById(R.id.widget_trend_daily);
            trendParent.setData(
                    new int[] {
                            weather.getYesterday().getDaytimeTemperature(),
                            weather.getYesterday().getNighttimeTemperature()
                    },
                    highestTemperature,
                    lowestTemperature,
                    temperatureUnit,
                    true
            );
            trendParent.setColor(lightTheme);
        }
        WidgetItemView[] items = new WidgetItemView[] {
                drawableView.findViewById(R.id.widget_trend_daily_item_1),
                drawableView.findViewById(R.id.widget_trend_daily_item_2),
                drawableView.findViewById(R.id.widget_trend_daily_item_3),
                drawableView.findViewById(R.id.widget_trend_daily_item_4),
                drawableView.findViewById(R.id.widget_trend_daily_item_5)
        };
        int[] colors = WeatherViewController.getThemeColors(context, weather, TimeManager.isDaylight(location));
        for (int i = 0; i < items.length; i ++) {
            Daily daily = weather.getDailyForecast().get(i);

            if (daily.getDate().equals(new Date())) {
                items[i].setTitleText(context.getString(R.string.today));
            } else {
                items[i].setTitleText(daily.getWeek(context));
            }

            items[i].setSubtitleText(daily.getShortDate(context));

            items[i].setTopIconDrawable(
                    ResourceHelper.getWidgetNotificationIcon(
                            provider, daily.day().getWeatherCode(), true, minimalIcon, lightTheme)
            );

            Float daytimePrecipitationProbability = daily.day().getPrecipitationProbability().getTotal();
            Float nighttimePrecipitationProbability = daily.night().getPrecipitationProbability().getTotal();
            float p = Math.max(
                    daytimePrecipitationProbability == null ? 0 : daytimePrecipitationProbability,
                    nighttimePrecipitationProbability == null ? 0 : nighttimePrecipitationProbability
            );
            items[i].getTrendItemView().setData(
                    buildTemperatureArrayForItem(daytimeTemperatures, i),
                    buildTemperatureArrayForItem(nighttimeTemperatures, i),
                    daily.day().getTemperature().getShortTemperature(temperatureUnit),
                    daily.night().getTemperature().getShortTemperature(temperatureUnit),
                    (float) highestTemperature,
                    (float) lowestTemperature,
                    p < 5 ? null : p,
                    p < 5 ? null : ((int) p + "%"),
                    100f,
                    0f
            );
            items[i].getTrendItemView().setLineColors(
                    colors[1], colors[2],
                    lightTheme
                            ? ColorUtils.setAlphaComponent(Color.BLACK, (int) (255 * 0.05))
                            : ColorUtils.setAlphaComponent(Color.WHITE, (int) (255 * 0.1))
            );
            items[i].getTrendItemView().setShadowColors(colors[1], colors[2], lightTheme);
            items[i].getTrendItemView().setTextColors(
                    lightTheme
                            ? ContextCompat.getColor(context, R.color.colorTextContent_light)
                            : ContextCompat.getColor(context, R.color.colorTextContent_dark),
                    lightTheme
                            ? ContextCompat.getColor(context, R.color.colorTextSubtitle_light)
                            : ContextCompat.getColor(context, R.color.colorTextSubtitle_dark)
            );
            items[i].getTrendItemView().setHistogramAlpha(lightTheme ? 0.2f : 0.5f);

            items[i].setBottomIconDrawable(
                    ResourceHelper.getWidgetNotificationIcon(
                            provider, daily.night().getWeatherCode(), false, minimalIcon, lightTheme
                    )
            );

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
                drawableView.findViewById(R.id.widget_trend_daily_item_1),
                drawableView.findViewById(R.id.widget_trend_daily_item_2),
                drawableView.findViewById(R.id.widget_trend_daily_item_3),
                drawableView.findViewById(R.id.widget_trend_daily_item_4),
                drawableView.findViewById(R.id.widget_trend_daily_item_5),
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

        setOnClickPendingIntent(
                context,
                views,
                location,
                SettingsOptionManager.getInstance(context).isWidgetClickToRefreshEnabled()
        );

        return views;
    }

    @WorkerThread
    public static RemoteViews getRemoteViews(Context context, Location location,
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
                lightTheme = TimeManager.isDaylight(location);
                break;
        }
        return getRemoteViews(
                context,
                getDrawableView(context, location, lightTheme),
                location,
                width,
                !lightTheme, cardAlpha
        );
    }

    public static boolean isEnable(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, WidgetTrendDailyProvider.class));
        return widgetIds != null && widgetIds.length > 0;
    }

    private static Float[] buildTemperatureArrayForItem(float[] temps, int index) {
        Float[] a = new Float[3];
        a[1] = temps[2 * index];
        if (2 * index - 1 < 0) {
            a[0] = null;
        } else {
            a[0] = temps[2 * index - 1];
        }
        if (2 * index + 1 >= temps.length) {
            a[2] = null;
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
                            GeometricWeather.WIDGET_TREND_DAILY_PENDING_INTENT_CODE_REFRESH
                    )
            );
        } else {
            views.setOnClickPendingIntent(
                    R.id.widget_remote_drawable,
                    getWeatherPendingIntent(
                            context,
                            location,
                            GeometricWeather.WIDGET_TREND_DAILY_PENDING_INTENT_CODE_WEATHER
                    )
            );
        }
    }
}
