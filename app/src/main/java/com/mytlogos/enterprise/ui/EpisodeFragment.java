package com.mytlogos.enterprise.ui;

import android.annotation.SuppressLint;
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
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.background.RepositoryImpl;
import com.mytlogos.enterprise.background.TaskManager;
import com.mytlogos.enterprise.model.DisplayRelease;
import com.mytlogos.enterprise.tools.Utils;
import com.mytlogos.enterprise.viewmodel.EpisodeViewModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.flexibleadapter.items.IFlexible;
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
                                return new int[]{-1, 1, 0};
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
                                return new int[]{-1, 1, 0};
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
            public int getFilterLayout() {
                return R.layout.filter_unread_episode_layout;
            }
        };
    }

    @Override
    List<IFlexible> convertToFlexibles(Collection<DisplayRelease> list) {
        List<IFlexible> items = new ArrayList<>();
        for (DisplayRelease episode : list) {
            if (episode == null) {
                break;
            }
            if (groupByMedium) {
                items.add(new SectionableEpisodeItem(episode, this));
            } else {
                items.add(new EpisodeItem(episode, this));
            }
        }
        return items;
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
        List<IFlexible> flexibles = this.convertToFlexibles(list);
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
