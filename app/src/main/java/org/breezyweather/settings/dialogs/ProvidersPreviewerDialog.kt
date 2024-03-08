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

package org.breezyweather.settings.dialogs

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.appcompat.app.AlertDialog
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import org.breezyweather.common.extensions.dpToPx
import org.breezyweather.common.utils.helpers.AsyncHelper
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.settings.adapters.IconProviderAdapter
import org.breezyweather.theme.resource.ResourcesProviderFactory.getProviderList
import org.breezyweather.theme.resource.providers.ResourceProvider

object ProvidersPreviewerDialog {
    const val ACTION_RESOURCE_PROVIDER_CHANGED = "org.breezyweather.RESOURCE_PROVIDER_CHANGED"
    const val KEY_PACKAGE_NAME = "package_name"

    fun show(
        activity: Activity,
        callback: (String) -> Unit
    ) {
        val view = LayoutInflater
            .from(activity)
            .inflate(R.layout.dialog_providers_previewer, null, false)
        initWidget(
            activity,
            view,
            MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.settings_icon_packs_title)
                .setView(view)
                .show(),
            callback
        )
    }

    private fun initWidget(
        activity: Activity,
        view: View,
        dialog: AlertDialog,
        callback: (String) -> Unit
    ) {
        val progressView = view.findViewById<View>(R.id.dialog_providers_previewer_progress_container)
        progressView.visibility = View.VISIBLE
        val listView = view.findViewById<RecyclerView>(R.id.dialog_providers_previewer_list)
        listView.layoutManager = LinearLayoutManager(activity)
        listView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            val elevation = activity.dpToPx(2f)
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!listView.canScrollVertically(-1)) {
                    listView.translationZ = 0f
                } else {
                    listView.translationZ = elevation
                }
            }
        })
        listView.visibility = View.GONE
        AsyncHelper.runOnIO(
            { emitter: AsyncHelper.Emitter<List<ResourceProvider>> ->
                emitter.send(
                    getProviderList(BreezyWeather.instance),
                    true
                )
            },
            { resourceProviders: List<ResourceProvider>?, _: Boolean ->
                bindAdapter(
                    activity,
                    listView,
                    progressView,
                    resourceProviders ?: emptyList(),
                    dialog,
                    callback
                )
            }
        )
    }

    private fun bindAdapter(
        activity: Activity,
        listView: RecyclerView,
        progressView: View,
        providerList: List<ResourceProvider>,
        dialog: AlertDialog,
        callback: (String) -> Unit
    ) {
        listView.adapter = IconProviderAdapter(
            activity,
            providerList,
            object : IconProviderAdapter.OnItemClickedListener {
                override fun onItemClicked(provider: ResourceProvider, adapterPosition: Int) {
                    callback(provider.packageName)
                    dialog.dismiss()
                }

                override fun onWebItemClicked(query: String) {
                    IntentHelper.startWebViewActivity(activity, query)
                    dialog.dismiss()
                }
            }
        )

        val show: Animation = AlphaAnimation(0f, 1f)
        show.duration = 300
        show.interpolator = FastOutSlowInInterpolator()
        listView.startAnimation(show)
        listView.visibility = View.VISIBLE

        val out: Animation = AlphaAnimation(1f, 0f)
        show.duration = 300
        show.interpolator = FastOutSlowInInterpolator()
        progressView.startAnimation(out)
        progressView.visibility = View.GONE
    }
}
