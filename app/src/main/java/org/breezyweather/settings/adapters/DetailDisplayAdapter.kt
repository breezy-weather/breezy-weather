package org.breezyweather.settings.adapters

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
import org.breezyweather.common.basic.models.options.appearance.DetailDisplay
import org.breezyweather.common.ui.widgets.slidingItem.SlidingItemContainerLayout

class DetailDisplayAdapter(
    private val mDetailDisplayList: MutableList<DetailDisplay>,
    private val mRemoveListener: OnItemRemoveListener,
    private val mDragListener: OnItemDragListener
) : RecyclerView.Adapter<DetailDisplayAdapter.ViewHolder>() {
    interface OnItemRemoveListener {
        fun onRemoved(detailDisplay: DetailDisplay)
    }

    interface OnItemDragListener {
        fun onDrag(holder: ViewHolder)
    }

    inner class ViewHolder @SuppressLint("ClickableViewAccessibility") constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val container: SlidingItemContainerLayout = itemView.findViewById(R.id.item_card_display_container)
        val item: RelativeLayout = itemView.findViewById(R.id.item_card_display)
        val title: TextView = itemView.findViewById(R.id.item_card_display_title)
        val sortButton: ImageButton = itemView.findViewById(R.id.item_card_display_sortButton)
        val deleteButton: ImageButton = itemView.findViewById(R.id.item_card_display_deleteBtn)

        init {
            sortButton.setOnTouchListener { _: View, event: MotionEvent ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    mDragListener.onDrag(this)
                }
                false
            }
            deleteButton.setOnClickListener { removeItem(bindingAdapterPosition) }
        }

        fun onBindView(detailDisplay: DetailDisplay) {
            title.text = detailDisplay.getName(title.context)
            container.swipe(0f)
            container.setOnClickListener { }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_card_display, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindView(mDetailDisplayList[position])
    }

    override fun getItemCount() = mDetailDisplayList.size

    val detailDisplayList: List<DetailDisplay> = mDetailDisplayList

    fun insertItem(detailDisplay: DetailDisplay) {
        mDetailDisplayList.add(detailDisplay)
        notifyItemInserted(mDetailDisplayList.size - 1)
    }

    fun removeItem(adapterPosition: Int) {
        val detailDisplay = mDetailDisplayList.removeAt(adapterPosition)
        notifyItemRemoved(adapterPosition)
        mRemoveListener.onRemoved(detailDisplay)
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        mDetailDisplayList.add(toPosition, mDetailDisplayList.removeAt(fromPosition))
        notifyItemMoved(fromPosition, toPosition)
    }
}
