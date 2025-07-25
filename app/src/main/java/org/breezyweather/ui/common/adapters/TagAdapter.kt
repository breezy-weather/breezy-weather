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

package org.breezyweather.ui.common.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import org.breezyweather.R

class TagAdapter @JvmOverloads constructor(
    private val tagList: MutableList<Tag>,
    private val listener: ((checked: Boolean, oldPosition: Int, newPosition: Int) -> Boolean)? = null,
    private var checkedIndex: Int = UNCHECKABLE_INDEX,
) : RecyclerView.Adapter<TagAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tagView: Chip = itemView.findViewById(R.id.item_tag)

        init {
            tagView.setOnClickListener {
                var consumed = false
                if (listener != null) {
                    consumed = listener.invoke(
                        !tagView.isChecked,
                        checkedIndex,
                        bindingAdapterPosition
                    )
                }
                if (!consumed && checkedIndex != bindingAdapterPosition) {
                    val i = checkedIndex
                    checkedIndex = bindingAdapterPosition
                    notifyItemChanged(i)
                    notifyItemChanged(checkedIndex)
                }
            }
        }

        fun onBindView(tag: Tag, checked: Boolean) {
            tagView.apply {
                text = tag.name
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
        holder.onBindView(tagList[position], position == checkedIndex)
    }

    override fun getItemCount(): Int {
        return tagList.size
    }

    fun insertItem(tag: Tag) {
        tagList.add(tag)
        notifyItemInserted(tagList.size - 1)
    }

    fun removeItem(position: Int): Tag {
        val tag = tagList.removeAt(position)
        notifyItemRemoved(position)
        return tag
    }

    companion object {
        const val UNCHECKABLE_INDEX = -1
    }
}
