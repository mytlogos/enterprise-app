package com.mytlogos.enterprise.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.*
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.TimeAgo
import com.mytlogos.enterprise.model.*
import com.mytlogos.enterprise.requireSupportActionBar
import com.mytlogos.enterprise.tools.Sortings
import com.mytlogos.enterprise.tools.transformPaging
import com.mytlogos.enterprise.viewmodel.ListMediaViewModel
import com.mytlogos.enterprise.viewmodel.ListsViewModel
import eu.davidea.flexibleadapter.utils.DrawableUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.max

class ListMediumFragment : BasePagingFragment<MediumItem, ListMediaViewModel>() {
    private var listId = 0
    private var isExternal = false
    private var listTitle: String? = null
    private var inActionMode = false

    private val callback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.title = "Edit MediumList"
            mode.menuInflater.inflate(R.menu.list_medium_action_mode_menu, menu)
            mainActivity.requireSupportActionBar().hide()
            changeSelectionMode(SelectionMode.MULTI)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            if (item.itemId == R.id.move_item_to_list) {
                return moveItemsToList(mode)
            } else if (item.itemId == R.id.delete_items) {
                return deleteItemsFromList(mode)
            }
            return false
        }

        private fun deleteItemsFromList(mode: ActionMode): Boolean {
            if (ListsFragment.TRASH_LIST.listId == listId) {
                showToast("You cannot delete from the Trash list")
                mode.finish()
                // TODO: 01.08.2019 ask if we really want to remove this item
                return true
            }
            val selectedMediaIds = getSelectedItems().map { it.mediumId }.toMutableList()

            val size = selectedMediaIds.size

            lifecycleScope.launch {
                val success = viewModel.removeMedia(listId, selectedMediaIds)

                val text: String = if (!success) {
                    "Could not delete $size Media from List '$listTitle'"
                } else {
                    mode.finish()
                    "Removed $size Media from '$listTitle'"
                }
                // TODO: 29.07.2019 replace toast with undoable snackbar
                showToast(text)
            }
            return false
        }

        fun moveItemsToList(mode: ActionMode): Boolean {
            val context = Objects.requireNonNull(context)
            val builder = AlertDialog.Builder(context)
            val listsViewModel =
                ViewModelProvider(this@ListMediumFragment).get(ListsViewModel::class.java)

            val listLiveData = Transformations.map(
                listsViewModel.internLists
            ) { input: MutableList<MediaList> ->
                input.removeIf { list: MediaList -> list.listId == listId }
                input
            }
            val adapter: ArrayAdapter<MediaList> = TextOnlyListAdapter(
                this@ListMediumFragment,
                listLiveData,
                MediaList::name
            )
            builder.setAdapter(adapter) { _: DialogInterface?, which: Int ->
                val list = adapter.getItem(which) ?: return@setAdapter

                val selectedMediaIds = getSelectedItems().map { it.mediumId }.toMutableList()

                lifecycleScope.launch {
                    val success = listsViewModel.moveMediumToList(
                        listId,
                        list.listId,
                        selectedMediaIds
                    )
                    val text = if (!success) {
                        "Could not move Media to List '" + list.name + "'"
                    } else {
                        mode.finish()
                        "Moved " + selectedMediaIds.size + " Media to " + list.name
                    }
                    // TODO: 29.07.2019 replace toast with undoable snackbar
                    showToast(text)
                }
            }
            builder.show()
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            mainActivity.requireSupportActionBar().show()
            changeSelectionMode(SelectionMode.IDLE)
            println("destroyed action mode")
            inActionMode = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arguments = requireArguments()
        listId = arguments.getInt(ID)
        isExternal = arguments.getBoolean(EXTERNAL)
        listTitle = arguments.getString(TITLE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        this.setTitle(listTitle)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        var fragment: Fragment? = null
        var bundle: Bundle? = null
        var selected = false

        if (item.itemId == R.id.list_setting) {
            fragment = ListSettings()
            bundle = this.requireArguments()
            selected = true
        }
        if (fragment != null) {
            this.mainActivity.switchWindow(fragment, bundle, true)
        }
        return if (selected) {
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onItemLongClick(position: Int, item: MediumItem?): Boolean {
        // actionMode on external Lists is forbidden, they are not modifiable
        if (!inActionMode && !isExternal) {
            inActionMode = true
            println("starting action mode")
            item?.let { selectionTracker.select(it.getSelectionKey()) }
            this.mainActivity.startActionMode(callback)
        }
        return true
    }

    override fun onItemClick(position: Int, item: MediumItem?) {
        if (inActionMode || item == null) {
            return
        }
        val fragment: TocFragment = TocFragment.newInstance(item.mediumId)
        mainActivity.switchWindow(fragment, true)
    }

    override val sortMap: LinkedHashMap<String, Sortings>
        get() {
            val map = LinkedHashMap<String, Sortings>()
            map["Title A-Z"] = Sortings.TITLE_AZ
            map["Title Z-A"] = Sortings.TITLE_ZA
            map["Medium Asc"] = Sortings.MEDIUM
            map["Medium Desc"] = Sortings.MEDIUM_REVERSE
            map["Latest Update Asc"] = Sortings.LAST_UPDATE_ASC
            map["Latest Update Desc"] = Sortings.LAST_UPDATE_DESC
            map["Episodes Asc"] = Sortings.NUMBER_EPISODE_ASC
            map["Episodes Desc"] = Sortings.NUMBER_EPISODE_DESC
            map["Episodes Read Asc"] = Sortings.NUMBER_EPISODE_READ_ASC
            map["Episodes Read Desc"] = Sortings.NUMBER_EPISODE_READ_DESC
            map["Episodes UnRead Asc"] = Sortings.NUMBER_EPISODE_UNREAD_ASC
            map["Episodes UnRead Desc"] = Sortings.NUMBER_EPISODE_UNREAD_DESC
            return map
        }
    override val viewModelClass: Class<ListMediaViewModel>
        get() = ListMediaViewModel::class.java

    override fun createPaged(model: ListMediaViewModel): Flow<PagingData<MediumItem>> {
        return transformPaging(viewModel.getMedia(listId, isExternal)).asFlow()
    }

    override fun createAdapter(): BaseAdapter<MediumItem, *> = MediumAdapter()

    private class MediumItemDiff : DiffUtil.ItemCallback<MediumItem>() {
        override fun areItemsTheSame(oldItem: MediumItem, newItem: MediumItem): Boolean {
            return oldItem.mediumId == newItem.mediumId
        }

        override fun areContentsTheSame(oldItem: MediumItem, newItem: MediumItem): Boolean {
            return oldItem == newItem
        }
    }

    private class MediumAdapter : BaseAdapter<MediumItem, NewMetaViewHolder>(MediumItemDiff()) {

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: NewMetaViewHolder, position: Int) {
            val item = getItem(position)

            if (item != null) {
                val currentReadEpisode = max(item.currentReadEpisode, 0)
                val lastEpisode = max(item.lastEpisode, 0)
                val lastUpdated = item.lastUpdated
                val relativeTime: String = TimeAgo.toRelative(lastUpdated) ?: "No Updates"

                holder.topLeftText.text = "$currentReadEpisode/$lastEpisode"
                holder.topRightText.text = relativeTime
                holder.mainText.text = item.title

                val drawable = DrawableUtils.getSelectableBackgroundCompat(
                    Color.WHITE,  // normal background
                    Color.GRAY,  // pressed background
                    Color.BLACK) // ripple color
                DrawableUtils.setBackgroundCompat(holder.itemView, drawable)

                holder.itemView.isActivated = selectionTracker.isSelected(item.getSelectionKey())
            } else {
                holder.itemView.isActivated = false
            }
        }

        override val layoutId = R.layout.meta_item

        override fun createViewHolder(root: View, viewType: Int) = NewMetaViewHolder(root)
    }

    companion object {
        const val ID = "id"
        const val TITLE = "listTitle"
        const val EXTERNAL = "external"

        fun getInstance(list: MediaList): ListMediumFragment {
            val bundle = Bundle()
            bundle.putInt(ID, list.listId)
            bundle.putString(TITLE, list.name)
            bundle.putBoolean(EXTERNAL, list is ExternalMediaList)
            val fragment = ListMediumFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}