package org.breezyweather.common.di;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import org.breezyweather.BreezyWeather;

import java.io.File;
import java.util.concurrent.TimeUnit;

@InstallIn(SingletonComponent.class)
@Module
public class RetrofitModule {

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(Application app, HttpLoggingInterceptor loggingInterceptor) {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(45, TimeUnit.SECONDS)
                .cache(new Cache(
                    new File(app.getCacheDir(), "http_cache"),
                    // $0.05 worth of phone storage in 2020
                    50L * 1024L * 1024L // 50 MiB
                ))
                .addInterceptor(loggingInterceptor)
                .build();
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
            BreezyWeather.getInstance().getDebugMode()
                    ? HttpLoggingInterceptor.Level.BODY
                    : HttpLoggingInterceptor.Level.NONE
        );
        return httpLoggingInterceptor;
    }
}
