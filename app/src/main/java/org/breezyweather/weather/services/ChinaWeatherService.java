package org.breezyweather.weather.services;

import android.content.Context;

import android.text.TextUtils;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import org.breezyweather.common.basic.models.ChineseCity;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.db.repositories.ChineseCityEntityRepository;
import org.breezyweather.main.utils.RequestErrorType;
import org.breezyweather.weather.apis.ChinaApi;
import org.breezyweather.common.rxjava.BaseObserver;
import org.breezyweather.common.rxjava.ObserverContainer;
import org.breezyweather.common.rxjava.SchedulerTransformer;
import org.breezyweather.common.utils.LanguageUtils;
import org.breezyweather.settings.SettingsManager;
import org.breezyweather.weather.converters.ChinaResultConverterKt;
import org.breezyweather.weather.json.china.ChinaMinutelyResult;
import org.breezyweather.weather.json.china.ChinaForecastResult;

public class ChinaWeatherService extends WeatherService {

    private final ChinaApi mApi;
    private final CompositeDisposable mCompositeDisposable;

    @Inject
    public ChinaWeatherService(ChinaApi cyApi, CompositeDisposable disposable) {
        mApi = cyApi;
        mCompositeDisposable = disposable;
    }

    @Override
    public Boolean isConfigured(Context context) {
        return true;
    }

