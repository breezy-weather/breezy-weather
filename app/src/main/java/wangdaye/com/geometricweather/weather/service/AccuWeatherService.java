package wangdaye.com.geometricweather.weather.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Retrofit;
import wangdaye.com.geometricweather.BuildConfig;
import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.weather.SchedulerTransformer;
import wangdaye.com.geometricweather.weather.api.AccuWeatherApi;
import wangdaye.com.geometricweather.basic.model.History;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Alert;
import wangdaye.com.geometricweather.basic.model.weather.Aqi;
import wangdaye.com.geometricweather.basic.model.weather.Base;
import wangdaye.com.geometricweather.basic.model.weather.Daily;
import wangdaye.com.geometricweather.basic.model.weather.Hourly;
import wangdaye.com.geometricweather.basic.model.weather.Index;
import wangdaye.com.geometricweather.basic.model.weather.RealTime;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.weather.json.accu.AccuAlertResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuAqiResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuDailyResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuHourlyResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuLocationResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuMinuteResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuRealtimeResult;
import wangdaye.com.geometricweather.weather.interceptor.GzipInterceptor;
import wangdaye.com.geometricweather.utils.LanguageUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.weather.WeatherHelper;
import wangdaye.com.geometricweather.weather.observer.BaseObserver;
import wangdaye.com.geometricweather.weather.observer.ObserverContainer;

/**
 * Accu weather service.
 * */

public class AccuWeatherService extends WeatherService {

    private AccuWeatherApi api;
    private CompositeDisposable compositeDisposable;

    private static final String PREFERENCE_LOCAL = "LOCAL_PREFERENCE";
    private static final String KEY_OLD_DISTRICT = "OLD_DISTRICT";
    private static final String KEY_OLD_CITY = "OLD_CITY";
    private static final String KEY_OLD_PROVINCE = "OLD_PROVINCE";
    private static final String KEY_OLD_KEY = "OLD_KEY";

    private class CacheLocationRequestCallback implements RequestLocationCallback {

        private Context context;
        @NonNull private RequestLocationCallback callback;

        CacheLocationRequestCallback(Context context, @NonNull RequestLocationCallback callback) {
            this.context = context;
            this.callback = callback;
        }

        @Override
        public void requestLocationSuccess(String query, List<Location> locationList) {
            if (!TextUtils.isEmpty(locationList.get(0).cityId)) {
                context.getSharedPreferences(PREFERENCE_LOCAL, Context.MODE_PRIVATE)
                        .edit()
                        .putString(KEY_OLD_KEY, locationList.get(0).cityId)
                        .apply();
            }
            callback.requestLocationSuccess(query, locationList);
        }

        @Override
        public void requestLocationFailed(String query) {
            context.getSharedPreferences(PREFERENCE_LOCAL, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_OLD_DISTRICT, "")
                    .putString(KEY_OLD_CITY, "")
                    .putString(KEY_OLD_PROVINCE, "")
                    .putString(KEY_OLD_KEY, "")
                    .apply();
            callback.requestLocationFailed(query);
        }
    }

    private class EmptyMinuteResult extends AccuMinuteResult {
    }

    private class EmptyAqiResult extends AccuAqiResult {
    }

