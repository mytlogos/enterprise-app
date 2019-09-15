package com.mytlogos.enterprise.ui;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.RecyclerView;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.TimeAgo;
import com.mytlogos.enterprise.model.MediaList;
import com.mytlogos.enterprise.model.MediumItem;
import com.mytlogos.enterprise.model.MediumType;
import com.mytlogos.enterprise.tools.Sortings;
import com.mytlogos.enterprise.viewmodel.ListsViewModel;
import com.mytlogos.enterprise.viewmodel.MediumViewModel;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.utils.DrawableUtils;
import eu.davidea.viewholders.FlexibleViewHolder;

public class MediumListFragment extends BaseListFragment<MediumItem, MediumViewModel> {
    private boolean inMoveMediumMode;
    private ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle("Add Medium To List");
            mode.getMenuInflater().inflate(R.menu.add_medium_to_list_menu, menu);
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
            if (item.getItemId() == R.id.add_item_to_list) {
                Context context = Objects.requireNonNull(getContext());
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                ListsViewModel listsViewModel = new ViewModelProvider(MediumListFragment.this)
                        .get(ListsViewModel.class);

                ArrayAdapter<MediaList> adapter = new TextOnlyListAdapter<>(
                        MediumListFragment.this,
                        listsViewModel.getInternLists(),
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

                        if (!(flexible instanceof FlexibleMediumItem)) {
                            continue;
                        }
                        MediumItem mediumItem = ((FlexibleMediumItem) flexible).item;

                        if (mediumItem != null) {
                            selectedMediaIds.add(mediumItem.getMediumId());
                        }
                    }
                    CompletableFuture<Boolean> future = listsViewModel.addMediumToList(list.getListId(), selectedMediaIds);
                    future.whenComplete((aBoolean, throwable) -> {
                        Handler mainHandler = new Handler(Looper.getMainLooper());
                        mainHandler.post(() -> {
                            String text;
                            if (aBoolean == null || !aBoolean || throwable != null) {
                                text = "Could not add Media to List '" + list.getName() + "'";
                            } else {
                                text = "Added " + selectedMediaIds.size() + " Media to " + list.getName();
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
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            getMainActivity().getSupportActionBar().show();
            getFlexibleAdapter().setMode(SelectableAdapter.Mode.IDLE);
            getFlexibleAdapter().clearSelection();
            System.out.println("destroyed action mode");
            inMoveMediumMode = false;
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        this.setTitle("Media");
        return view;
    }

    @Override
    public void onItemLongClick(int position) {
        if (!inMoveMediumMode) {
            inMoveMediumMode = true;
            System.out.println("starting move mode");
            getFlexibleAdapter().addSelection(position);

            ActionMode mode = this.getMainActivity().startActionMode(callback);
            System.out.println("mode: " + mode);
        }
    }

    @Override
    public boolean onItemClick(View view, int position) {
        if (this.inMoveMediumMode) {
            if (position != RecyclerView.NO_POSITION) {
                getFlexibleAdapter().toggleSelection(position);
                return true;
            } else {
                return false;
            }
        } else {
            IFlexible item = getFlexibleAdapter().getItem(position);
            if (!(item instanceof FlexibleMediumItem)) {
                return false;
            }
            MediumItem mediumItem = ((FlexibleMediumItem) item).item;

            if (mediumItem == null) {
                return false;
            }
            TocFragment fragment = TocFragment.newInstance(mediumItem.getMediumId());
            getMainActivity().switchWindow(fragment, true);
        }
        return false;
    }

    @Override
    Class<MediumViewModel> getViewModelClass() {
        return MediumViewModel.class;
    }

    @Override
    LiveData<PagedList<MediumItem>> createPagedListLiveData() {
        return this.getViewModel().getAllMedia();
    }

    @Override
    List<IFlexible> convertToFlexibles(Collection<MediumItem> list) {
        List<IFlexible> items = new ArrayList<>();

        for (MediumItem item : list) {
            if (item == null) {
                break;
            }
            items.add(new FlexibleMediumItem(item));
        }

        return items;
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

    @Nullable
    @Override
    Filterable createFilterable() {
        return new Filterable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onCreateFilter(View view, AlertDialog.Builder builder) {
                setMediumCheckbox(view, R.id.text_medium, MediumType.TEXT);
                setMediumCheckbox(view, R.id.audio_medium, MediumType.AUDIO);
                setMediumCheckbox(view, R.id.video_medium, MediumType.VIDEO);
                setMediumCheckbox(view, R.id.image_medium, MediumType.IMAGE);

                int minEpisodeFilter = getViewModel().getMinEpisodeFilter();
                setNumberTextField(view, R.id.text_min_episode, minEpisodeFilter, 0);

                int minReadEpisodeFilter = getViewModel().getMinReadEpisodeFilter();
                setNumberTextField(view, R.id.text_min_episode_read, minReadEpisodeFilter, 0);
            }

            @Override
            public int getFilterLayout() {
                return R.layout.filter_medium_layout;
            }

            @Override
            public FilterProperty[] getSearchFilterProperties() {
                return new FilterProperty[]{
                        new FilterProperty() {
                            @Override
                            public int getSearchViewId() {
                                return R.id.title_filter;
                            }

                            @Override
                            public int getClearSearchButtonId() {
                                return R.id.clear_title;
                            }

                            @Override
                            public String get() {
                                return getViewModel().getTitleFilter();
                            }

                            @Override
                            public void set(String newFilter) {
                                getViewModel().setTitleFilter(newFilter);
                            }
                        },
                        new FilterProperty() {
                            @Override
                            public int getSearchViewId() {
                                return R.id.author_filter;
                            }

                            @Override
                            public int getClearSearchButtonId() {
                                return R.id.clear_author;
                            }

                            @Override
                            public String get() {
                                return getViewModel().getAuthorFilter();
                            }

                            @Override
                            public void set(String newFilter) {
                                getViewModel().setAuthorFilter(newFilter);
                            }
                        }
                };
            }
        };
    }

    static class FlexibleMediumItem extends AbstractFlexibleItem<MetaViewHolder> {
        public final MediumItem item;

        FlexibleMediumItem(@NonNull MediumItem item) {
            this.item = item;
            this.setDraggable(false);
            this.setSwipeable(false);
            this.setSelectable(true);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FlexibleMediumItem)) return false;

            FlexibleMediumItem other = (FlexibleMediumItem) o;
            return this.item.getMediumId() == other.item.getMediumId();
        }

        @Override
        public int hashCode() {
            return this.item.getMediumId();
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
            Integer currentReadEpisode = this.item.getCurrentReadEpisode() == null ? 0 : this.item.getCurrentReadEpisode();
            Integer lastEpisode = this.item.getLastEpisode() == null ? 0 : this.item.getLastEpisode();
            holder.topLeftText.setText(String.format("%d/%d", currentReadEpisode, lastEpisode));

            CharSequence relativeTime;
            DateTime lastUpdated = this.item.getLastUpdated();

            if (lastUpdated != null) {
                relativeTime = TimeAgo.toRelative(lastUpdated, DateTime.now());
            } else {
                relativeTime = "No Updates";
            }

            holder.topRightText.setText(relativeTime);
            holder.mainText.setText(this.item.getTitle());

            Drawable drawable = DrawableUtils.getSelectableBackgroundCompat(
                    Color.WHITE,             // normal background
                    Color.GRAY, // pressed background
                    Color.BLACK);                 // ripple color
            DrawableUtils.setBackgroundCompat(holder.itemView, drawable);
        }
    }

    private static class SectionableListItem extends AbstractSectionableItem<ViewHolder, HeaderItem> {
        private final MediumItem item;
        private final BaseFragment fragment;

        SectionableListItem(@NonNull MediumItem item, BaseFragment fragment) {
            super(new HeaderItem(item.getAuthor()));
            this.item = item;
            this.fragment = fragment;
            this.setDraggable(false);
            this.setSwipeable(false);
            this.setSelectable(false);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SectionableListItem)) return false;

            SectionableListItem other = (SectionableListItem) o;
            return this.item.getMediumId() == other.item.getMediumId();
        }

        @Override
        public int hashCode() {
            return this.item.getMediumId();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.list_item;
        }

        @Override
        public ViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new ViewHolder(view, adapter);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolder holder, int position, List<Object> payloads) {
            holder.mItem = this.item;
            // transform news id (int) to a string,
            // because it would expect a resource id if it is an int
            holder.metaView.setText(String.format("%d/%d", this.item.getCurrentReadEpisode(), this.item.getLastEpisode()));

            CharSequence relativeTime;
            DateTime lastUpdated = this.item.getLastUpdated();

            if (lastUpdated != null) {
                relativeTime = TimeAgo.toRelative(lastUpdated, DateTime.now());
            } else {
                relativeTime = "No Updates";
            }

            holder.denominatorView.setText(relativeTime);
            holder.contentView.setText(this.item.getTitle());

        }

    }

