package com.mytlogos.enterprise.tools

import android.view.MotionEvent
import android.view.View
import androidx.core.view.children
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker.SelectionPredicate
import androidx.recyclerview.widget.RecyclerView
import com.mytlogos.enterprise.ui.BasePagingFragment


class SimpleItemKeyProvider(
    private val recyclerView: RecyclerView,
) :
    ItemKeyProvider<Long>(SCOPE_MAPPED) {

    override fun getKey(position: Int): Long? {
        val adapter = recyclerView.adapter
        if (adapter !is BasePagingFragment.BaseAdapter<*, *>) {
            return null
        }
        return getKeyFrom(position, adapter)
    }

    override fun getPosition(key: Long): Int = getPositionFrom(key, recyclerView)
}
fun getKeyFrom(position: Int, adapter: BasePagingFragment.BaseAdapter<*, *>): Long? {
    val item = adapter.getItemAt(position)

    return if (item is Selectable) {
        item.getSelectionKey()
    } else null
}

fun getPositionFrom(key: Long, recyclerView: RecyclerView): Int {
    val viewHolder = recyclerView.findViewHolderForItemId(key)
    val position = viewHolder?.layoutPosition

    if (position != null) {
        return position
    }
    for (child in recyclerView.children) {
        val childViewHolder = recyclerView.getChildViewHolder(child)
        val adapter = childViewHolder.bindingAdapter

        if (adapter !is BasePagingFragment.BaseAdapter<*, *>) {
            continue
        }

        @Suppress("UNCHECKED_CAST")
        val item =
            (adapter as BasePagingFragment.BaseAdapter<*, RecyclerView.ViewHolder>).getItemFrom(
                childViewHolder
            )

        if (item !is Selectable) {
            continue
        }
        if (item.getSelectionKey() == key) {
            return childViewHolder.layoutPosition
        }
    }
    return RecyclerView.NO_POSITION
}

fun <K> createSelectNothing(): SelectionPredicate<K> {
    return object : SelectionPredicate<K>() {
        override fun canSetStateForKey(key: K, nextState: Boolean): Boolean {
            return false
        }

        override fun canSetStateAtPosition(position: Int, nextState: Boolean): Boolean {
            return false
        }

        override fun canSelectMultiple(): Boolean {
            return false
        }
    }
}

interface Selectable {
    fun getSelectionKey(): Long
}

internal open class SelectableViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> {
        return object : ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition(): Int = bindingAdapterPosition

            override fun getSelectionKey(): Long? {
                return bindingAdapter?.let {
                    if (it !is BasePagingFragment.BaseAdapter<*, *>) {
                        return@let null
                    }
                    val position = bindingAdapterPosition
                    if (position == RecyclerView.NO_POSITION) {
                        return@let null
                    }
                    val item = it.getItemAt(position)

                    return@let if (item is Selectable) {
                        item.getSelectionKey()
                    } else {
                        null
                    }
                }
            }
        }
    }
}

internal class DetailsLookup(private val mRecyclerView: RecyclerView) : ItemDetailsLookup<Long>() {

    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
        val view: View? = mRecyclerView.findChildViewUnder(e.x, e.y)

        if (view != null) {
            val holder: RecyclerView.ViewHolder = mRecyclerView.getChildViewHolder(view)

            if (holder is SelectableViewHolder) {
                return holder.getItemDetails()
            }
        }
        return null
    }
}