package wangdaye.com.geometricweather.location.service.ip;

import android.content.Context;
import androidx.annotation.NonNull;

import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Retrofit;
import wangdaye.com.geometricweather.BuildConfig;
import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.location.service.LocationService;
import wangdaye.com.geometricweather.weather.GzipInterceptor;
import wangdaye.com.geometricweather.weather.SchedulerTransformer;
import wangdaye.com.geometricweather.weather.observer.BaseObserver;
import wangdaye.com.geometricweather.weather.observer.ObserverContainer;

public class BaiduIPLocationService extends LocationService {

    private BaiduIPLocationApi api;
    private CompositeDisposable compositeDisposable;

    public BaiduIPLocationService() {
        api = new Retrofit.Builder()
                .baseUrl(BuildConfig.BAIDU_IP_LOCATION_BASE_URL)
                .client(GeometricWeather.getInstance()
                        .getOkHttpClient()
                        .newBuilder()
                        .addInterceptor(new GzipInterceptor())
                        .build())
                .addConverterFactory(GeometricWeather.getInstance().getGsonConverterFactory())
                .addCallAdapterFactory(GeometricWeather.getInstance().getRxJava2CallAdapterFactory())
                .build()
                .create((BaiduIPLocationApi.class));
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void requestLocation(Context context, @NonNull LocationCallback callback) {
        api.getLocation(BuildConfig.BAIDU_IP_LOCATION_AK, "gcj02")
                .compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(compositeDisposable, new BaseObserver<BaiduIPLocationResult>() {
                    @Override
                    public void onSucceed(BaiduIPLocationResult baiduIPLocationResult) {
                        try {
                            Result result = new Result();

                            result.district = baiduIPLocationResult.getContent().getAddress_detail().getDistrict();
                            result.city = baiduIPLocationResult.getContent().getAddress_detail().getCity();
                            result.province = baiduIPLocationResult.getContent().getAddress_detail().getProvince();
                            result.country = "中国";
                            result.latitude = baiduIPLocationResult.getContent().getPoint().getY();
                            result.longitude = baiduIPLocationResult.getContent().getPoint().getX();
                            result.inChina = true;

                            callback.onCompleted(result);
                        } catch (Exception ignore) {
                            callback.onCompleted(null);
                        }
                    }

                    @Override
                    public void onFailed() {
                        callback.onCompleted(null);
                    }
                }));
    }

    @Override
    public void cancel() {
        compositeDisposable.clear();
    }

    @Override
    public boolean hasPermissions(Context context) {
        return true;
    }

    @Override
    public String[] getPermissions() {
        return new String[0];
    }
}
