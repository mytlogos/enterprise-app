package com.mytlogos.enterprise.ui

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mytlogos.enterprise.R

typealias RemoveCallback = (position: Int) -> Unit

internal class CloseableTextViewHolder(view: View, removeCallback: RemoveCallback, closable: Boolean = true) : RecyclerView.ViewHolder(view) {
    var textView: TextView = view.findViewById(R.id.text)

    init {
        if (closable) {
            val closeButton: View = view.findViewById(R.id.close)
            closeButton.setOnClickListener {
                val position = this.bindingAdapterPosition

                if (position != RecyclerView.NO_POSITION) {
                    removeCallback(position)
                }
            }
        }
    }
}