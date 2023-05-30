package wangdaye.com.geometricweather.common.di;

import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.common.retrofit.ClientCacheHelper;

@InstallIn(SingletonComponent.class)
@Module
public class RetrofitModule {

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(HttpLoggingInterceptor loggingInterceptor) {
        return ClientCacheHelper.getClientBuilder()
                .addInterceptor(loggingInterceptor)
                .build();
    }

    @Provides
    @Singleton
    public GsonConverterFactory provideGsonConverterFactory() {
        return GsonConverterFactory.create(
                new GsonBuilder().create()
        );
    }

    @Provides
    @Singleton
    public RxJava3CallAdapterFactory provideRxJava3CallAdapterFactory() {
        return RxJava3CallAdapterFactory.create();
    }

    @Provides
    @Singleton
    public HttpLoggingInterceptor provideHttpLoggingInterceptor() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.level(
            GeometricWeather.getInstance().getDebugMode()
                    ? HttpLoggingInterceptor.Level.BODY
                    : HttpLoggingInterceptor.Level.NONE
        );
        return httpLoggingInterceptor;
    }
}
