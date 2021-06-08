package com.mytlogos.enterprise.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.mytlogos.enterprise.R
import java.util.function.Function

class TextOnlyListAdapter<E> : ArrayAdapter<E> {
    private val extractor: Function<E, String>?

    internal constructor(
        fragment: Fragment,
        liveData: LiveData<MutableList<E>>,
        extractor: Function<E, String>?
    ) : super(fragment.requireContext(), R.layout.text_only_item) {

        this.extractor = extractor
        liveData.observe(fragment, { mediaLists: List<E>? ->
            this.clear()
            this.addAll(mediaLists ?: listOf())
        })
    }

    internal constructor(context: Context, extractor: Function<E, String>?) : super(context, R.layout.text_only_item) {
        this.extractor = extractor
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent) as TextView
        return bindViewToItem(position, view)
    }

    private fun bindViewToItem(position: Int, view: TextView): View {
        val item = getItem(position)
        if (item is String) {
            // if it is a string, it was already correctly bound by arrayAdapter
            return view
        }
        if (extractor == null) {
            return view
        }
        if (item != null) {
            view.text = extractor.apply(item)
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent) as TextView
        return bindViewToItem(position, view)
    }
}