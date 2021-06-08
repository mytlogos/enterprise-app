package com.mytlogos.enterprise.ui

import android.view.View
import android.widget.TextView
import com.mytlogos.enterprise.R
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.viewholders.FlexibleViewHolder

internal class MetaViewHolder(
    mView: View,
    adapter: FlexibleAdapter<*>,
) : FlexibleViewHolder(mView, adapter) {
    val mainText: TextView = mView.findViewById(R.id.content) as TextView
    val topLeftText: TextView = mView.findViewById(R.id.item_top_left) as TextView
    val topRightText: TextView = mView.findViewById(R.id.item_top_right) as TextView

    override fun toString(): String {
        return "${super.toString()} '${mainText.text}'"
    }

}