package wangdaye.com.geometricweather.weather.di;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import wangdaye.com.geometricweather.BuildConfig;
import wangdaye.com.geometricweather.weather.apis.*;

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
    public AccuWeatherApi provideAccuWeatherApi(OkHttpClient client,
                                                Converter.Factory converterFactory,
                                                RxJava3CallAdapterFactory callAdapterFactory) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.ACCU_WEATHER_BASE_URL)
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
    public NominatimApi provideNominatimApi(OkHttpClient client,
                                            Converter.Factory converterFactory,
                                            RxJava3CallAdapterFactory callAdapterFactory) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.NOMINATIM_BASE_URL)
                .client(client)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
                .create(NominatimApi.class);
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
