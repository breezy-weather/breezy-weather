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

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import javax.inject.Named

@InstallIn(SingletonComponent::class)
@Module
class RetrofitModule {

    @Provides
    fun provideRetrofitBuilder(
        client: OkHttpClient,
        @Named("JsonSerializer") jsonConverterFactory: Converter.Factory,
        callAdapterFactory: RxJava3CallAdapterFactory
    ): Retrofit.Builder {
        return Retrofit.Builder()
            .client(client)
            .addConverterFactory(jsonConverterFactory)
            // TODO: We should probably migrate to suspend
            // https://github.com/square/retrofit/blob/master/CHANGELOG.md#version-260-2019-06-05
            .addCallAdapterFactory(callAdapterFactory)
    }
}