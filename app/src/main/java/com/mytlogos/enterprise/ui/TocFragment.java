package com.mytlogos.enterprise.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.background.RepositoryImpl;
import com.mytlogos.enterprise.background.TaskManager;
import com.mytlogos.enterprise.model.Release;
import com.mytlogos.enterprise.model.TocEpisode;
import com.mytlogos.enterprise.tools.Sortings;
import com.mytlogos.enterprise.viewmodel.TocEpisodeViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class TocFragment extends BaseListFragment<TocEpisode, TocEpisodeViewModel> implements ItemListener {

    private static final String MEDIUM_ID = "mediumId";
    private int mediumId;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TocFragment() {
    }

    public static TocFragment newInstance(int mediumId) {
        TocFragment fragment = new TocFragment();
        Bundle args = new Bundle();
        args.putInt(MEDIUM_ID, mediumId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.requireArguments();
        mediumId = bundle.getInt(MEDIUM_ID);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        registerForContextMenu(getListView());
        // TODO: 22.07.2019 set the mediumTitle
        this.setTitle("Table of Contents");
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.medium_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.item_setting) {
            int mediumId = Objects.requireNonNull(this.getArguments()).getInt(MEDIUM_ID);
            MediumSettings fragment = MediumSettings.newInstance(mediumId);
            this.getMainActivity().switchWindow(fragment, true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    TocEpisodeViewModel createViewModel() {
        return ViewModelProviders.of(this).get(TocEpisodeViewModel.class);
    }

    @Override
    LiveData<PagedList<TocEpisode>> createPagedListLiveData() {
        return this.getViewModel().getToc(this.mediumId);
    }

    @Override
    List<IFlexible> convertToFlexibles(Collection<TocEpisode> list) {
        return list
                .stream()
                .filter(Objects::nonNull)
                .map(tocEpisode -> new ListItem(tocEpisode, this))
                .collect(Collectors.toList());
    }

    @Nullable
    @Override
    Filterable createFilterable() {
        return new Filterable() {
            @Override
            public void onCreateFilter(View view, AlertDialog.Builder builder) {
                LinkedHashMap<String, Byte> readValueMap = new LinkedHashMap<>();
                readValueMap.put("No Filter", (byte) -1);
                readValueMap.put("Read only", (byte) 1);
                readValueMap.put("Unread only", (byte) 0);

                setStringSpinner(view, R.id.read, readValueMap, saved -> getViewModel().setReadFilter(saved));

                LinkedHashMap<String, Byte> savedValueMap = new LinkedHashMap<>();
                savedValueMap.put("No Filter", (byte) -1);
                savedValueMap.put("Saved only", (byte) 1);
                savedValueMap.put("Not Saved only", (byte) 0);

                setStringSpinner(view, R.id.saved, savedValueMap, saved -> getViewModel().setSavedFilter(saved));
            }

            @Override
            public int getFilterLayout() {
                return R.layout.toc_filter;
            }
        };
    }

    @Override
    LinkedHashMap<String, Sortings> getSortMap() {
        LinkedHashMap<String, Sortings> hashMap = new LinkedHashMap<>();
        hashMap.put("Index Asc", Sortings.INDEX_ASC);
        hashMap.put("Index Desc", Sortings.INDEX_DESC);
        return hashMap;
    }

    @Override
    public boolean onItemClick(View view, int position) {
        IFlexible flexible = getFlexibleAdapter().getItem(position);

        if (!(flexible instanceof ListItem)) {
            return false;
        }
        ListItem item = (ListItem) flexible;
        if (item.item.isSaved()) {
            CompletableFuture<Integer> task = TaskManager.runCompletableTask(() -> RepositoryImpl.getInstance().getMediumType(this.mediumId));
            task.whenComplete((type, throwable) -> {
                if (type != null) {
                    openLocal(item.item.getEpisodeId(), this.mediumId, type);
                }
            });
        } else {
            List<String> urls = item.item.getReleases().stream().map(Release::getUrl).collect(Collectors.toList());
            openInBrowser(urls);
        }
        return false;
    }

    @Override
    public void onItemLongClick(int position) {
        IFlexible flexible = getFlexibleAdapter().getItem(position);

        if (!(flexible instanceof ListItem)) {
            return;
        }
        ListItem item = (ListItem) flexible;
        TocEpisode mItem = item.item;

        List<String> menuItems = new ArrayList<>();

        if (mItem.getProgress() == 1) {
            menuItems.add("Mark unread");
        } else {
            menuItems.add("Mark read");
            menuItems.add("Mark current and previously read");
        }

        new AlertDialog
                .Builder(requireContext())
                .setItems(menuItems.toArray(new String[0]), (dialog, which) -> {
                    if (mItem.getProgress() == 1) {
                        if (which == 0) {
                            this.handle(mItem, ActionType.MARK_UNREAD);
                        }
                    } else {
                        if (which == 0) {
                            this.handle(mItem, ActionType.MARK_READ);
                        } else if (which == 1) {
                            this.handle(mItem, ActionType.MARK_PREVIOUSLY_READ);
                        }
                    }
                })
                .show();
    }

    @Override
    public boolean handle(TocEpisode item, ActionType type) {
        new ChangeEpisodeReadStatus(type, item, mediumId, this.getItems(), this.getContext()).execute();
        return true;
    }

    private static class ChangeEpisodeReadStatus extends AsyncTask<Void, Void, Void> {
        private final ActionType type;
        private final TocEpisode episode;
        private final int mediumId;
        private final List<TocEpisode> episodes;
        @SuppressLint("StaticFieldLeak")
        private final Context context;
        private String errorMessage;

        private ChangeEpisodeReadStatus(ActionType type, TocEpisode episode, int mediumId, List<TocEpisode> episodes, Context context) {
            this.type = type;
            this.episode = episode;
            this.mediumId = mediumId;
            this.episodes = episodes;
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (mediumId <= 0 || episode == null || episodes == null) {
                return null;
            }
            try {
                if (type == ActionType.MARK_READ) {
                    RepositoryImpl.getInstance().updateRead(episode.getEpisodeId(), true);

                } else if (type == ActionType.MARK_UNREAD) {
                    RepositoryImpl.getInstance().updateRead(episode.getEpisodeId(), false);

                } else if (type == ActionType.MARK_PREVIOUSLY_READ) {
                    RepositoryImpl.getInstance().updateReadWithLowerIndex(episode.getEpisodeId(), true);
                }
            } catch (IOException e) {
                e.printStackTrace();
                errorMessage = "Could not update Read Status";
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (errorMessage != null) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }


    private static class ListItem extends AbstractFlexibleItem<ViewHolder> {
        private final TocEpisode item;
        private final ItemListener fragment;

        ListItem(@NonNull TocEpisode item, ItemListener fragment) {
            this.item = item;
            this.fragment = fragment;
            this.setDraggable(false);
            this.setSwipeable(false);
            this.setSelectable(false);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ListItem listItem = (ListItem) o;

            return this.item.equals(listItem.item);
        }

        @Override
        public int hashCode() {
            return this.item.getEpisodeId();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.episode_item;
        }

        @Override
        public ViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new ViewHolder(view, adapter);
        }


        @SuppressLint("DefaultLocale")
        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolder holder, int position, List<Object> payloads) {
            holder.mItem = this.item;
            holder.listener = fragment;
            String index;
            if (this.item.getPartialIndex() > 0) {
                index = String.format("#%d.%d", this.item.getTotalIndex(), this.item.getPartialIndex());
            } else {
                index = String.format("#%d", this.item.getTotalIndex());
            }
            Comparator<Release> comparator = (o1, o2) -> o1.getReleaseDate().compareTo(o2.getReleaseDate());
            Optional<Release> earliestRelease = this.item.getReleases().stream().min(comparator);
            Release release = earliestRelease.orElse(null);
            String topRight;
            String title;

            if (release == null) {
                topRight = "Not available";
                title = "Not available";
            } else {
                topRight = release.getReleaseDate().toString("dd.MM.yyyy HH:mm:ss");
                title = release.getTitle();
            }
            boolean hasOnline = this.item.getReleases().stream().anyMatch(any -> any.getUrl() != null && !any.getUrl().isEmpty());

            holder.textTopLeft.setText(index);
            holder.textTopRight.setText(topRight);
            holder.textContentView.setText(title);

            holder.episodeReadIcon.setAlpha(this.item.getProgress() == 1 ? 1 : 0.25f);
            holder.openLocalIcon.setAlpha(this.item.isSaved() ? 1 : 0.25f);
            holder.openBrowserIcon.setAlpha(hasOnline ? 1 : 0.25f);
        }

    }

    private static class ViewHolder extends FlexibleViewHolder {
        final View mView;
        final TextView textContentView;
        private final TextView textTopLeft;
        private final TextView textTopRight;
        private final ImageView episodeReadIcon;
        private final ImageView openBrowserIcon;
        private final ImageView openLocalIcon;
        private TocEpisode mItem;
        private ItemListener listener;

        ViewHolder(@NonNull View view, FlexibleAdapter adapter) {
            super(view, adapter);
            this.mView = view;
            this.textTopLeft = view.findViewById(R.id.item_top_left);
            this.textTopRight = view.findViewById(R.id.item_top_right);
            this.episodeReadIcon = view.findViewById(R.id.episode_read);
            this.openBrowserIcon = view.findViewById(R.id.open_in_browser);
            this.openLocalIcon = view.findViewById(R.id.open_local);
            this.textContentView = view.findViewById(R.id.content);
//            this.mView.setOnCreateContextMenuListener(mOnCreateContextMenuListener);
        }

        private final View.OnCreateContextMenuListener mOnCreateContextMenuListener = (menu, v, menuInfo) -> {
            if (mItem == null || listener == null) {
                return;
            }

            if (mItem.getProgress() == 1) {
                MenuItem markUnRead = menu.add("Mark unread");
                markUnRead.setOnMenuItemClickListener(item -> listener.handle(
                        mItem,
                        ActionType.MARK_UNREAD
                ));
            } else {
                MenuItem markRead = menu.add("Mark read");
                markRead.setOnMenuItemClickListener(item -> listener.handle(
                        mItem,
                        ActionType.MARK_READ
                ));
                MenuItem markReadAndPreviously = menu.add("Mark current and previously read");
                markReadAndPreviously.setOnMenuItemClickListener(item -> listener.handle(
                        mItem,
                        ActionType.MARK_PREVIOUSLY_READ
                ));
            }
        };


        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + textContentView.getText() + "'";
        }
    }

    public enum ActionType {
        MARK_READ,
        MARK_UNREAD,
        MARK_PREVIOUSLY_READ
    }
}
