package org.breezyweather.common.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.breezyweather.BreezyWeather
import org.breezyweather.BuildConfig
import org.breezyweather.sources.baiduip.BaiduIPLocationApi
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class RetrofitModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(app: Application, loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .cache(
                Cache(
                    File(app.cacheDir, "http_cache"),  // $0.05 worth of phone storage in 2020
                    50L * 1024L * 1024L // 50 MiB
                )
            )
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRxJava3CallAdapterFactory(): RxJava3CallAdapterFactory {
        return RxJava3CallAdapterFactory.create()
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BreezyWeather.instance.debugMode) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
    }

    @Provides
    fun provideRetrofitBuilder(
        client: OkHttpClient,
        converterFactory: Converter.Factory,
        callAdapterFactory: RxJava3CallAdapterFactory
    ): Retrofit.Builder {
        return Retrofit.Builder()
            .client(client)
            .addConverterFactory(converterFactory)
            // TODO: We should probably migrate to suspend
            // https://github.com/square/retrofit/blob/master/CHANGELOG.md#version-260-2019-06-05
            .addCallAdapterFactory(callAdapterFactory)
    }
}