    public AccuWeatherService() {
        api = new Retrofit.Builder()
                .baseUrl(BuildConfig.ACCU_WEATHER_BASE_URL)
                .client(
                        GeometricWeather.getInstance()
                                .getOkHttpClient()
                                .newBuilder()
                                .addInterceptor(new GzipInterceptor())
                                .build()
                ).addConverterFactory(GeometricWeather.getInstance().getGsonConverterFactory())
                .addCallAdapterFactory(GeometricWeather.getInstance().getRxJava2CallAdapterFactory())
                .build()
                .create((AccuWeatherApi.class));
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void requestWeather(Context context, Location location, @NonNull RequestWeatherCallback callback) {
        String languageCode = LanguageUtils.getLanguageCode(context);

        Observable<List<AccuRealtimeResult>> realtime = api.getRealtime(
                location.cityId, BuildConfig.ACCU_CURRENT_KEY, languageCode, true);

        Observable<AccuDailyResult> daily = api.getDaily(
                location.cityId, BuildConfig.ACCU_WEATHER_KEY, languageCode, true, true);

        Observable<List<AccuHourlyResult>> hourly = api.getHourly(
                location.cityId, BuildConfig.ACCU_WEATHER_KEY, languageCode, true);

        Observable<AccuMinuteResult> minute = api.getMinute(
                BuildConfig.ACCU_WEATHER_KEY, languageCode, true, location.lat + "," + location.lon)
                .onExceptionResumeNext(Observable.create(emitter -> emitter.onNext(new EmptyMinuteResult())));

        Observable<List<AccuAlertResult>> alert = api.getAlert(
                location.cityId, BuildConfig.ACCU_WEATHER_KEY, languageCode, true);

        Observable<AccuAqiResult> aqi = api.getAqi(location.cityId, BuildConfig.ACCU_AQI_KEY)
                .onExceptionResumeNext(Observable.create(emitter -> emitter.onNext(new EmptyAqiResult())));

        Observable.zip(realtime, daily, hourly, minute, alert, aqi,
                (accuRealtimeResults,
                 accuDailyResult, accuHourlyResults, accuMinuteResult,
                 accuAlertResults, accuAqiResult) -> {
            buildWeatherAndHistory(
                    context,
                    location,
                    accuRealtimeResults.get(0),
                    accuDailyResult,
                    accuHourlyResults,
                    accuMinuteResult instanceof EmptyMinuteResult ? null : accuMinuteResult,
                    accuAqiResult instanceof EmptyAqiResult ? null : accuAqiResult,
                    accuAlertResults
            );
            return location;
        }).compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(compositeDisposable, new BaseObserver<Location>() {
                    @Override
                    public void onSucceed(Location location) {
                        if (location.weather != null) {
                            callback.requestWeatherSuccess(location.weather, location.history, location);
                        } else {
                            callback.requestWeatherFailed(location);
                        }
                    }

                    @Override
                    public void onFailed() {
                        callback.requestWeatherFailed(location);
                    }
                }));

    }

