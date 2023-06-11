package wangdaye.com.geometricweather.location.di;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import wangdaye.com.geometricweather.BuildConfig;
import wangdaye.com.geometricweather.location.apis.BaiduIPLocationApi;

@InstallIn(SingletonComponent.class)
@Module
public class ApiModule {

    @Provides
    public BaiduIPLocationApi provideBaiduIPLocationApi(OkHttpClient client,
                                                        Converter.Factory converterFactory,
                                                        RxJava3CallAdapterFactory callAdapterFactory) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BAIDU_IP_LOCATION_BASE_URL)
                .client(client)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
                .create((BaiduIPLocationApi.class));
    }
}
