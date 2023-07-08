package org.breezyweather.common.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import org.breezyweather.R
import org.breezyweather.common.ui.widgets.TagView

class TagAdapter @JvmOverloads constructor(
    private val mTagList: MutableList<Tag>,
    @field:ColorInt @param:ColorInt private val mCheckedTitleColor: Int,
    @field:ColorInt @param:ColorInt private val mUncheckedTitleColor: Int,
    @field:ColorInt @param:ColorInt private val mCheckedBackgroundColor: Int,
    @field:ColorInt @param:ColorInt private val mUncheckedBackgroundColor: Int,
    private val mListener: ((checked: Boolean, oldPosition: Int, newPosition: Int) -> Boolean)? = null,
    private var mCheckedIndex: Int = UNCHECKABLE_INDEX
) : RecyclerView.Adapter<TagAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mTagView: TagView

        init {
            mTagView = itemView.findViewById(R.id.item_tag)
            mTagView.setOnClickListener {
                var consumed = false
                if (mListener != null) {
                    consumed = mListener.invoke(
                        !mTagView.isChecked,
                        mCheckedIndex,
                        bindingAdapterPosition
                    )
                }
                if (!consumed && mCheckedIndex != bindingAdapterPosition) {
                    val i = mCheckedIndex
                    mCheckedIndex = bindingAdapterPosition
                    notifyItemChanged(i)
                    notifyItemChanged(mCheckedIndex)
                }
            }
        }

        fun onBindView(tag: Tag, checked: Boolean) {
            mTagView.apply {
                text = tag.name
                checkedBackgroundColor = mCheckedBackgroundColor
                uncheckedBackgroundColor = mUncheckedBackgroundColor
            }
            setChecked(checked)
        }

        fun setChecked(checked: Boolean) {
            mTagView.apply {
                setTextColor(if (checked) mCheckedTitleColor else mUncheckedTitleColor)
                isChecked = checked
            }
        }
    }

    interface Tag {
        val name: String
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_tag, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindView(mTagList[position], position == mCheckedIndex)
    }

    override fun getItemCount(): Int {
        return mTagList.size
    }

    fun insertItem(tag: Tag) {
        mTagList.add(tag)
        notifyItemInserted(mTagList.size - 1)
    }

    fun removeItem(position: Int): Tag {
        val tag = mTagList.removeAt(position)
        notifyItemRemoved(position)
        return tag
    }

    companion object {
        const val UNCHECKABLE_INDEX = -1
    }
}