    @Override
    public void requestLocation(Context context, final String query,
                                @NonNull final RequestLocationCallback callback) {
        String languageCode = LanguageUtils.getLanguageCode(context);
        api.getWeatherLocation("Always", BuildConfig.ACCU_WEATHER_KEY, query, languageCode)
                .compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(compositeDisposable, new BaseObserver<List<AccuLocationResult>>() {
                    @Override
                    public void onSucceed(List<AccuLocationResult> accuLocationResults) {
                        if (accuLocationResults != null && accuLocationResults.size() != 0) {
                            List<Location> locationList = new ArrayList<>();
                            for (AccuLocationResult r : accuLocationResults) {
                                locationList.add(r.toLocation());
                            }
                            callback.requestLocationSuccess(query, locationList);
                        } else {
                            callback.requestLocationFailed(query);
                        }
                    }

                    @Override
                    public void onFailed() {
                        callback.requestLocationFailed(query);
                    }
                }));
    }

    @Override
    public void requestLocation(Context context, Location location,
                                @NonNull RequestLocationCallback callback) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                PREFERENCE_LOCAL,
                Context.MODE_PRIVATE
        );
        String oldDistrict = sharedPreferences.getString(KEY_OLD_DISTRICT, "");
        String oldCity = sharedPreferences.getString(KEY_OLD_CITY, "");
        String oldProvince = sharedPreferences.getString(KEY_OLD_PROVINCE, "");
        String oldKey = sharedPreferences.getString(KEY_OLD_KEY, "");

        if (location.hasGeocodeInformation()
                && queryEqualsIgnoreEmpty(location.district, oldDistrict)
                && queryEquals(location.city, oldCity)
                && queryEquals(location.province, oldProvince)
                && queryEquals(location.cityId, oldKey)) {
            List<Location> locationList = new ArrayList<>();
            locationList.add(location);
            callback.requestLocationSuccess(location.lat + "," + location.lon, locationList);
            return;
        }

        sharedPreferences.edit()
                .putString(KEY_OLD_DISTRICT, location.district)
                .putString(KEY_OLD_CITY, location.city)
                .putString(KEY_OLD_PROVINCE, location.province)
                .apply();

        if (GeometricWeather.getInstance().getLocationService().equals("baidu_ip")) {
            requestLocation(
                    context,
                    TextUtils.isEmpty(location.district)
                            ? formatLocationString(convertChinese(location.city))
                            : formatLocationString(convertChinese(location.district)),
                    new CacheLocationRequestCallback(context, callback)
            );
        } else {
            requestLocation(
                    context,
                    location.lat,
                    location.lon,
                    new CacheLocationRequestCallback(context, callback)
            );
        }
    }

    public void requestLocation(Context context, final String lat, final String lon,
                                @NonNull final RequestLocationCallback callback) {
        String languageCode = LanguageUtils.getLanguageCode(context);
        api.getWeatherLocationByGeoPosition(
                "Always",
                BuildConfig.ACCU_WEATHER_KEY,
                lat + "," + lon,
                languageCode
        ).compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(compositeDisposable, new BaseObserver<AccuLocationResult>() {
                    @Override
                    public void onSucceed(AccuLocationResult accuLocationResult) {
                        if (accuLocationResult != null) {
                            List<Location> locationList = new ArrayList<>();
                            locationList.add(accuLocationResult.toLocation());
                            callback.requestLocationSuccess(lat + ", " + lon, locationList);
                        } else {
                            callback.requestLocationFailed(lat + ", " + lon);
                        }
                    }

                    @Override
                    public void onFailed() {

                    }
                }));
    }

    @Override
    public void cancel() {
        compositeDisposable.clear();
    }

    private boolean queryEquals(String a, String b) {
        if (!TextUtils.isEmpty(a) && !TextUtils.isEmpty(b)) {
            return a.equals(b);
        }
        return false;
    }

    private boolean queryEqualsIgnoreEmpty(String a, String b) {
        if (TextUtils.isEmpty(a) && TextUtils.isEmpty(b)) {
            return true;
        }
        if (!TextUtils.isEmpty(a) && !TextUtils.isEmpty(b)) {
            return a.equals(b);
        }
        return false;
    }

    @SuppressLint("SimpleDateFormat")
    private void buildWeatherAndHistory(Context context,
                                        Location location,
                                        AccuRealtimeResult realtimeResult,
                                        AccuDailyResult dailyResult,
                                        List<AccuHourlyResult> hourlyResultList,
                                        @Nullable AccuMinuteResult minuteResult,
                                        @Nullable AccuAqiResult aqiResult,
                                        List<AccuAlertResult> alertResultList) {
        try {
            Base base = new Base(
                    location.cityId, location.getCityName(context),
                    realtimeResult.LocalObservationDateTime.split("T")[0],
                    WeatherHelper.buildTime(context),
                    System.currentTimeMillis()
            );

            RealTime realTime = new RealTime(
                    realtimeResult.WeatherText,
                    getWeatherKind(realtimeResult.WeatherIcon),
                    (int) realtimeResult.Temperature.Metric.Value,
                    (int) realtimeResult.RealFeelTemperature.Metric.Value,
                    realtimeResult.Wind.Direction.Localized,
                    WeatherHelper.getWindSpeed(realtimeResult.Wind.Speed.Metric.Value),
                    WeatherHelper.getWindLevel(context, realtimeResult.Wind.Speed.Metric.Value),
                    realtimeResult.Wind.Direction.Degrees,
                    dailyResult.Headline.Text
            );

            List<Daily> dailyList = new ArrayList<>();
            for (AccuDailyResult.DailyForecasts f : dailyResult.DailyForecasts) {
                String date = f.Date.split("T")[0];
                String[] astros;
                if (!TextUtils.isEmpty(f.Moon.Rise) && !TextUtils.isEmpty(f.Moon.Set)
                        && !TextUtils.isEmpty(f.Moon.Rise) && !TextUtils.isEmpty(f.Moon.Set)) {
                    astros = new String[] {
                            f.Sun.Rise.split("T")[1].split(":")[0]
                                    + ":"
                                    + f.Sun.Rise.split("T")[1].split(":")[1],
                            f.Sun.Set.split("T")[1].split(":")[0]
                                    + ":"
                                    + f.Sun.Set.split("T")[1].split(":")[1],
                            f.Moon.Rise.split("T")[1].split(":")[0]
                                    + ":"
                                    + f.Moon.Rise.split("T")[1].split(":")[1],
                            f.Moon.Set.split("T")[1].split(":")[0]
                                    + ":"
                                    + f.Moon.Set.split("T")[1].split(":")[1]
                    };
                } else if (!TextUtils.isEmpty(f.Moon.Rise) && !TextUtils.isEmpty(f.Moon.Set)) {
                    astros = new String[] {
                            f.Sun.Rise.split("T")[1].split(":")[0]
                                    + ":"
                                    + f.Sun.Rise.split("T")[1].split(":")[1],
                            f.Sun.Set.split("T")[1].split(":")[0]
                                    + ":"
                                    + f.Sun.Set.split("T")[1].split(":")[1],
                            "", ""
                    };
                } else {
                    astros = new String[] {"6:00", "18:00", "", ""};

                }
                Daily daily = new Daily(
                        date,
                        WeatherHelper.getWeek(context, date),
                        new String[] {
                                f.Day.IconPhrase,
                                f.Night.IconPhrase
                        }, new String[] {
                                getWeatherKind(f.Day.Icon),
                                getWeatherKind(f.Night.Icon)
                        }, new int[] {
                                (int) f.Temperature.Maximum.Value,
                                (int) f.Temperature.Minimum.Value
                        }, new String[] {
                                f.Day.Wind.Direction.Localized,
                                f.Night.Wind.Direction.Localized
                        }, new String[] {
                                WeatherHelper.getWindSpeed(f.Day.Wind.Speed.Value),
                                WeatherHelper.getWindSpeed(f.Night.Wind.Speed.Value)
                        }, new String[] {
                                WeatherHelper.getWindLevel(context, f.Day.Wind.Speed.Value),
                                WeatherHelper.getWindLevel(context, f.Night.Wind.Speed.Value)
                        }, new int[] {
                                f.Day.Wind.Direction.Degrees,
                                f.Night.Wind.Direction.Degrees
                        },
                        astros,
                        f.Moon.Phase,
                        new int[] {
                                f.Day.PrecipitationProbability,
                                f.Night.PrecipitationProbability
                        }
                );

                dailyList.add(daily);
            }

            List<Hourly> hourlyList = new ArrayList<>();
            for (AccuHourlyResult r : hourlyResultList) {
                Hourly hourly = new Hourly(
                        WeatherHelper.buildTime(
                                context,
                                r.DateTime.split("T")[1].split(":")[0]
                        ),
                        r.IsDaylight,
                        r.IconPhrase,
                        getWeatherKind(r.WeatherIcon),
                        (int) r.Temperature.Value, r.PrecipitationProbability
                );
                hourlyList.add(hourly);
            }

            Aqi aqi;
            if (aqiResult == null) {
                aqi = new Aqi("", -1, -1, -1, -1, -1, -1, -1);
            } else {
                aqi = new Aqi(
                        WeatherHelper.getAqiQuality(context, aqiResult.Index),
                        aqiResult.Index,
                        (int) aqiResult.ParticulateMatter2_5,
                        (int) aqiResult.ParticulateMatter10,
                        (int) aqiResult.SulfurDioxide,
                        (int) aqiResult.NitrogenDioxide,
                        (int) aqiResult.Ozone,
                        (float) aqiResult.CarbonMonoxide
                );
            }

            String briefing = "";
            if (minuteResult != null) {
                briefing = minuteResult.getSummary().getLongPhrase();
            }
            String pressure;
            if (GeometricWeather.getInstance().isImperial()) {
                pressure = realtimeResult.Pressure.Imperial.Value + realtimeResult.Pressure.Imperial.Unit;
            } else {
                pressure = realtimeResult.Pressure.Metric.Value + realtimeResult.Pressure.Metric.Unit;
            }
            String visibility;
            if (GeometricWeather.getInstance().isImperial()) {
                visibility = realtimeResult.Visibility.Imperial.Value + realtimeResult.Visibility.Imperial.Unit;
            } else {
                visibility = realtimeResult.Visibility.Metric.Value + realtimeResult.Visibility.Metric.Unit;
            }
            Index index = new Index(
                    dailyResult.Headline.Text,
                    briefing,
                    context.getString(R.string.live) + " : " + realtimeResult.Wind.Direction.Localized
                            + " " + WeatherHelper.getWindSpeed(realtimeResult.Wind.Speed.Metric.Value)
                            + " (" + WeatherHelper.getWindLevel(context, realtimeResult.Wind.Speed.Metric.Value) + ") "
                            + WeatherHelper.getWindArrows(realtimeResult.Wind.Direction.Degrees),
                    context.getString(R.string.daytime) + " : " + dailyResult.DailyForecasts.get(0).Day.Wind.Direction.Localized
                            + " " + WeatherHelper.getWindSpeed(dailyResult.DailyForecasts.get(0).Day.Wind.Speed.Value)
                            + " (" + WeatherHelper.getWindLevel(context, dailyResult.DailyForecasts.get(0).Day.Wind.Speed.Value) + ") "
                            + WeatherHelper.getWindArrows(dailyResult.DailyForecasts.get(0).Day.Wind.Direction.Degrees) + "\n"
                            + context.getString(R.string.nighttime) + " : " + dailyResult.DailyForecasts.get(0).Night.Wind.Direction.Localized
                            + " " + WeatherHelper.getWindSpeed(dailyResult.DailyForecasts.get(0).Night.Wind.Speed.Value)
                            + " (" + WeatherHelper.getWindLevel(context, dailyResult.DailyForecasts.get(0).Night.Wind.Speed.Value) + ") "
                            + WeatherHelper.getWindArrows(dailyResult.DailyForecasts.get(0).Night.Wind.Direction.Degrees),
                    context.getString(R.string.sensible_temp) + " : "
                            + ValueUtils.buildCurrentTemp(
                                    (int) realtimeResult.RealFeelTemperature.Metric.Value,
                                    false,
                                    GeometricWeather.getInstance().isFahrenheit()
                            ),
                    context.getString(R.string.humidity) + " : " + realtimeResult.RelativeHumidity + "%",
                    realtimeResult.UVIndex + " / " + realtimeResult.UVIndexText,
                    pressure,
                    visibility,
                    ValueUtils.buildCurrentTemp(
                            (int) realtimeResult.DewPoint.Metric.Value,
                            false,
                            GeometricWeather.getInstance().isFahrenheit()
                    )
            );

            List<Alert> alertList = new ArrayList<>();
            for (AccuAlertResult r : alertResultList) {
                Alert alert = new Alert(
                        r.AlertID,
                        r.Description.Localized,
                        r.Area.get(0).Text,
                        context.getString(R.string.publish_at) + " " + r.Area.get(0).StartTime.split("T")[0]
                                + " " + r.Area.get(0).StartTime.split("T")[1].split(":")[0]
                                + ":" + r.Area.get(0).StartTime.split("T")[1].split(":")[1]
                );
                alertList.add(alert);
            }

            location.weather = new Weather(base, realTime, dailyList, hourlyList, aqi, index, alertList);

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date date = format.parse(location.weather.base.date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DATE, -1);

            location.history = new History(
                    location.cityId, location.weather.base.city, format.format(calendar.getTime()),
                    (int) realtimeResult.TemperatureSummary.Past24HourRange.Maximum.Metric.Value,
                    (int) realtimeResult.TemperatureSummary.Past24HourRange.Minimum.Metric.Value
            );
        } catch (Exception ignored) {
            // do nothing.
        }
    }

    private static String getWeatherKind(int icon) {
        if (icon == 1 || icon == 2 || icon == 30
                || icon == 33 || icon == 34) {
            return Weather.KIND_CLEAR;
        } else if (icon == 3 || icon == 4 || icon == 6 || icon == 7
                || icon == 35 || icon == 36 || icon == 38) {
            return Weather.KIND_PARTLY_CLOUDY;
        } else if (icon == 5 || icon == 37) {
            return Weather.KIND_HAZE;
        } else if (icon == 8) {
            return Weather.KIND_CLOUDY;
        } else if (icon == 11) {
            return Weather.KIND_FOG;
        } else if (icon == 12 || icon == 13 || icon == 14 || icon == 18
                || icon == 39 || icon == 40) {
            return Weather.KIND_RAIN;
        } else if (icon == 15 || icon == 16 || icon == 17 || icon == 41 || icon == 42) {
            return Weather.KIND_THUNDERSTORM;
        } else if (icon == 19 || icon == 20 || icon == 21 || icon == 22 || icon == 23 || icon == 24
                || icon == 31 || icon == 43 || icon == 44) {
            return Weather.KIND_SNOW;
        } else if (icon == 25) {
            return Weather.KIND_HAIL;
        } else if (icon == 26 || icon == 29) {
            return Weather.KIND_SLEET;
        } else if (icon == 32) {
            return Weather.KIND_WIND;
        } else {
            return Weather.KIND_CLOUDY;
        }
    }
}