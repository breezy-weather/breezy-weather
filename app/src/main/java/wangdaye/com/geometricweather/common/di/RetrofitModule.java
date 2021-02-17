package wangdaye.com.geometricweather.common.di;

import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import okhttp3.OkHttpClient;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import wangdaye.com.geometricweather.weather.TLSCompactHelper;

@InstallIn(ApplicationComponent.class)
@Module
public class RetrofitModule {

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient() {
        return TLSCompactHelper.getClientBuilder().build();
    }

    @Provides
    @Singleton
    public GsonConverterFactory provideGsonConverterFactory() {
        return GsonConverterFactory.create(
                new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()
        );
    }

    @Provides
    @Singleton
    public RxJava2CallAdapterFactory provideRxJava2CallAdapterFactory() {
        return RxJava2CallAdapterFactory.create();
    }
}
