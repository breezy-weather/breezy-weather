package wangdaye.com.geometricweather.weather.service;

import android.content.Context;

import androidx.annotation.NonNull;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Retrofit;
import wangdaye.com.geometricweather.BuildConfig;
import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.basic.model.location.ChineseCity;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.weather.SchedulerTransformer;
import wangdaye.com.geometricweather.weather.api.CaiYunApi;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.weather.converter.CaiyunResultConverter;
import wangdaye.com.geometricweather.weather.json.caiyun.CaiYunForecastResult;
import wangdaye.com.geometricweather.weather.json.caiyun.CaiYunMainlyResult;
import wangdaye.com.geometricweather.weather.interceptor.GzipInterceptor;
import wangdaye.com.geometricweather.weather.observer.BaseObserver;
import wangdaye.com.geometricweather.weather.observer.ObserverContainer;

/**
 * CaiYun weather service.
 * */

public class CaiYunWeatherService extends CNWeatherService {

    private CaiYunApi api;
    private CompositeDisposable compositeDisposable;

    public CaiYunWeatherService() {
        api = new Retrofit.Builder()
                .baseUrl(BuildConfig.CAIYUN_WEATHER_BASE_URL)
                .client(
                        GeometricWeather.getInstance()
                                .getOkHttpClient()
                                .newBuilder()
                                .addInterceptor(new GzipInterceptor())
                                .build()
                ).addConverterFactory(GeometricWeather.getInstance().getGsonConverterFactory())
                .addCallAdapterFactory(GeometricWeather.getInstance().getRxJava2CallAdapterFactory())
                .build()
                .create((CaiYunApi.class));
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void requestWeather(Context context,
                               Location location, @NonNull RequestWeatherCallback callback) {
        Observable<CaiYunMainlyResult> mainly = api.getMainlyWeather(
                String.valueOf(location.getLatitude()),
                String.valueOf(location.getLongitude()),
                location.isCurrentPosition(),
                "weathercn%3A" + location.getCityId(),
                15,
                "weather20151024",
                "zUFJoAR2ZVrDy1vF3D07",
                "V10.0.1.0.OAACNFH",
                "10010002",
                false,
                false,
                "gemini",
                "",
                "zh_cn"
        );
        Observable<CaiYunForecastResult> forecast = api.getForecastWeather(
                String.valueOf(location.getLatitude()),
                String.valueOf(location.getLongitude()),
                "zh_cn",
                false,
                "weather20151024",
                "weathercn%3A" + location.getCityId(),
                "zUFJoAR2ZVrDy1vF3D07"
        );

        Observable.zip(mainly, forecast, (mainlyResult, forecastResult) ->
                CaiyunResultConverter.convert(context, location, mainlyResult, forecastResult)
        ).compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(compositeDisposable, new BaseObserver<Weather>() {
                    @Override
                    public void onSucceed(Weather weather) {
                        if (weather != null) {
                            location.setWeather(weather);
                            callback.requestWeatherSuccess(location);
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

    @Override
    public ChineseCity.CNWeatherSource getSource() {
        return ChineseCity.CNWeatherSource.CAIYUN;
    }
}