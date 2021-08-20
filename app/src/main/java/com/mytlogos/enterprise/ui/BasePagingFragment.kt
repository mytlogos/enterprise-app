package com.mytlogos.enterprise.ui

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.model.*
import com.mytlogos.enterprise.tools.*
import com.mytlogos.enterprise.viewmodel.FilterableViewModel
import com.mytlogos.enterprise.viewmodel.MediumFilterableViewModel
import com.mytlogos.enterprise.viewmodel.SortableViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import java.util.function.Consumer
import kotlin.reflect.KMutableProperty0

abstract class BasePagingFragment<Value : Any, ViewModel : AndroidViewModel> : BaseFragment() {

    private lateinit var fragmentRoot: ViewGroup
    private lateinit var adapter: BaseAdapter<Value, *>
    private var filterable: Filterable? = null
    protected lateinit var viewModel: ViewModel
    protected lateinit var listView: RecyclerView
    protected lateinit var selectionTracker: SelectionTracker<Long>
    protected var selectionMode: SelectionMode = SelectionMode.IDLE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // TODO: 28.07.2019 when items where loaded and
        //  then a swipe refresh follow somewhere, everything will disappear
        fragmentRoot = inflater.inflate(layoutId, container, false) as ViewGroup

        listView = fragmentRoot.findViewById(R.id.list) as RecyclerView

        val context = fragmentRoot.context
        val layoutManager = LinearLayoutManager(context)
        listView.layoutManager = layoutManager
        val decoration = DividerItemDecoration(context, layoutManager.orientation)
        listView.addItemDecoration(decoration)

        listView.adapter = initAdapter()

        viewModel = createViewModel()

        setHasOptionsMenu(true)

        filterable = createFilterable()

        initFabButton(fragmentRoot, listView, layoutManager)

        this.changeSelectionMode(SelectionMode.IDLE)

