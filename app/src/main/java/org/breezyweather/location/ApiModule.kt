package org.breezyweather.location

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import org.breezyweather.BuildConfig
import org.breezyweather.location.baiduip.BaiduIPLocationApi
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {
    @Provides
    fun provideBaiduIPLocationApi(
        client: OkHttpClient,
        converterFactory: Converter.Factory,
        callAdapterFactory: RxJava3CallAdapterFactory
    ): BaiduIPLocationApi {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BAIDU_IP_LOCATION_BASE_URL)
            .client(client)
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(callAdapterFactory)
            .build()
            .create(BaiduIPLocationApi::class.java)
    }
}