    @Override
    public void requestWeather(Context context,
                               Location location, @NonNull RequestWeatherCallback callback) {
        Observable<ChinaForecastResult> mainly = mApi.getForecastWeather(
                location.getLatitude(),
                location.getLongitude(),
                location.isCurrentPosition(),
                "weathercn%3A" + location.getCityId(),
                15,
                "weather20151024",
                "zUFJoAR2ZVrDy1vF3D07",
                false,
                SettingsManager.getInstance(context).getLanguage().getCode()
        );
        Observable<ChinaMinutelyResult> forecast = mApi.getMinutelyWeather(
                location.getLatitude(),
                location.getLongitude(),
                SettingsManager.getInstance(context).getLanguage().getCode(),
                false,
                "weather20151024",
                "weathercn%3A" + location.getCityId(),
                "zUFJoAR2ZVrDy1vF3D07"
        );

        Observable.zip(mainly, forecast, (mainlyResult, forecastResult) ->
                ChinaResultConverterKt.convert(context, location, mainlyResult, forecastResult)
        ).compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<WeatherResultWrapper>() {
                    @Override
                    public void onSucceed(WeatherResultWrapper wrapper) {
                        if (wrapper.result != null) {
                            callback.requestWeatherSuccess(
                                    Location.copy(location, wrapper.result)
                            );
                        } else {
                            onFailed();
                        }
                    }

                    @Override
                    public void onFailed() {
                        callback.requestWeatherFailed(location, RequestErrorType.WEATHER_REQ_FAILED);
                    }
                }));
    }

    @NonNull
    @Override
    public List<Location> requestLocation(Context context, String query) {
        if (!LanguageUtils.isChinese(query)) {
            return new ArrayList<>();
        }

        ChineseCityEntityRepository.INSTANCE.ensureChineseCityList(context);

        List<Location> locationList = new ArrayList<>();
        List<ChineseCity> cityList = ChineseCityEntityRepository.INSTANCE.readChineseCityList(query);
        for (ChineseCity c : cityList) {
            locationList.add(c.toLocation());
        }

        return locationList;
    }

    @Override
    public void requestLocation(Context context, Location location, @NonNull RequestLocationCallback callback) {

        final boolean hasGeocodeInformation = location.hasGeocodeInformation();

        Observable.create((ObservableOnSubscribe<List<Location>>) emitter -> {
            ChineseCityEntityRepository.INSTANCE.ensureChineseCityList(context);
            List<Location> locationList = new ArrayList<>();

            if (hasGeocodeInformation) {
                ChineseCity chineseCity = ChineseCityEntityRepository.INSTANCE.readChineseCity(
                        formatLocationString(convertChinese(location.getProvince())),
                        formatLocationString(convertChinese(location.getCity())),
                        formatLocationString(convertChinese(location.getDistrict()))
                );
                if (chineseCity != null) {
                    locationList.add(chineseCity.toLocation());
                }
            }
            if (locationList.size() > 0) {
                emitter.onNext(locationList);
                return;
            }

            ChineseCity chineseCity = ChineseCityEntityRepository.INSTANCE.readChineseCity(
                    location.getLatitude(), location.getLongitude());
            if (chineseCity != null) {
                locationList.add(chineseCity.toLocation());
            }

            emitter.onNext(locationList);

        }).compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<List<Location>>() {
                    @Override
                    public void onSucceed(List<Location> locations) {
                        if (locations.size() > 0) {
                            callback.requestLocationSuccess(location.getFormattedId(), locations);
                        } else {
                            onFailed();
                        }
                    }

                    @Override
                    public void onFailed() {
                        callback.requestLocationFailed(location.getFormattedId(), RequestErrorType.LOCATION_FAILED);
                    }
                }));
    }

    @Override
    public void cancel() {
        mCompositeDisposable.clear();
    }

    protected static String formatLocationString(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }

        if (str.endsWith("地区")) {
            return str.substring(0, str.length() - 2);
        }
        if (str.endsWith("区")
                && !str.endsWith("新区")
                && !str.endsWith("矿区")
                && !str.endsWith("郊区")
                && !str.endsWith("风景区")
                && !str.endsWith("东区")
                && !str.endsWith("西区")) {
            return str.substring(0, str.length() - 1);
        }
        if (str.endsWith("县")
                && str.length() != 2
                && !str.endsWith("通化县")
                && !str.endsWith("本溪县")
                && !str.endsWith("辽阳县")
                && !str.endsWith("建平县")
                && !str.endsWith("承德县")
                && !str.endsWith("大同县")
                && !str.endsWith("五台县")
                && !str.endsWith("乌鲁木齐县")
                && !str.endsWith("伊宁县")
                && !str.endsWith("南昌县")
                && !str.endsWith("上饶县")
                && !str.endsWith("吉安县")
                && !str.endsWith("长沙县")
                && !str.endsWith("衡阳县")
                && !str.endsWith("邵阳县")
                && !str.endsWith("宜宾县")) {
            return str.substring(0, str.length() - 1);
        }

        if (str.endsWith("市")
                && !str.endsWith("新市")
                && !str.endsWith("沙市")
                && !str.endsWith("津市")
                && !str.endsWith("芒市")
                && !str.endsWith("西市")
                && !str.endsWith("峨眉山市")) {
            return str.substring(0, str.length() - 1);
        }
        if (str.endsWith("回族自治州")
                || str.endsWith("藏族自治州")
                || str.endsWith("彝族自治州")
                || str.endsWith("白族自治州")
                || str.endsWith("傣族自治州")
                || str.endsWith("蒙古自治州")) {
            return str.substring(0, str.length() - 5);
        }
        if (str.endsWith("朝鲜族自治州")
                || str.endsWith("哈萨克自治州")
                || str.endsWith("傈僳族自治州")
                || str.endsWith("蒙古族自治州")) {
            return str.substring(0, str.length() - 6);
        }
        if (str.endsWith("哈萨克族自治州")
                || str.endsWith("苗族侗族自治州")
                || str.endsWith("藏族羌族自治州")
                || str.endsWith("壮族苗族自治州")
                || str.endsWith("柯尔克孜自治州")) {
            return str.substring(0, str.length() - 7);
        }
        if (str.endsWith("布依族苗族自治州")
                || str.endsWith("土家族苗族自治州")
                || str.endsWith("蒙古族藏族自治州")
                || str.endsWith("柯尔克孜族自治州")
                || str.endsWith("傣族景颇族自治州")
                || str.endsWith("哈尼族彝族自治州")) {
            return str.substring(0, str.length() - 8);
        }
        if (str.endsWith("自治州")) {
            return str.substring(0, str.length() - 3);
        }

        if (str.endsWith("省")) {
            return str.substring(0, str.length() - 1);
        }
        if (str.endsWith("壮族自治区") || str.endsWith("回族自治区")) {
            return str.substring(0, str.length() - 5);
        }
        if (str.endsWith("维吾尔自治区")) {
            return str.substring(0, str.length() - 6);
        }
        if (str.endsWith("维吾尔族自治区")) {
            return str.substring(0, str.length() - 7);
        }
        if (str.endsWith("自治区")) {
            return str.substring(0, str.length() - 3);
        }
        return str;
    }

    protected static String convertChinese(String text) {
        try {
            return LanguageUtils.traditionalToSimplified(text);
        } catch (Exception e) {
            return text;
        }
    }
}