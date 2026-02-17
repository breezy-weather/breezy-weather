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

package org.breezyweather.ui.settings.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.breezyweather.R
import org.breezyweather.common.activities.BreezyActivity

class WeatherIconAdapter(
    private val mActivity: BreezyActivity,
    private val mItemList: List<Item>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // item.
    interface Item
    class Title(var content: String) : Item
    abstract class WeatherIcon : Item {
        abstract val drawable: Drawable
        abstract val contentDescription: String
        abstract fun onItemClicked(activity: BreezyActivity)
    }

    class Line : Item

    // holder.
    internal inner class TitleHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {
        private val mTitle: TextView = itemView.findViewById(R.id.item_weather_icon_title)

        fun onBindView() {
            val t = mItemList[bindingAdapterPosition] as Title
            mTitle.text = t.content
        }
    }

    internal inner class IconHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {
        private val mImageView: AppCompatImageView = itemView.findViewById(R.id.item_weather_icon_image)

        fun onBindView() {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
            val icon = mItemList[bindingAdapterPosition] as WeatherIcon
            mImageView.setImageDrawable(icon.drawable)
            itemView.contentDescription = icon.contentDescription
            itemView.setOnClickListener { icon.onItemClicked(mActivity) }
        }
    }

    internal inner class LineHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        if (viewType == 1) {
            return TitleHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_weather_icon_title, parent, false)
            )
        }
        return if (viewType == -1) {
            LineHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_line, parent, false)
            )
        } else {
            IconHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_weather_icon, parent, false)
            )
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        if (holder is LineHolder) return
        if (holder is TitleHolder) {
            holder.onBindView()
        } else {
            (holder as IconHolder).onBindView()
        }
    }

    override fun getItemCount(): Int {
        return mItemList.size
    }

    override fun getItemViewType(position: Int): Int {
        if (mItemList[position] is Title) return 1
        return if (mItemList[position] is Line) -1 else 0
    }

    companion object {
        fun getSpanSizeLookup(
            columnCount: Int,
            itemList: List<Item>,
        ): GridLayoutManager.SpanSizeLookup {
            return SpanSizeLookup(columnCount, itemList)
        }
    }
}

internal class SpanSizeLookup(
    private val mColumnCount: Int,
    private val mItemList: List<WeatherIconAdapter.Item>,
) : GridLayoutManager.SpanSizeLookup() {
    override fun getSpanSize(position: Int): Int {
        return if (mItemList[position] is WeatherIconAdapter.Title || mItemList[position] is WeatherIconAdapter.Line) {
            mColumnCount
        } else {
            1
        }
    }
}
