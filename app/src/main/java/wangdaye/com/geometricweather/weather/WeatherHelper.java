package wangdaye.com.geometricweather.weather;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.Size;
import androidx.core.content.ContextCompat;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.History;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.resource.provider.DefaultResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.resource.ResourceUtils;
import wangdaye.com.geometricweather.weather.observer.BaseObserver;
import wangdaye.com.geometricweather.weather.observer.ObserverContainer;
import wangdaye.com.geometricweather.weather.service.AccuWeatherService;
import wangdaye.com.geometricweather.weather.service.CNWeatherService;
import wangdaye.com.geometricweather.weather.service.CaiYunWeatherService;
import wangdaye.com.geometricweather.weather.service.WeatherService;
import wangdaye.com.geometricweather.utils.manager.TimeManager;

/**
 * Weather kind tools.
 * */

public class WeatherHelper {

    @Nullable private WeatherService weatherService;

    @Nullable private WeatherService[] searchServices;
    private CompositeDisposable compositeDisposable;

    public WeatherHelper() {
        weatherService = null;
        searchServices = null;
        compositeDisposable = new CompositeDisposable();
    }

    @NonNull
    private static WeatherService getWeatherService(@SettingsOptionManager.WeatherSourceRule String source) {
        switch (source) {
            case SettingsOptionManager.WEATHER_SOURCE_CN:
                return new CNWeatherService();

            case SettingsOptionManager.WEATHER_SOURCE_CAIYUN:
                return new CaiYunWeatherService();

            default:
                return new AccuWeatherService();
        }
    }

    public void requestWeather(Context c, Location location, @NonNull final OnRequestWeatherListener l) {
        weatherService = getWeatherService(location.source);
        weatherService.requestWeather(c, location, new WeatherService.RequestWeatherCallback() {

            @Override
            public void requestWeatherSuccess(@Nullable Weather weather, @Nullable History history,
                                              @NonNull Location requestLocation) {
                DatabaseHelper.getInstance(c).writeWeather(requestLocation, weather);
                DatabaseHelper.getInstance(c).writeTodayHistory(weather);
                DatabaseHelper.getInstance(c).writeYesterdayHistory(history);
                l.requestWeatherSuccess(weather, history, requestLocation);
            }

            @Override
            public void requestWeatherFailed(@NonNull Location requestLocation) {
                l.requestWeatherFailed(requestLocation);
            }
        });
    }

