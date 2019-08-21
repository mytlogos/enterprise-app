package com.mytlogos.enterprise.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.RecyclerView;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.model.ExternalMediaList;
import com.mytlogos.enterprise.model.MediaList;
import com.mytlogos.enterprise.model.MediumItem;
import com.mytlogos.enterprise.tools.Sortings;
import com.mytlogos.enterprise.tools.Utils;
import com.mytlogos.enterprise.viewmodel.ListMediaViewModel;
import com.mytlogos.enterprise.viewmodel.ListsViewModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.items.IFlexible;

public class ListMediumFragment extends BaseListFragment<MediumItem, ListMediaViewModel> {
    static final String ID = "id";
    static final String TITLE = "listTitle";
    static final String EXTERNAL = "external";
    private int listId;
    private boolean isExternal;
    private String listTitle;
    private boolean inActionMode;
    private ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle("Edit MediumList");
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
            if (item.getItemId() == R.id.move_item_to_list) {
                return moveItemsToList(mode);
            } else if (item.getItemId() == R.id.delete_items) {
                return deleteItemsFromList(mode);
            }
            return false;
        }

        private boolean deleteItemsFromList(ActionMode mode) {
            if (ListsFragment.TRASH_LIST.getListId() == listId) {
                showToast("You cannot delete from the Trash list");
                mode.finish();
                // TODO: 01.08.2019 ask if we really want to remove this item
                return true;
            }
            List<Integer> selectedPositions = getFlexibleAdapter().getSelectedPositions();
            List<Integer> selectedMediaIds = new ArrayList<>();
            for (Integer selectedPosition : selectedPositions) {
                IFlexible flexible = getFlexibleAdapter().getItem(selectedPosition);

                if (!(flexible instanceof MediumFragment.FlexibleMediumItem)) {
                    continue;
                }
                MediumItem mediumItem = ((MediumFragment.FlexibleMediumItem) flexible).item;

                if (mediumItem != null) {
                    selectedMediaIds.add(mediumItem.getMediumId());
                }
            }
            int size = selectedMediaIds.size();
            getViewModel().removeMedia(listId, selectedMediaIds).whenComplete((aBoolean, throwable) -> {
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(() -> {
                    String text;
                    if (aBoolean == null || !aBoolean || throwable != null) {
                        text = String.format("Could not delete %s Media from List '%s'", size, listTitle);
                    } else {
                        text = String.format("Removed %s Media from '%s'", size, listTitle);
                        // TODO: 29.07.2019 replace toast with undoable snackbar
                        mode.finish();
                    }
                    requireActivity().runOnUiThread(() -> showToast(text));
                });
            });
            return false;
        }

        boolean moveItemsToList(ActionMode mode) {
            Context context = Objects.requireNonNull(getContext());
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            ListsViewModel listsViewModel = ViewModelProviders
                    .of(ListMediumFragment.this)
                    .get(ListsViewModel.class);

            LiveData<List<MediaList>> listLiveData = Transformations.map(
                    listsViewModel.getInternLists(),
                    input -> {
                        input.removeIf(list -> list.getListId() == listId);
                        return input;
                    }
            );
            ArrayAdapter<MediaList> adapter = new TextOnlyListAdapter<>(
                    ListMediumFragment.this,
                    listLiveData,
                    MediaList::getName
            );

            builder.setAdapter(adapter, (dialog, which) -> {
                MediaList list = adapter.getItem(which);

                if (list == null) {
                    return;
                }
                List<Integer> selectedPositions = getFlexibleAdapter().getSelectedPositions();
                List<Integer> selectedMediaIds = new ArrayList<>();
                for (Integer selectedPosition : selectedPositions) {
                    IFlexible flexible = getFlexibleAdapter().getItem(selectedPosition);

                    if (!(flexible instanceof MediumFragment.FlexibleMediumItem)) {
                        continue;
                    }
                    MediumItem mediumItem = ((MediumFragment.FlexibleMediumItem) flexible).item;

                    if (mediumItem != null) {
                        selectedMediaIds.add(mediumItem.getMediumId());
                    }
                }
                CompletableFuture<Boolean> future = listsViewModel.moveMediumToList(listId, list.getListId(), selectedMediaIds);
                future.whenComplete((aBoolean, throwable) -> {
                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    mainHandler.post(() -> {
                        String text;
                        if (aBoolean == null || !aBoolean || throwable != null) {
                            text = "Could not move Media to List '" + list.getName() + "'";
                        } else {
                            text = "Moved " + selectedMediaIds.size() + " Media to " + list.getName();
                            // TODO: 29.07.2019 replace toast with undoable snackbar
                            mode.finish();
                        }
                        requireActivity().runOnUiThread(() -> showToast(text));
                    });
                });

            });
            builder.show();
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


    public static ListMediumFragment getInstance(MediaList list) {
        Bundle bundle = new Bundle();
        bundle.putInt(ListMediumFragment.ID, list.getListId());
        bundle.putString(ListMediumFragment.TITLE, list.getName());
        bundle.putBoolean(ListMediumFragment.EXTERNAL, list instanceof ExternalMediaList);

        ListMediumFragment fragment = new ListMediumFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = this.requireArguments();
        listId = arguments.getInt(ID);
        isExternal = arguments.getBoolean(EXTERNAL);
        listTitle = arguments.getString(TITLE);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        this.setTitle(listTitle);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Fragment fragment = null;
        Bundle bundle = null;
        boolean selected = false;

        if (item.getItemId() == R.id.list_setting) {
            fragment = new ListSettings();
            bundle = Objects.requireNonNull(this.getArguments());
            selected = true;
        }

        if (fragment != null) {
            this.getMainActivity().switchWindow(fragment, bundle, true);
        }
        if (selected) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemLongClick(int position) {
        if (!inActionMode && !this.isExternal) {
            inActionMode = true;
            System.out.println("starting action mode");
            getFlexibleAdapter().addSelection(position);

            this.getMainActivity().startActionMode(callback);
        }
    }

    @Override
    public boolean onItemClick(View view, int position) {
        if (this.inActionMode) {
            if (position != RecyclerView.NO_POSITION) {
                getFlexibleAdapter().toggleSelection(position);
                return true;
            } else {
                return false;
            }
        }
        IFlexible item = this.getFlexibleAdapter().getItem(position);

        if (!(item instanceof MediumFragment.FlexibleMediumItem)) {
            return false;
        }
        MediumItem mediumItem = ((MediumFragment.FlexibleMediumItem) item).item;

        if (mediumItem != null) {
            TocFragment fragment = TocFragment.newInstance(mediumItem.getMediumId());
            getMainActivity().switchWindow(fragment, true);
        }
        return false;
    }

    @Override
    public void onItemSwipe(int position, int direction) {
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


    @Override
    LinkedHashMap<String, Sortings> getSortMap() {
        LinkedHashMap<String, Sortings> map = new LinkedHashMap<>();
        map.put("Title A-Z", Sortings.TITLE_AZ);
        map.put("Title Z-A", Sortings.TITLE_ZA);
        map.put("Medium Asc", Sortings.MEDIUM);
        map.put("Medium Desc", Sortings.MEDIUM_REVERSE);
        map.put("Latest Update Asc", Sortings.LAST_UPDATE_ASC);
        map.put("Latest Update Desc", Sortings.LAST_UPDATE_DESC);
        map.put("Episodes Asc", Sortings.NUMBER_EPISODE_ASC);
        map.put("Episodes Desc", Sortings.NUMBER_EPISODE_DESC);
        map.put("Episodes Read Asc", Sortings.NUMBER_EPISODE_READ_ASC);
        map.put("Episodes Read Desc", Sortings.NUMBER_EPISODE_READ_DESC);
        map.put("Episodes UnRead Asc", Sortings.NUMBER_EPISODE_UNREAD_ASC);
        map.put("Episodes UnRead Desc", Sortings.NUMBER_EPISODE_UNREAD_DESC);
        return map;
    }

    @Override
    ListMediaViewModel createViewModel() {
        return ViewModelProviders.of(this).get(ListMediaViewModel.class);
    }

    @Override
    LiveData<PagedList<MediumItem>> createPagedListLiveData() {
        return Utils.transform(this.getViewModel().getMedia(this.listId, this.isExternal));
    }

    @Override
    List<IFlexible> convertToFlexibles(Collection<MediumItem> list) {
        List<IFlexible> flexibles = new ArrayList<>();

        for (MediumItem item : list) {
            if (item == null) {
                break;
            }
            flexibles.add(new MediumFragment.FlexibleMediumItem(item));
        }
        return flexibles;
    }


}
