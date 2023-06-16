package org.breezyweather.remoteviews.presenters;

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

import java.util.Date;

import org.breezyweather.BreezyWeather;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit;
import org.breezyweather.common.basic.models.weather.Daily;
import org.breezyweather.common.basic.models.weather.Weather;
import org.breezyweather.theme.ThemeManager;
import org.breezyweather.theme.resource.ResourceHelper;
import org.breezyweather.theme.resource.ResourcesProviderFactory;
import org.breezyweather.theme.resource.providers.ResourceProvider;
import org.breezyweather.theme.weatherView.WeatherViewController;
import org.breezyweather.R;
import org.breezyweather.background.receiver.widget.WidgetTrendDailyProvider;
import org.breezyweather.common.utils.DisplayUtils;
import org.breezyweather.common.utils.helpers.AsyncHelper;
import org.breezyweather.remoteviews.trend.TrendLinearLayout;
import org.breezyweather.remoteviews.trend.WidgetItemView;
import org.breezyweather.settings.SettingsManager;

public class DailyTrendWidgetIMP extends AbstractRemoteViewsPresenter {

    public static void updateWidgetView(Context context, Location location) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            innerUpdateWidget(context, location);
            return;
        }

        AsyncHelper.runOnIO(() -> innerUpdateWidget(context, location));
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
                        DisplayUtils.getTabletListAdaptiveWidth(
                                context,
                                context.getResources().getDisplayMetrics().widthPixels
                        ),
                        config.cardStyle, config.cardAlpha
                )
        );
    }

    @WorkerThread
    @Nullable
    @SuppressLint({"InflateParams", "WrongThread"})
    private static View getDrawableView(Context context, Location location,
                                        boolean lightTheme) {
        Weather weather = location.getWeather();
        if (weather == null) {
            return null;
        }

        ResourceProvider provider = ResourcesProviderFactory.getNewInstance();

        int itemCount = Math.min(5, weather.getDailyForecast().size());
        Float[] daytimeTemperatures;
        Float[] nighttimeTemperatures;
        Integer highestTemperature = null;
        Integer lowestTemperature = null;

        boolean minimalIcon = SettingsManager.getInstance(context).isWidgetMinimalIconEnabled();
        TemperatureUnit temperatureUnit = SettingsManager.getInstance(context).getTemperatureUnit();

        daytimeTemperatures = new Float[Math.max(0, itemCount * 2 - 1)];
        for (int i = 0; i < daytimeTemperatures.length; i += 2) {
            daytimeTemperatures[i] = weather.getDailyForecast().get(i / 2).getDay() != null
                    && weather.getDailyForecast().get(i / 2).getDay().getTemperature() != null
                    && weather.getDailyForecast().get(i / 2).getDay().getTemperature().getTemperature() != null ?
                    Float.valueOf(weather.getDailyForecast().get(i / 2).getDay().getTemperature().getTemperature())
                    : null;
        }
        for (int i = 1; i < daytimeTemperatures.length; i += 2) {
            if (daytimeTemperatures[i - 1] != null && daytimeTemperatures[i + 1] != null) {
                daytimeTemperatures[i] = (daytimeTemperatures[i - 1] + daytimeTemperatures[i + 1]) * 0.5F;
            } else {
                daytimeTemperatures[i] = null;
            }
        }

        nighttimeTemperatures = new Float[Math.max(0, itemCount * 2 - 1)];
        for (int i = 0; i < nighttimeTemperatures.length; i += 2) {
            nighttimeTemperatures[i] = weather.getDailyForecast().get(i / 2).getNight() != null
                    && weather.getDailyForecast().get(i / 2).getNight().getTemperature() != null
                    && weather.getDailyForecast().get(i / 2).getNight().getTemperature().getTemperature() != null ?
                    Float.valueOf(weather.getDailyForecast().get(i / 2).getNight().getTemperature().getTemperature())
                    : null;
        }
        for (int i = 1; i < nighttimeTemperatures.length; i += 2) {
            if (nighttimeTemperatures[i - 1] != null && nighttimeTemperatures[i + 1] != null) {
                nighttimeTemperatures[i] = (nighttimeTemperatures[i - 1] + nighttimeTemperatures[i + 1]) * 0.5F;
            } else {
                nighttimeTemperatures[i] = null;
            }
        }

        if (weather.getYesterday() != null) {
            if (weather.getYesterday().getDaytimeTemperature() != null) {
                highestTemperature = weather.getYesterday().getDaytimeTemperature();
            }
            if (weather.getYesterday().getNighttimeTemperature() != null) {
                lowestTemperature = weather.getYesterday().getNighttimeTemperature();
            }
        }
        for (int i = 0; i < itemCount; i++) {
            if (weather.getDailyForecast().get(i).getDay() != null && weather.getDailyForecast().get(i).getDay().getTemperature() != null && weather.getDailyForecast().get(i).getDay().getTemperature().getTemperature() != null) {
                if (highestTemperature == null || weather.getDailyForecast().get(i).getDay().getTemperature().getTemperature() > highestTemperature) {
                    highestTemperature = weather.getDailyForecast().get(i).getDay().getTemperature().getTemperature();
                }
            }
            if (weather.getDailyForecast().get(i).getNight() != null && weather.getDailyForecast().get(i).getNight().getTemperature() != null && weather.getDailyForecast().get(i).getNight().getTemperature().getTemperature() != null) {
                if (lowestTemperature == null || weather.getDailyForecast().get(i).getNight().getTemperature().getTemperature() < lowestTemperature) {
                    lowestTemperature = weather.getDailyForecast().get(i).getNight().getTemperature().getTemperature();
                }
            }
        }

        View drawableView = LayoutInflater.from(context)
                .inflate(R.layout.widget_trend_daily, null, false);
        if (weather.getYesterday() != null) {
            TrendLinearLayout trendParent = drawableView.findViewById(R.id.widget_trend_daily);
            trendParent.setData(
                    new Integer[]{
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
        WidgetItemView[] items;
        if (itemCount == 5) {
            items = new WidgetItemView[] {
                    drawableView.findViewById(R.id.widget_trend_daily_item_1),
                    drawableView.findViewById(R.id.widget_trend_daily_item_2),
                    drawableView.findViewById(R.id.widget_trend_daily_item_3),
                    drawableView.findViewById(R.id.widget_trend_daily_item_4),
                    drawableView.findViewById(R.id.widget_trend_daily_item_5)
            };
        } else if (itemCount == 4) {
            items = new WidgetItemView[] {
                    drawableView.findViewById(R.id.widget_trend_daily_item_1),
                    drawableView.findViewById(R.id.widget_trend_daily_item_2),
                    drawableView.findViewById(R.id.widget_trend_daily_item_3),
                    drawableView.findViewById(R.id.widget_trend_daily_item_4)
            };
        } else if (itemCount == 3) {
            items = new WidgetItemView[] {
                    drawableView.findViewById(R.id.widget_trend_daily_item_1),
                    drawableView.findViewById(R.id.widget_trend_daily_item_2),
                    drawableView.findViewById(R.id.widget_trend_daily_item_3)
            };
        } else if (itemCount == 2) {
            items = new WidgetItemView[] {
                    drawableView.findViewById(R.id.widget_trend_daily_item_1),
                    drawableView.findViewById(R.id.widget_trend_daily_item_2)
            };
        } else if (itemCount == 1) {
            items = new WidgetItemView[] {
                    drawableView.findViewById(R.id.widget_trend_daily_item_1)
            };
        } else {
            items = new WidgetItemView[] {};
        }
        int[] colors = ThemeManager
                .getInstance(context)
                .getWeatherThemeDelegate()
                .getThemeColors(
                        context,
                        WeatherViewController.getWeatherKind(weather),
                        location.isDaylight()
                );
        for (int i = 0; i < items.length; i++) {
            Daily daily = weather.getDailyForecast().get(i);

            if (daily.getDate().equals(new Date())) {
                items[i].setTitleText(context.getString(R.string.short_today));
            } else {
                items[i].setTitleText(daily.getWeek(context, location.getTimeZone()));
            }

            items[i].setSubtitleText(daily.getShortDate(context, location.getTimeZone()));

            if (daily.getDay() != null && daily.getDay().getWeatherCode() != null) {
                items[i].setTopIconDrawable(
                        ResourceHelper.getWidgetNotificationIcon(
                                provider, daily.getDay().getWeatherCode(), true, minimalIcon, lightTheme)
                );
            }

            Float daytimePrecipitationProbability = null;
            Float nighttimePrecipitationProbability = null;
            if (daily.getDay() != null && daily.getDay().getPrecipitationProbability() != null) {
                daytimePrecipitationProbability = daily.getDay().getPrecipitationProbability().getTotal();
            }
            if (daily.getNight() != null && daily.getNight().getPrecipitationProbability() != null) {
                nighttimePrecipitationProbability = daily.getNight().getPrecipitationProbability().getTotal();
            }

            float p = Math.max(
                    daytimePrecipitationProbability == null ? 0 : daytimePrecipitationProbability,
                    nighttimePrecipitationProbability == null ? 0 : nighttimePrecipitationProbability
            );
            items[i].getTrendItemView().setData(
                    buildTemperatureArrayForItem(daytimeTemperatures, i),
                    buildTemperatureArrayForItem(nighttimeTemperatures, i),
                    daily.getDay() != null && daily.getDay().getTemperature() != null ? daily.getDay().getTemperature().getShortTemperature(context, temperatureUnit) : null,
                    daily.getNight() != null && daily.getNight().getTemperature() != null ? daily.getNight().getTemperature().getShortTemperature(context, temperatureUnit) : null,
                    highestTemperature != null ? Float.valueOf(highestTemperature) : null,
                    lowestTemperature != null ? Float.valueOf(lowestTemperature) : null,
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
                            ? ContextCompat.getColor(context, R.color.colorTextDark)
                            : ContextCompat.getColor(context, R.color.colorTextLight),
                    lightTheme
                            ? ContextCompat.getColor(context, R.color.colorTextDark2nd)
                            : ContextCompat.getColor(context, R.color.colorTextLight2nd),
                    lightTheme
                            ? ContextCompat.getColor(context, R.color.colorTextGrey2nd)
                            : ContextCompat.getColor(context, R.color.colorTextGrey)
            );
            items[i].getTrendItemView().setHistogramAlpha(lightTheme ? 0.2f : 0.5f);

            if (daily.getNight() != null && daily.getNight().getWeatherCode() != null) {
                items[i].setBottomIconDrawable(
                        ResourceHelper.getWidgetNotificationIcon(
                                provider, daily.getNight().getWeatherCode(), false, minimalIcon, lightTheme
                        )
                );
            }

            items[i].setColor(lightTheme);
        }

        return drawableView;
    }

    @SuppressLint("WrongThread")
    @WorkerThread
    private static RemoteViews getRemoteViews(Context context, @Nullable View drawableView,
                                              Location location, int width,
                                              int cardAlpha, String cardStyle) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_remote);
        if (drawableView == null) {
            return views;
        }

        WidgetItemView[] items = new WidgetItemView[]{
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

        WidgetColor.ColorType colorType;
        switch (cardStyle) {
            case "light":
                colorType = WidgetColor.ColorType.LIGHT;
                break;

            case "dark":
                colorType = WidgetColor.ColorType.DARK;
                break;

            default:
                colorType = WidgetColor.ColorType.AUTO;
                break;
        }
        views.setImageViewResource(
                R.id.widget_remote_card,
                getCardBackgroundId(colorType)
        );
        views.setInt(
                R.id.widget_remote_card,
                "setImageAlpha",
                (int) (cardAlpha / 100.0 * 255)
        );

        setOnClickPendingIntent(context, views, location);

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
                lightTheme = location.isDaylight();
                break;
        }
        return getRemoteViews(
                context,
                getDrawableView(context, location, lightTheme),
                location,
                width,
                cardAlpha,
                cardStyle
        );
    }

    public static boolean isEnable(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, WidgetTrendDailyProvider.class));
        return widgetIds != null && widgetIds.length > 0;
    }

    private static Float[] buildTemperatureArrayForItem(Float[] temps, int index) {
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
                                                Location location) {
        views.setOnClickPendingIntent(
                R.id.widget_remote_drawable,
                getWeatherPendingIntent(
                        context,
                        location,
                        BreezyWeather.WIDGET_TREND_DAILY_PENDING_INTENT_CODE_WEATHER
                )
        );
    }
}
