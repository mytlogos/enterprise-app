package com.mytlogos.enterprise.ui

import android.view.View
import android.widget.TextView
import com.mytlogos.enterprise.R
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.viewholders.FlexibleViewHolder

internal class CloseableTextViewHolder(view: View, adapter: FlexibleAdapter<*>) :
    FlexibleViewHolder(view, adapter) {
    var textView: TextView

    init {
        textView = view.findViewById(R.id.text) as TextView
        val closeButton: View = view.findViewById(R.id.close)
        closeButton.setOnClickListener { v: View? -> adapter.removeItem(this.flexibleAdapterPosition) }
    }
}