package org.breezyweather.weather;

import android.app.Application;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import org.breezyweather.settings.SettingsManager;
import org.breezyweather.weather.accu.AccuWeatherApi;
import org.breezyweather.weather.china.ChinaApi;
import org.breezyweather.weather.metno.MetNoApi;
import org.breezyweather.weather.mf.AtmoAuraIqaApi;
import org.breezyweather.weather.mf.MfWeatherApi;
import org.breezyweather.weather.openmeteo.OpenMeteoAirQualityApi;
import org.breezyweather.weather.openmeteo.OpenMeteoGeocodingApi;
import org.breezyweather.weather.openmeteo.OpenMeteoWeatherApi;
import org.breezyweather.weather.openweather.OpenWeatherApi;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import org.breezyweather.BuildConfig;

@InstallIn(SingletonComponent.class)
@Module
public class ApiModule {

    @Provides
    public OpenMeteoWeatherApi provideOpenMeteoWeatherApi(OkHttpClient client,
                                                          Converter.Factory converterFactory,
                                                          RxJava3CallAdapterFactory callAdapterFactory) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.OPEN_METEO_WEATHER_BASE_URL)
                .client(client)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
                .create(OpenMeteoWeatherApi.class);
    }

    @Provides
    public OpenMeteoGeocodingApi provideOpenMeteoGeocodingApi(OkHttpClient client,
                                                              Converter.Factory converterFactory,
                                                              RxJava3CallAdapterFactory callAdapterFactory) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.OPEN_METEO_GEOCODING_BASE_URL)
                .client(client)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
                .create(OpenMeteoGeocodingApi.class);
    }

    @Provides
    public OpenMeteoAirQualityApi provideOpenMeteoAirQualityApi(OkHttpClient client,
                                                                Converter.Factory converterFactory,
                                                                RxJava3CallAdapterFactory callAdapterFactory) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.OPEN_METEO_AIR_QUALITY_BASE_URL)
                .client(client)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
                .create(OpenMeteoAirQualityApi.class);
    }

    @Provides
    public AccuWeatherApi provideAccuWeatherApi(Application app,
                                                OkHttpClient client,
                                                Converter.Factory converterFactory,
                                                RxJava3CallAdapterFactory callAdapterFactory) {
        return new Retrofit.Builder()
                .baseUrl(SettingsManager.getInstance(app).getCustomAccuPortal().getUrl())
                .client(client)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
                .create(AccuWeatherApi.class);
    }

    @Provides
    public MetNoApi provideMetNoApi(OkHttpClient client,
                                    Converter.Factory converterFactory,
                                    RxJava3CallAdapterFactory callAdapterFactory) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.METNO_BASE_URL)
                .client(client)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
                .create(MetNoApi.class);
    }

    @Provides
    public OpenWeatherApi provideOpenWeatherApi(OkHttpClient client,
                                                Converter.Factory converterFactory,
                                                RxJava3CallAdapterFactory callAdapterFactory) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.OPEN_WEATHER_BASE_URL)
                .client(client)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
                .create(OpenWeatherApi.class);
    }

    @Provides
    public MfWeatherApi provideMfWeatherApi(OkHttpClient client,
                                            Converter.Factory converterFactory,
                                            RxJava3CallAdapterFactory callAdapterFactory) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.MF_WSFT_BASE_URL)
                .client(client)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
                .create(MfWeatherApi.class);
    }

    @Provides
    public AtmoAuraIqaApi provideAtmoAuraIqaApi(OkHttpClient client,
                                                Converter.Factory converterFactory,
                                                RxJava3CallAdapterFactory callAdapterFactory) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.IQA_ATMO_AURA_URL)
                .client(client)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
                .create(AtmoAuraIqaApi.class);
    }

    @Provides
    public ChinaApi provideChinaApi(OkHttpClient client,
                                    Converter.Factory converterFactory,
                                    RxJava3CallAdapterFactory callAdapterFactory) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.CHINA_WEATHER_BASE_URL)
                .client(client)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
                .create(ChinaApi.class);
    }
}
