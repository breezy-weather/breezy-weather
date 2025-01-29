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
    private val updateChecker: AppUpdateChecker
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
        forceCheck: Boolean = false
    ): GetApplicationRelease.Result {
        return updateChecker.checkForUpdate(context, forceCheck)
    }
}
