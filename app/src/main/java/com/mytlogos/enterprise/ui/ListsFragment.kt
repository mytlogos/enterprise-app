package com.mytlogos.enterprise.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.model.*
import com.mytlogos.enterprise.tools.Utils.transform
import com.mytlogos.enterprise.viewmodel.ListsViewModel
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.SelectableAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.flexibleadapter.utils.DrawableUtils
import java.net.URI
import java.util.*
import java.util.regex.Pattern

/**
 * A fragment representing a list of Items.
 */
class ListsFragment
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
    : BaseListFragment<MediaList, ListsViewModel>() {
    private var inActionMode = false
    private val callback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.title = "Delete Lists"
            mode.menuInflater.inflate(R.menu.list_medium_action_mode_menu, menu)
            Objects.requireNonNull(mainActivity.supportActionBar)!!.hide()
            flexibleAdapter!!.mode = SelectableAdapter.Mode.MULTI
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return if (item.itemId == R.id.delete_items) {
                deleteItemsFromList(mode)
            } else false
        }

        private fun deleteItemsFromList(mode: ActionMode): Boolean {
            val selectedPositions = flexibleAdapter!!.selectedPositions
            val selectedListsIds: MutableList<Int> = ArrayList()
            for (selectedPosition in selectedPositions) {
                val flexible = flexibleAdapter!!.getItem(selectedPosition) as? ListItem ?: continue
                val list = flexible.list
                if (TRASH_LIST.listId == list.listId) {
                    showToast("You cannot delete the Trash list")
                    return true
                }
                if (getString(R.string.standard_list_name) == list.name) {
                    showToast("You cannot delete the Standard list")
                    return true
                }
                selectedListsIds.add(list.listId)
            }
            println("removed " + selectedListsIds.size + " lists")
            showToast("Is not implemented yet")
            mode.finish()
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            Objects.requireNonNull(mainActivity.supportActionBar)!!.show()
            flexibleAdapter!!.mode = SelectableAdapter.Mode.IDLE
            flexibleAdapter!!.clearSelection()
            println("destroyed action mode")
            inActionMode = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.lists_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add) {
            this.mainActivity.switchWindow(AddList(), true)
        }
        return super.onOptionsItemSelected(item)
    }

    override val viewModelClass: Class<ListsViewModel>
        get() = ListsViewModel::class.java

    override fun createPagedListLiveData(): LiveData<PagedList<MediaList>> {
        return transform(Transformations.map(
            viewModel!!.lists
        ) { input: MutableList<MediaList> ->
            input.remove(TRASH_LIST)
            input.add(TRASH_LIST)
            input
        })
    }

    override fun createFlexible(value: MediaList): IFlexible<*> {
        return ListItem(value)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        this.setTitle("Lists")
        return view
    }

    override fun onItemLongClick(position: Int) {
        if (inActionMode) {
            return
        }
        val list = flexibleAdapter!!.getItem(position) as? ListItem ?: return
        if (list.list is ExternalMediaList) {
            return
        }
        inActionMode = true
        println("starting action mode")
        flexibleAdapter!!.addSelection(position)
        this.mainActivity.startActionMode(callback)
    }

    override fun onItemClick(view: View, position: Int): Boolean {
        val list = flexibleAdapter!!.getItem(position) as? ListItem ?: return false
        val item = list
        if (inActionMode) {
            return if (position != RecyclerView.NO_POSITION && item.list !is ExternalMediaList) {
                flexibleAdapter!!.toggleSelection(position)
                true
            } else {
                false
            }
        }
        mainActivity.switchWindow(ListMediumFragment.getInstance(item.list), true)
        return false
    }

    private class ListItem(val list: MediaList) :
        AbstractFlexibleItem<MetaViewHolder>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val that = other as ListItem
            return list.listId == that.list.listId
        }

        override fun hashCode(): Int {
            return list.listId
        }

        override fun getLayoutRes(): Int {
            return R.layout.meta_item
        }

        override fun createViewHolder(
            view: View,
            adapter: FlexibleAdapter<IFlexible<*>?>?
        ): MetaViewHolder {
            return MetaViewHolder(view, adapter)
        }

        @SuppressLint("DefaultLocale")
        override fun bindViewHolder(
            adapter: FlexibleAdapter<IFlexible<*>?>?,
            holder: MetaViewHolder,
            position: Int,
            payloads: List<Any>
        ) {
            // transform news id (int) to a string,
            // because it would expect a resource id if it is an int
            holder.topLeftText.text = String.format("%d Items", list.size)
            holder.topRightText.text = getDenominator(list)
            holder.mainText.text = list.name
            val drawable = DrawableUtils.getSelectableBackgroundCompat(
                Color.WHITE,  // normal background
                Color.GRAY,  // pressed background
                Color.BLACK) // ripple color
            DrawableUtils.setBackgroundCompat(holder.itemView, drawable)
        }

        init {
            this.isDraggable = false
            this.isSwipeable = false
            this.isSelectable = true
        }
    }

    companion object {
        val TRASH_LIST = MediaList(
            "", Int.MIN_VALUE,
            "Trashbin List",
            0,
            0
        )

        private fun getDenominator(list: MediaList): String {
            val denominator: String
            denominator = if (list is ExternalMediaList) {
                val host = URI.create(list.url).host
                val matcher = Pattern.compile("(www\\.)?(.+)").matcher(host)
                if (matcher.matches()) {
                    matcher
                        .group(2)
                } else {
                    host
                }
            } else {
                "Intern"
            }
            return denominator
        }
    }
}