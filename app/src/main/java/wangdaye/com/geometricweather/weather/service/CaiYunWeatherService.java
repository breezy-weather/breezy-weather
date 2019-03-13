package wangdaye.com.geometricweather.weather.service;

import android.content.Context;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Retrofit;
import wangdaye.com.geometricweather.BuildConfig;
import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.weather.SchedulerTransformer;
import wangdaye.com.geometricweather.weather.api.CaiYunApi;
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
import wangdaye.com.geometricweather.weather.json.caiyun.CaiYunForecastResult;
import wangdaye.com.geometricweather.weather.json.caiyun.CaiYunMainlyResult;
import wangdaye.com.geometricweather.weather.GzipInterceptor;
import wangdaye.com.geometricweather.weather.WeatherHelper;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.weather.observer.BaseObserver;
import wangdaye.com.geometricweather.weather.observer.ObserverContainer;

/**
 * CaiYun weather service.
 * */

public class CaiYunWeatherService extends CNWeatherService {

    private CaiYunApi api;
    private CompositeDisposable compositeDisposable;

    public CaiYunWeatherService() {
        this.api = new Retrofit.Builder()
                .baseUrl(BuildConfig.CAIYUN_WEATHER_BASE_URL)
                .client(GeometricWeather.getInstance()
                        .getOkHttpClient()
                        .newBuilder()
                        .addInterceptor(new GzipInterceptor())
                        .build())
                .addConverterFactory(GeometricWeather.getInstance().getGsonConverterFactory())
                .addCallAdapterFactory(GeometricWeather.getInstance().getRxJava2CallAdapterFactory())
                .build()
                .create((CaiYunApi.class));
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void requestWeather(Context context,
                               Location location, @NonNull RequestWeatherCallback callback) {
        Observable<CaiYunMainlyResult> mainly = api.getMainlyWeather(
                location.lat,
                location.lon,
                location.isLocal(),
                "weathercn%3A" + location.cityId,
                15,
                "weather20151024",
                "zUFJoAR2ZVrDy1vF3D07",
                "V10.0.1.0.OAACNFH",
                "10010002",
                false,
                false,
                "gemini",
                "",
                "zh_cn");

        Observable<CaiYunForecastResult> forecast = api.getForecastWeather(
                location.lat,
                location.lon,
                "zh_cn",
                false,
                "weather20151024",
                "weathercn%3A" + location.cityId,
                "zUFJoAR2ZVrDy1vF3D07");

        Observable.zip(mainly, forecast, (mainlyResult, forecastResult) -> {
            buildWeatherAndHistory(context, location, mainlyResult, forecastResult);
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
    public void cancel() {
        super.cancel();
        compositeDisposable.clear();
    }

    private void buildWeatherAndHistory(Context context, Location location,
                                        CaiYunMainlyResult mainlyResult,
                                        CaiYunForecastResult forecastResult) {
        try {
            Base base = new Base(
                    location.cityId, location.getCityName(context),
                    mainlyResult.current.pubTime.split("T")[0],
                    WeatherHelper.buildTime(context),
                    System.currentTimeMillis());

            int windDegree = Integer.parseInt(mainlyResult.current.wind.direction.value);
            RealTime realTime = new RealTime(
                    WeatherHelper.getCNWeatherName(mainlyResult.current.weather),
                    WeatherHelper.getCNWeatherKind(mainlyResult.current.weather),
                    Integer.parseInt(mainlyResult.current.temperature.value),
                    Integer.parseInt(mainlyResult.current.feelsLike.value),
                    WeatherHelper.getCNWindName(windDegree),
                    WeatherHelper.getWindSpeed(Double.parseDouble(mainlyResult.current.wind.speed.value)),
                    WeatherHelper.getWindLevel(context, Double.parseDouble(mainlyResult.current.wind.speed.value)),
                    windDegree,
                    "");

            List<Daily> dailyList = new ArrayList<>();
            for (int i = 0; i < mainlyResult.forecastDaily.weather.value.size(); i ++) {
                String date = mainlyResult.forecastDaily.sunRiseSet.value.get(i).from.split("T")[0];
                int[] windDegrees = new int[] {
                        Integer.parseInt(mainlyResult.forecastDaily.wind.direction.value.get(i).from),
                        Integer.parseInt(mainlyResult.forecastDaily.wind.direction.value.get(i).to)};
                String[] windSpeeds = new String[] {
                        mainlyResult.forecastDaily.wind.speed.value.get(i).from,
                        mainlyResult.forecastDaily.wind.speed.value.get(i).to};
                String[] windLevels;
                try {
                    windLevels = new String[] {
                            WeatherHelper.getWindLevel(context, Double.parseDouble(windSpeeds[0])),
                            WeatherHelper.getWindLevel(context, Double.parseDouble(windSpeeds[1]))};
                } catch (Exception e) {
                    windLevels = new String[] {"", ""};
                }
                int[] precipitations;
                if (i < mainlyResult.forecastDaily.precipitationProbability.value.size()) {
                    precipitations = new int[] {
                            Integer.parseInt(mainlyResult.forecastDaily.precipitationProbability.value.get(i)),
                            Integer.parseInt(mainlyResult.forecastDaily.precipitationProbability.value.get(i)),};
                } else {
                    precipitations = new int[] {-1, -1};
                }
                Daily daily = new Daily(
                        date, WeatherHelper.getWeek(context, date),
                        new String[] {
                                WeatherHelper.getCNWeatherName(mainlyResult.forecastDaily.weather.value.get(i).from),
                                WeatherHelper.getCNWeatherName(mainlyResult.forecastDaily.weather.value.get(i).to)},
                        new String[] {
                                WeatherHelper.getCNWeatherKind(mainlyResult.forecastDaily.weather.value.get(i).from),
                                WeatherHelper.getCNWeatherKind(mainlyResult.forecastDaily.weather.value.get(i).to)},
                        new int[] {
                                Integer.parseInt(mainlyResult.forecastDaily.temperature.value.get(i).from),
                                Integer.parseInt(mainlyResult.forecastDaily.temperature.value.get(i).to)},
                        new String[] {
                                WeatherHelper.getCNWindName(windDegrees[0]),
                                WeatherHelper.getCNWindName(windDegrees[1])},
                        windSpeeds, windLevels, windDegrees,
                        new String[] {
                                mainlyResult.forecastDaily.sunRiseSet.value.get(i).from.split("T")[1].substring(0, 5),
                                mainlyResult.forecastDaily.sunRiseSet.value.get(i).to.split("T")[1].substring(0, 5),
                                "", ""},
                        "",
                        precipitations);

                dailyList.add(daily);
            }

            List<Hourly> hourlyList = new ArrayList<>();
            for (int i = 0; i < mainlyResult.forecastHourly.weather.value.size(); i ++) {
                int hour = Integer.parseInt(
                        mainlyResult.forecastHourly.temperature.pubTime.split("T")[1].substring(0, 2));
                hour = (hour + i) % 24;
                Hourly hourly = new Hourly(
                        WeatherHelper.buildTime(context, String.valueOf(hour)),
                        TimeManager.isDaylight(
                                String.valueOf(hour),
                                mainlyResult.forecastDaily.sunRiseSet.value.get(0).from.split("T")[1].substring(0, 5),
                                mainlyResult.forecastDaily.sunRiseSet.value.get(0).to.split("T")[1].substring(0, 5)),
                        WeatherHelper.getCNWeatherName(String.valueOf(mainlyResult.forecastHourly.weather.value.get(i))),
                        WeatherHelper.getCNWeatherKind(String.valueOf(mainlyResult.forecastHourly.weather.value.get(i))),
                        mainlyResult.forecastHourly.temperature.value.get(i), -1);
                hourlyList.add(hourly);
            }

            String quality = WeatherHelper.getAqiQuality(context, Integer.parseInt(mainlyResult.aqi.aqi));
            int aqiInt;
            try {
                aqiInt = (int) Double.parseDouble(mainlyResult.aqi.aqi);
            } catch (Exception e) {
                aqiInt = -1;
            }
            int pm25;
            try {
                pm25 = (int) Double.parseDouble(mainlyResult.aqi.pm25);
            } catch (Exception e) {
                pm25 = -1;
            }
            int pm10;
            try {
                pm10 = (int) Double.parseDouble(mainlyResult.aqi.pm10);
            } catch (Exception e) {
                pm10 = -1;
            }
            int so2;
            try {
                so2 = (int) Double.parseDouble(mainlyResult.aqi.so2);
            } catch (Exception e) {
                so2 = -1;
            }
            int no2;
            try {
                no2 = (int) Double.parseDouble(mainlyResult.aqi.no2);
            } catch (Exception e) {
                no2 = -1;
            }
            int o3;
            try {
                o3 = (int) Double.parseDouble(mainlyResult.aqi.o3);
            } catch (Exception e) {
                o3 = -1;
            }
            float co;
            try {
                co = Float.parseFloat(mainlyResult.aqi.co);
            } catch (Exception e) {
                co = -1;
            }
            Aqi aqi = new Aqi(quality, aqiInt, pm25, pm10, so2, no2, o3, co);

            Index index = new Index(
                    "", forecastResult.precipitation.description,
                    context.getString(R.string.live) + " : "
                            + WeatherHelper.getCNWindName(Integer.parseInt(mainlyResult.current.wind.direction.value))
                            + " " + WeatherHelper.getWindSpeed(mainlyResult.current.wind.speed.value)
                            + " (" + WeatherHelper.getWindLevel(context, Double.parseDouble(mainlyResult.current.wind.speed.value)) + ")",
                    context.getString(R.string.daytime) + " : "
                            + WeatherHelper.getCNWindName(Integer.parseInt(mainlyResult.forecastDaily.wind.direction.value.get(0).from))
                            + " " + WeatherHelper.getWindSpeed(mainlyResult.forecastDaily.wind.speed.value.get(0).from)
                            + " (" + WeatherHelper.getWindLevel(context, Double.parseDouble(mainlyResult.forecastDaily.wind.speed.value.get(0).from)) + ")" + "\n"
                            + context.getString(R.string.nighttime) + " : "
                            + WeatherHelper.getCNWindName(Integer.parseInt(mainlyResult.forecastDaily.wind.direction.value.get(0).to))
                            + " " + WeatherHelper.getWindSpeed(mainlyResult.forecastDaily.wind.speed.value.get(0).to)
                            + " (" + WeatherHelper.getWindLevel(context, Double.parseDouble(mainlyResult.forecastDaily.wind.speed.value.get(0).to)) + ")",
                    context.getString(R.string.sensible_temp) + " : " + mainlyResult.current.feelsLike.value + "℃",
                    context.getString(R.string.humidity) + " : " + mainlyResult.current.humidity.value,
                    WeatherHelper.getCNUVIndex(mainlyResult.current.uvIndex),
                    mainlyResult.current.pressure.value + mainlyResult.current.pressure.unit,
                    "", "");

            List<Alert> alertList = new ArrayList<>();
            for (CaiYunMainlyResult.Alerts a : mainlyResult.alerts) {
                Alert alert = new Alert(
                        (int) (Long.parseLong(a.alertId.split(":")[1].split("-")[1]) / 10000),
                        a.title,
                        a.detail,
                        context.getString(R.string.publish_at)
                                + " " + a.pubTime.split("T")[0]
                                + " " + a.pubTime.split("T")[1].substring(0, 5));
                alertList.add(alert);
            }

            location.weather = new Weather(base, realTime, dailyList, hourlyList, aqi, index, alertList);

            location.history = new History(
                    location.cityId, location.weather.base.city,
                    mainlyResult.yesterday.date.split("T")[0],
                    Integer.parseInt(mainlyResult.yesterday.tempMax),
                    Integer.parseInt(mainlyResult.yesterday.tempMin));
        } catch (Exception ignored) {
        }
    }
}