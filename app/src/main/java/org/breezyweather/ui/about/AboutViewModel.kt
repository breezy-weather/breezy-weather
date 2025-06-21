/*
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

package org.breezyweather.ui.about

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.breezyweather.R
import org.breezyweather.background.updater.AppUpdateChecker
import org.breezyweather.background.updater.interactor.GetApplicationRelease
import org.breezyweather.common.utils.helpers.IntentHelper
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    private val updateChecker: AppUpdateChecker,
) : ViewModel() {
    internal fun getAboutAppLinks(activity: Activity): Array<AboutAppLinkItem> {
        return arrayOf(
            AboutAppLinkItem(
                iconId = R.drawable.ic_shield_lock,
                titleId = R.string.about_privacy_policy
            ) {
                IntentHelper.startPrivacyPolicyActivity(activity)
            },
            AboutAppLinkItem(
                iconId = R.drawable.ic_contract,
                titleId = R.string.about_dependencies
            ) {
                IntentHelper.startDependenciesActivity(activity)
            }
        )
    }

    internal suspend fun checkForUpdate(
        context: Context,
        forceCheck: Boolean = false,
    ): GetApplicationRelease.Result {
        return updateChecker.checkForUpdate(context, forceCheck)
    }
}
