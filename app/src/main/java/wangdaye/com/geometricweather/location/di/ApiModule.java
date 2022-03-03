package wangdaye.com.geometricweather.location.di;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import wangdaye.com.geometricweather.BuildConfig;
import wangdaye.com.geometricweather.location.services.ip.BaiduIPLocationApi;

@InstallIn(SingletonComponent.class)
@Module
public class ApiModule {

    @Provides
    public BaiduIPLocationApi provideBaiduIPLocationApi(OkHttpClient client,
                                                        GsonConverterFactory converterFactory,
                                                        RxJava2CallAdapterFactory callAdapterFactory) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BAIDU_IP_LOCATION_BASE_URL)
                .client(client)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
                .create((BaiduIPLocationApi.class));
    }
}
