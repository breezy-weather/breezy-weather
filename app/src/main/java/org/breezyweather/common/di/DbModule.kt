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

import android.content.Context
import android.os.Build
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import breezyweather.data.AlertSeverityColumnAdapter
import breezyweather.data.Alerts
import breezyweather.data.AndroidDatabaseHandler
import breezyweather.data.Dailys
import breezyweather.data.Database
import breezyweather.data.DatabaseHandler
import breezyweather.data.DurationColumnAdapter
import breezyweather.data.Hourlys
import breezyweather.data.Locations
import breezyweather.data.PressureColumnAdapter
import breezyweather.data.TimeZoneColumnAdapter
import breezyweather.data.WeatherCodeColumnAdapter
import breezyweather.data.Weathers
import breezyweather.data.location.LocationRepository
import breezyweather.data.weather.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import org.breezyweather.BuildConfig
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DbModule {

    @Provides
    @Singleton
    fun provideSqlDriver(@ApplicationContext context: Context): SqlDriver {
        return AndroidSqliteDriver(
            schema = Database.Schema,
            context = context,
            name = "breezyweather.db",
            factory = if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Support database inspector in Android Studio
                FrameworkSQLiteOpenHelperFactory()
            } else {
                RequerySQLiteOpenHelperFactory()
            },
            callback = object : AndroidSqliteDriver.Callback(Database.Schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    setPragma(db, "foreign_keys = ON")
                    setPragma(db, "journal_mode = WAL")
                    setPragma(db, "synchronous = NORMAL")
                }
                private fun setPragma(db: SupportSQLiteDatabase, pragma: String) {
                    val cursor = db.query("PRAGMA $pragma")
                    cursor.moveToFirst()
                    cursor.close()
                }
            }
        )
    }

    @Provides
    @Singleton
    fun provideDatabase(driver: SqlDriver): Database {
        return Database(
            driver,
            locationsAdapter = Locations.Adapter(
                timezoneAdapter = TimeZoneColumnAdapter
            ),
            weathersAdapter = Weathers.Adapter(
                weather_codeAdapter = WeatherCodeColumnAdapter,
                pressureAdapter = PressureColumnAdapter
            ),
            dailysAdapter = Dailys.Adapter(
                daytime_weather_codeAdapter = WeatherCodeColumnAdapter,
                daytime_total_precipitation_durationAdapter = DurationColumnAdapter,
                daytime_thunderstorm_precipitation_durationAdapter = DurationColumnAdapter,
                daytime_rain_precipitation_durationAdapter = DurationColumnAdapter,
                daytime_snow_precipitation_durationAdapter = DurationColumnAdapter,
                daytime_ice_precipitation_durationAdapter = DurationColumnAdapter,
                nighttime_weather_codeAdapter = WeatherCodeColumnAdapter,
                nighttime_total_precipitation_durationAdapter = DurationColumnAdapter,
                nighttime_thunderstorm_precipitation_durationAdapter = DurationColumnAdapter,
                nighttime_rain_precipitation_durationAdapter = DurationColumnAdapter,
                nighttime_snow_precipitation_durationAdapter = DurationColumnAdapter,
                nighttime_ice_precipitation_durationAdapter = DurationColumnAdapter,
                sunshine_durationAdapter = DurationColumnAdapter,
                pressure_averageAdapter = PressureColumnAdapter,
                pressure_maxAdapter = PressureColumnAdapter,
                pressure_minAdapter = PressureColumnAdapter
            ),
            hourlysAdapter = Hourlys.Adapter(
                weather_codeAdapter = WeatherCodeColumnAdapter,
                pressureAdapter = PressureColumnAdapter
            ),
            alertsAdapter = Alerts.Adapter(
                severityAdapter = AlertSeverityColumnAdapter
            )
        )
    }

    @Provides
    @Singleton
    fun provideDatabaseHandler(db: Database, driver: SqlDriver): DatabaseHandler {
        return AndroidDatabaseHandler(db, driver)
    }

    @Provides
    @Singleton
    fun provideLocationRepository(databaseHandler: DatabaseHandler): LocationRepository {
        return LocationRepository(databaseHandler)
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(databaseHandler: DatabaseHandler): WeatherRepository {
        return WeatherRepository(databaseHandler)
    }
}
