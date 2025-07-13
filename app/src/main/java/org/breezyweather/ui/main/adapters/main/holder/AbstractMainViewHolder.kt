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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.view.View
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import breezyweather.domain.location.model.Location
import org.breezyweather.common.utils.helpers.AsyncHelper
import org.breezyweather.ui.main.utils.MainModuleUtils
import org.breezyweather.ui.theme.resource.providers.ResourceProvider

abstract class AbstractMainViewHolder(
    view: View,
) : RecyclerView.ViewHolder(view) {
    protected lateinit var context: Context
    protected var provider: ResourceProvider? = null
    protected var itemAnimationEnabled = false
    private var mInScreen = false
    private var mItemAnimator: Animator? = null
    private var mDelayController: AsyncHelper.Controller? = null

    @CallSuper
    open fun onBindView(
        context: Context,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
    ) {
        this.context = context
        this.provider = provider
        this.itemAnimationEnabled = itemAnimationEnabled
        mInScreen = false
        mDelayController = null
        if (listAnimationEnabled) {
            itemView.alpha = 0f
        }
    }

    val top
        get() = itemView.top

    fun checkEnterScreen(
        host: RecyclerView,
        pendingAnimatorList: MutableList<Animator>,
        listAnimationEnabled: Boolean,
    ): Boolean {
        if (!itemView.isLaidOut || top >= host.measuredHeight) {
            return false
        }
        if (!mInScreen) {
            mInScreen = true
            if (listAnimationEnabled) {
                executeEnterAnimator(pendingAnimatorList)
            } else {
                onEnterScreen()
            }
            return true
        }
        return false
    }

    fun executeEnterAnimator(pendingAnimatorList: MutableList<Animator>) {
        itemView.alpha = 0f
        mItemAnimator = getEnterAnimator(pendingAnimatorList).apply {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationCancel(animation: Animator) {
                    pendingAnimatorList.remove(mItemAnimator)
                }
            })
        }
        mDelayController = AsyncHelper.delayRunOnUI({
            pendingAnimatorList.remove(mItemAnimator)
            onEnterScreen()
        }, mItemAnimator!!.startDelay)
        pendingAnimatorList.add(mItemAnimator!!)
        mItemAnimator!!.start()
    }

    protected open fun getEnterAnimator(pendingAnimatorList: List<Animator>): Animator {
        return MainModuleUtils.getEnterAnimator(itemView, pendingAnimatorList.size)
    }

    open fun onEnterScreen() {
        // do nothing.
    }

    open fun onRecycleView() {
        mDelayController?.let {
            it.cancel()
            mDelayController = null
        }
        mItemAnimator?.let {
            it.cancel()
            mItemAnimator = null
        }
    }
}
