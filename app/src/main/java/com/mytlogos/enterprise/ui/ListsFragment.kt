package com.mytlogos.enterprise.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.lifecycle.Transformations
import androidx.lifecycle.asFlow
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.model.ExternalMediaList
import com.mytlogos.enterprise.model.MediaList
import com.mytlogos.enterprise.requireSupportActionBar
import com.mytlogos.enterprise.tools.setDefaultSelectableBackgroundCompat
import com.mytlogos.enterprise.tools.transformPaging
import com.mytlogos.enterprise.viewmodel.ListsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import java.net.URI
import java.util.*
import java.util.regex.Pattern

/**
 * A fragment representing a list of [MediaList].
 */
@ExperimentalCoroutinesApi
class ListsFragment
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
    : BasePagingFragment<MediaList, ListsViewModel>() {
    private var inActionMode = false
    private val callback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.title = "Delete Lists"
            mode.menuInflater.inflate(R.menu.list_medium_action_mode_menu, menu)
            mainActivity.requireSupportActionBar().hide()
            changeSelectionMode(SelectionMode.MULTI)
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
            val selectedListsIds: MutableList<Int> = ArrayList()

            for (list in getSelectedItems()) {
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
            mainActivity.requireSupportActionBar().show()
            changeSelectionMode(SelectionMode.IDLE)

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        this.setTitle("Lists")
        return view
    }

    override fun onItemLongClick(position: Int, item: MediaList?): Boolean {
        if (inActionMode || item == null) {
            return false
        }

        if (item is ExternalMediaList) {
            return false
        }
        inActionMode = true
        println("starting action mode")
        selectionTracker.select(item.getSelectionKey())
        this.mainActivity.startActionMode(callback)
        return true
    }

    override fun onItemClick(position: Int, item: MediaList?) {
        if (item == null) {
            return
        }

        if (!inActionMode) {
            mainActivity.switchWindow(ListMediumFragment.getInstance(item), true)
        }
    }

    private class ListDiff : DiffUtil.ItemCallback<MediaList>() {
        override fun areItemsTheSame(oldItem: MediaList, newItem: MediaList): Boolean {
            return oldItem.listId == newItem.listId
        }

        override fun areContentsTheSame(oldItem: MediaList, newItem: MediaList): Boolean {
            return oldItem == newItem
        }
    }

    private class ListAdapter : BaseAdapter<MediaList, NewMetaViewHolder>(ListDiff()) {
        override val layoutId = R.layout.meta_item

        override fun createViewHolder(root: View, viewType: Int) = NewMetaViewHolder(root)

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: NewMetaViewHolder, position: Int) {
            getItem(position)?.let {
                // transform news id (int) to a string,
                // because it would expect a resource id if it is an int
                holder.topLeftText.text = "${it.size} Items"
                holder.topRightText.text = getDenominator(it)
                holder.mainText.text = it.name

                holder.itemView.setDefaultSelectableBackgroundCompat()
            }
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
            return if (list is ExternalMediaList) {
                val host = URI.create(list.url).host
                val matcher = Pattern.compile("(www\\.)?(.+)").matcher(host)

                if (matcher.matches()) {
                    matcher.group(2)
                } else {
                    host
                }
            } else {
                "Intern"
            }
        }
    }

    override fun createAdapter(): BaseAdapter<MediaList, *> = ListAdapter()

    override fun createPaged(model: ListsViewModel): Flow<PagingData<MediaList>> {
        val listLiveData = Transformations.map(
            viewModel.lists
        ) { input: MutableList<MediaList> ->
            input.remove(TRASH_LIST)
            input.add(TRASH_LIST)
            input
        }
        return listLiveData.transformPaging().asFlow()
    }
}