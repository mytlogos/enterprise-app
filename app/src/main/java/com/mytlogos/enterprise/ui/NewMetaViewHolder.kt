package com.mytlogos.enterprise.ui

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.tools.SelectableViewHolder
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.viewholders.FlexibleViewHolder

/**
 * Stand in Class for [MetaViewHolder] until every usage of FlexibleAdapter is replaced
 * or a better name is found.
 */
internal class NewMetaViewHolder(mView: View) : SelectableViewHolder(mView) {
    val mainText: TextView = mView.findViewById(R.id.content)
    val topLeftText: TextView = mView.findViewById(R.id.item_top_left)
    val topRightText: TextView = mView.findViewById(R.id.item_top_right)

    override fun toString(): String {
        return super.toString() + " '" + mainText.text + "'"
    }

}