package org.breezyweather.weather;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.common.basic.models.options.provider.WeatherSource;
import org.breezyweather.common.basic.models.weather.Weather;
import org.breezyweather.db.repositories.HistoryEntityRepository;
import org.breezyweather.db.repositories.WeatherEntityRepository;
import org.breezyweather.main.utils.RequestErrorType;
import org.breezyweather.weather.services.WeatherService;
import org.breezyweather.common.rxjava.BaseObserver;
import org.breezyweather.common.rxjava.ObserverContainer;
import org.breezyweather.common.rxjava.SchedulerTransformer;
import org.breezyweather.common.utils.NetworkUtils;
import org.breezyweather.common.utils.helpers.AsyncHelper;

public class WeatherHelper {

    private final WeatherServiceSet mServiceSet;
    private final CompositeDisposable mCompositeDisposable;

    public interface OnRequestWeatherListener {
        void requestWeatherSuccess(@NonNull Location requestLocation);
        void requestWeatherFailed(@NonNull Location requestLocation, RequestErrorType requestErrorType);
    }

    public interface OnRequestLocationListener {
        void requestLocationSuccess(String query, List<Location> locationList);
        void requestLocationFailed(String query, RequestErrorType requestErrorType);
    }

    @Inject
    public WeatherHelper(WeatherServiceSet weatherServiceSet,
                         CompositeDisposable compositeDisposable) {
        mServiceSet = weatherServiceSet;
        mCompositeDisposable = compositeDisposable;
    }

    public void requestWeather(Context c, Location location, @NonNull final OnRequestWeatherListener l) {
        final WeatherService service = mServiceSet.get(location.getWeatherSource());
        if (!NetworkUtils.isAvailable(c)) {
            l.requestWeatherFailed(location, RequestErrorType.NETWORK_UNAVAILABLE);
            return;
        } else if (!location.isUsable()) {
            l.requestWeatherFailed(location, RequestErrorType.LOCATION_FAILED);
            return;
        }

        service.requestWeather(c, location.copy(), new WeatherService.RequestWeatherCallback() {

            @Override
            public void requestWeatherSuccess(@NonNull Location requestLocation) {
                Weather weather = requestLocation.getWeather();
                if (weather != null) {
                    WeatherEntityRepository.INSTANCE.writeWeather(requestLocation, weather);
                    if (weather.getYesterday() == null) {
                        weather.setYesterday(
                                HistoryEntityRepository.INSTANCE.readHistory(requestLocation, weather)
                        );
                    }
                    l.requestWeatherSuccess(requestLocation);
                } else {
                    requestWeatherFailed(requestLocation, RequestErrorType.WEATHER_REQ_FAILED);
                }
            }

            @Override
            public void requestWeatherFailed(@NonNull Location requestLocation, @NonNull RequestErrorType requestErrorType) {
                l.requestWeatherFailed(
                        Location.copy(
                                requestLocation,
                                WeatherEntityRepository.INSTANCE.readWeather(requestLocation)
                        ),
                        requestErrorType
                );
            }
        });
    }

    public void requestSearchLocations(Context context, String query, WeatherSource enabledSource,
                                       @NonNull final OnRequestLocationListener l) {
        if (enabledSource == null) {
            AsyncHelper.delayRunOnUI(() -> l.requestLocationFailed(query, RequestErrorType.LOCATION_SEARCH_FAILED), 0);
            return;
        }

        // generate weather services.
        final WeatherService service = mServiceSet.get(enabledSource);

        if (!service.isConfigured(context)) {
            AsyncHelper.delayRunOnUI(() -> l.requestLocationFailed(query, RequestErrorType.API_KEY_REQUIRED_MISSING), 0);
            return;
        }

        // generate observable list.
        Observable<List<Location>> observable =
                    Observable.create(emitter ->
                            emitter.onNext(service.requestLocation(context, query))
                    );

        observable.compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<List<Location>>() {
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
                        if (this.isApiLimitReached()) {
                            l.requestLocationFailed(query, RequestErrorType.API_LIMIT_REACHED);
                        } else if (this.isApiUnauthorized()) {
                            l.requestLocationFailed(query, RequestErrorType.API_UNAUTHORIZED);
                        } else {
                            l.requestLocationFailed(query, RequestErrorType.LOCATION_SEARCH_FAILED);
                        }
                    }
                }));
    }

    public void cancel() {
        for (WeatherService s : mServiceSet.getAll()) {
            s.cancel();
        }
        mCompositeDisposable.clear();
    }
}
