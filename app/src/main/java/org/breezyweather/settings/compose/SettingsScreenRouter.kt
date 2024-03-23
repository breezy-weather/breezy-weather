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

package org.breezyweather.settings.compose

sealed class SettingsScreenRouter(val route: String) {
    object Root : SettingsScreenRouter("org.breezyweather.settings.root")
    object BackgroundUpdates : SettingsScreenRouter("org.breezyweather.settings.background")
    object Location : SettingsScreenRouter("org.breezyweather.settings.location")
    object WeatherProviders : SettingsScreenRouter("org.breezyweather.settings.providers")
    object Appearance : SettingsScreenRouter("org.breezyweather.settings.appearance")
    object MainScreen : SettingsScreenRouter("org.breezyweather.settings.main")
    object Notifications : SettingsScreenRouter("org.breezyweather.settings.notifications")
    object Unit : SettingsScreenRouter("org.breezyweather.settings.unit")
    object Widgets : SettingsScreenRouter("org.breezyweather.settings.widgets")
    object Debug : SettingsScreenRouter("org.breezyweather.settings.debug")
}
