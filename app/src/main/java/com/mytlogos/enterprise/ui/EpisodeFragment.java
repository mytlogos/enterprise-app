package com.mytlogos.enterprise.ui;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.background.RepositoryImpl;
import com.mytlogos.enterprise.background.TaskManager;
import com.mytlogos.enterprise.model.DisplayRelease;
import com.mytlogos.enterprise.model.ExternalMediaList;
import com.mytlogos.enterprise.model.MediaList;
import com.mytlogos.enterprise.tools.Utils;
import com.mytlogos.enterprise.viewmodel.EpisodeViewModel;

import java.util.ArrayList;
import java.util.Collections;
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

/**
 * A fragment representing a list of Items.
 */
public class EpisodeFragment extends BaseListFragment<DisplayRelease, EpisodeViewModel> {

    private boolean groupByMedium;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EpisodeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        this.setTitle("Chapters");
        return view;
    }

    @Override
    Class<EpisodeViewModel> getViewModelClass() {
        return EpisodeViewModel.class;
    }

    @Override
    LiveData<PagedList<DisplayRelease>> createPagedListLiveData() {
        return this.getViewModel().getDisplayEpisodes();
    }

    @Nullable
    @Override
    Filterable createFilterable() {
        return new Filterable() {

            @Override
            public Property[] getSearchFilterProperties() {
                return new Property[]{
                        new TextProperty() {
                            @Override
                            public int getViewId() {
                                return R.id.minIndex;
                            }

                            @Override
                            public String get() {
                                return getViewModel().getMinIndex() + "";
                            }

                            @Override
                            public void set(String newFilter) {

                                try {
                                    int index = Integer.parseInt(newFilter);
                                    getViewModel().setMinIndex(index);
                                } catch (NumberFormatException e) {
                                    showToast("Invalid Input");
                                }
                            }
                        },
                        new TextProperty() {
                            @Override
                            public int getViewId() {
                                return R.id.maxIndex;
                            }

                            @Override
                            public String get() {
                                return getViewModel().getMaxIndex() + "";
                            }

                            @Override
                            public void set(String newFilter) {
                                try {
                                    int index = Integer.parseInt(newFilter);
                                    getViewModel().setMaxIndex(index);
                                } catch (NumberFormatException e) {
                                    showToast("Invalid Input");
                                }
                            }
                        },
                        new TextProperty() {
                            @Override
                            public int getViewId() {
                                return R.id.host_filter;
                            }

                            @Override
                            public String get() {
                                return getViewModel().getHost();
                            }

                            @Override
                            public void set(String newFilter) {
                                getViewModel().setHost(newFilter == null ? null : newFilter.toLowerCase());
                            }
                        },
                        new PositionProperty() {
                            @Override
                            public int getViewId() {
                                return R.id.read;
                            }

                            @Override
                            public Integer get() {
                                return getViewModel().getRead();
                            }

                            @Override
                            public int[] positionalMapping() {
                                return new int[]{1, 0, -1};
                            }

                            @Override
                            public void set(Integer value) {
                                getViewModel().setRead(value);
                            }
                        },
                        new PositionProperty() {
                            @Override
                            public int getViewId() {
                                return R.id.saved;
                            }

                            @Override
                            public int[] positionalMapping() {
                                return new int[]{1, 0, -1};
                            }

                            @Override
                            public Integer get() {
                                return getViewModel().getSaved();
                            }

                            @Override
                            public void set(Integer value) {
                                getViewModel().setSaved(value);
                            }
                        },
                        new BooleanProperty() {
                            @Override
                            public int getViewId() {
                                return R.id.latest_only;
                            }

                            @Override
                            public Boolean get() {
                                return getViewModel().isLatestOnly();
                            }

                            @Override
                            public void set(Boolean newFilter) {
                                getViewModel().setLatestOnly(newFilter);
                            }
                        }
                };
            }

            @Override
            public void onCreateFilter(View view, AlertDialog.Builder builder) {
                RecyclerView recycler = view.findViewById(R.id.listsFilter);
                LiveData<List<MediaList>> lists = getViewModel().getLists();

                LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
                recycler.setLayoutManager(layoutManager);

                DividerItemDecoration decoration = new DividerItemDecoration(requireContext(), layoutManager.getOrientation());
                recycler.addItemDecoration(decoration);

                FlexibleAdapter<IFlexible> flexibleAdapter = new FlexibleAdapter<>(null);

                this.updateRecycler(flexibleAdapter, lists.getValue());

                lists.observe(EpisodeFragment.this, mediaLists -> updateRecycler(flexibleAdapter, mediaLists));

                flexibleAdapter.setMode(SelectableAdapter.Mode.MULTI);
                flexibleAdapter.addListener((FlexibleAdapter.OnItemClickListener) (view1, position) -> {
                    flexibleAdapter.toggleSelection(position);

                    List<Integer> listIds = new ArrayList<>();

                    for (Integer selectedPosition : flexibleAdapter.getSelectedPositions()) {
                        FlexibleListItem item = flexibleAdapter.getItem(selectedPosition, FlexibleListItem.class);
                        listIds.add(Objects.requireNonNull(item).mediaList.getListId());
                    }
                    getViewModel().setFilterListIds(listIds);
                    sortFlexibleList(flexibleAdapter);
                    return true;
                });

                recycler.setAdapter(flexibleAdapter);
            }

            void updateRecycler(FlexibleAdapter<IFlexible> flexibleAdapter, List<MediaList> mediaLists) {
                System.out.println("List: " + mediaLists);
                if (mediaLists == null) {
                    return;
                }
                List<Integer> shouldListIds = getViewModel().getFilterListIds();

                Collections.sort(mediaLists, (o1, o2) -> {
                    boolean selected1 = shouldListIds.contains(o1.getListId());
                    boolean selected2 = shouldListIds.contains(o2.getListId());
                    if (selected1 == selected2) {
                        return o1.getName().compareTo(o2.getName());
                    } else {
                        return selected1 ? -1 : 1;
                    }
                });

                List<IFlexible> flexibles = new ArrayList<>(mediaLists.size());

                for (MediaList mediaList : mediaLists) {
                    flexibles.add(new FlexibleListItem(mediaList));
                }

                flexibleAdapter.updateDataSet(flexibles);
                List<IFlexible> currentItems = flexibleAdapter.getCurrentItems();

                for (int i = 0, currentItemsSize = currentItems.size(); i < currentItemsSize; i++) {
                    FlexibleListItem item = (FlexibleListItem) currentItems.get(i);

                    if (shouldListIds.contains(item.mediaList.getListId())) {
                        flexibleAdapter.addSelection(i);
                    } else {
                        flexibleAdapter.removeSelection(i);
                    }
                }
            }

            private void sortFlexibleList(FlexibleAdapter<IFlexible> flexibleAdapter) {
                List<IFlexible> list = new ArrayList<>(flexibleAdapter.getCurrentItems());
                List<Integer> listIds = getViewModel().getFilterListIds();

                Collections.sort(list, (o1, o2) -> {
                    FlexibleListItem item1 = (FlexibleListItem) o1;
                    FlexibleListItem item2 = (FlexibleListItem) o2;

                    boolean selected1 = listIds.contains(item1.mediaList.getListId());
                    boolean selected2 = listIds.contains(item2.mediaList.getListId());
                    if (selected1 == selected2) {
                        return item1.mediaList.getName().compareTo(item2.mediaList.getName());
                    } else {
                        return selected1 ? -1 : 1;
                    }
                });
                flexibleAdapter.updateDataSet(list);

                for (int i = 0, currentItemsSize = list.size(); i < currentItemsSize; i++) {
                    FlexibleListItem item = (FlexibleListItem) list.get(i);

                    if (listIds.contains(item.mediaList.getListId())) {
                        flexibleAdapter.addSelection(i);
                    } else {
                        flexibleAdapter.removeSelection(i);
                    }
                }
            }

            @Override
            public int getFilterLayout() {
                return R.layout.filter_unread_episode_layout;
            }
        };
    }

    private static class FlexibleListItem extends AbstractFlexibleItem<TextOnlyViewHolder> {
        private final MediaList mediaList;

        private FlexibleListItem(MediaList mediaList) {
            this.mediaList = mediaList;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FlexibleListItem that = (FlexibleListItem) o;

            return mediaList.equals(that.mediaList);
        }

        @Override
        public int hashCode() {
            return mediaList.hashCode();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.text_only_item;
        }

        @Override
        public TextOnlyViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new TextOnlyViewHolder(view, adapter);
        }

        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, TextOnlyViewHolder holder, int position, List<Object> payloads) {
            String listSource = mediaList instanceof ExternalMediaList ? "External" : "Internal";
            String title = String.format("%s (%s)", this.mediaList.getName(), listSource);
            holder.textView.setText(title);

            Drawable drawable = DrawableUtils.getSelectableBackgroundCompat(
                    Color.WHITE,             // normal background
                    Color.GRAY, // pressed background
                    Color.BLACK);                 // ripple color
            DrawableUtils.setBackgroundCompat(holder.itemView, drawable);
        }
    }

    private static class TextOnlyViewHolder extends FlexibleViewHolder {
        private TextView textView;

        TextOnlyViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            textView = (TextView) view;
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) textView.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            textView.setTextSize(15);
            textView.requestLayout();
        }
    }


    @Override
    IFlexible createFlexible(DisplayRelease displayRelease) {
        if (groupByMedium) {
            return new SectionableEpisodeItem(displayRelease, this);
        } else {
            return new EpisodeItem(displayRelease, this);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.unread_chapter_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.group_by_medium) {
            toggleGroupByMedium(item);
        } else if (item.getItemId() == R.id.group_by_medium_first) {
            item.setChecked(!item.isChecked());
            getViewModel().setGrouped(item.isChecked());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onItemClick(View view, int position) {
        IFlexible item = getFlexibleAdapter().getItem(position);


        int mediumId;
        if (item instanceof EpisodeItem) {
            mediumId = ((EpisodeItem) item).episode.getMediumId();
        } else if (item instanceof SectionableEpisodeItem) {
            mediumId = ((SectionableEpisodeItem) item).episode.getMediumId();
        } else {
            return false;
        }
        TocFragment fragment = TocFragment.newInstance(mediumId);
        getMainActivity().switchWindow(fragment);
        return true;
    }

    private void toggleGroupByMedium(MenuItem item) {
        item.setChecked(!item.isChecked());
        this.groupByMedium = item.isChecked();
        PagedList<DisplayRelease> list = this.getLivePagedList().getValue();

        if (list == null) {
            return;
        }
        List<IFlexible> flexibles = this.convertToFlexible(list);
        this.getFlexibleAdapter().updateDataSet(flexibles);
    }

    private void openPopup(ViewHolder holder, DisplayRelease episode) {
        PopupMenu popupMenu = new PopupMenu(getContext(), holder.optionsButtonView);

        if (episode.isSaved()) {
            popupMenu
                    .getMenu()
                    .add("Open Local")
                    .setOnMenuItemClickListener(item -> {

                        CompletableFuture<Integer> task = TaskManager.runCompletableTask(
                                () -> RepositoryImpl.getInstance().getMediumType(episode.getMediumId())
                        );
                        task.whenComplete((type, throwable) -> {
                            if (type != null) {
                                openLocal(episode.getEpisodeId(), episode.getMediumId(), type);
                            }
                        });
                        return true;
                    });
        }
        popupMenu
                .getMenu()
                .add("Open in Browser")
                .setOnMenuItemClickListener(item -> {
                    this.openInBrowser(episode.getUrl());
                    return true;
                });
        popupMenu.show();
    }

    private static class SectionableEpisodeItem extends AbstractSectionableItem<ViewHolder, HeaderItem> {
        private final DisplayRelease episode;
        private final EpisodeFragment fragment;

        SectionableEpisodeItem(@NonNull DisplayRelease episode, EpisodeFragment fragment) {
            super(new HeaderItem(episode.getMediumTitle(), episode.getMediumId()));
            this.episode = episode;
            this.fragment = fragment;
            this.setDraggable(false);
            this.setSwipeable(false);
            this.setSelectable(false);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SectionableEpisodeItem)) return false;

            SectionableEpisodeItem other = (SectionableEpisodeItem) o;
            return this.episode.equals(other.episode);
        }

        @Override
        public int hashCode() {
            return this.episode.hashCode();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.unreadchapter_item;
        }

        @Override
        public ViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new ViewHolder(view, adapter);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolder holder, int position, List<Object> payloads) {
            holder.metaView.setText(episode.getReleaseDate().toString("dd.MM.yyyy HH:mm:ss"));
            holder.novelView.setText(this.episode.getMediumTitle());
            holder.contentView.setText(episode.getTitle());
            holder.optionsButtonView.setOnClickListener(v -> this.fragment.openPopup(holder, episode));
        }
    }

    private static class HeaderItem extends AbstractHeaderItem<HeaderViewHolder> {
        private final String title;
        private final int mediumId;

        private HeaderItem(String title, int mediumId) {
            this.title = title;
            this.mediumId = mediumId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof HeaderItem)) return false;

            HeaderItem other = (HeaderItem) o;
            return this.mediumId == other.mediumId;
        }

        @Override
        public int hashCode() {
            return mediumId;
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

    private static class EpisodeItem extends AbstractFlexibleItem<ViewHolder> {
        private final DisplayRelease episode;
        private final EpisodeFragment fragment;

        EpisodeItem(@NonNull DisplayRelease episode, EpisodeFragment fragment) {
            this.episode = episode;
            this.fragment = fragment;
            this.setDraggable(false);
            this.setSwipeable(false);
            this.setSelectable(false);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SectionableEpisodeItem)) return false;

            SectionableEpisodeItem other = (SectionableEpisodeItem) o;
            return this.episode.getEpisodeId() == other.episode.getEpisodeId();
        }

        @Override
        public int hashCode() {
            return this.episode.getEpisodeId();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.unreadchapter_item;
        }

        @Override
        public ViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new ViewHolder(view, adapter);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolder holder, int position, List<Object> payloads) {
            // transform news id (int) to a string,
            // because it would expect a resource id if it is an int
            holder.metaView.setText(episode.getReleaseDate().toString("dd.MM.yyyy HH:mm:ss"));
            holder.novelView.setText(this.episode.getMediumTitle());

            String title = episode.getTitle();

            if (episode.getPartialIndex() > 0) {
                title = String.format("#%d.%d - %s", episode.getTotalIndex(), episode.getPartialIndex(), title);
            } else {
                title = String.format("#%d - %s", episode.getTotalIndex(), title);
            }
            title = String.format("%s (%s) ", title, Utils.getDomain(episode.getUrl()));
            holder.contentView.setText(title);

            holder.optionsButtonView.setOnClickListener(v -> this.fragment.openPopup(holder, episode));
        }
    }

    private static class HeaderViewHolder extends FlexibleViewHolder {
        private TextView textView;

        HeaderViewHolder(@NonNull View itemView, FlexibleAdapter<IFlexible> adapter) {
            super(itemView, adapter, true);
            textView = itemView.findViewById(R.id.text);
        }
    }

    private static class ViewHolder extends FlexibleViewHolder {
        final View mView;
        final TextView contentView;
        private final TextView metaView;
        private final TextView novelView;
        private final ImageButton optionsButtonView;

        ViewHolder(@NonNull View view, FlexibleAdapter adapter) {
            super(view, adapter);
            mView = view;
            metaView = view.findViewById(R.id.item_top_left);
            novelView = view.findViewById(R.id.item_top_right);
            contentView = view.findViewById(R.id.content);
            optionsButtonView = view.findViewById(R.id.item_options_button);
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + contentView.getText() + "'";
        }
    }
}