    private static class HeaderItem extends AbstractHeaderItem<HeaderViewHolder> {


        private final String title;

        private HeaderItem(String title) {
            this.title = title;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            HeaderItem that = (HeaderItem) o;

            return title.equals(that.title);
        }

        @Override
        public int hashCode() {
            return title.hashCode();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.flexible_header;
        }

        @Override
        public HeaderViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new HeaderViewHolder(view, adapter);
        }

        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, HeaderViewHolder holder, int position, List<Object> payloads) {
            holder.textView.setText(this.title);
        }
    }

    private static class HeaderViewHolder extends FlexibleViewHolder {
        private TextView textView;

        HeaderViewHolder(@NonNull View itemView, FlexibleAdapter<IFlexible> adapter) {
            super(itemView, adapter, true);
            textView = itemView.findViewById(R.id.text);
        }
    }

    static class ViewHolder extends FlexibleViewHolder {
        final View mView;
        final TextView contentView;
        private final TextView metaView;
        private final TextView denominatorView;
        MediumItem mItem;

        ViewHolder(@NonNull View view, FlexibleAdapter adapter) {
            super(view, adapter);
            mView = view;
            metaView = view.findViewById(R.id.item_top_left);
            denominatorView = view.findViewById(R.id.item_top_right);
            contentView = view.findViewById(R.id.content);
        }

        @Override
        protected boolean shouldAddSelectionInActionMode() {
            return true;
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + contentView.getText() + "'";
        }
    }
}
