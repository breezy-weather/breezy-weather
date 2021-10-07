package wangdaye.com.geometricweather.weather.services;

import android.content.Context;

import androidx.annotation.NonNull;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import wangdaye.com.geometricweather.common.basic.models.ChineseCity;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.rxjava.BaseObserver;
import wangdaye.com.geometricweather.common.rxjava.ObserverContainer;
import wangdaye.com.geometricweather.common.rxjava.SchedulerTransformer;
import wangdaye.com.geometricweather.weather.apis.CNWeatherApi;
import wangdaye.com.geometricweather.weather.apis.CaiYunApi;
import wangdaye.com.geometricweather.weather.converters.CaiyunResultConverter;
import wangdaye.com.geometricweather.weather.json.caiyun.CaiYunForecastResult;
import wangdaye.com.geometricweather.weather.json.caiyun.CaiYunMainlyResult;

/**
 * CaiYun weather service.
 * */

public class CaiYunWeatherService extends CNWeatherService {

    private final CaiYunApi mApi;
    private final CompositeDisposable mCompositeDisposable;

    @Inject
    public CaiYunWeatherService(CaiYunApi cyApi, CNWeatherApi cnApi, CompositeDisposable disposable) {
        super(cnApi, disposable);
        mApi = cyApi;
        mCompositeDisposable = disposable;
    }

    @Override
    public void requestWeather(Context context,
                               Location location, @NonNull RequestWeatherCallback callback) {
        Observable<CaiYunMainlyResult> mainly = mApi.getMainlyWeather(
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
        Observable<CaiYunForecastResult> forecast = mApi.getForecastWeather(
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
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<WeatherResultWrapper>() {
                    @Override
                    public void onSucceed(WeatherResultWrapper wrapper) {
                        if (wrapper.result != null) {
                            location.setWeather(wrapper.result);
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
        mCompositeDisposable.clear();
    }

    @Override
    public ChineseCity.CNWeatherSource getSource() {
        return ChineseCity.CNWeatherSource.CAIYUN;
    }
}