    public void requestLocation(Context context, String query, @NonNull final OnRequestLocationListener l) {
        searchServices = new WeatherService[] {
                getWeatherService(SettingsOptionManager.WEATHER_SOURCE_ACCU),
                getWeatherService(SettingsOptionManager.WEATHER_SOURCE_CN),
                getWeatherService(SettingsOptionManager.WEATHER_SOURCE_CAIYUN)
        };

        Observable<List<Location>> accu = Observable.create(emitter ->
                emitter.onNext(searchServices[0].requestLocation(context, query)));

        Observable<List<Location>> cn = Observable.create(emitter ->
                emitter.onNext(searchServices[1].requestLocation(context, query)));

        Observable<List<Location>> caiyun = Observable.create(emitter ->
                emitter.onNext(searchServices[2].requestLocation(context, query)));

        Observable.zip(accu, cn, caiyun, (accuList, cnList, caiyunList) -> {
            List<Location> locationList = new ArrayList<>();
            locationList.addAll(accuList);
            locationList.addAll(cnList);
            locationList.addAll(caiyunList);
            return locationList;
        }).compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(compositeDisposable, new BaseObserver<List<Location>>() {
                    @Override
                    public void onSucceed(List<Location> locationList) {
                        if (locationList != null && locationList.size() != 0) {
                            l.requestLocationSuccess(query, locationList);
                        } else {
                            onFailed();
                        }
                    }

                    @Override
                    public void onFailed() {
                        l.requestLocationFailed(query);
                    }
                }));
    }

    public void cancel() {
        if (weatherService != null) {
            weatherService.cancel();
        }
        if (searchServices != null) {
            for (WeatherService s : searchServices) {
                if (s != null) {
                    s.cancel();
                }
            }
        }
        compositeDisposable.clear();
    }

    @NonNull
    public static Drawable getWeatherIcon(ResourceProvider provider,
                                          String weatherKind, boolean dayTime) {
        return provider.getWeatherIcon(weatherKind, dayTime);
    }

    @Size(3)
    public static Drawable[] getWeatherIcons(ResourceProvider provider,
                                             String weatherKind, boolean dayTime) {
        return provider.getWeatherIcons(weatherKind, dayTime);
    }

    @Size(3)
    public static Animator[] getWeatherAnimators(ResourceProvider provider,
                                                 String weatherKind, boolean dayTime) {
        return provider.getWeatherAnimators(weatherKind, dayTime);
    }

    @NonNull
    public static Drawable getWidgetNotificationIcon(ResourceProvider provider,
                                                     String weatherInfo, boolean dayTime,
                                                     boolean minimal, String textColor) {
        if (minimal) {
            switch (textColor) {
                case "light":
                    return provider.getMinimalLightIcon(weatherInfo, dayTime);

                case "grey":
                    return provider.getMinimalGreyIcon(weatherInfo, dayTime);

                case "dark":
                    return provider.getMinimalDarkIcon(weatherInfo, dayTime);
            }
        }

        return provider.getWeatherIcon(weatherInfo, dayTime);
    }

    @NonNull
    public static Drawable getWidgetNotificationIcon(ResourceProvider provider,
                                                     String weatherInfo, boolean dayTime,
                                                     boolean minimal, boolean darkText) {
        return getWidgetNotificationIcon(
                provider, weatherInfo, dayTime, minimal, darkText ? "dark" : "light");
    }

    @NonNull
    public static Uri getWidgetNotificationIconUri(ResourceProvider provider,
                                                   String weatherInfo, boolean dayTime,
                                                   boolean minimal, String textColor) {
        if (minimal) {
            switch (textColor) {
                case "light":
                    return provider.getMinimalLightIconUri(weatherInfo, dayTime);

                case "grey":
                    return provider.getMinimalGreyIconUri(weatherInfo, dayTime);

                case "dark":
                    return provider.getMinimalDarkIconUri(weatherInfo, dayTime);
            }
        }

        return provider.getWeatherIconUri(weatherInfo, dayTime);
    }

    @NonNull
    public static Uri getWidgetNotificationIconUri(ResourceProvider provider,
                                                   String weatherInfo, boolean dayTime,
                                                   boolean minimal, boolean darkText) {
        return getWidgetNotificationIconUri(
                provider, weatherInfo, dayTime, minimal, darkText ? "dark" : "light");
    }

    @NonNull
    public static Drawable getMinimalXmlIcon(ResourceProvider provider,
                                             String weatherKind, boolean daytime) {
        return provider.getMinimalXmlIcon(weatherKind, daytime);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    public static Icon getMinimalIcon(ResourceProvider provider,
                                      String weatherKind, boolean daytime) {
        return provider.getMinimalIcon(weatherKind, daytime);
    }

    @DrawableRes
    public static int getDefaultMinimalXmlIconId(String weatherKind, boolean daytime) {
        int id = new DefaultResourceProvider().getMinimalXmlIconId(weatherKind, daytime);
        if (id == 0) {
            return R.drawable.weather_clear_day_mini_xml;
        } else {
            return id;
        }
    }

    @NonNull
    public static Drawable getShortcutsIcon(ResourceProvider provider,
                                            String weatherKind, boolean dayTime) {
        return provider.getShortcutsIcon(weatherKind, dayTime);
    }

    @NonNull
    public static Drawable getShortcutsForegroundIcon(ResourceProvider provider,
                                                      String weatherKind, boolean dayTime) {
        return provider.getShortcutsForegroundIcon(weatherKind, dayTime);
    }

    @NonNull
    public static Drawable getSunDrawable(ResourceProvider provider) {
        return provider.getSunDrawable();
    }

    @NonNull
    public static Drawable getMoonDrawable(ResourceProvider provider) {
        return provider.getMoonDrawable();
    }

    @DrawableRes
    public static int getTempIconId(Context context, int temp) {
        int id = ResourceUtils.getResId(
                context,
                "notif_temp_" + temp,
                "drawable"
        );
        if (id == 0) {
            return R.drawable.notif_temp_0;
        } else {
            return id;
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static String getWeek(Context c, String dateTxt) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(
                    Objects.requireNonNull(
                            simpleDateFormat.parse(dateTxt)
                    )
            );
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int day = calendar.get(Calendar.DAY_OF_WEEK);
        if (day == 1){
            return c.getString(R.string.week_7);
        } else if (day == 2) {
            return c.getString(R.string.week_1);
        } else if (day == 3) {
            return c.getString(R.string.week_2);
        } else if (day == 4) {
            return c.getString(R.string.week_3);
        } else if (day == 5) {
            return c.getString(R.string.week_4);
        } else if (day == 6) {
            return c.getString(R.string.week_5);
        } else {
            return c.getString(R.string.week_6);
        }
    }

    public static String getWindLevel(Context c, double speed) {
        if (speed <= 2) {
            return c.getString(R.string.wind_0);
        } else if (speed <= 6) {
            return c.getString(R.string.wind_1);
        } else if (speed <= 12) {
            return c.getString(R.string.wind_2);
        } else if (speed <= 19) {
            return c.getString(R.string.wind_3);
        } else if (speed <= 30) {
            return c.getString(R.string.wind_4);
        } else if (speed <= 40) {
            return c.getString(R.string.wind_5);
        } else if (speed <= 51) {
            return c.getString(R.string.wind_6);
        } else if (speed <= 62) {
            return c.getString(R.string.wind_7);
        } else if (speed <= 75) {
            return c.getString(R.string.wind_8);
        } else if (speed <= 87) {
            return c.getString(R.string.wind_9);
        } else if (speed <= 103) {
            return c.getString(R.string.wind_10);
        } else if (speed <= 117) {
            return c.getString(R.string.wind_11);
        } else {
            return c.getString(R.string.wind_12);
        }
    }

    public static int getWindColorResId(String speed) {
        double s = 0;
        try {
            s = Double.parseDouble(speed.split("km/h")[0]);
        } catch (Exception ignore) {

        }
        if (s <= 30) {
            return 0;
        } else if (s <= 51) {
            return 0;
        } else if (s <= 75) {
            return 0;
        } else if (s <= 103) {
            return R.color.colorLevel_4;
        } else if (s <= 117) {
            return R.color.colorLevel_5;
        } else {
            return R.color.colorLevel_6;
        }
    }

    public static String getCNWindName(int degree) {
        if (degree < 0) {
            return "无风向";
        }if (22.5 < degree && degree <= 67.5) {
            return "东北风";
        } else if (67.5 < degree && degree <= 112.5) {
            return "东风";
        } else if (112.5 < degree && degree <= 157.5) {
            return "东南风";
        } else if (157.5 < degree && degree <= 202.5) {
            return "南风";
        } else if (202.5 < degree && degree <= 247.5) {
            return "西南风";
        } else if (247.5 < degree && degree <= 292.5) {
            return "西风";
        } else if (292. < degree && degree <= 337.5) {
            return "西北风";
        } else {
            return "北风";
        }
    }

    public static String getWindArrows(int degree) {
        if (degree < 0) {
            return "";
        }if (22.5 < degree && degree <= 67.5) {
            return "↙";
        } else if (67.5 < degree && degree <= 112.5) {
            return "←";
        } else if (112.5 < degree && degree <= 157.5) {
            return "↖";
        } else if (157.5 < degree && degree <= 202.5) {
            return "↑";
        } else if (202.5 < degree && degree <= 247.5) {
            return "↗";
        } else if (247.5 < degree && degree <= 292.5) {
            return "→";
        } else if (292. < degree && degree <= 337.5) {
            return "↘";
        } else {
            return "↓";
        }
    }

    public static String getCNUVIndex(String number) {
        int num = Integer.parseInt(number);
        if (num <= 2) {
            return "最弱";
        } else if (num <= 4) {
            return "弱";
        } else if (num <= 6) {
            return "中等";
        } else if (num <= 9) {
            return "强";
        } else {
            return "很强";
        }
    }

    @SuppressLint("DefaultLocale")
    public static String getWindSpeed(Context context, double speed) {
        return SettingsOptionManager.getInstance(context).isImperial()
                ? (String.format("%.1f", speed * 0.621F) + "mi/h")
                : (speed + "km/h");
    }

    public static String getWindSpeed(Context context, String speed) {
        if (speed.endsWith("km/h")) {
            return getWindSpeed(
                    context,
                    Double.parseDouble(
                            speed.replace("km/h", "")
                    )
            );
        } else if (speed.endsWith("mi/h")) {
            return getWindSpeed(
                    context,
                    Double.parseDouble(
                            speed.replace("mi/h", "")
                    ) * 1.6093
            );
        } else {
            try {
                return getWindSpeed(context, Double.parseDouble(speed));
            } catch (Exception e) {
                return speed;
            }
        }
    }
/*
    public static int getPrecipitation(int precipitation) {
        if (precipitation < 3) {
            return 10;
        } else if (precipitation < 6) {
            return 30;
        } else if (precipitation < 9) {
            return 60;
        } else {
            return 90;
        }
    }
*/
    public static String getAqiQuality(Context c, int index) {
        if (index <= 50) {
            return c.getString(R.string.aqi_1);
        } else if (index <= 100) {
            return c.getString(R.string.aqi_2);
        } else if (index <= 150) {
            return c.getString(R.string.aqi_3);
        } else if (index <= 200) {
            return c.getString(R.string.aqi_4);
        } else if (index <= 300) {
            return c.getString(R.string.aqi_5);
        } else {
            return c.getString(R.string.aqi_6);
        }
    }

    public static int getAqiColorResId(int index) {
        if (index <= 50) {
            return 0;
        } else if (index <= 100) {
            return 0;
        } else if (index <= 150) {
            return 0;
        } else if (index <= 200) {
            return R.color.colorLevel_4;
        } else if (index <= 300) {
            return R.color.colorLevel_5;
        } else {
            return R.color.colorLevel_6;
        }
    }

    @ColorInt
    public static int getAqiColor(Context context, int index) {
        if (index <= 50) {
            return ContextCompat.getColor(context, R.color.colorLevel_1);
        } else if (index <= 100) {
            return ContextCompat.getColor(context, R.color.colorLevel_2);
        } else if (index <= 150) {
            return ContextCompat.getColor(context, R.color.colorLevel_3);
        } else if (index <= 200) {
            return ContextCompat.getColor(context, R.color.colorLevel_4);
        } else if (index <= 300) {
            return ContextCompat.getColor(context, R.color.colorLevel_5);
        } else {
            return ContextCompat.getColor(context, R.color.colorLevel_6);
        }
    }

    @ColorInt
    public static int getPm25Color(Context context, int index) {
        if (index <= 35) {
            return ContextCompat.getColor(context, R.color.colorLevel_1);
        } else if (index <= 75) {
            return ContextCompat.getColor(context, R.color.colorLevel_2);
        } else if (index <= 115) {
            return ContextCompat.getColor(context, R.color.colorLevel_3);
        } else if (index <= 150) {
            return ContextCompat.getColor(context, R.color.colorLevel_4);
        } else if (index <= 250) {
            return ContextCompat.getColor(context, R.color.colorLevel_5);
        } else {
            return ContextCompat.getColor(context, R.color.colorLevel_6);
        }
    }

    @ColorInt
    public static int getPm10Color(Context context, int index) {
        if (index <= 50) {
            return ContextCompat.getColor(context, R.color.colorLevel_1);
        } else if (index <= 150) {
            return ContextCompat.getColor(context, R.color.colorLevel_2);
        } else if (index <= 250) {
            return ContextCompat.getColor(context, R.color.colorLevel_3);
        } else if (index <= 350) {
            return ContextCompat.getColor(context, R.color.colorLevel_4);
        } else if (index <= 420) {
            return ContextCompat.getColor(context, R.color.colorLevel_5);
        } else {
            return ContextCompat.getColor(context, R.color.colorLevel_6);
        }
    }

    @ColorInt
    public static int getSo2Color(Context context, int index) {
        if (index <= 50) {
            return ContextCompat.getColor(context, R.color.colorLevel_1);
        } else if (index <= 150) {
            return ContextCompat.getColor(context, R.color.colorLevel_2);
        } else if (index <= 475) {
            return ContextCompat.getColor(context, R.color.colorLevel_3);
        } else if (index <= 800) {
            return ContextCompat.getColor(context, R.color.colorLevel_4);
        } else if (index <= 1600) {
            return ContextCompat.getColor(context, R.color.colorLevel_5);
        } else {
            return ContextCompat.getColor(context, R.color.colorLevel_6);
        }
    }

    @ColorInt
    public static int getNo2Color(Context context, int index) {
        if (index <= 40) {
            return ContextCompat.getColor(context, R.color.colorLevel_1);
        } else if (index <= 80) {
            return ContextCompat.getColor(context, R.color.colorLevel_2);
        } else if (index <= 180) {
            return ContextCompat.getColor(context, R.color.colorLevel_3);
        } else if (index <= 280) {
            return ContextCompat.getColor(context, R.color.colorLevel_4);
        } else if (index <= 565) {
            return ContextCompat.getColor(context, R.color.colorLevel_5);
        } else {
            return ContextCompat.getColor(context, R.color.colorLevel_6);
        }
    }

    @ColorInt
    public static int getO3Color(Context context, int index) {
        if (index <= 160) {
            return ContextCompat.getColor(context, R.color.colorLevel_1);
        } else if (index <= 200) {
            return ContextCompat.getColor(context, R.color.colorLevel_2);
        } else if (index <= 300) {
            return ContextCompat.getColor(context, R.color.colorLevel_3);
        } else if (index <= 400) {
            return ContextCompat.getColor(context, R.color.colorLevel_4);
        } else if (index <= 800) {
            return ContextCompat.getColor(context, R.color.colorLevel_5);
        } else {
            return ContextCompat.getColor(context, R.color.colorLevel_6);
        }
    }

    @ColorInt
    public static int getCOColor(Context context, float index) {
        if (index <= 5) {
            return ContextCompat.getColor(context, R.color.colorLevel_1);
        } else if (index <= 10) {
            return ContextCompat.getColor(context, R.color.colorLevel_2);
        } else if (index <= 35) {
            return ContextCompat.getColor(context, R.color.colorLevel_3);
        } else if (index <= 60) {
            return ContextCompat.getColor(context, R.color.colorLevel_4);
        } else if (index <= 90) {
            return ContextCompat.getColor(context, R.color.colorLevel_5);
        } else {
            return ContextCompat.getColor(context, R.color.colorLevel_6);
        }
    }

    public static String getMoonPhaseName(Context context, @Nullable String phase) {
        if (TextUtils.isEmpty(phase)) {
            return context.getString(R.string.phase_new);
        }
        switch (phase.toLowerCase()) {
            case "waxingcrescent":
            case "waxing crescent":
                return context.getString(R.string.phase_waxing_crescent);

            case "first":
            case "firstquarter":
            case "first quarter":
                return context.getString(R.string.phase_first);

            case "waxinggibbous":
            case "waxing gibbous":
                return context.getString(R.string.phase_waxing_gibbous);

            case "full":
            case "fullmoon":
            case "full moon":
                return context.getString(R.string.phase_full);

            case "waninggibbous":
            case "waning gibbous":
                return context.getString(R.string.phase_waning_gibbous);

            case "third":
            case "thirdquarter":
            case "third quarter":
            case "last":
            case "lastquarter":
            case "last quarter":
                return context.getString(R.string.phase_third);

            case "waningcrescent":
            case "waning crescent":
                return context.getString(R.string.phase_waning_crescent);

            default:
                return context.getString(R.string.phase_new);
        }
    }

    public static int getMoonPhaseAngle(@Nullable String phase) {
        if (TextUtils.isEmpty(phase)) {
            return 0;
        }
        switch (phase.toLowerCase()) {
            case "waxingcrescent":
            case "waxing crescent":
                return 45;

            case "first":
            case "firstquarter":
            case "first quarter":
                return 90;

            case "waxinggibbous":
            case "waxing gibbous":
                return 135;

            case "full":
            case "fullmoon":
            case "full moon":
                return 180;

            case "waninggibbous":
            case "waning gibbous":
                return 225;

            case "third":
            case "thirdquarter":
            case "third quarter":
            case "last":
            case "lastquarter":
            case "last quarter":
                return 270;

            case "waningcrescent":
            case "waning crescent":
                return 315;

            default:
                return 360;
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static String buildTime(Context c) {
        if (TimeManager.is12Hour(c)) {
            return new SimpleDateFormat("h:mm aa").format(new Date(System.currentTimeMillis()));
        }
        return new SimpleDateFormat("HH:mm").format(new Date(System.currentTimeMillis()));
    }

    public static String buildTime(Context c, String hourString) {
        if (TimeManager.is12Hour(c)) {
            try {
                int hour = Integer.parseInt(hourString);
                if (hour == 0) {
                    hour = 24;
                }
                if (hour > 12) {
                    hour -= 12;
                }
                return hour + c.getString(R.string.of_clock);
            } catch (Exception ignored) {
                // do nothing.
            }
        }
        return hourString + c.getString(R.string.of_clock);
    }

    // interface.

    public interface OnRequestWeatherListener {
        void requestWeatherSuccess(@Nullable Weather weather, @Nullable History history,
                                   @NonNull Location requestLocation);
        void requestWeatherFailed(@NonNull Location requestLocation);
    }

    public interface OnRequestLocationListener {
        void requestLocationSuccess(String query, List<Location> locationList);
        void requestLocationFailed(String query);
    }
}
