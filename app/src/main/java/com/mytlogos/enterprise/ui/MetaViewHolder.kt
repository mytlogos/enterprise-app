package com.mytlogos.enterprise.ui

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mytlogos.enterprise.R

internal class MetaViewHolder(
    mView: View,
) : RecyclerView.ViewHolder(mView) {
    val mainText: TextView = mView.findViewById(R.id.content) as TextView
    val topLeftText: TextView = mView.findViewById(R.id.item_top_left) as TextView
    val topRightText: TextView = mView.findViewById(R.id.item_top_right) as TextView

    override fun toString(): String {
        return "${super.toString()} '${mainText.text}'"
    }

}