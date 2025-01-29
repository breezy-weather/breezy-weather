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

package org.breezyweather.ui.settings.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.appearance.CardDisplay
import org.breezyweather.ui.common.widgets.slidingItem.SlidingItemContainerLayout

class CardDisplayAdapter(
    private val mCardDisplayList: MutableList<CardDisplay>,
    private val mRemoveListener: (CardDisplay) -> Unit,
    private val mDragListener: (ViewHolder) -> Unit,
) : RecyclerView.Adapter<CardDisplayAdapter.ViewHolder>() {

    inner class ViewHolder
    @SuppressLint("ClickableViewAccessibility")
    constructor(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {
        val container: SlidingItemContainerLayout = itemView.findViewById(R.id.item_card_display_container)
        val item: RelativeLayout = itemView.findViewById(R.id.item_card_display)
        val title: TextView = itemView.findViewById(R.id.item_card_display_title)
        val sortButton: ImageButton = itemView.findViewById(R.id.item_card_display_sortButton)
        val deleteButton: ImageButton = itemView.findViewById(R.id.item_card_display_deleteBtn)

        init {
            sortButton.setOnTouchListener { _: View, event: MotionEvent ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    mDragListener(this)
                }
                false
            }
            deleteButton.setOnClickListener { removeItem(bindingAdapterPosition) }
        }

        fun onBindView(cardDisplay: CardDisplay) {
            title.text = cardDisplay.getName(title.context)
            container.swipe(0f)
            container.setOnClickListener { }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_card_display, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindView(mCardDisplayList[position])
    }

    override fun getItemCount() = mCardDisplayList.size

    val cardDisplayList: List<CardDisplay> = mCardDisplayList

    fun insertItem(cardDisplay: CardDisplay) {
        mCardDisplayList.add(cardDisplay)
        notifyItemInserted(mCardDisplayList.size - 1)
    }

    fun removeItem(adapterPosition: Int) {
        val cardDisplay = mCardDisplayList.removeAt(adapterPosition)
        notifyItemRemoved(adapterPosition)
        mRemoveListener(cardDisplay)
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        mCardDisplayList.add(toPosition, mCardDisplayList.removeAt(fromPosition))
        notifyItemMoved(fromPosition, toPosition)
    }
}
