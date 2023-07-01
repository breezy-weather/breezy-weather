package org.breezyweather.common.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Converter
import org.breezyweather.BreezyWeather
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class KotlinModule {
    @Provides
    @Singleton
    fun provideKotlinxSerializationConverterFactory(): Converter.Factory {
        val contentType = "application/json".toMediaType()
        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            isLenient = !BreezyWeather.instance.debugMode
        }
        return json.asConverterFactory(contentType)
    }
}
