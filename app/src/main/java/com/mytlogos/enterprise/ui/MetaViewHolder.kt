package com.mytlogos.enterprise.ui

import android.view.View
import android.widget.TextView
import com.mytlogos.enterprise.R
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.viewholders.FlexibleViewHolder

internal class MetaViewHolder(val mView: View, adapter: FlexibleAdapter<*>?) : FlexibleViewHolder(
    mView, adapter) {
    val mainText: TextView
    val topLeftText: TextView
    val topRightText: TextView
    override fun toString(): String {
        return super.toString() + " '" + mainText.text + "'"
    }

    init {
        topLeftText = mView.findViewById(R.id.item_top_left)
        topRightText = mView.findViewById(R.id.item_top_right)
        mainText = mView.findViewById(R.id.content)
    }
}