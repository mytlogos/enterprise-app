package com.mytlogos.enterprise.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.model.MediumType
import com.mytlogos.enterprise.model.MediumType.isType
import com.mytlogos.enterprise.model.MediumType.addMediumType
import com.mytlogos.enterprise.model.MediumType.removeMediumType
import com.mytlogos.enterprise.tools.Sortings
import com.mytlogos.enterprise.viewmodel.FilterableViewModel
import com.mytlogos.enterprise.viewmodel.MediumFilterableViewModel
import com.mytlogos.enterprise.viewmodel.SortableViewModel
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.FlexibleAdapter.*
import eu.davidea.flexibleadapter.items.IFlexible
import java.util.*
import java.util.function.Consumer
import kotlin.math.min

abstract class BaseListFragment<Value : Any, ViewModel : AndroidViewModel?> : BaseFragment(),
    OnItemClickListener, EndlessScrollListener, OnActionStateListener,
    OnDeleteCompleteListener, OnFilterListener, OnItemLongClickListener,
    OnItemMoveListener, OnItemSwipeListener, OnStickyHeaderChangeListener, OnUpdateListener {
    var flexibleAdapter: FlexibleAdapter<IFlexible<*>>? = null
        private set
    var viewModel: ViewModel? = null
        private set
    var livePagedList: LiveData<PagedList<Value>>? = null
        private set
    open var listContainer: View? = null

    private var fragmentRoot: ViewGroup? = null
    private var filterable: Filterable? = null
    private var scrollToWhenLoaded = -1
    private val callback: PagedList.Callback = object : PagedList.Callback() {
        override fun onChanged(position: Int, count: Int) {
            val pagedList = livePagedList!!.value ?: return
            val fragment: BaseListFragment<Value, ViewModel> = this@BaseListFragment
            val values: List<Value> = pagedList.subList(position, position + count)
            val newItems = fragment.convertToFlexible(values)
            val adapter = fragment.flexibleAdapter
            newItems.removeAll(adapter!!.currentItems)
            adapter.onLoadMoreComplete(newItems)
            var previouslyUnloaded = 0
            for (i in 0 until position) {
                if (pagedList[i] == null) {
                    previouslyUnloaded++
                }
            }
            val startIndex = position - previouslyUnloaded
            val currentItems = adapter.currentItems
            var currentIndex = startIndex
            var newIndex = 0
            while (currentIndex < currentItems.size && newIndex < newItems.size) {
                val flexible = currentItems[currentIndex]
                val newFlexible = newItems[newIndex]
                if (flexible != newFlexible) {
                    val oldIndex = currentItems.indexOf(newFlexible)
                    adapter.moveItem(oldIndex, currentIndex)
                }
                currentIndex++
                newIndex++
            }
            val scrollTo = fragment.scrollToWhenLoaded
            if (scrollTo >= 0) {
                if (pagedList[scrollTo] != null) {
                    fragment.scrollToWhenLoaded = -1
                    adapter.smoothScrollToPosition(scrollTo)
                } else if (scrollTo < position) {
                    adapter.smoothScrollToPosition(position)
                }
            }
        }

        override fun onInserted(position: Int, count: Int) {
            println(String.format("Position: %s and Count: %s - Inserted", position, count))
        }

        override fun onRemoved(position: Int, count: Int) {
            println(String.format("Position: %s and Count: %s - Removed", position, count))
        }
    }
    private val pagedListObserver = Observer { items: PagedList<Value> ->
        if (checkEmptyList(items, fragmentRoot!!, listContainer)) {
            println("empty dataset")
            flexibleAdapter!!.updateDataSet(null)
            return@Observer
        }
        val flexibles: List<IFlexible<*>> = convertToFlexible(items)
        flexibleAdapter!!.updateDataSet(flexibles)
        val snapshot = items.snapshot()
        items.addWeakCallback(snapshot, callback)
    }
    var listView: RecyclerView? = null
        private set

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // TODO: 28.07.2019 when items where loaded and
        //  then a swipe refresh follow somewhere, everything will disappear
        fragmentRoot = inflater.inflate(layoutId, container, false) as ViewGroup
        val button: FloatingActionButton = fragmentRoot!!.findViewById(R.id.fab) as FloatingActionButton
        button.tag = R.drawable.ic_arrow_down_bright
        button.setOnLongClickListener {
            val newDrawableResource: Int = if (R.drawable.ic_arrow_down_bright == button.tag) {
                R.drawable.ic_arrow_up_bright
            } else {
                R.drawable.ic_arrow_down_bright
            }
            button.setImageResource(newDrawableResource)
            button.tag = newDrawableResource
            true
        }
        button.setOnClickListener {
            if (flexibleAdapter!!.itemCount == 0) {
                return@setOnClickListener
            }
            if (R.drawable.ic_arrow_down_bright == button.tag) {
                flexibleAdapter!!.smoothScrollToPosition(flexibleAdapter!!.itemCount - 1)
            } else {
                flexibleAdapter!!.smoothScrollToPosition(0)
            }
        }
        listView = fragmentRoot!!.findViewById(R.id.list) as RecyclerView?
        val localListView = listView!!

        // Set the adapter
        val context = fragmentRoot!!.context
        val layoutManager = LinearLayoutManager(context)
        localListView.layoutManager = layoutManager
        val decoration = DividerItemDecoration(context, layoutManager.orientation)
        localListView.addItemDecoration(decoration)
        flexibleAdapter = FlexibleAdapter<IFlexible<*>>(null)
            .setStickyHeaders(true)
            .setDisplayHeadersAtStartUp(true)
        val localFlexibleAdapter = flexibleAdapter!!

        localFlexibleAdapter.setEndlessScrollListener(this, ProgressItem())
        localFlexibleAdapter.addListener(this)
        onFlexibleCreated(flexibleAdapter)
        localListView.adapter = flexibleAdapter
        viewModel = createViewModel()
        listContainer = fragmentRoot!!.findViewById(listContainerId)
        setLivePagedList(createPagedListLiveData())
        setHasOptionsMenu(true)
        filterable = createFilterable()
        return fragmentRoot!!
    }

    private fun setSearchViewFilter(
        searchView: SearchView,
        textProperty: TextProperty,
        clearView: View?
    ) {
        val filter = textProperty.get()!!
        searchView.setQuery(filter, false)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                textProperty.set(newText)
                return false
            }
        })
        clearView?.setOnClickListener { searchView.setQuery("", true) }
    }

    private fun setSpinner(spinner: Spinner, property: PositionProperty) {
        val value = property.get()!!
        val values = property.positionalMapping()
        var selected = 0
        var index = 0
        val valuesLength = values.size
        while (index < valuesLength) {
            val i = values[index]
            if (i == value) {
                selected = index
            }
            index++
        }
        spinner.setSelection(selected)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                property.set(values[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setEditText(editText: EditText, property: TextProperty) {
        editText.setText(property.get())
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                property.set(s.toString())
            }
        })
    }

    private fun setCheckbox(checkBox: CheckBox, property: BooleanProperty) {
        checkBox.isChecked = property.get()!!
        checkBox.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            property.set(isChecked)
        }
    }

    private fun setRadiogroup(view: RadioGroup, property: PositionProperty) {
        val value = property.get()!!
        val values = property.positionalMapping()
        var selected = -1
        var index = 0
        val valuesLength = values.size
        while (index < valuesLength) {
            val i = values[index]
            if (i == value) {
                selected = index
                break
            }
            index++
        }
        if (selected >= 0) {
            val id = view.getChildAt(selected).id
            view.check(id)
        }
        view.setOnCheckedChangeListener { group: RadioGroup, checkedId: Int ->
            val radioButton: View = group.findViewById(checkedId)
            val childIndex = group.indexOfChild(radioButton)
            property.set(values[childIndex])
        }
    }

    private fun openFilter() {
        val inflater = this.layoutInflater
        @SuppressLint("InflateParams") val view = inflater.inflate(filterable!!.filterLayout, null)
        if (filterable!!.searchFilterProperties != null) {
            for (property in filterable!!.searchFilterProperties!!) {
                val filterView: View = view.findViewById(property.viewId)
                val clearSearchButtonId = property.clearViewId
                var clearTitleButton: ImageButton? = null
                if (clearSearchButtonId != View.NO_ID) {
                    clearTitleButton = view.findViewById(clearSearchButtonId) as ImageButton?
                }
                when (filterView) {
                    is SearchView -> {
                        setSearchViewFilter(filterView, property as TextProperty, clearTitleButton)
                    }
                    is Spinner -> {
                        setSpinner(filterView, property as PositionProperty)
                    }
                    is RadioGroup -> {
                        setRadiogroup(filterView, property as PositionProperty)
                    }
                    is EditText -> {
                        setEditText(filterView, property as TextProperty)
                    }
                    is CheckBox -> {
                        setCheckbox(filterView, property as BooleanProperty)
                    }
                }
            }
        }
        val builder = AlertDialog.Builder(this.mainActivity)
            .setView(view)
        setMediumCheckbox(view, R.id.text_medium, MediumType.TEXT)
        setMediumCheckbox(view, R.id.audio_medium, MediumType.AUDIO)
        setMediumCheckbox(view, R.id.video_medium, MediumType.VIDEO)
        setMediumCheckbox(view, R.id.image_medium, MediumType.IMAGE)
        filterable!!.onCreateFilter(view, builder)
        builder
            .setNeutralButton("Reset Filter") { _: DialogInterface?, _: Int ->
                if (viewModel is FilterableViewModel) {
                    (viewModel as FilterableViewModel?)!!.resetFilter()
                } else {
                    filterable!!.onResetFilter()
                }
            }
            .setPositiveButton("OK", null)
            .create()
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (filterable != null) {
            inflater.inflate(R.menu.filter_menu, menu)
        }
        val sortMap: Map<String, Sortings> = sortMap
        if (sortMap.isNotEmpty()) {
            inflater.inflate(R.menu.sort_menu, menu)
        }
        inflater.inflate(R.menu.base_list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.filter_menu -> {
                openFilter()
                return true
            }
            R.id.sort_menu -> {
                onSortMenuClicked()
                return true
            }
            R.id.go_to -> {
                onGotoItemClicked()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun setLivePagedList(livePagedList: LiveData<PagedList<Value>>?) {
        if (this.livePagedList != null) {
            this.livePagedList!!.removeObserver(pagedListObserver)
        }
        this.livePagedList = livePagedList
        this.livePagedList!!.observe(viewLifecycleOwner, pagedListObserver)
    }

    override fun noMoreLoad(newItemsSize: Int) {
        println("nothing to load anymore")
    }

    override fun onLoadMore(lastPosition: Int, currentPage: Int) {
        val pagedList = livePagedList!!.value
        if (pagedList == null) {
            flexibleAdapter!!.onLoadMoreComplete(null)
            return
        }
        if (lastPosition >= pagedList.size) {
            flexibleAdapter!!.onLoadMoreComplete(null)
            return
        }
        pagedList.loadAround(lastPosition)
        println("loading more")
    }

    override fun onActionStateChanged(viewHolder: RecyclerView.ViewHolder, actionState: Int) {}
    override fun onDeleteConfirmed(event: Int) {}
    override fun onUpdateFilterView(size: Int) {}
    override fun onItemClick(view: View, position: Int): Boolean {
        return false
    }

    override fun onItemLongClick(position: Int) {}
    override fun shouldMoveItem(fromPosition: Int, toPosition: Int): Boolean {
        return false
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {}
    override fun onItemSwipe(position: Int, direction: Int) {}
    override fun onStickyHeaderChange(newPosition: Int, oldPosition: Int) {}
    override fun onUpdateEmptyView(size: Int) {}
    fun onFlexibleCreated(adapter: FlexibleAdapter<IFlexible<*>>?) {}
    @SuppressLint("SetTextI18n")
    fun setNumberTextField(view: View, @IdRes id: Int, value: Int, minValue: Int) {
        val minEpisodeRead = view.findViewById(id) as EditText
        if (value < minValue) {
            minEpisodeRead.setText(null)
        } else {
            minEpisodeRead.setText(value.toString())
        }
    }

    interface Filterable {
        fun onCreateFilter(view: View, builder: AlertDialog.Builder?) {}
        val filterLayout: Int
        fun onResetFilter() {}
        val searchFilterProperties: Array<Property<*>>?
            get() = arrayOf()
    }

    interface Property<E> {
        @get:IdRes
        val viewId: Int

        @get:IdRes
        val clearViewId: Int
            get() = View.NO_ID

        fun get(): E
        fun set(newFilter: E)
    }

    internal interface TextProperty : Property<String>
    internal interface BooleanProperty : Property<Boolean>
    internal interface PositionProperty : Property<Int> {
        fun positionalMapping(): IntArray
    }

    open fun createFilterable(): Filterable? {
        return null
    }

    @get:LayoutRes
    open val layoutId: Int
        get() = R.layout.normal_list

    @get:IdRes
    open val listContainerId: Int
        get() = R.id.list
    val items: List<Value>
        get() {
            val values: MutableList<Value> = ArrayList()
            val pagedList = livePagedList!!.value ?: return values
            for (value in pagedList) {
                if (value == null) {
                    break
                }
                values.add(value)
            }
            return values
        }

    fun <E> setStringSpinner(
        view: View,
        @IdRes resId: Int,
        valueMap: LinkedHashMap<String, E>,
        consumer: Consumer<E?>
    ) {
        val items = valueMap.keys.toTypedArray()
        val readSpinner = view.findViewById(resId) as Spinner
        val readAdapter = TextOnlyListAdapter<String?>(requireContext(), null)
        readAdapter.addAll(*items)
        readSpinner.adapter = readAdapter
        readSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                val item = items[position]
                consumer.accept(valueMap[item])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    fun onGotoItemClicked() {
        val context = requireContext()
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Go to Item")
        val inputView = createGotoView(context)
        builder.setView(inputView)
        builder.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            val position = getPosition(inputView)
            if (position < 0) {
                return@setPositiveButton
            }
            val liveData = livePagedList
            val list = liveData!!.value
            if (list == null) {
                showToast("Cannot go anywhere: No Data available.")
                return@setPositiveButton
            }
            var loadAround = min(position, list.size - 1)
            // we know it is an integer key (it always is with room)
            val lastKey = list.lastKey as Int?
            val pageSize = list.config.pageSize
            // this is a unnecessary safety check for lint
            val startKey = lastKey ?: 0
            if (startKey < loadAround) {
                scrollToWhenLoaded = loadAround
                var i = startKey
                while (i <= loadAround) {
                    list.loadAround(i)
                    i += pageSize
                }
            } else {
                val upperLimit = flexibleAdapter!!.currentItems.size - 1
                loadAround = min(loadAround, upperLimit)
                flexibleAdapter!!.smoothScrollToPosition(loadAround)
            }
        }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.cancel() }
        builder.show()
    }

    fun createGotoView(context: Context?): View {
        val input = EditText(context)
        input.inputType =
            InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
        return input
    }

    fun getPosition(view: View): Int {
        return if (view is EditText) {
            getPosition(view.text.toString())
        } else {
            throw IllegalArgumentException("Expected EditText: Got $view")
        }
    }

    open fun getPosition(text: String): Int {
        return try {
            text.toInt()
        } catch (e: NumberFormatException) {
            showToast("Cannot go anywhere: expected an Integer")
            -1
        }
    }

    open val sortMap: LinkedHashMap<String, Sortings>
        get() = LinkedHashMap()

    fun onSortingChanged(sortings: Sortings) {
        if (viewModel is SortableViewModel) {
            (viewModel as SortableViewModel).setSort(sortings)
        }
    }

    fun onSortMenuClicked() {
        val map = sortMap
        val strings = map.keys.toTypedArray()
        val builder = AlertDialog.Builder(this.requireContext())
        builder.setItems(strings) { _: DialogInterface?, which: Int ->
            if (which < strings.size && which >= 0) {
                val title = strings[which]
                val sortings = map[title]
                onSortingChanged(sortings!!)
            }
        }
        builder.setTitle("Sort By")
        builder.create().show()
    }

    private fun setMediumCheckbox(view: View, @IdRes boxId: Int, @MediumType.Medium type: Int) {
        val model = viewModel
        if (model !is MediumFilterableViewModel) {
            return
        }
        val filterableViewModel = model as MediumFilterableViewModel
        val medium = filterableViewModel.mediumFilter
        val box = view.findViewById(boxId) as CheckBox?
            ?: throw IllegalStateException(String.format(
                "%s extends %s,expected a filter checkbox with id: %d",
                model.javaClass.simpleName,
                MediumFilterableViewModel::class.java.canonicalName,
                boxId
            ))
        box.isChecked = isType(medium, type)
        box.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            val filter = filterableViewModel.mediumFilter
            val newMediumFilter: Int = if (isChecked) {
                addMediumType(filter, type)
            } else {
                removeMediumType(filter, type)
            }
            filterableViewModel.mediumFilter = newMediumFilter
        }
    }

    abstract val viewModelClass: Class<ViewModel>
    abstract fun createPagedListLiveData(): LiveData<PagedList<Value>>
    fun convertToFlexible(list: Collection<Value?>): MutableList<IFlexible<*>> {
        val items: MutableList<IFlexible<*>> = ArrayList()
        for (value in list) {
            if (value == null) {
                continue
            }
            items.add(createFlexible(value))
        }
        return items
    }

    abstract fun createFlexible(value: Value): IFlexible<*>
    private fun createViewModel(): ViewModel {
        return ViewModelProvider(this).get(viewModelClass)
    }
}