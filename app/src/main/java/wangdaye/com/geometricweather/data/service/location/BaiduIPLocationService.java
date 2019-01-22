package wangdaye.com.geometricweather.data.service.location;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import wangdaye.com.geometricweather.BuildConfig;
import wangdaye.com.geometricweather.basic.TLSCompactService;
import wangdaye.com.geometricweather.data.api.BaiduLocationApi;
import wangdaye.com.geometricweather.data.entity.result.location.BaiduIPLocationResult;

public class BaiduIPLocationService extends LocationService {

    private Call call;

    @Override
    public void requestLocation(Context context, @NonNull LocationCallback callback) {
        Call<BaiduIPLocationResult> getLocation = buildApi()
                .getLocation(BuildConfig.BAIDU_IP_LOCATION_AK, "gcj02");
        getLocation.enqueue(new Callback<BaiduIPLocationResult>() {
            @Override
            public void onResponse(Call<BaiduIPLocationResult> call, Response<BaiduIPLocationResult> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        Result result = new Result();

                        result.district = response.body().getContent().getAddress_detail().getDistrict();
                        result.city = response.body().getContent().getAddress_detail().getCity();
                        result.province = response.body().getContent().getAddress_detail().getProvince();
                        result.country = "中国";
                        result.latitude = response.body().getContent().getPoint().getY();
                        result.longitude = response.body().getContent().getPoint().getX();
                        result.inChina = true;

                        callback.onCompleted(result);
                        return;
                    } catch (Exception ignore) {
                        // do nothing.
                    }
                }
                callback.onCompleted(null);
            }

            @Override
            public void onFailure(Call<BaiduIPLocationResult> call, Throwable t) {
                callback.onCompleted(null);
            }
        });
        call = getLocation;
    }

    @Override
    public void cancel() {
        if (call != null) {
            call.cancel();
        }
        call = null;
    }

    @Override
    public boolean hasPermissions(Context context) {
        return true;
    }

    @Override
    public String[] getPermissions() {
        return new String[0];
    }

    private BaiduLocationApi buildApi() {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BAIDU_IP_LOCATION_BASE_URL)
                .addConverterFactory(
                        GsonConverterFactory.create(
                                new GsonBuilder().setLenient().create()))
                .client(buildClient())
                .build()
                .create((BaiduLocationApi.class));
    }

    private OkHttpClient buildClient() {
        return TLSCompactService.getClientBuilder()
                // .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
    }
}
