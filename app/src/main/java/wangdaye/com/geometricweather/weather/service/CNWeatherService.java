package wangdaye.com.geometricweather.weather.service;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Retrofit;
import wangdaye.com.geometricweather.BuildConfig;
import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.weather.SchedulerTransformer;
import wangdaye.com.geometricweather.weather.api.CNWeatherApi;
import wangdaye.com.geometricweather.basic.model.CNCity;
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
import wangdaye.com.geometricweather.weather.json.cn.CNWeatherResult;
import wangdaye.com.geometricweather.utils.FileUtils;
import wangdaye.com.geometricweather.weather.interceptor.GzipInterceptor;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.weather.WeatherHelper;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.weather.observer.BaseObserver;
import wangdaye.com.geometricweather.weather.observer.ObserverContainer;

/**
 * CN weather service.
 * */

public class CNWeatherService extends WeatherService {

    private CNWeatherApi api;
    private CompositeDisposable compositeDisposable;

    public CNWeatherService() {
        api = new Retrofit.Builder()
                .baseUrl(BuildConfig.CN_WEATHER_BASE_URL)
                .client(
                        GeometricWeather.getInstance()
                                .getOkHttpClient()
                                .newBuilder()
                                .addInterceptor(new GzipInterceptor())
                                .build()
                ).addConverterFactory(GeometricWeather.getInstance().getGsonConverterFactory())
                .addCallAdapterFactory(GeometricWeather.getInstance().getRxJava2CallAdapterFactory())
                .build()
                .create((CNWeatherApi.class));
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void requestWeather(Context context,
                               Location location, @NonNull RequestWeatherCallback callback) {
        api.getWeather(location.cityId)
                .compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(compositeDisposable, new BaseObserver<CNWeatherResult>() {
                    @Override
                    public void onSucceed(CNWeatherResult cnWeatherResult) {
                        try {
                            History history = new History(
                                    location.cityId,
                                    location.getCityName(context),
                                    cnWeatherResult.weather.get(0).date,
                                    Integer.parseInt(cnWeatherResult.weather.get(0).info.day.get(2)),
                                    Integer.parseInt(cnWeatherResult.weather.get(0).info.night.get(2))
                            );
                            cnWeatherResult.weather.remove(0);

                            Base base = new Base(
                                    location.cityId, location.getCityName(context),
                                    cnWeatherResult.realtime.date,
                                    WeatherHelper.buildTime(context),
                                    System.currentTimeMillis()
                            );

                            RealTime realTime = new RealTime(
                                    cnWeatherResult.realtime.weather.info,
                                    getWeatherKind(cnWeatherResult.realtime.weather.img),
                                    Integer.parseInt(cnWeatherResult.realtime.weather.temperature),
                                    Integer.parseInt(cnWeatherResult.realtime.feelslike_c),
                                    cnWeatherResult.realtime.wind.direct,
                                    WeatherHelper.getWindSpeed(cnWeatherResult.realtime.wind.windspeed),
                                    cnWeatherResult.realtime.wind.power,
                                    -1,
                                    ""
                            );

                            List<Daily> dailyList = new ArrayList<>();
                            for (CNWeatherResult.WeatherX w : cnWeatherResult.weather) {
                                Daily daily = new Daily(
                                        w.date, WeatherHelper.getWeek(context, w.date),
                                        new String[] {
                                                w.info.day.get(1),
                                                w.info.night.get(1)},
                                        new String[] {
                                                getWeatherKind(w.info.day.get(0)),
                                                getWeatherKind(w.info.night.get(0))},
                                        new int[] {
                                                Integer.parseInt(w.info.day.get(2)),
                                                Integer.parseInt(w.info.night.get(2))},
                                        new String[] {
                                                w.info.day.get(3),
                                                w.info.night.get(3)},
                                        new String[] {null, null},
                                        new String[] {
                                                w.info.day.get(4),
                                                w.info.night.get(4)},
                                        new int[] {-1, -1},
                                        new String[] {
                                                w.info.day.get(5),
                                                w.info.night.get(5),
                                                "",
                                                ""
                                        },
                                        "",
                                        new int[] {-1, -1}
                                );

                                dailyList.add(daily);
                            }

                            List<Hourly> hourlyList = new ArrayList<>();
                            for (CNWeatherResult.HourlyForecast h : cnWeatherResult.hourly_forecast) {
                                Hourly hourly = new Hourly(
                                        WeatherHelper.buildTime(context, h.hour),
                                        TimeManager.isDaylight(
                                                h.hour,
                                                cnWeatherResult.weather.get(0).info.day.get(5),
                                                cnWeatherResult.weather.get(0).info.night.get(5)
                                        ),
                                        h.info,
                                        getWeatherKind(h.img),
                                        Integer.parseInt(h.temperature),
                                        -1);
                                hourlyList.add(hourly);
                            }

                            float co;
                            try {
                                co = Float.parseFloat(cnWeatherResult.pm25.co);
                            } catch (Exception e) {
                                co = -1;
                            }
                            Aqi aqi = new Aqi(
                                    WeatherHelper.getAqiQuality(context, cnWeatherResult.pm25.aqi),
                                    cnWeatherResult.pm25.aqi,
                                    cnWeatherResult.pm25.pm25,
                                    cnWeatherResult.pm25.pm10,
                                    cnWeatherResult.pm25.so2,
                                    cnWeatherResult.pm25.no2,
                                    cnWeatherResult.pm25.o3,
                                    co);

                            Index index = new Index(
                                    "",
                                    cnWeatherResult.life.info.daisan.get(1),
                                    context.getString(R.string.live) + " : " + cnWeatherResult.realtime.wind.direct
                                            + " " + WeatherHelper.getWindSpeed(cnWeatherResult.realtime.wind.windspeed)
                                            + " (" + cnWeatherResult.realtime.wind.power + ")",
                                    context.getString(R.string.daytime) + " : " + cnWeatherResult.weather.get(0).info.day.get(3)
                                            + " " + WeatherHelper.getWindSpeed(cnWeatherResult.weather.get(0).info.day.get(4)) + "\n"
                                            + context.getString(R.string.nighttime) + " : " + cnWeatherResult.weather.get(0).info.night.get(3)
                                            + " " + WeatherHelper.getWindSpeed(cnWeatherResult.weather.get(0).info.night.get(4)),
                                    context.getString(R.string.sensible_temp) + " : " + cnWeatherResult.realtime.feelslike_c + "℃",
                                    context.getString(R.string.humidity) + " : " + cnWeatherResult.realtime.weather.humidity,
                                    cnWeatherResult.life.info.ziwaixian.get(0) + "。" + cnWeatherResult.life.info.ziwaixian.get(1),
                                    cnWeatherResult.realtime.pressure + "hPa",
                                    "",
                                    "");

                            List<Alert> alertList = new ArrayList<>();
                            for (CNWeatherResult.Alert a : cnWeatherResult.alert) {
                                int id;
                                try {
                                    String[] dates = a.pubTime.split(" ")[0].split("-");
                                    String[] times = a.pubTime.split(" ")[1].split(":");
                                    id = Integer.parseInt(a.alarmPic2 + a.alarmPic1 + dates[2] + times[0]);
                                } catch (Exception e) {
                                    id = 0;
                                }
                                Alert alert = new Alert(
                                        id,
                                        a.alarmTp1 + a.alarmTp2 + context.getString(R.string.action_alert),
                                        a.content,
                                        context.getString(R.string.publish_at) + " " + a.pubTime
                                );
                                alertList.add(alert);
                            }

                            Weather weather = new Weather(base, realTime, dailyList, hourlyList, aqi, index, alertList);

                            callback.requestWeatherSuccess(weather, history, location);
                        } catch (Exception e) {
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
    public void requestLocation(Context context, String query, @NonNull RequestLocationCallback callback) {
        searchInThread(context, new String[] {query}, true, callback);
    }

    @Override
    public void requestLocation(Context context, Location location, @NonNull RequestLocationCallback callback) {
        if (!location.hasGeocodeInformation()) {
            callback.requestLocationFailed(null);
        }
        searchInThread(
                context,
                new String[] {location.district, location.city, location.province},
                false,
                callback
        );
    }

    @Override
    public void cancel() {
        compositeDisposable.clear();
    }

    private void searchInThread(Context context, String[] queries, boolean fuzzy,
                                RequestLocationCallback callback) {
        if (callback == null) {
            return;
        }

        for (int i = 0; i < queries.length; i ++) {
            queries[i] = formatLocationString(convertChinese(queries[i]));
        }

        Observable.create((ObservableOnSubscribe<List<Location>>) emitter -> {

            if (DatabaseHelper.getInstance(context).countCNCity() < 3216) {
                DatabaseHelper.getInstance(context).writeCityList(FileUtils.readCityList(context));
            }

            List<Location> locationList = new ArrayList<>();
            List<CNCity> cityList;
            CNCity city;

            if (fuzzy) {
                cityList = DatabaseHelper.getInstance(context).fuzzyReadCNCity(queries[0]);
                if (cityList != null) {
                    for (CNCity c : cityList) {
                        locationList.add(c.toLocation());
                    }
                }
            } else {
                if (queries.length == 3) {
                    city = DatabaseHelper.getInstance(context).readCNCity(queries[0], queries[1], queries[2]);
                    if (city != null) {
                        locationList.add(city.toLocation());
                    }
                } else {
                    city = DatabaseHelper.getInstance(context).readCNCity(queries[0]);
                    if (city != null) {
                        locationList.add(city.toLocation());
                    }
                }
            }

            emitter.onNext(locationList);
        }).compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(compositeDisposable, new BaseObserver<List<Location>>() {
                    @Override
                    public void onSucceed(List<Location> locations) {
                        if (locations.size() > 0) {
                            callback.requestLocationSuccess(queries[0], locations);
                        } else {
                            callback.requestLocationFailed(queries[0]);
                        }
                    }

                    @Override
                    public void onFailed() {
                        callback.requestLocationFailed(queries[0]);
                    }
                }));
    }

    static String getWeatherKind(String icon) {
        if (TextUtils.isEmpty(icon)) {
            return Weather.KIND_CLOUDY;
        }

        switch (icon) {
            case "0":
            case "00":
                return Weather.KIND_CLEAR;

            case "1":
            case "01":
                return Weather.KIND_PARTLY_CLOUDY;

            case "2":
            case "02":
                return Weather.KIND_CLOUDY;

            case "3":
            case "7":
            case "8":
            case "9":
            case "03":
            case "07":
            case "08":
            case "09":
            case "10":
            case "11":
            case "12":
            case "21":
            case "22":
            case "23":
            case "24":
            case "25":
                return Weather.KIND_RAIN;

            case "4":
            case "04":
                return Weather.KIND_THUNDERSTORM;

            case "5":
            case "05":
                return Weather.KIND_HAIL;

            case "6":
            case "06":
                return Weather.KIND_SLEET;

            case "13":
            case "14":
            case "15":
            case "16":
            case "17":
            case "26":
            case "27":
            case "28":
                return Weather.KIND_SNOW;

            case "18":
            case "32":
            case "49":
            case "57":
                return Weather.KIND_FOG;

            case "19":
                return Weather.KIND_SLEET;

            case "20":
            case "29":
            case "30":
                return Weather.KIND_WIND;

            case "53":
            case "54":
            case "55":
            case "56":
                return Weather.KIND_HAZE;

            default:
                return Weather.KIND_CLOUDY;
        }
    }
}