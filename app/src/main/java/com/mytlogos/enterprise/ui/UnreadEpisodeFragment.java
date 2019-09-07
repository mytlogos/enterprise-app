package com.mytlogos.enterprise.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.background.RepositoryImpl;
import com.mytlogos.enterprise.background.TaskManager;
import com.mytlogos.enterprise.model.DisplayUnreadEpisode;
import com.mytlogos.enterprise.model.MediumType;
import com.mytlogos.enterprise.model.Release;
import com.mytlogos.enterprise.viewmodel.UnreadEpisodeViewModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
public class UnreadEpisodeFragment extends BaseListFragment<DisplayUnreadEpisode, UnreadEpisodeViewModel> {

    private boolean groupByMedium;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public UnreadEpisodeFragment() {
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
        this.setTitle("Unread Chapters");
        return view;
    }

    @Override
    Class<UnreadEpisodeViewModel> getViewModelClass() {
        return UnreadEpisodeViewModel.class;
    }

    @Override
    LiveData<PagedList<DisplayUnreadEpisode>> createPagedListLiveData() {
        return this.getViewModel().getUnreadEpisodes();
    }

    @Nullable
    @Override
    Filterable createFilterable() {
        return new Filterable() {
            @Override
            public void onCreateFilter(View view, AlertDialog.Builder builder) {
                setMediumCheckbox(view, R.id.text_medium, MediumType.TEXT);
                setMediumCheckbox(view, R.id.audio_medium, MediumType.AUDIO);
                setMediumCheckbox(view, R.id.video_medium, MediumType.VIDEO);
                setMediumCheckbox(view, R.id.image_medium, MediumType.IMAGE);

                CheckBox box = view.findViewById(R.id.saved);
                box.setChecked(getViewModel().getSaved());
                box.setOnCheckedChangeListener((buttonView, isChecked) -> getViewModel().setSaved(isChecked ? 1 : -1));
            }

            @Override
            public int getFilterLayout() {
                return R.layout.filter_unread_episode_layout;
            }
        };
    }

    @Override
    List<IFlexible> convertToFlexibles(Collection<DisplayUnreadEpisode> list) {
        List<IFlexible> items = new ArrayList<>();
        for (DisplayUnreadEpisode episode : list) {
            if (episode == null) {
                break;
            }
            if (groupByMedium) {
                items.add(new SectionableUnreadEpisodeItem(episode, this));
            } else {
                items.add(new UnreadEpisodeItem(episode, this));
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
        if (item instanceof UnreadEpisodeItem) {
            mediumId = ((UnreadEpisodeItem) item).episode.getMediumId();
        } else if (item instanceof SectionableUnreadEpisodeItem) {
            mediumId = ((SectionableUnreadEpisodeItem) item).episode.getMediumId();
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
        PagedList<DisplayUnreadEpisode> list = this.getLivePagedList().getValue();

        if (list == null) {
            return;
        }
        List<IFlexible> flexibles = this.convertToFlexibles(list);
        this.getFlexibleAdapter().updateDataSet(flexibles);
    }

    private void openPopup(ViewHolder holder, DisplayUnreadEpisode episode) {
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
                    List<Release> releases = episode.getReleases();
                    List<String> urls = new ArrayList<>();

                    for (Release release : releases) {
                        String url = release.getUrl();

                        if (url != null && !url.isEmpty()) {
                            urls.add(url);
                        }
                    }
                    this.openInBrowser(urls);
                    return true;
                });
        popupMenu.show();
    }

    private static class SectionableUnreadEpisodeItem extends AbstractSectionableItem<ViewHolder, HeaderItem> {
        private final DisplayUnreadEpisode episode;
        private final UnreadEpisodeFragment fragment;

        SectionableUnreadEpisodeItem(@NonNull DisplayUnreadEpisode episode, UnreadEpisodeFragment fragment) {
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
            if (!(o instanceof SectionableUnreadEpisodeItem)) return false;

            SectionableUnreadEpisodeItem other = (SectionableUnreadEpisodeItem) o;
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
            Optional<Release> maxRelease = this.episode.getReleases().stream().max(Comparator.comparing(Release::getReleaseDate));

            holder.metaView.setText(maxRelease.map(release -> release.getReleaseDate().toString("dd.MM.yyyy HH:mm:ss")).orElse("Not available"));
            holder.novelView.setText(this.episode.getMediumTitle());
            holder.contentView.setText(maxRelease.map(Release::getTitle).orElse("Not available"));
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

    private static class UnreadEpisodeItem extends AbstractFlexibleItem<ViewHolder> {
        private final DisplayUnreadEpisode episode;
        private final UnreadEpisodeFragment fragment;

        UnreadEpisodeItem(@NonNull DisplayUnreadEpisode episode, UnreadEpisodeFragment fragment) {
            this.episode = episode;
            this.fragment = fragment;
            this.setDraggable(false);
            this.setSwipeable(false);
            this.setSelectable(false);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SectionableUnreadEpisodeItem)) return false;

            SectionableUnreadEpisodeItem other = (SectionableUnreadEpisodeItem) o;
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
            Optional<Release> maxRelease = this.episode.getReleases().stream().max(Comparator.comparing(Release::getReleaseDate));

            holder.metaView.setText(maxRelease.map(release -> release.getReleaseDate().toString("dd.MM.yyyy HH:mm:ss")).orElse("Not available"));
            holder.novelView.setText(episode.getMediumTitle());

            String title = maxRelease.map(Release::getTitle).orElse("Not available");

            if (episode.getPartialIndex() > 0) {
                title = String.format("#%d.%d - %s", episode.getTotalIndex(), episode.getPartialIndex(), title);
            } else {
                title = String.format("#%d - %s", episode.getTotalIndex(), title);
            }
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
