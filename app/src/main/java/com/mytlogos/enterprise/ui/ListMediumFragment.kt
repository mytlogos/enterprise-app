package com.mytlogos.enterprise.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.*
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.model.*
import com.mytlogos.enterprise.tools.Sortings
import com.mytlogos.enterprise.tools.Utils.transform
import com.mytlogos.enterprise.ui.MediumListFragment.FlexibleMediumItem
import com.mytlogos.enterprise.viewmodel.ListMediaViewModel
import com.mytlogos.enterprise.viewmodel.ListsViewModel
import eu.davidea.flexibleadapter.SelectableAdapter
import eu.davidea.flexibleadapter.items.IFlexible
import java.util.*

class ListMediumFragment : BaseListFragment<MediumItem, ListMediaViewModel>() {
    private var listId = 0
    private var isExternal = false
    private var listTitle: String? = null
    private var inActionMode = false
    private val callback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.title = "Edit MediumList"
            mode.menuInflater.inflate(R.menu.list_medium_action_mode_menu, menu)
            Objects.requireNonNull(mainActivity.supportActionBar)!!.hide()
            flexibleAdapter!!.mode = SelectableAdapter.Mode.MULTI
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
            val selectedPositions = flexibleAdapter!!.selectedPositions
            val selectedMediaIds: MutableList<Int> = ArrayList()
            for (selectedPosition in selectedPositions) {
                val flexible =
                    flexibleAdapter!!.getItem(selectedPosition) as? FlexibleMediumItem ?: continue
                val mediumItem = flexible.item
                selectedMediaIds.add(mediumItem.mediumId)
            }
            val size = selectedMediaIds.size
            viewModel!!.removeMedia(listId, selectedMediaIds)
                .whenComplete { aBoolean: Boolean?, throwable: Throwable? ->
                    val mainHandler = Handler(Looper.getMainLooper())
                    mainHandler.post {
                        val text: String
                        if (aBoolean == null || !aBoolean || throwable != null) {
                            text = String.format("Could not delete %s Media from List '%s'",
                                size,
                                listTitle)
                        } else {
                            text = String.format("Removed %s Media from '%s'", size, listTitle)
                            // TODO: 29.07.2019 replace toast with undoable snackbar
                            mode.finish()
                        }
                        requireActivity().runOnUiThread { showToast(text) }
                    }
                }
            return false
        }

        fun moveItemsToList(mode: ActionMode): Boolean {
            val context = Objects.requireNonNull(context)
            val builder = AlertDialog.Builder(context)
            val listsViewModel = ViewModelProvider(this@ListMediumFragment)
                .get(ListsViewModel::class.java)
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
                val selectedPositions = flexibleAdapter!!.selectedPositions
                val selectedMediaIds: MutableList<Int> = ArrayList()
                for (selectedPosition in selectedPositions) {
                    val flexible =
                        flexibleAdapter!!.getItem(selectedPosition) as? FlexibleMediumItem ?: continue
                    val mediumItem = flexible.item
                    selectedMediaIds.add(mediumItem.mediumId)
                }
                val future = listsViewModel.moveMediumToList(listId, list.listId, selectedMediaIds)
                future.whenComplete { aBoolean: Boolean?, throwable: Throwable? ->
                    val mainHandler = Handler(Looper.getMainLooper())
                    mainHandler.post {
                        val text: String
                        if (aBoolean == null || !aBoolean || throwable != null) {
                            text = "Could not move Media to List '" + list.name + "'"
                        } else {
                            text = "Moved " + selectedMediaIds.size + " Media to " + list.name
                            // TODO: 29.07.2019 replace toast with undoable snackbar
                            mode.finish()
                        }
                        requireActivity().runOnUiThread { showToast(text) }
                    }
                }
            }
            builder.show()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arguments = requireArguments()
        listId = arguments.getInt(ID)
        isExternal = arguments.getBoolean(EXTERNAL)
        listTitle = arguments.getString(TITLE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
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

    override fun onItemLongClick(position: Int) {
        if (!inActionMode && !isExternal) {
            inActionMode = true
            println("starting action mode")
            flexibleAdapter!!.addSelection(position)
            this.mainActivity.startActionMode(callback)
        }
    }

    override fun onItemClick(view: View, position: Int): Boolean {
        if (inActionMode) {
            return if (position != RecyclerView.NO_POSITION) {
                flexibleAdapter!!.toggleSelection(position)
                true
            } else {
                false
            }
        }
        val item = flexibleAdapter!!.getItem(position) as? FlexibleMediumItem ?: return false
        val mediumItem = item.item
        val fragment: TocFragment = TocFragment.newInstance(mediumItem.mediumId)
        mainActivity.switchWindow(fragment, true)
        return false
    }

    override fun onItemSwipe(position: Int, direction: Int) {
        /*CompletableFuture<Boolean> future = this.getViewModel().removeMedia(this.listId, item.item.getMediumId());
        future.whenComplete((aBoolean, throwable) -> {
            Context context = this.getContext();
            if (!this.isAdded() || context == null) {
                return;
            }
            String msg;

            if (aBoolean == null || !aBoolean || throwable != null) {
                msg = "Could not remove item from list";
            } else {
                msg = "Successfully removed Item from List";
            }
            this.requireActivity().runOnUiThread(() -> {
                showToast(msg)
                this.requireActivity().onBackPressed();
            });

        });*/
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

    override fun createPagedListLiveData(): LiveData<PagedList<MediumItem>> {
        return transform(viewModel!!.getMedia(listId, isExternal))
    }

    override fun createFlexible(value: MediumItem): IFlexible<*> {
        return FlexibleMediumItem(value)
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