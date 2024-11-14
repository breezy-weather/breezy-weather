/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.common.di

import android.app.Application
import android.os.Build
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.tls.HandshakeCertificates
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class HttpModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(app: Application, loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        val client = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            /**
             * Add support for Let’s encrypt certificate authority on Android < 7.0
             */
            try {
                val certificateFactory = CertificateFactory.getInstance("X.509")
                val certificateIsrgRootX1 = certificateFactory
                    .generateCertificates(app.resources.openRawResource(R.raw.isrg_root_x1))
                    .single() as X509Certificate
                val certificateIsrgRootX2 = certificateFactory
                    .generateCertificates(app.resources.openRawResource(R.raw.isrg_root_x2))
                    .single() as X509Certificate
                val certificates = HandshakeCertificates.Builder()
                    .addTrustedCertificate(certificateIsrgRootX1)
                    .addTrustedCertificate(certificateIsrgRootX2)
                    .addPlatformTrustedCertificates()
                    .build()

                OkHttpClient.Builder()
                    .sslSocketFactory(certificates.sslSocketFactory(), certificates.trustManager)
            } catch (ignored: Exception) {
                OkHttpClient.Builder()
            }
        } else {
            OkHttpClient.Builder()
        }

        return client
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .cache(
                Cache(
                    File(app.cacheDir, "http_cache"), // $0.05 worth of phone storage in 2020
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
            level = if (BreezyWeather.instance.debugMode) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    @Named("JsonSerializer")
    fun provideKotlinxJsonSerializationConverterFactory(): Converter.Factory {
        val contentType = "application/json".toMediaType()
        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            isLenient = !BreezyWeather.instance.debugMode
        }
        return json.asConverterFactory(contentType)
    }

    @Provides
    @Named("JsonClient")
    fun provideJsonRetrofitBuilder(
        client: OkHttpClient,
        @Named("JsonSerializer") jsonConverterFactory: Converter.Factory,
        callAdapterFactory: RxJava3CallAdapterFactory,
    ): Retrofit.Builder {
        return Retrofit.Builder()
            .client(client)
            .addConverterFactory(jsonConverterFactory)
            // TODO: We should probably migrate to suspend
            // https://github.com/square/retrofit/blob/master/CHANGELOG.md#version-260-2019-06-05
            .addCallAdapterFactory(callAdapterFactory)
    }
}
