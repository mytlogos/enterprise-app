package com.mytlogos.enterprise.ui;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.RecyclerView;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.model.ExternalMediaList;
import com.mytlogos.enterprise.model.MediaList;
import com.mytlogos.enterprise.tools.Utils;
import com.mytlogos.enterprise.viewmodel.ListsViewModel;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.utils.DrawableUtils;

/**
 * A fragment representing a list of Items.
 */
public class ListsFragment extends BaseListFragment<MediaList, ListsViewModel> {

    static final MediaList TRASH_LIST = new MediaList(
            "",
            Integer.MIN_VALUE,
            "Trashbin List",
            0,
            0
    );
    private boolean inActionMode;

    private ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle("Delete Lists");
            mode.getMenuInflater().inflate(R.menu.list_medium_action_mode_menu, menu);
            Objects.requireNonNull(getMainActivity().getSupportActionBar()).hide();
            getFlexibleAdapter().setMode(SelectableAdapter.Mode.MULTI);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.delete_items) {
                return deleteItemsFromList(mode);
            }
            return false;
        }

        private boolean deleteItemsFromList(ActionMode mode) {
            List<Integer> selectedPositions = getFlexibleAdapter().getSelectedPositions();
            List<Integer> selectedListsIds = new ArrayList<>();

            for (Integer selectedPosition : selectedPositions) {
                IFlexible flexible = getFlexibleAdapter().getItem(selectedPosition);

                if (!(flexible instanceof ListItem)) {
                    continue;
                }
                MediaList list = ((ListItem) flexible).list;

                if (ListsFragment.TRASH_LIST.getListId() == list.getListId()) {
                    showToast("You cannot delete the Trash list");
                    return true;
                }
                if (getString(R.string.standard_list_name).equals(list.getName())) {
                    showToast("You cannot delete the Standard list");
                    return true;
                }

                selectedListsIds.add(list.getListId());
            }
            System.out.println("removed " + selectedListsIds.size() + " lists");
            showToast("Is not implemented yet");
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            Objects.requireNonNull(getMainActivity().getSupportActionBar()).show();
            getFlexibleAdapter().setMode(SelectableAdapter.Mode.IDLE);
            getFlexibleAdapter().clearSelection();
            System.out.println("destroyed action mode");
            inActionMode = false;
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ListsFragment() {
    }

    private static String getDenominator(MediaList list) {
        String denominator;

        if (list instanceof ExternalMediaList) {
            String host = URI.create(((ExternalMediaList) list).getUrl()).getHost();
            Matcher matcher = Pattern.compile("(www\\.)?(.+)").matcher(host);

            if (matcher.matches()) {
                denominator = matcher
                        .group(2);
            } else {
                denominator = host;
            }
        } else {
            denominator = "Intern";
        }
        return denominator;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.lists_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add) {
            this.getMainActivity().switchWindow(new AddList(), true);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    Class<ListsViewModel> getViewModelClass() {
        return ListsViewModel.class;
    }

    @Override
    LiveData<PagedList<MediaList>> createPagedListLiveData() {
        return Utils.transform(Transformations.map(
                getViewModel().getLists(),
                input -> {
                    input.remove(TRASH_LIST);
                    input.add(TRASH_LIST);
                    return input;
                }));
    }

    @Override
    List<IFlexible> convertToFlexibles(Collection<MediaList> list) {
        List<IFlexible> items = new ArrayList<>();

        for (MediaList mediaList : list) {
            items.add(new ListItem(mediaList));
        }
        return items;
    }


    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        this.setTitle("Lists");
        return view;
    }

    @Override
    public void onItemLongClick(int position) {
        if (inActionMode) {
            return;
        }
        IFlexible list = getFlexibleAdapter().getItem(position);

        if (!(list instanceof ListItem)) {
            return;
        }
        ListItem item = (ListItem) list;

        if (item.list instanceof ExternalMediaList) {
            return;
        }
        inActionMode = true;
        System.out.println("starting action mode");
        getFlexibleAdapter().addSelection(position);

        this.getMainActivity().startActionMode(callback);
    }

    @Override
    public boolean onItemClick(View view, int position) {
        IFlexible list = getFlexibleAdapter().getItem(position);

        if (!(list instanceof ListItem)) {
            return false;
        }
        ListItem item = (ListItem) list;

        if (this.inActionMode) {
            if (position != RecyclerView.NO_POSITION && !(item.list instanceof ExternalMediaList)) {
                getFlexibleAdapter().toggleSelection(position);
                return true;
            } else {
                return false;
            }
        }

        getMainActivity().switchWindow(ListMediumFragment.getInstance(item.list), true);
        return false;
    }

    private static class ListItem extends AbstractFlexibleItem<MetaViewHolder> {
        private final MediaList list;

        ListItem(@NonNull MediaList list) {
            this.list = list;
            this.setDraggable(false);
            this.setSwipeable(false);
            this.setSelectable(true);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ListItem other = (ListItem) o;
            return this.list.getListId() == other.list.getListId();
        }

        @Override
        public int hashCode() {
            return this.list.getListId();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.meta_item;
        }

        @Override
        public MetaViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new MetaViewHolder(view, adapter);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, MetaViewHolder holder, int position, List<Object> payloads) {
            // transform news id (int) to a string,
            // because it would expect a resource id if it is an int
            holder.topLeftText.setText(String.format("%d Items", this.list.getSize()));
            holder.topRightText.setText(getDenominator(list));
            holder.mainText.setText(this.list.getName());

            Drawable drawable = DrawableUtils.getSelectableBackgroundCompat(
                    Color.WHITE,             // normal background
                    Color.GRAY, // pressed background
                    Color.BLACK);                 // ripple color
            DrawableUtils.setBackgroundCompat(holder.itemView, drawable);
        }
    }

}
