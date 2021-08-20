package com.mytlogos.enterprise.ui

import android.view.View
import android.widget.TextView
import com.mytlogos.enterprise.R
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.viewholders.FlexibleViewHolder

internal class CloseableTextViewHolder(view: View, adapter: FlexibleAdapter<*>) :
    FlexibleViewHolder(view, adapter) {
    var textView: TextView = view.findViewById(R.id.text)

    init {
        val closeButton: View = view.findViewById(R.id.close)
        closeButton.setOnClickListener { adapter.removeItem(this.flexibleAdapterPosition) }
    }
}