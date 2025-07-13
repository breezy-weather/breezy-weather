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

package org.breezyweather.ui.main.adapters.main.holder

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.LinearLayout
import androidx.annotation.CallSuper
import breezyweather.domain.location.model.Location
import com.google.android.material.card.MaterialCardView
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.ui.main.adapters.main.FirstCardHeaderController
import org.breezyweather.ui.main.utils.MainThemeColorProvider
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.resource.providers.ResourceProvider

@SuppressLint("ObjectAnimatorBinding")
abstract class AbstractMainCardViewHolder(
    view: View,
) : AbstractMainViewHolder(view) {
    private var mFirstCardHeaderController: FirstCardHeaderController? = null
    protected var mLocation: Location? = null

    @CallSuper
    open fun onBindView(
        activity: GeoActivity,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
        firstCard: Boolean,
    ) {
        super.onBindView(activity, location, provider, listAnimationEnabled, itemAnimationEnabled)
        mLocation = location
        val delegate = ThemeManager.getInstance(activity).weatherThemeDelegate
        if (itemView is MaterialCardView) {
            (itemView as MaterialCardView).apply {
                elevation = delegate.getHomeCardElevation(activity)
                setCardBackgroundColor(MainThemeColorProvider.getColor(location, R.attr.colorMainCardBackground))
            }
        }
        val params = itemView.layoutParams as MarginLayoutParams
        params.setMargins(
            delegate.getHomeCardMargins(context).div(2),
            delegate.getHomeCardMargins(context).div(2),
            delegate.getHomeCardMargins(context).div(2),
            delegate.getHomeCardMargins(context).div(2)
        )
        itemView.layoutParams = params
        if (firstCard) {
            mFirstCardHeaderController = FirstCardHeaderController(activity, location).apply {
                bind((itemView as ViewGroup).getChildAt(0) as LinearLayout)
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBindView(
        context: Context,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
    ) {
        throw RuntimeException("Deprecated method.")
    }

    override fun onRecycleView() {
        super.onRecycleView()
        mFirstCardHeaderController?.let {
            it.unbind()
            mFirstCardHeaderController = null
        }
    }
}