        viewLifecycleOwner.lifecycleScope.launch {
            createPaged(viewModel).collectLatest { adapter.submitData(it) }
        }
        return fragmentRoot
    }

    private fun initAdapter(): BaseAdapter<Value, *> {
        // Set the adapter
        adapter = createAdapter()
        val previousInit = adapter.holderInit
        adapter.holderInit = BaseAdapter.ViewInit { holder: RecyclerView.ViewHolder ->
            if (previousInit != null) {
                @Suppress("UNCHECKED_CAST")
                (previousInit as BaseAdapter.ViewInit<RecyclerView.ViewHolder>).init(holder)
            }

            // add long click listener on view holder with a bound item
            holder.itemView.setOnLongClickListener {
                val position = holder.bindingAdapterPosition

                if (position != RecyclerView.NO_POSITION) {
                    val item = getAdapter().getItemAt(position)
                    return@setOnLongClickListener onItemLongClick(position, item)
                }
                true
            }
            // add click listener on view holder with a bound item
            holder.itemView.setOnClickListener {
                val position = holder.bindingAdapterPosition

                if (position != RecyclerView.NO_POSITION) {
                    val item = getAdapter().getItemAt(position)
                    onItemClick(position, item)
                }
            }
        }
        return adapter
    }

    protected enum class SelectionMode {
        // do not allow any select
        IDLE,

        // allow only a single select
        SINGLE,

        // allow multiple selects
        MULTI
    }

    protected fun changeSelectionMode(mode: SelectionMode) {
        val predicate = when (mode) {
            SelectionMode.SINGLE -> SelectionPredicates.createSelectSingleAnything<Long>()
            SelectionMode.MULTI -> SelectionPredicates.createSelectAnything()
            SelectionMode.IDLE -> createSelectNothing()
        }
        val newTracker = SelectionTracker.Builder(
            "MediumSelection",
            listView,
            SimpleItemKeyProvider(listView),
            DetailsLookup(listView),
            StorageStrategy.createLongStorage(),
        ).withSelectionPredicate(predicate).build()

        // this ensures that only a initialized [selectionTracker] is used
        if (mode != SelectionMode.IDLE) {
            newTracker.setItemsSelected(selectionTracker.selection, true)
        }
        // update or initialize [selectionTracker]
        selectionTracker = newTracker
        selectionMode = mode

        val adapter = getAdapter()
        adapter.selectionTracker = selectionTracker
        adapter.notifyDataSetChanged()
    }

    private fun setSearchViewFilter(
        searchView: SearchView,
        textProperty: TextProperty,
        clearView: View?,
    ) {
        val filter = textProperty.get()
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


    abstract class BaseAdapter<Value : Any, ViewHolder : RecyclerView.ViewHolder>(
        diff: DiffUtil.ItemCallback<Value>,
    ) : PagingDataAdapter<Value, ViewHolder>(diff) {

        lateinit var selectionTracker: SelectionTracker<Long>

        fun interface ViewInit<ViewHolder : RecyclerView.ViewHolder> {
            fun init(holder: ViewHolder)
        }

        open var holderInit: ViewInit<in ViewHolder>? = null

        fun getItemAt(position: Int) = super.getItem(position)

        fun getItemFrom(holder: ViewHolder): Value? {
            val position = holder.bindingAdapterPosition
            return if (position == RecyclerView.NO_POSITION) null else getItem(position)
        }

        abstract val layoutId: Int

        abstract fun createViewHolder(root: View, viewType: Int): ViewHolder

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val root = LayoutInflater.from(parent.context).inflate(
                layoutId,
                parent,
                false
            )
            val viewHolder = createViewHolder(root, viewType)
            holderInit?.init(viewHolder)
            return viewHolder
        }
    }

    protected fun getAdapter(): BaseAdapter<Value, *> {
        return this.adapter
    }

    protected abstract fun createAdapter(): BaseAdapter<Value, *>

    private fun setSpinner(spinner: Spinner, property: PositionProperty) {
        val value = property.get()
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
                id: Long,
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

    private fun setCheckbox(checkBox: CheckBox, property: Property<Boolean>) {
        checkBox.isChecked = property.get()
        checkBox.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            property.set(isChecked)
        }
    }

    private fun setRadiogroup(view: RadioGroup, property: PositionProperty) {
        val value = property.get()
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

    fun getSelectedItems(): List<Value> {
        return selectionTracker.selection.mapNotNull {
            val position = getPositionFrom(it, listView)

            if (position == RecyclerView.NO_POSITION) {
                return@mapNotNull null
            }
            return@mapNotNull getAdapter().getItemAt(position)
        }
    }

    open fun onItemClick(position: Int, item: Value?) {

    }

    open fun onItemLongClick(position: Int, item: Value?): Boolean = false

    private fun openFilter() {
        val inflater = this.layoutInflater
        val filterable = filterable ?: return

        @SuppressLint("InflateParams") val view = inflater.inflate(filterable.filterLayout, null)
        if (filterable.searchFilterProperties != null) {
            for (property in filterable.searchFilterProperties!!) {
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
                        @Suppress("UNCHECKED_CAST")
                        setCheckbox(filterView, property as Property<Boolean>)
                    }
                }
            }
        }
        val builder = AlertDialog.Builder(this.mainActivity)
            .setView(view)

        setMediumCheckbox(view, R.id.text_medium, TEXT)
        setMediumCheckbox(view, R.id.audio_medium, AUDIO)
        setMediumCheckbox(view, R.id.video_medium, VIDEO)
        setMediumCheckbox(view, R.id.image_medium, IMAGE)

        filterable.onCreateFilter(view, builder)

        builder
            .setNeutralButton("Reset Filter") { _: DialogInterface?, _: Int ->
                if (viewModel is FilterableViewModel) {
                    (viewModel as FilterableViewModel?)!!.resetFilter()
                } else {
                    filterable.onResetFilter()
                }
            }
            .setPositiveButton("OK", null)
            .create()
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.filter_menu, menu)
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
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n")
    fun setNumberTextField(view: View, @IdRes id: Int, value: Int, minValue: Int = 0) {
        val minEpisodeRead: EditText = view.findViewById(id) as EditText
        if (value < minValue) {
            minEpisodeRead.text = null
        } else {
            minEpisodeRead.setText(value.toString())
        }
    }

    interface Filterable {
        fun onCreateFilter(view: View, builder: AlertDialog.Builder?) {}

        val filterLayout: Int

        val searchFilterProperties: Array<Property<*>>?

        fun onResetFilter() {}
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

    protected interface TextProperty : Property<String>

    protected class SimpleTextProperty(
        @IdRes
        viewId: Int,
        property: KMutableProperty0<String>,
        @IdRes
        clearViewId: Int = View.NO_ID,
    ) : SimpleProperty<String>(viewId, property, clearViewId), TextProperty

    protected class IntTextProperty(
        @IdRes
        override val viewId: Int,
        val property: KMutableProperty0<Int>,
        val showToast: (v: String, duration: Int) -> Unit,
    ) : TextProperty {
        override fun get(): String = property.get().toString()
        override fun set(newFilter: String) {
            try {
                property.set(newFilter.toInt())
            } catch (e: NumberFormatException) {
                showToast("Invalid Input", Toast.LENGTH_SHORT)
            }
        }
    }

    protected open class SimpleProperty<E>(
        @IdRes
        override val viewId: Int,
        val property: KMutableProperty0<E>,
        @IdRes
        override val clearViewId: Int = View.NO_ID,
    ) : Property<E> {
        override fun get(): E = property.get()
        override fun set(newFilter: E) = property.set(newFilter)
    }

    protected interface PositionProperty : Property<Int> {
        fun positionalMapping(): IntArray
    }

    protected class SimplePositionProperty(
        @IdRes
        override val viewId: Int,
        val property: KMutableProperty0<Int>,
        val values: IntArray = intArrayOf(1, 0, -1),
    ) : PositionProperty {
        override fun positionalMapping(): IntArray {
            return values
        }

        override fun get(): Int {
            return property.get()
        }

        override fun set(newFilter: Int) {
            property.set(newFilter)
        }

    }

    open fun createFilterable(): Filterable? {
        return null
    }

    @get:LayoutRes
    open val layoutId: Int = R.layout.normal_list

    @get:IdRes
    open val listContainerId: Int = R.id.list

    protected fun <E> setStringSpinner(
        view: View,
        @IdRes resId: Int,
        valueMap: LinkedHashMap<String, E>,
        consumer: Consumer<E?>,
    ) {
        val items = valueMap.keys.toTypedArray()
        val readSpinner: Spinner = view.findViewById(resId) as Spinner
        val readAdapter = TextOnlyListAdapter<String?>(requireContext(), null)
        readAdapter.addAll(*items)
        readSpinner.adapter = readAdapter
        readSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long,
            ) {
                val item = items[position]
                consumer.accept(valueMap[item])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    protected fun getPosition(view: View): Int {
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

    private fun onSortingChanged(sortings: Sortings) {
        if (viewModel is SortableViewModel) {
            (viewModel as SortableViewModel).setSort(sortings)
        }
    }

    private fun onSortMenuClicked() {
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

    private fun setMediumCheckbox(view: View, @IdRes boxId: Int, @MediumType type: Int) {
        val model = viewModel
        if (model !is MediumFilterableViewModel) {
            return
        }
        val filterableViewModel = model as MediumFilterableViewModel
        val medium = filterableViewModel.mediumFilter
        val box: CheckBox = view.findViewById(boxId) as CheckBox?
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

    abstract fun createPaged(model: ViewModel): Flow<PagingData<Value>>

    /**
     * Find and initialize the FAB Button for fast up and down navigation.
     */
    private fun initFabButton(
        root: View,
        listView: RecyclerView,
        layoutManager: LinearLayoutManager,
    ) {
        val button: FloatingActionButton = root.findViewById(R.id.fab) as FloatingActionButton

        val downArrow = R.drawable.ic_arrow_down_bright
        val upArrow = R.drawable.ic_arrow_up_bright

        button.tag = downArrow
        button.setOnLongClickListener {
            val newDrawableResource: Int = if (downArrow == button.tag) {
                upArrow
            } else {
                downArrow
            }
            button.setImageResource(newDrawableResource)
            button.tag = newDrawableResource
            true
        }
        button.setOnClickListener {
            val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()

            if (firstVisiblePosition == RecyclerView.NO_POSITION) {
                return@setOnClickListener
            }

            // either scroll down or up depending on arrow direction
            val direction = if (downArrow == button.tag) {
                1
            } else {
                -1
            }

            listView.scrollToPosition(firstVisiblePosition + 50 * direction)
        }
    }

    private fun createViewModel(): ViewModel {
        return ViewModelProvider(this).get(viewModelClass)
    }
}