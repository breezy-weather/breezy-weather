package org.breezyweather.weather

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import org.breezyweather.BuildConfig
import org.breezyweather.settings.SettingsManager
import org.breezyweather.weather.accu.AccuWeatherApi
import org.breezyweather.weather.china.ChinaApi
import org.breezyweather.weather.metno.MetNoApi
import org.breezyweather.weather.mf.AtmoAuraIqaApi
import org.breezyweather.weather.mf.MfWeatherApi
import org.breezyweather.weather.openmeteo.OpenMeteoAirQualityApi
import org.breezyweather.weather.openmeteo.OpenMeteoGeocodingApi
import org.breezyweather.weather.openmeteo.OpenMeteoWeatherApi
import org.breezyweather.weather.openweather.OpenWeatherApi
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {
    @Provides
    fun provideOpenMeteoWeatherApi(
        client: OkHttpClient,
        converterFactory: Converter.Factory,
        callAdapterFactory: RxJava3CallAdapterFactory
    ): OpenMeteoWeatherApi {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.OPEN_METEO_WEATHER_BASE_URL)
            .client(client)
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(callAdapterFactory)
            .build()
            .create(OpenMeteoWeatherApi::class.java)
    }

    @Provides
    fun provideOpenMeteoGeocodingApi(
        client: OkHttpClient,
        converterFactory: Converter.Factory,
        callAdapterFactory: RxJava3CallAdapterFactory
    ): OpenMeteoGeocodingApi {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.OPEN_METEO_GEOCODING_BASE_URL)
            .client(client)
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(callAdapterFactory)
            .build()
            .create(OpenMeteoGeocodingApi::class.java)
    }

    @Provides
    fun provideOpenMeteoAirQualityApi(
        client: OkHttpClient,
        converterFactory: Converter.Factory,
        callAdapterFactory: RxJava3CallAdapterFactory
    ): OpenMeteoAirQualityApi {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.OPEN_METEO_AIR_QUALITY_BASE_URL)
            .client(client)
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(callAdapterFactory)
            .build()
            .create(OpenMeteoAirQualityApi::class.java)
    }

    @Provides
    fun provideAccuWeatherApi(
        app: Application,
        client: OkHttpClient,
        converterFactory: Converter.Factory,
        callAdapterFactory: RxJava3CallAdapterFactory
    ): AccuWeatherApi {
        return Retrofit.Builder()
            .baseUrl(SettingsManager.getInstance(app).customAccuPortal.url)
            .client(client)
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(callAdapterFactory)
            .build()
            .create(AccuWeatherApi::class.java)
    }

    @Provides
    fun provideMetNoApi(
        client: OkHttpClient,
        converterFactory: Converter.Factory,
        callAdapterFactory: RxJava3CallAdapterFactory
    ): MetNoApi {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.METNO_BASE_URL)
            .client(client)
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(callAdapterFactory)
            .build()
            .create(MetNoApi::class.java)
    }

    @Provides
    fun provideOpenWeatherApi(
        client: OkHttpClient,
        converterFactory: Converter.Factory,
        callAdapterFactory: RxJava3CallAdapterFactory
    ): OpenWeatherApi {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.OPEN_WEATHER_BASE_URL)
            .client(client)
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(callAdapterFactory)
            .build()
            .create(OpenWeatherApi::class.java)
    }

    @Provides
    fun provideMfWeatherApi(
        client: OkHttpClient,
        converterFactory: Converter.Factory,
        callAdapterFactory: RxJava3CallAdapterFactory
    ): MfWeatherApi {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.MF_WSFT_BASE_URL)
            .client(client)
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(callAdapterFactory)
            .build()
            .create(MfWeatherApi::class.java)
    }

    @Provides
    fun provideAtmoAuraIqaApi(
        client: OkHttpClient,
        converterFactory: Converter.Factory,
        callAdapterFactory: RxJava3CallAdapterFactory
    ): AtmoAuraIqaApi {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.IQA_ATMO_AURA_URL)
            .client(client)
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(callAdapterFactory)
            .build()
            .create(AtmoAuraIqaApi::class.java)
    }

    @Provides
    fun provideChinaApi(
        client: OkHttpClient,
        converterFactory: Converter.Factory,
        callAdapterFactory: RxJava3CallAdapterFactory
    ): ChinaApi {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.CHINA_WEATHER_BASE_URL)
            .client(client)
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(callAdapterFactory)
            .build()
            .create(ChinaApi::class.java)
    }
}
