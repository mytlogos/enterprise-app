package com.mytlogos.enterprise.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.model.MediumType;
import com.mytlogos.enterprise.tools.Sortings;
import com.mytlogos.enterprise.viewmodel.FilterableViewModel;
import com.mytlogos.enterprise.viewmodel.MediumFilterableViewModel;
import com.mytlogos.enterprise.viewmodel.SortableViewModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IFlexible;

abstract class BaseListFragment<Value, ViewModel extends AndroidViewModel> extends BaseFragment
        implements FlexibleAdapter.OnItemClickListener, FlexibleAdapter.EndlessScrollListener,
        FlexibleAdapter.OnActionStateListener, FlexibleAdapter.OnDeleteCompleteListener,
        FlexibleAdapter.OnFilterListener, FlexibleAdapter.OnItemLongClickListener,
        FlexibleAdapter.OnItemMoveListener, FlexibleAdapter.OnItemSwipeListener,
        FlexibleAdapter.OnStickyHeaderChangeListener, FlexibleAdapter.OnUpdateListener {

    private FlexibleAdapter<IFlexible> flexibleAdapter;
    private ViewModel viewModel;
    private LiveData<PagedList<Value>> livePagedList;
    private View listContainer;
    private ViewGroup fragmentRoot;
    private Filterable filterable;
    private int scrollToWhenLoaded = -1;
    private PagedList.Callback callback = new PagedList.Callback() {
        @Override
        public void onChanged(int position, int count) {
            PagedList<Value> pagedList = getLivePagedList().getValue();
            if (pagedList == null) {
                return;
            }
            BaseListFragment<Value, ViewModel> fragment = BaseListFragment.this;

            List<Value> values = pagedList.subList(position, position + count);
            List<IFlexible> newItems = fragment.convertToFlexible(values);

            FlexibleAdapter<IFlexible> adapter = fragment.getFlexibleAdapter();
            newItems.removeAll(adapter.getCurrentItems());
            adapter.onLoadMoreComplete(newItems);

            int previouslyUnloaded = 0;
            for (int i = 0; i < position; i++) {
                if (pagedList.get(i) == null) {
                    previouslyUnloaded++;
                }
            }
            int startIndex = position - previouslyUnloaded;
            List<IFlexible> currentItems = adapter.getCurrentItems();

            for (int currentIndex = startIndex, newIndex = 0; currentIndex < currentItems.size() && newIndex < newItems.size(); currentIndex++, newIndex++) {
                IFlexible flexible = currentItems.get(currentIndex);
                IFlexible newFlexible = newItems.get(newIndex);

                if (!flexible.equals(newFlexible)) {
                    int oldIndex = currentItems.indexOf(newFlexible);
                    adapter.moveItem(oldIndex, currentIndex);
                }
            }

            int scrollTo = fragment.scrollToWhenLoaded;

            if (scrollTo >= 0) {
                if (pagedList.get(scrollTo) != null) {
                    fragment.scrollToWhenLoaded = -1;
                    adapter.smoothScrollToPosition(scrollTo);
                } else if (scrollTo < position) {
                    adapter.smoothScrollToPosition(position);
                }
            }
        }

        @Override
        public void onInserted(int position, int count) {
            System.out.println(String.format("Position: %s and Count: %s - Inserted", position, count));
        }

        @Override
        public void onRemoved(int position, int count) {
            System.out.println(String.format("Position: %s and Count: %s - Removed", position, count));
        }
    };
    private final Observer<PagedList<Value>> pagedListObserver = items -> {
        if (checkEmptyList(items, this.fragmentRoot, this.listContainer)) {
            System.out.println("empty dataset");
            this.flexibleAdapter.updateDataSet(null);
            return;
        }
        List<IFlexible> flexibles = convertToFlexible(items);
        this.flexibleAdapter.updateDataSet(flexibles);
        List<Value> snapshot = items.snapshot();
        items.addWeakCallback(snapshot, this.callback);
    };
    private RecyclerView recyclerView;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // TODO: 28.07.2019 when items where loaded and
        //  then a swipe refresh follow somewhere, everything will disappear
        fragmentRoot = (ViewGroup) inflater.inflate(getLayoutId(), container, false);

        FloatingActionButton button = fragmentRoot.findViewById(R.id.fab);
        button.setTag(R.drawable.ic_arrow_down_bright);
        button.setOnLongClickListener(v -> {
            int newDrawableResource;
            if (((Integer) R.drawable.ic_arrow_down_bright).equals(button.getTag())) {
                newDrawableResource = R.drawable.ic_arrow_up_bright;
            } else {
                newDrawableResource = R.drawable.ic_arrow_down_bright;
            }
            button.setImageResource(newDrawableResource);
            button.setTag(newDrawableResource);
            return true;
        });

        button.setOnClickListener(v -> {
            if (flexibleAdapter.getItemCount() == 0) {
                return;
            }
            if (((Integer) R.drawable.ic_arrow_down_bright).equals(button.getTag())) {
                flexibleAdapter.smoothScrollToPosition(flexibleAdapter.getItemCount() - 1);
            } else {
                flexibleAdapter.smoothScrollToPosition(0);
            }
        });

        recyclerView = fragmentRoot.findViewById(R.id.list);

        // Set the adapter
        Context context = fragmentRoot.getContext();

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration decoration = new DividerItemDecoration(context, layoutManager.getOrientation());
        recyclerView.addItemDecoration(decoration);

        flexibleAdapter = new FlexibleAdapter<>(null)
                .setStickyHeaders(true)
                .setDisplayHeadersAtStartUp(true);

        flexibleAdapter.setEndlessScrollListener(this, new ProgressItem());
        flexibleAdapter.addListener(this);

        this.onFlexibleCreated(flexibleAdapter);
        recyclerView.setAdapter(flexibleAdapter);

        this.viewModel = this.createViewModel();
        this.listContainer = fragmentRoot.findViewById(getListContainerId());

        this.setLivePagedList(this.createPagedListLiveData());

        this.setHasOptionsMenu(true);
        this.filterable = createFilterable();
        return fragmentRoot;
    }

    private void setSearchViewFilter(SearchView searchView, TextProperty textProperty, View clearView) {
        String filter = textProperty.get();

        searchView.setQuery(filter, false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                textProperty.set(newText);
                return false;
            }
        });

        if (clearView != null) {
            clearView.setOnClickListener(v -> searchView.setQuery("", true));
        }
    }

    private void setSpinner(Spinner spinner, PositionProperty property) {
        Integer value = property.get();
        int[] values = property.positionalMapping();
        int selected = 0;
        for (int index = 0, valuesLength = values.length; index < valuesLength; index++) {
            int i = values[index];
            if (i == value) {
                selected = index;
            }
        }
        spinner.setSelection(selected);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                property.set(values[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setEditText(EditText editText, TextProperty property) {
        editText.setText(property.get());
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                property.set(s.toString());
            }
        });
    }

    private void setCheckbox(CheckBox checkBox, BooleanProperty property) {
        checkBox.setChecked(property.get());
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> property.set(isChecked));
    }

    private void openFilter() {
        LayoutInflater inflater = this.getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(this.filterable.getFilterLayout(), null);

        if (this.filterable.getSearchFilterProperties() != null) {
            for (Property property : this.filterable.getSearchFilterProperties()) {
                View filterView = view.findViewById(property.getViewId());

                int clearSearchButtonId = property.getClearViewId();
                ImageButton clearTitleButton = null;

                if (clearSearchButtonId != View.NO_ID) {
                    clearTitleButton = view.findViewById(clearSearchButtonId);
                }

                if (filterView instanceof SearchView) {
                    SearchView searchView = (SearchView) filterView;
                    this.setSearchViewFilter(searchView, (TextProperty) property, clearTitleButton);
                } else if (filterView instanceof Spinner) {
                    this.setSpinner(((Spinner) filterView), (PositionProperty) property);
                } else if (filterView instanceof EditText) {
                    this.setEditText(((EditText) filterView), (TextProperty) property);
                } else if (filterView instanceof CheckBox) {
                    this.setCheckbox(((CheckBox) filterView), (BooleanProperty) property);
                }
            }
        }
        AlertDialog.Builder builder = new AlertDialog
                .Builder(this.getMainActivity())
                .setView(view);

        setMediumCheckbox(view, R.id.text_medium, MediumType.TEXT);
        setMediumCheckbox(view, R.id.audio_medium, MediumType.AUDIO);
        setMediumCheckbox(view, R.id.video_medium, MediumType.VIDEO);
        setMediumCheckbox(view, R.id.image_medium, MediumType.IMAGE);

        this.filterable.onCreateFilter(view, builder);
        builder
                .setNeutralButton("Reset Filter", (dialog, which) -> {
                    if (getViewModel() instanceof FilterableViewModel) {
                        ((FilterableViewModel) getViewModel()).resetFilter();
                    } else {
                        this.filterable.onResetFilter();
                    }
                })
                .setPositiveButton("OK", null)
                .create()
                .show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (this.filterable != null) {
            inflater.inflate(R.menu.filter_menu, menu);
        }
        Map<String, Sortings> sortMap = this.getSortMap();

        if (sortMap != null && !sortMap.isEmpty()) {
            inflater.inflate(R.menu.sort_menu, menu);
        }
        inflater.inflate(R.menu.base_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filter_menu:
                openFilter();
                return true;
            case R.id.sort_menu:
                onSortMenuClicked();
                return true;
            case R.id.go_to:
                onGotoItemClicked();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("WeakerAccess")
    final void setLivePagedList(LiveData<PagedList<Value>> livePagedList) {
        if (this.livePagedList != null) {
            this.livePagedList.removeObserver(pagedListObserver);
        }
        this.livePagedList = livePagedList;
        this.livePagedList.observe(this, pagedListObserver);
    }

    @Override
    public void noMoreLoad(int newItemsSize) {
        System.out.println("nothing to load anymore");
    }

    @Override
    public void onLoadMore(int lastPosition, int currentPage) {
        PagedList<Value> pagedList = livePagedList.getValue();

        if (pagedList == null) {
            flexibleAdapter.onLoadMoreComplete(null);
            return;
        }
        if (lastPosition >= pagedList.size()) {
            flexibleAdapter.onLoadMoreComplete(null);
            return;
        }
        pagedList.loadAround(lastPosition);
        System.out.println("loading more");
    }

    @Override
    public void onActionStateChanged(RecyclerView.ViewHolder viewHolder, int actionState) {

    }

    @Override
    public void onDeleteConfirmed(int event) {

    }

    @Override
    public void onUpdateFilterView(int size) {

    }

    @Override
    public boolean onItemClick(View view, int position) {
        return false;
    }

    @Override
    public void onItemLongClick(int position) {

    }

    @Override
    public boolean shouldMoveItem(int fromPosition, int toPosition) {
        return false;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {

    }

    @Override
    public void onItemSwipe(int position, int direction) {

    }

    @Override
    public void onStickyHeaderChange(int newPosition, int oldPosition) {

    }

    @Override
    public void onUpdateEmptyView(int size) {

    }

    @SuppressWarnings("WeakerAccess")
    void onFlexibleCreated(FlexibleAdapter<IFlexible> adapter) {

    }

    @SuppressLint("SetTextI18n")
    void setNumberTextField(View view, @IdRes int id, int value, int minValue) {
        EditText minEpisodeRead = view.findViewById(id);

        if (value < minValue) {
            minEpisodeRead.setText(null);
        } else {
            minEpisodeRead.setText(Integer.toString(value));
        }
    }

    interface Filterable {

        default void onCreateFilter(View view, AlertDialog.Builder builder) {

        }

        int getFilterLayout();

        default void onResetFilter() {

        }

        default Property[] getSearchFilterProperties() {
            return new Property[0];
        }
    }

    interface Property<E> {
        @IdRes
        int getViewId();

        @IdRes
        default int getClearViewId() {
            return View.NO_ID;
        }

        E get();

        void set(E newFilter);
    }


    interface TextProperty extends Property<String> {

    }

    interface BooleanProperty extends Property<Boolean> {

    }

    interface PositionProperty extends Property<Integer> {
        int[] positionalMapping();
    }

    @Nullable
    Filterable createFilterable() {
        return null;
    }

    @LayoutRes
    public int getLayoutId() {
        return R.layout.normal_list;
    }

    final ViewModel getViewModel() {
        return viewModel;
    }

    final LiveData<PagedList<Value>> getLivePagedList() {
        return livePagedList;
    }

    View getListContainer() {
        return listContainer;
    }

    final RecyclerView getListView() {
        return recyclerView;
    }

    @IdRes
    int getListContainerId() {
        return R.id.list;
    }

    final FlexibleAdapter<IFlexible> getFlexibleAdapter() {
        return flexibleAdapter;
    }

    final List<Value> getItems() {
        List<Value> values = new ArrayList<>();
        PagedList<Value> pagedList = this.livePagedList.getValue();

        if (pagedList == null) {
            return values;
        }
        for (Value value : pagedList) {
            if (value == null) {
                break;
            }
            values.add(value);
        }
        return values;
    }

    <E> void setStringSpinner(View view, @IdRes int resId, LinkedHashMap<String, E> valueMap, Consumer<E> consumer) {
        String[] items = valueMap.keySet().toArray(new String[0]);

        Spinner readSpinner = view.findViewById(resId);
        TextOnlyListAdapter<String> readAdapter = new TextOnlyListAdapter<>(requireContext(), null);
        readAdapter.addAll(items);

        readSpinner.setAdapter(readAdapter);
        readSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = items[position];
                consumer.accept(valueMap.get(item));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @SuppressWarnings("WeakerAccess")
    void onGotoItemClicked() {
        Context context = this.requireContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Go to Item");

        View inputView = createGotoView(context);
        builder.setView(inputView);

        builder.setPositiveButton("OK", (dialog, which) -> {
            int position = getPosition(inputView);

            if (position < 0) {
                return;
            }
            LiveData<PagedList<Value>> liveData = getLivePagedList();
            PagedList<Value> list = liveData.getValue();

            if (list == null) {
                showToast("Cannot go anywhere: No Data available.");
                return;
            }
            int loadAround = Math.min(position, list.size() - 1);
            // we know it is an integer key (it always is with room)
            Integer lastKey = (Integer) list.getLastKey();
            int pageSize = list.getConfig().pageSize;
            // this is a unnecessary safety check for lint
            int startKey = lastKey == null ? 0 : lastKey;

            if (startKey < loadAround) {
                this.scrollToWhenLoaded = loadAround;

                for (int i = startKey; i <= loadAround; i += pageSize) {
                    list.loadAround(i);
                }
            } else {
                int upperLimit = this.flexibleAdapter.getCurrentItems().size() - 1;
                loadAround = Math.min(loadAround, upperLimit);
                this.flexibleAdapter.smoothScrollToPosition(loadAround);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @SuppressWarnings("WeakerAccess")
    View createGotoView(Context context) {
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        return input;
    }

    @SuppressWarnings("WeakerAccess")
    int getPosition(View view) {
        if (view instanceof EditText) {
            return getPosition(((EditText) view).getText().toString());
        } else {
            throw new IllegalArgumentException("Expected EditText: Got " + view);
        }
    }

    int getPosition(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            showToast("Cannot go anywhere: expected an Integer");
            return -1;
        }
    }

    LinkedHashMap<String, Sortings> getSortMap() {
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("WeakerAccess")
    void onSortingChanged(Sortings sortings) {
        if (this.getViewModel() instanceof SortableViewModel) {
            ((SortableViewModel) this.getViewModel()).setSort(sortings);
        }
    }

    @SuppressWarnings("WeakerAccess")
    void onSortMenuClicked() {
        LinkedHashMap<String, Sortings> map = getSortMap();
        String[] strings = map.keySet().toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(this.getContext()));
        builder.setItems(strings, (dialog, which) -> {
            if (which < strings.length && which >= 0) {
                String title = strings[which];
                Sortings sortings = map.get(title);
                onSortingChanged(sortings);
            }
        });

        builder.setTitle("Sort By");
        builder.create().show();
    }

    private void setMediumCheckbox(View view, @IdRes int boxId, @MediumType.Medium int type) {
        ViewModel model = getViewModel();
        if (!(model instanceof MediumFilterableViewModel)) {
            return;
        }
        MediumFilterableViewModel filterableViewModel = (MediumFilterableViewModel) model;
        int medium = filterableViewModel.getMediumFilter();

        CheckBox box = view.findViewById(boxId);

        if (box == null) {
            throw new IllegalStateException(String.format(
                    "%s extends %s,expected a filter checkbox with id: %d",
                    model.getClass().getSimpleName(),
                    MediumFilterableViewModel.class.getCanonicalName(),
                    boxId
            ));
        }

        box.setChecked(MediumType.is(medium, type));
        box.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int filter = filterableViewModel.getMediumFilter();

            int newMediumFilter;
            if (isChecked) {
                newMediumFilter = MediumType.addMediumType(filter, type);
            } else {
                newMediumFilter = MediumType.removeMediumType(filter, type);
            }
            filterableViewModel.setMediumFilter(newMediumFilter);
        });
    }

    abstract Class<ViewModel> getViewModelClass();

    abstract LiveData<PagedList<Value>> createPagedListLiveData();

    List<IFlexible> convertToFlexible(Collection<Value> list) {
        List<IFlexible> items = new ArrayList<>();
        for (Value value : list) {
            if (value == null) {
                continue;
            }
            items.add(this.createFlexible(value));
        }
        return items;
    }

    abstract IFlexible createFlexible(Value value);

    private ViewModel createViewModel() {
        return new ViewModelProvider(this).get(getViewModelClass());
    }
}
